//package com.xms.app.client;
//
//import com.xms.app.client.hander.WebSocketClientHandler;
//import com.xms.app.server.WsStockService;
//import com.xms.common.constant.SysConstant;
//import com.xms.common.utils.spring.SpringUtils;
//import com.xms.dao.service.IMarketTradeConfigService;
//import io.netty.bootstrap.Bootstrap;
//import io.netty.channel.*;
//import io.netty.channel.group.ChannelGroup;
//import io.netty.channel.group.DefaultChannelGroup;
//import io.netty.channel.nio.NioEventLoopGroup;
//import io.netty.channel.socket.nio.NioSocketChannel;
//import io.netty.handler.codec.http.DefaultHttpHeaders;
//import io.netty.handler.codec.http.HttpHeaders;
//import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
//import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
//import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
//import io.netty.handler.codec.http.websocketx.WebSocketVersion;
//import io.netty.handler.logging.LogLevel;
//import io.netty.handler.logging.LoggingHandler;
//import io.netty.handler.ssl.SslContext;
//import io.netty.handler.ssl.SslContextBuilder;
//import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
//import io.netty.util.concurrent.GlobalEventExecutor;
//import lombok.Data;
//import lombok.SneakyThrows;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.core.env.Environment;
//
//import javax.net.ssl.SSLException;
//import java.net.*;
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.CountDownLatch;
//import java.util.concurrent.TimeUnit;
//
///**
// * @author: renengadePISTA
// * @createDate: 2023/10/13
// */
//@Data
//@Slf4j
//public class TcpClient {
//	/**
//	 * 通道ID与ws地址关联
//	 */
//	public static Map<ChannelId, String> channelIdsUrl = new ConcurrentHashMap<>();
//
//	public static String WSS_PREFIX = "wss";
//	public static String WS_PREFIX = "ws";
//	public static String WS_URL = "ws_url";
//	private String uri;
//	private CountDownLatch latch;
//	private ClientInitializer clientInitializer;
//	private SslContext sslCtx;
//	private String host;
//	private int port;
//	private String scheme;
//	private URI tcpURI;
//	private String type;
//	private String userId;
//	private String mode;
//	private Bootstrap bootstrap;
//	private Channel channel;
//	int repeatConnectCount = 0;
//
//	/**
//	 * @param uri
//	 * @param type  1 websocket  2 tcp
//	 * @param latch
//	 * @throws URISyntaxException
//	 */
//	public TcpClient(String uri, String type, CountDownLatch latch, String mode,
//					 WsStockService wsStockServiceImpl, IMarketTradeConfigService marketTradeConfigServiceImpl) throws URISyntaxException {
//		this.uri = uri;
//		this.mode = mode;
//		this.tcpURI = new URI(uri);
//		this.host = tcpURI.getHost();
//		this.port = tcpURI.getPort();
//		this.scheme = tcpURI.getScheme();
//		this.latch = latch;
//		this.type = type;
//		if (WSS_PREFIX.equals(scheme)) {
//			//初始化SslContext，这个在wss协议升级的时候需要用到
//			try {
//				this.sslCtx = SslContextBuilder.forClient()
//					.trustManager(InsecureTrustManagerFactory.INSTANCE).build();
//			} catch (SSLException e) {
//				e.printStackTrace();
//			}
//		} else if (WS_PREFIX.equals(scheme)) {
//			this.sslCtx = null;
//		} else {
//			this.sslCtx = null;
//		}
//		this.clientInitializer = new ClientInitializer(latch, host, port, sslCtx, type, TcpClient.this, mode,
//			wsStockServiceImpl, marketTradeConfigServiceImpl);
//	}
//
//	public void connect() {
//		EventLoopGroup group = new NioEventLoopGroup(4);
//		try {
//			bootstrap = new Bootstrap();
//			//keepalvie，默认关闭，开启keepalive
//			bootstrap.option(ChannelOption.SO_KEEPALIVE, true)
//				//是否启用nagle算法，默认是false（启用），该算法是tcp在发送数据时将小的、碎片化的数据拼接成一个大的报文一起发送
//				.option(ChannelOption.TCP_NODELAY, true)
//				.group(group)
//				//打印日志级别
//				.handler(new LoggingHandler(LogLevel.INFO))
//				//选择客户端Channel实现，Channel是数据的传输通道
//				.channel(NioSocketChannel.class)
//				.handler(clientInitializer);
//			doConnect(null, 10000);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
//
//
//	@SneakyThrows
//	public void doConnect(ChannelHandlerContext ctx, Integer count) {
//		//重连次数每次加一
//		repeatConnectCount++;
//		if (repeatConnectCount > count) {
//			if (null != ctx) {
//				log.error("通道关闭、重连失败");
//				ctx.channel().close();
//				channelIdsUrl.remove(ctx.channel().id());
//				return;
//			}
//		}
//		if (channel != null && channel.isActive()) {
//			log.info("通道正常");
//			return;
//		}
//		//建立HTTP连接
//		ChannelFuture future;
//		if ("2".equals(type)) {
//			future = tcpClientHostConnect(ctx, count);
//			channel = future.channel();
//			waitActiveChannel(channel);
//			return;
//		}
//		if (port == -1) {
//			future = clientHostConnect(ctx, count);
//		} else {
//			future = bootstrap.connect(host, port).addListener((ChannelFutureListener) futureListener -> {
//				if (futureListener.isSuccess()) {
//					channel = futureListener.channel();
//					//连接成功重置重连次数为0
//					repeatConnectCount = 0;
//					log.info("通道开启成功~");
//					HttpHeaders httpHeaders = new DefaultHttpHeaders();
//					WebSocketClientHandshaker webSocketClientHandshaker = WebSocketClientHandshakerFactory.newHandshaker
//						(tcpURI, WebSocketVersion.V13, null, true, httpHeaders);
//					WebSocketClientHandler handler = (WebSocketClientHandler) channel.pipeline().get("websocketHandler");
//					//升级为ws协议
//					log.info("开始升级http协议~准备开始握手");
//					webSocketClientHandshaker.handshake(channel);
//					handler.setHandshaker(webSocketClientHandshaker);
//				} else {
//					//调度任务开启
//					futureListener.channel().eventLoop().schedule(() -> {
//						log.info("重试开启通道~{}", repeatConnectCount);
//						doConnect(ctx, count);
//					}, 5, TimeUnit.SECONDS);
//				}
//			});
//		}
//		channel = future.channel();
//		waitActiveChannel(channel);
//
//	}
//
//	private ChannelFuture tcpClientHostConnect(ChannelHandlerContext ctx, Integer count) throws Exception {
//		ChannelFuture future;
//		future = bootstrap.connect(host, port).addListener((ChannelFutureListener) futureListener -> {
//			if (futureListener.isSuccess()) {
//				channel = futureListener.channel();
//				//连接成功重置重连次数为0
//				repeatConnectCount = 0;
//				log.info("tcp通道开启成功~");
//			} else {
//				//调度任务开启
//				futureListener.channel().eventLoop().schedule(() -> {
//					log.info("tcp重试开启通道~{}", repeatConnectCount);
//					doConnect(ctx, count);
//				}, 5, TimeUnit.SECONDS);
//			}
//		}).sync();
//		return future;
//	}
//
//	private ChannelFuture clientHostConnect(ChannelHandlerContext ctx, Integer count) throws UnknownHostException {
//		ChannelFuture future;
//		InetAddress address = InetAddress.getByName(host);
//		InetSocketAddress inetAddress = new InetSocketAddress(address, sslCtx != null ? 443 : 80);
//		future = bootstrap.connect(inetAddress).addListener((ChannelFutureListener) futureListener -> {
//			if (futureListener.isSuccess()) {
//				channel = futureListener.channel();
//				//连接成功重置重连次数为0
//				repeatConnectCount = 0;
//				log.info("通道开启成功~");
//				HttpHeaders httpHeaders = new DefaultHttpHeaders();
//				WebSocketClientHandshaker webSocketClientHandshaker = WebSocketClientHandshakerFactory.newHandshaker
//					(tcpURI, WebSocketVersion.V13, null, true, httpHeaders);
//				WebSocketClientHandler handler = (WebSocketClientHandler) channel.pipeline().get("websocketHandler");
//				//升级为ws协议
//				log.info("开始升级http协议~准备开始握手");
//				webSocketClientHandshaker.handshake(channel);
//				handler.setHandshaker(webSocketClientHandshaker);
//			} else {
//				//调度任务开启
//				futureListener.channel().eventLoop().schedule(() -> {
//					log.info("重试开启通道~{}", repeatConnectCount);
//					doConnect(ctx, count);
//				}, 3, TimeUnit.SECONDS);
//			}
//		});
//		return future;
//	}
//
//	private void waitActiveChannel(Channel channel) {
//		//添加第三方通道，用来订阅/取消第三方通道信息
//		com.xms.app.client.GlobalConstant.THIRD_WS_CHANNEL.add(channel);
//		List<String> globalGroupChannel = com.xms.app.client.GlobalConstant.GLOBAL_GROUP_CHANNEL;
//		for (String s : globalGroupChannel) {
//			channel.writeAndFlush(new TextWebSocketFrame(s));
//			log.info("连接成功订阅市场代码:{}", s);
//		}
//		channelIdsUrl.put(channel.id(), uri);
//	}
//
//}
