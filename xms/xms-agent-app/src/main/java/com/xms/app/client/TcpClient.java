package com.xms.app.client;

import com.xms.app.client.hander.WebSocketClientHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.SSLException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Polymarket WebSocket客户端连接器。
 *
 * <p>该类保留旧Netty客户端的连接和重连能力，但业务已改为Polymarket Market Channel：
 * 只建立WebSocket连接，具体订阅和事件解析交给handler处理。</p>
 */
@Data
@Slf4j
public class TcpClient {

	/** 通道ID与WebSocket地址的对应关系，便于日志和重连排查。 */
	public static final Map<ChannelId, String> CHANNEL_IDS_URL = new ConcurrentHashMap<>();

	private static final String WSS_PREFIX = "wss";
	private static final String WS_PREFIX = "ws";
	private static final int MAX_FRAME_PAYLOAD_LENGTH = 1024 * 1024;

	private final String uri;
	private final CountDownLatch latch;
	private final LongLiveClientStart clientStart;
	private final URI tcpURI;
	private final String host;
	private final int port;
	private final String scheme;
	private ClientInitializer clientInitializer;
	private SslContext sslCtx;
	private Bootstrap bootstrap;
	private Channel channel;
	private int repeatConnectCount = 0;

	/**
	 * 创建Polymarket WebSocket客户端。
	 *
	 * @param uri Polymarket WebSocket地址
	 * @param latch 握手完成通知锁
	 * @param clientStart 订阅和结算派发服务
	 * @throws URISyntaxException WebSocket地址格式错误时抛出
	 */
	public TcpClient(String uri, CountDownLatch latch, LongLiveClientStart clientStart) throws URISyntaxException {
		this.uri = uri;
		this.tcpURI = new URI(uri);
		this.host = tcpURI.getHost();
		this.port = tcpURI.getPort();
		this.scheme = tcpURI.getScheme();
		this.latch = latch;
		this.clientStart = clientStart;
		initSslContext();
		this.clientInitializer = new ClientInitializer(latch, host, port, sslCtx, this, clientStart);
	}

	/**
	 * 启动Netty客户端并连接Polymarket。
	 */
	public void connect() {
		EventLoopGroup group = new NioEventLoopGroup(4);
		try {
			bootstrap = new Bootstrap();
			bootstrap.option(ChannelOption.SO_KEEPALIVE, true)
				.option(ChannelOption.TCP_NODELAY, true)
				.group(group)
				.handler(new LoggingHandler(LogLevel.INFO))
				.channel(NioSocketChannel.class)
				.handler(clientInitializer);
			doConnect(null, 10000);
		} catch (Exception e) {
			log.error("Polymarket WebSocket客户端启动失败", e);
		}
	}

	/**
	 * 连接或重连Polymarket WebSocket。
	 *
	 * @param ctx 触发重连的通道上下文，首次连接时为空
	 * @param count 最大重连次数
	 */
	@SneakyThrows
	public void doConnect(ChannelHandlerContext ctx, Integer count) {
		repeatConnectCount++;
		if (repeatConnectCount > count) {
			if (ctx != null) {
				log.error("Polymarket WebSocket重连失败，关闭通道");
				ctx.channel().close();
				CHANNEL_IDS_URL.remove(ctx.channel().id());
			}
			return;
		}
		if (channel != null && channel.isActive()) {
			log.info("Polymarket WebSocket通道仍然可用");
			return;
		}
		ChannelFuture future = port == -1 ? connectDefaultPort(ctx, count) : connectConfiguredPort(ctx, count);
		channel = future.channel();
		registerChannel(channel);
	}

	/**
	 * 使用URL中显式端口连接Polymarket。
	 */
	private ChannelFuture connectConfiguredPort(ChannelHandlerContext ctx, Integer count) {
		return bootstrap.connect(host, port).addListener((ChannelFutureListener) listener -> {
			if (listener.isSuccess()) {
				onConnected(listener.channel());
			} else {
				scheduleReconnect(listener.channel(), ctx, count, 5);
			}
		});
	}

	/**
	 * URL没有端口时按协议默认端口连接。
	 */
	private ChannelFuture connectDefaultPort(ChannelHandlerContext ctx, Integer count) throws UnknownHostException {
		InetAddress address = InetAddress.getByName(host);
		InetSocketAddress inetAddress = new InetSocketAddress(address, sslCtx != null ? 443 : 80);
		return bootstrap.connect(inetAddress).addListener((ChannelFutureListener) listener -> {
			if (listener.isSuccess()) {
				onConnected(listener.channel());
			} else {
				scheduleReconnect(listener.channel(), ctx, count, 3);
			}
		});
	}

	/**
	 * 连接成功后发起WebSocket握手。
	 */
	private void onConnected(Channel connectedChannel) {
		channel = connectedChannel;
		repeatConnectCount = 0;
		log.info("Polymarket WebSocket通道开启成功");
		HttpHeaders httpHeaders = new DefaultHttpHeaders();
		WebSocketClientHandshaker handshaker = WebSocketClientHandshakerFactory.newHandshaker(
			tcpURI, WebSocketVersion.V13, null, true, httpHeaders, MAX_FRAME_PAYLOAD_LENGTH);
		WebSocketClientHandler handler = (WebSocketClientHandler) channel.pipeline().get("websocketHandler");
		log.info("Polymarket WebSocket开始握手");
		handshaker.handshake(channel);
		handler.setHandshaker(handshaker);
	}

	/**
	 * 连接失败时按固定延迟重试。
	 */
	private void scheduleReconnect(Channel failedChannel, ChannelHandlerContext ctx, Integer count, int delaySeconds) {
		failedChannel.eventLoop().schedule(() -> {
			log.info("Polymarket WebSocket准备重连，第{}次", repeatConnectCount);
			doConnect(ctx, count);
		}, delaySeconds, TimeUnit.SECONDS);
	}

	/**
	 * 记录第三方通道，便于后续排查和统一管理。
	 */
	private void registerChannel(Channel channel) {
		GlobalConstant.THIRD_WS_CHANNEL.add(channel);
		CHANNEL_IDS_URL.put(channel.id(), uri);
	}

	/**
	 * 根据ws/wss协议初始化SSL上下文。
	 */
	private void initSslContext() {
		if (WSS_PREFIX.equals(scheme)) {
			try {
				this.sslCtx = SslContextBuilder.forClient()
					.trustManager(InsecureTrustManagerFactory.INSTANCE).build();
			} catch (SSLException e) {
				throw new IllegalStateException("Polymarket WSS SSL初始化失败", e);
			}
		} else if (WS_PREFIX.equals(scheme)) {
			this.sslCtx = null;
		} else {
			throw new IllegalArgumentException("Unsupported websocket scheme: " + scheme);
		}
	}
}
