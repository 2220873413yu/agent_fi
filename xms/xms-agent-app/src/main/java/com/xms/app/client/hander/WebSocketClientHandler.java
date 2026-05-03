//package com.xms.app.client.hander;
//
//
//import cn.hutool.core.date.DatePattern;
//import cn.hutool.core.date.DateUtil;
//import cn.hutool.crypto.digest.MD5;
//import com.alibaba.fastjson2.JSON;
//import com.alibaba.fastjson2.JSONObject;
//import com.jcraft.jzlib.Inflater;
//import com.jcraft.jzlib.JZlib;
//import com.xms.app.client.GlobalConstant;
//import com.xms.app.client.TcpClient;
//import com.xms.app.server.WsStockService;
//import com.xms.common.config.redis.XmsRedis;
//import com.xms.common.constant.Constants;
//import com.xms.common.constant.RedisConstant;
//import com.xms.common.constant.SysConstant;
//import com.xms.common.thread.ExecutorRegionKit;
//import com.xms.common.thread.RuntimeKit;
//import com.xms.common.utils.StringUtils;
//import com.xms.common.utils.spring.SpringUtils;
//import com.xms.dao.domain.MarketTradeConfig;
//import com.xms.dao.service.IMarketTradeConfigService;
//import com.xms.dao.service.ISysParaService;
//import com.xms.dao.service.impl.MqSendTemplate;
//import io.netty.buffer.ByteBuf;
//import io.netty.buffer.PooledByteBufAllocator;
//import io.netty.channel.Channel;
//import io.netty.channel.ChannelHandlerContext;
//import io.netty.channel.ChannelPromise;
//import io.netty.channel.SimpleChannelInboundHandler;
//import io.netty.handler.codec.http.FullHttpResponse;
//import io.netty.handler.codec.http.websocketx.*;
//import io.netty.handler.timeout.IdleStateEvent;
//import io.netty.util.CharsetUtil;
//import jakarta.annotation.PostConstruct;
//import jakarta.annotation.Resource;
//import lombok.Data;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.collections4.map.HashedMap;
//import org.jetbrains.annotations.Nullable;
//import org.springframework.core.env.Environment;
//
//import java.io.UnsupportedEncodingException;
//import java.math.BigDecimal;
//import java.nio.charset.StandardCharsets;
//import java.time.Instant;
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.CountDownLatch;
//import java.util.concurrent.atomic.AtomicInteger;
//import java.util.stream.Collectors;
//
///**
// * @author MIER
// */
//@Data
//@Slf4j
//public class WebSocketClientHandler extends SimpleChannelInboundHandler {
//
//	private WebSocketClientHandshaker handshaker;
//	private ChannelPromise handshakeFuture;
//	/**
//	 * 加入计数器的目的：由于连接是异步的，可能出现拿着还没连接成功的 channel来进行发送消息（报错），加入计数器后，只有连接成功才会放行测试类中发送消息的代码
//	 */
//	private CountDownLatch lathc;
//	private TcpClient webSocketClient;
//	private int step;
//	private static final AtomicInteger roundRobinCounter = new AtomicInteger(0);
//	private WsStockService wsStockServiceImpl;
//	public static Map<String,String> martetTradeConfigMap = new HashedMap<>(200);
//	/**
//	 * 心跳
//	 *
//	 * @param ctx
//	 * @param evt
//	 */
//	@Override
//	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
//		if (!(evt instanceof IdleStateEvent)) {
//			return;
//		}
//		IdleStateEvent event = (IdleStateEvent) evt;
//		switch (event.state()) {
//			case READER_IDLE:
//				//确保连接上有以后处理订阅
//				log.info("读空闲，即将重新sub======>>>");
//				String uri = TcpClient.channelIdsUrl.get(ctx.channel().id());
//					//重新订阅第三方交易对数据
//					reSubData(ctx.channel());
//				break;
//			case WRITER_IDLE:
//				//心跳特殊处理 通源数据
//					String stamp = String.valueOf(System.currentTimeMillis() / 1000);
//					String str = "u=SIGMA&p=SIGMA@13&stamp=" + stamp;
//					JSONObject json = new JSONObject(), pingEvent = new JSONObject();
//					json.put("u", "SIGMA");
//					json.put("sign", MD5.create().digestHex(str));
//					json.put("stamp", stamp);
//					pingEvent.put("event", GlobalConstant.PING);
//					pingEvent.put("data", json);
//					ctx.writeAndFlush(new TextWebSocketFrame(JSONObject.toJSONString(pingEvent)));
//					log.info("写空闲，心跳数据 send success： {}", pingEvent);
//
//				break;
//			case ALL_IDLE:
//				log.debug("读写两者都空闲，暂时不做任何处理");
//				break;
//			default:
//				break;
//		}
//	}
//
//
//	@Override
//	protected void channelRead0(ChannelHandlerContext ctx, Object msg) {
//		Channel ch = ctx.channel();
//		if (!this.handshaker.isHandshakeComplete()) {
//			log.warn("通道状态：{} ,是否握手成功：{}", ctx.channel().isActive(), this.handshaker.isHandshakeComplete());
//			handshakeEvt((FullHttpResponse) msg, ch);
//		} else if (msg instanceof FullHttpResponse) {
//			FullHttpResponse response = (FullHttpResponse) msg;
//			throw new IllegalStateException("Unexpected FullHttpResponse (getStatus=" + response.status() + ", content=" + response.content().toString(CharsetUtil.UTF_8) + ')');
//		} else {
//			WebSocketFrame frame = (WebSocketFrame) msg;
//			if (frame instanceof BinaryWebSocketFrame) {
//				BinaryWebSocketFrame webSocketFrame = (BinaryWebSocketFrame) frame;
//				// 1. 快速解析ByteBuf，生成纯业务数据对象，比如一个POJO
//				// 浅复制，只增加引用计数，不复制数据，性能最好
//				ByteBuf content = webSocketFrame.content();
//				ByteBuf buf = content.retainedDuplicate();;
//				// 在channelRead0方法中
//				int currentValue = roundRobinCounter.getAndIncrement();
//				int i = currentValue & 0x7; // 等价于 % 8，但保证非负
//				ExecutorRegionKit.createExecutorRegion("SHUHAI_ORIGIN_EXECUTOR_REGION" + i).getUserThreadExecutor(i).executeTry(() -> {
//					try {
//						decodeByteData(buf);
//					} catch (Exception e) {
//						// 解压或业务异常处理
//						ctx.executor().submit(() -> ctx.fireExceptionCaught(e));
//					} finally {
//						buf.release(); // 解析完释放内存
//					}
//				});
//
//			} else if (frame instanceof TextWebSocketFrame) {
//				String content = ((TextWebSocketFrame) msg).text();
//				handlerTextBizData(content, ch);
//			} else if (frame instanceof PingWebSocketFrame) {
//				ctx.channel().writeAndFlush(new PongWebSocketFrame(frame.content().retain()));
//				log.info("Ping 数据：{}", frame);
//			}
//		}
//	}
//
//
//	private void decodeByteData(ByteBuf content) {
//		ByteBuf buf = decompress(content);
//		try {
//			int len = buf.readableBytes();
//			for (int i = 0; i < len; i += step) {
//				// 提前从 buf 中“切割”出当前段的数据
//				ByteBuf segment = buf.retainedSlice(i, step); // 分段 + 引用计数 + 线程安全
//				int time = segment.getIntLE(0);
//				long l = time * 1000L;
//				byte[] symbol = new byte[12];
//				segment.getBytes(4, symbol);
//				String symbolStr = new String(symbol, CharsetUtil.UTF_8).trim();
//				JSONObject decodePKData = new JSONObject();
//				JSONObject decodePriceData = new JSONObject();
//				decodePKData.put("date", l);
//				decodePriceData.put("date", l);
//				ExecutorRegionKit.createExecutorRegion("SHUHAI_EXECUTOR_REGION" + symbolStr)
//					.getUserThreadExecutor(symbolStr.hashCode())
//					.execute(() -> {
//						try {
//							syncDecodeData(decodePriceData, symbolStr, decodePKData, segment);
//						} finally {
//							segment.release(); // 每个线程释放自己 slice 出来的 segment
//						}
//					});
//			}
//		} finally {
//			buf.release();
//		}
//
//
//	}
//
//	private void syncDecodeData(JSONObject decodePriceData, String symbolStr, JSONObject decodePKData, ByteBuf buf) {
//		decodePriceData.put("symbol", symbolStr);
//		decodePKData.put("symbol", symbolStr);
//		byte[] name = new byte[16];
//		buf.getBytes(16, name);
//		int nameLen = 0;
//		for (; nameLen < 16; nameLen++) {
//			if (name[nameLen] == 0) break;
//		}
//		try {
//			// decodeData.put("name", new String(name, 0, nameLen, "GBK").trim());
//			decodePKData.put("name", new String(name, 0, nameLen, "GBK").trim());
//		} catch (UnsupportedEncodingException e) {
//			throw new RuntimeException(e);
//		}
//
//		//decodePKData.put("marketType", marketConfigDo.getType());
//		//股票为成交总笔数，期货是前一交易日结算价
//		// decodeData.put("price3", buf.getFloatLE(32));
//		decodePriceData.put("price3", buf.getFloatLE(32));
//		decodePKData.put("price3", buf.getFloatLE(32));
//		//现量，当前最近一笔成交量
//		// decodeData.put("vol2", buf.getFloatLE(36));
//		decodePriceData.put("vol2", buf.getFloatLE(36));
//		decodePKData.put("vol2", buf.getFloatLE(36));
//		//期货有效，持仓（未平仓合约
//		// decodeData.put("openInt", buf.getFloatLE(40));
//		decodePKData.put("openInt", buf.getFloatLE(40));
//		//期货当日结算价（盘中为0，收盘后交易所才提供）
//		// decodeData.put("price2", buf.getFloatLE(44));
//		decodePriceData.put("price2", buf.getFloatLE(44));
//		decodePKData.put("price2", buf.getFloatLE(44));
//		//昨收价
//		// decodeData.put("lastclose", buf.getFloatLE(48));
//		decodePriceData.put("lastclose", buf.getFloatLE(48));
//
//		decodePKData.put("lastclose", buf.getFloatLE(52));
//		// decodeData.put("open", buf.getFloatLE(52));
//		decodePKData.put("open", buf.getFloatLE(52));
//
//		// decodeData.put("high", buf.getFloatLE(56));
//		decodePKData.put("high", buf.getFloatLE(56));
//
//		// decodeData.put("low", buf.getFloatLE(60));
//		decodePKData.put("low", buf.getFloatLE(60) );
//		//最新价
//		// float floatValue = buf.getFloatLE(64) + marketConfigDo.getFloatPrice().floatValue();
//		float newprice = buf.getFloatLE(64);
//		// decodeData.put("newprice", floatValue);
//		decodePriceData.put("newprice", newprice);
//		decodePKData.put("newprice", newprice);
//		//成交量
//		// decodeData.put("volume", buf.getFloatLE(68));
//		decodePriceData.put("volume", buf.getFloatLE(68));
//		decodePKData.put("volume", buf.getFloatLE(68));
//		//成交额
//		// decodeData.put("amount", buf.getFloatLE(72));
//		decodePKData.put("amount", buf.getFloatLE(72));
//
//		// decodeData.put("BP1", buf.getFloatLE(76));
//		decodePKData.put("BP1", buf.getFloatLE(76));
//
//		// decodeData.put("BP2", buf.getFloatLE(80));
//		// decodeData.put("BP3", buf.getFloatLE(84));
//		// decodeData.put("BP4", buf.getFloatLE(88));
//		// decodeData.put("BP5", buf.getFloatLE(92));
//		//
//		// decodeData.put("BV1", buf.getFloatLE(96));
//		decodePKData.put("BV1", buf.getFloatLE(96));
//
//		// decodeData.put("BV2", buf.getFloatLE(100));
//		// decodeData.put("BV3", buf.getFloatLE(104));
//		// decodeData.put("BV4", buf.getFloatLE(108));
//		// decodeData.put("BV5", buf.getFloatLE(112));
//		//
//		// decodeData.put("SP1", buf.getFloatLE(116));
//		decodePKData.put("SP1", buf.getFloatLE(116));
//
//		// decodeData.put("SP2", buf.getFloatLE(120));
//		// decodeData.put("SP3", buf.getFloatLE(124));
//		// decodeData.put("SP4", buf.getFloatLE(128));
//		// decodeData.put("SP5", buf.getFloatLE(132));
//		//
//		// decodeData.put("SV1", buf.getFloatLE(136));
//		decodePKData.put("SV1", buf.getFloatLE(136));
//
//		// decodeData.put("SV2", buf.getFloatLE(140));
//		// decodeData.put("SV3", buf.getFloatLE(144));
//		// decodeData.put("SV4", buf.getFloatLE(148));
//		// decodeData.put("SV5", buf.getFloatLE(152));
//		// log.info("BinaryWebSocketFrame 解码以后的数据：{}", decodeData);
//		//log.debug("BinaryWebSocketFrame 解码：{}", decodePKData);
//
//
//		// log.info("消息 {} body {}", MqQueueConstant.PANKOU_QUEUE, body);
//
////		//主题格式 市场|类型|数据类型|代码  string.equals("WI|1|O|CMSFA0")
//		String string = martetTradeConfigMap.get(decodePKData.getString("symbol")) + "|1|" + com.xms.app.server.GlobalConstant.STOCK_O + "|" + decodePKData.getString("symbol");
//		wsStockServiceImpl.sendGroupBySymbol(string, decodePKData.toJSONString());
//	}
//
//
//
//
//	private static final ThreadLocal<Inflater> INFLATER_THREAD_LOCAL =
//		ThreadLocal.withInitial(Inflater::new);
//
//	private static final ThreadLocal<byte[]> BUFFER_8K =
//		ThreadLocal.withInitial(() -> new byte[8192]);
//
//	/**
//	 * 解压数据
//	 *
//	 * @param buf 压缩数据缓冲区
//	 * @return 解压后的ByteBuf
//	 */
//	public static ByteBuf decompress(ByteBuf buf) {
//		Inflater inflater = new Inflater();
//		inflater.init();
//		try {
//			// 零拷贝优化
//			byte[] compressed;
//			int compressedLength;
//			if (buf.hasArray()) {
//				compressed = buf.array();
//				compressedLength = buf.readableBytes();
//				inflater.setInput(compressed); // ← 关键！
//			} else {
//				compressedLength = buf.readableBytes();
//				compressed = new byte[compressedLength];
//				buf.getBytes(buf.readerIndex(), compressed);
//				inflater.setInput(compressed); // 这里默认从 offset 0 开始就没问题
//			}
//
//			// 预估解压后大小
//			int estimatedSize = compressedLength * 4;
//			ByteBuf out = PooledByteBufAllocator.DEFAULT.buffer(estimatedSize);
//
//			byte[] buffer = BUFFER_8K.get();
//			int totalInflated = 0;
//
//			while (inflater.avail_in > 0) {
//				inflater.setOutput(buffer, 0, buffer.length);
//				int ret = inflater.inflate(JZlib.Z_NO_FLUSH);
//
//				if (ret == JZlib.Z_STREAM_END) {
//					int inflated = buffer.length - inflater.avail_out;
//					if (inflated > 0) {
//						out.writeBytes(buffer, 0, inflated);
//						totalInflated += inflated;
//					}
//					break;
//				} else if (ret == JZlib.Z_OK) {
//					int inflated = buffer.length - inflater.avail_out;
//					if (inflated > 0) {
//						out.writeBytes(buffer, 0, inflated);
//						totalInflated += inflated;
//					}
//				} else {
//					throw new RuntimeException("解压失败: " + ret);
//				}
//			}
//
//			// 调整ByteBuf大小
//			if (totalInflated < out.writableBytes()) {
//				out.capacity(totalInflated);
//			}
//
//			return out;
//		} finally {
//			inflater.end();
//		}
//	}
//
//	/**
//	 * 处理握手事件
//	 *
//	 * @param msg
//	 * @param ch
//	 */
//	private void handshakeEvt(FullHttpResponse msg, Channel ch) {
//		try {
//			//完成握手
//			this.handshaker.finishHandshake(ch, msg);
//			this.handshakeFuture.setSuccess();
//			if (this.handshaker.isHandshakeComplete()) {
//				log.info("ws协议升级成功~");
//				String uri = TcpClient.channelIdsUrl.get(ch.id());
//				step =156;
//					//重新订阅登录第三方交易对数据
//					String stamp = String.valueOf(System.currentTimeMillis() / 1000);
//					String str = "u=SIGMA&p=SIGMA@13&stamp=" + stamp;
//					JSONObject json = new JSONObject(), event = new JSONObject();
//					json.put("u", "SIGMA");
//					json.put("sign", MD5.create().digestHex(str));
//					json.put("stamp", stamp);
//					event.put("event", "login");
//					event.put("data", json);
//					ch.writeAndFlush(new TextWebSocketFrame(JSONObject.toJSONString(event)));
//					log.info("数海要发送的数据是：{}", event);
//			}
//		} catch (WebSocketHandshakeException var7) {
//			String errorMsg = String.format("WebSocket Client failed to connect,status:%s,reason:%s", msg.status(), msg.content().toString(CharsetUtil.UTF_8));
//			this.handshakeFuture.setFailure(new Exception(errorMsg));
//		} finally {
//			lathc.countDown();
//		}
//	}
//
//	/**
//	 * 处理业务消息
//	 *
//	 * @param content
//	 * @param channel
//	 */
//	private void handlerTextBizData(String content, Channel channel) {
//		if (content.contains(GlobalConstant.PONG)) {
//			return;
//		}
//		// 解析 JSON 数据
//		JSONObject json = JSON.parseObject(content);
//		log.debug("jsonData {}", json);
//		if (json.getString("event").equals("login")) {
//			if (json.getString("data").equals("Login successfully")) {
//				List<String> symbols = GlobalConstant.GLOBAL_GROUP_CHANNEL;
//				for (String s : symbols) {
//					channel.writeAndFlush(new TextWebSocketFrame(s));
//				}
//			}
//		} else {
//			if (json.getString("data").equals("Please log in first")) {
//				//重新订阅第三方交易对数据
//				reSubData(channel);
//			}
//		}
//	}
//
//	private static void reSubData(Channel channel) {
//			JSONObject json;
//			String stamp = String.valueOf(System.currentTimeMillis() / 1000);
//			String str = "u=SIGMA&p=SIGMA@13&stamp=" + stamp;
//			json = new JSONObject();
//			JSONObject longEvent = new JSONObject();
//			json.put("u", "SIGMA");
//			json.put("sign", MD5.create().digestHex(str));
//			json.put("stamp", stamp);
//			longEvent.put("event", "login");
//			longEvent.put("data", json);
//			channel.writeAndFlush(new TextWebSocketFrame(JSONObject.toJSONString(longEvent)));
//			log.info("数海要发送的数据是：{}", longEvent);
//			ISysParaService sysParaServiceImpl = SpringUtils.getBean(ISysParaService.class);
//				List<String> globalGroupChannel = GlobalConstant.GLOBAL_GROUP_CHANNEL;
//				for (String s : globalGroupChannel) {
//					channel.writeAndFlush(new TextWebSocketFrame(s));
//					log.info("防止覆盖连接成功订阅市场代码:{}", s);
//				}
//	}
//
//
//	/**
//	 * 通道关闭将会触发此方法,并且进行重连10w次
//	 *
//	 * @param ctx
//	 * @throws Exception
//	 */
//	@Override
//	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
//		log.error("ws客户端 channelInactive 触发，通道关闭！{}", ctx.channel().isActive());
//		webSocketClient.doConnect(ctx, 199999);
//	}
//
//	public WebSocketClientHandler(CountDownLatch lathc, TcpClient webSocketClient, WsStockService wsStockServiceImpl,
//								  IMarketTradeConfigService marketTradeConfigServiceImpl) {
//		this.lathc = lathc;
//		this.webSocketClient = webSocketClient;
//		this.wsStockServiceImpl = wsStockServiceImpl;
//		this.martetTradeConfigMap= marketTradeConfigServiceImpl.lambdaQuery()
//			.select(MarketTradeConfig::getDataCode,MarketTradeConfig::getType)
//			.list().stream().collect(Collectors.toMap(MarketTradeConfig::getDataCode,MarketTradeConfig::getType));
//	}
//	/**
//	 * 这个方法netty执行的时候自己会去调
//	 *
//	 * @param ctx
//	 */
//	@Override
//	public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
//		this.handshakeFuture = ctx.newPromise();
//	}
//}
