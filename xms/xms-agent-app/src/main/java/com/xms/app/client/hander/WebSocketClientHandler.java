package com.xms.app.client.hander;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.xms.app.client.GlobalConstant;
import com.xms.app.client.LongLiveClientStart;
import com.xms.app.client.TcpClient;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketHandshakeException;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CountDownLatch;

/**
 * Polymarket Market Channel事件处理器。
 *
 * <p>该处理器只解析Polymarket文本事件。收到market_resolved时派发市场进入结算中；
 * 其他价格或盘口类事件仅记录日志，不触发内部发奖。</p>
 */
@Slf4j
public class WebSocketClientHandler extends SimpleChannelInboundHandler<Object> {

	private WebSocketClientHandshaker handshaker;
	private ChannelPromise handshakeFuture;
	private final CountDownLatch latch;
	private final TcpClient webSocketClient;
	private final LongLiveClientStart clientStart;
	private volatile long lastLargeMessageLogTime;

	public WebSocketClientHandler(CountDownLatch latch, TcpClient webSocketClient, LongLiveClientStart clientStart) {
		this.latch = latch;
		this.webSocketClient = webSocketClient;
		this.clientStart = clientStart;
	}

	/**
	 * 设置Netty握手器。
	 *
	 * <p>TcpClient在连接成功后创建握手器并注入，handler收到握手响应后用它完成协议升级。</p>
	 *
	 * @param handshaker WebSocket客户端握手器
	 */
	public void setHandshaker(WebSocketClientHandshaker handshaker) {
		this.handshaker = handshaker;
	}

	/**
	 * 处理Polymarket WebSocket空闲事件。
	 *
	 * <p>读空闲时重新发送订阅，写空闲时发送空ping帧维持连接。</p>
	 *
	 * @param ctx 通道上下文
	 * @param evt Netty空闲事件
	 */
	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
		if (!(evt instanceof IdleStateEvent)) {
			return;
		}
		IdleStateEvent event = (IdleStateEvent) evt;
		switch (event.state()) {
			case READER_IDLE:
				log.info("Polymarket WebSocket读空闲，重新发送订阅");
				clientStart.sendSubscribeMessage(ctx.channel());
				break;
			case WRITER_IDLE:
				ctx.writeAndFlush(new PingWebSocketFrame());
				log.debug("Polymarket WebSocket发送ping帧");
				break;
			case ALL_IDLE:
				log.debug("Polymarket WebSocket读写空闲");
				break;
			default:
				break;
		}
	}

	/**
	 * 读取Polymarket WebSocket消息。
	 *
	 * <p>握手响应先完成协议升级；之后只处理TextWebSocketFrame中的JSON事件。</p>
	 *
	 * @param ctx 通道上下文
	 * @param msg Netty消息
	 */
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Object msg) {
		Channel channel = ctx.channel();
		if (!handshaker.isHandshakeComplete()) {
			handleHandshake((FullHttpResponse) msg, channel);
			return;
		}
		if (msg instanceof FullHttpResponse) {
			FullHttpResponse response = (FullHttpResponse) msg;
			throw new IllegalStateException("Unexpected FullHttpResponse, status=" + response.status()
				+ ", content=" + response.content().toString(CharsetUtil.UTF_8));
		}
		WebSocketFrame frame = (WebSocketFrame) msg;
		if (frame instanceof TextWebSocketFrame) {
			handleTextEvent(((TextWebSocketFrame) frame).text());
		} else if (frame instanceof PingWebSocketFrame) {
			channel.writeAndFlush(new PongWebSocketFrame(frame.content().retain()));
		} else if (frame instanceof PongWebSocketFrame) {
			log.debug("Polymarket WebSocket收到pong帧");
		} else if (frame instanceof CloseWebSocketFrame) {
			log.warn("Polymarket WebSocket收到关闭帧");
			channel.close();
		}
	}

	/**
	 * 完成WebSocket握手，并在握手成功后发送Polymarket订阅。
	 *
	 * @param msg 握手响应
	 * @param channel 当前通道
	 */
	private void handleHandshake(FullHttpResponse msg, Channel channel) {
		try {
			handshaker.finishHandshake(channel, msg);
			handshakeFuture.setSuccess();
			log.info("Polymarket WebSocket握手成功");
			clientStart.sendSubscribeMessage(channel);
		} catch (WebSocketHandshakeException e) {
			String errorMsg = String.format("Polymarket WebSocket握手失败，status:%s, reason:%s",
				msg.status(), msg.content().toString(CharsetUtil.UTF_8));
			handshakeFuture.setFailure(new Exception(errorMsg));
			log.error(errorMsg, e);
		} finally {
			latch.countDown();
		}
	}

	/**
	 * 解析Polymarket文本事件。
	 *
	 * <p>Polymarket可能返回单个JSON对象，也可能返回JSON数组；数组内每个对象逐条处理。</p>
	 *
	 * @param content Polymarket原始文本消息
	 */
	private void handleTextEvent(String content) {
		if (content == null || content.trim().isEmpty()) {
			return;
		}
		// 临时观测大帧来源：只记录长度和事件摘要，不打印完整JSON，避免日志被盘口快照撑爆。
		log.info("处理之前的数据 content:{}",content);
		logLargeMessageSummary(content);
		try {
			Object parsed = JSON.parse(content);
			if (parsed instanceof JSONArray) {
				JSONArray array = (JSONArray) parsed;
				for (int i = 0; i < array.size(); i++) {
					handleEventObject(array.getJSONObject(i));
				}
				return;
			}
			if (parsed instanceof JSONObject) {
				handleEventObject((JSONObject) parsed);
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.warn("Polymarket WebSocket文本事件解析失败，content={}, error={}", content, e.getMessage());
		}
	}

	/**
	 * 按事件类型分发Polymarket事件。
	 *
	 * <p>只有market_resolved触发内部市场派发；其他事件暂时不改变数据库状态。</p>
	 *
	 * @param event Polymarket事件对象
	 */
	/**
	 * 记录Polymarket WebSocket大消息摘要。
	 *
	 * <p>该日志用于临时确认超过默认64KB的消息类型。日志只抽取事件类型、asset_id、bids/asks数量和字节数，
	 * 不输出完整盘口内容，避免大量订单簿快照进一步放大日志IO。</p>
	 *
	 * @param content Polymarket原始文本帧内容
	 */
	private void logLargeMessageSummary(String content) {
		int bytes = content.getBytes(CharsetUtil.UTF_8).length;
		if (bytes < 64 * 1024) {
			return;
		}
		long now = System.currentTimeMillis();
		if (now - lastLargeMessageLogTime < 1000L) {
			return;
		}
		lastLargeMessageLogTime = now;
		try {
			Object parsed = JSON.parse(content);
			if (parsed instanceof JSONArray) {
				JSONArray array = (JSONArray) parsed;
				log.info("Polymarket WebSocket收到大消息，bytes={}, arraySize={}, firstEvent={}",
					bytes, array.size(), array.isEmpty() ? null : eventSummary(array.getJSONObject(0)));
				return;
			}
			if (parsed instanceof JSONObject) {
				log.info("Polymarket WebSocket收到大消息，bytes={}, event={}", bytes, eventSummary((JSONObject) parsed));
				return;
			}
		} catch (Exception e) {
			log.info("Polymarket WebSocket收到大消息，bytes={}, parseError={}", bytes, e.getMessage());
			return;
		}
		log.info("Polymarket WebSocket收到大消息，bytes={}", bytes);
	}

	/**
	 * 提取Polymarket事件的轻量摘要。
	 *
	 * @param event Polymarket WebSocket事件对象
	 * @return 包含类型、asset和盘口档位数量的摘要
	 */
	private String eventSummary(JSONObject event) {
		if (event == null) {
			return null;
		}
		JSONArray bids = event.getJSONArray("bids");
		JSONArray asks = event.getJSONArray("asks");
		String eventType = firstNotBlank(event.getString("event_type"), event.getString("eventType"), event.getString("type"));
		String assetId = firstNotBlank(event.getString("asset_id"), event.getString("assetId"));
		return "type=" + eventType
			+ ", assetId=" + assetId
			+ ", bids=" + (bids == null ? 0 : bids.size())
			+ ", asks=" + (asks == null ? 0 : asks.size());
	}

	/**
	 * 按事件类型分发Polymarket事件。
	 *
	 * <p>只有market_resolved触发内部市场派发；其他价格或盘口类事件只用于观察，不改变数据库状态。</p>
	 *
	 * @param event Polymarket事件对象
	 */
	private void handleEventObject(JSONObject event) {
		if (event == null || event.isEmpty()) {
			return;
		}
		String eventType = firstNotBlank(event.getString("event_type"), event.getString("eventType"), event.getString("type"));
		if (GlobalConstant.EVENT_TYPE_MARKET_RESOLVED.equals(eventType)) {
			handleMarketResolved(event);
			return;
		}
		//log.debug("Polymarket WebSocket收到非结算事件，eventType={}, event={}", eventType, event);
	}

	/**
	 * 处理Polymarket market_resolved事件。
	 *
	 * <p>事件中优先读取slug，其次读取winning_asset_id；匹配到内部待结算市场后，只派发为结算中，不直接发奖。</p>
	 *
	 * @param event market_resolved事件
	 */
	private void handleMarketResolved(JSONObject event) {
		String slug = firstNotBlank(event.getString("slug"), event.getString("market_slug"), event.getString("marketSlug"));
		String winningAssetId = firstNotBlank(event.getString("winning_asset_id"), event.getString("winningAssetId"));
		String winningOutcome = firstNotBlank(event.getString("winning_outcome"), event.getString("winningOutcome"));
		clientStart.dispatchResolvedMarket(slug, winningAssetId, winningOutcome);
	}

	/**
	 * 返回第一个非空字符串。
	 *
	 * @param values 候选字符串
	 * @return 第一个非空值；全部为空时返回null
	 */
	private String firstNotBlank(String... values) {
		if (values == null) {
			return null;
		}
		for (String value : values) {
			if (value != null && !value.trim().isEmpty()) {
				return value.trim();
			}
		}
		return null;
	}

	/**
	 * 通道关闭后触发重连。
	 *
	 * @param ctx 通道上下文
	 */
	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		log.error("Polymarket WebSocket通道关闭，准备重连");
		webSocketClient.doConnect(ctx, 199999);
	}

	/**
	 * Netty添加handler时创建握手Promise。
	 *
	 * @param ctx 通道上下文
	 */
	@Override
	public void handlerAdded(ChannelHandlerContext ctx) {
		this.handshakeFuture = ctx.newPromise();
	}
}
