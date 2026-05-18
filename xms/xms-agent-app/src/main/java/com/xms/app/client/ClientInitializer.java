package com.xms.app.client;

import com.xms.app.client.hander.WebSocketClientHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.CountDownLatch;

/**
 * Polymarket WebSocket客户端Netty管道初始化器。
 *
 * <p>负责配置SSL、HTTP协议升级、心跳检测和Polymarket事件处理器。</p>
 */
public class ClientInitializer extends ChannelInitializer<SocketChannel> {

	private final CountDownLatch latch;
	private final String host;
	private final int port;
	private final SslContext sslCtx;
	private final TcpClient webSocketClient;
	private final LongLiveClientStart clientStart;

	public ClientInitializer(CountDownLatch latch, String host, int port, SslContext sslCtx,
							 TcpClient webSocketClient, LongLiveClientStart clientStart) {
		this.latch = latch;
		this.host = host;
		this.port = port;
		this.sslCtx = sslCtx;
		this.webSocketClient = webSocketClient;
		this.clientStart = clientStart;
	}

	/**
	 * 初始化Polymarket WebSocket客户端管道。
	 *
	 * @param socketChannel Netty客户端通道
	 */
	@Override
	protected void initChannel(SocketChannel socketChannel) {
		ChannelPipeline pipeline = socketChannel.pipeline();
		if (sslCtx != null) {
			pipeline.addLast(sslCtx.newHandler(socketChannel.alloc(), host, port));
		}
		pipeline.addLast(new IdleStateHandler(90, 120, 210));
		pipeline.addLast(new HttpClientCodec(), new HttpObjectAggregator(1024 * 1024 * 10));
		pipeline.addLast("websocketHandler", new WebSocketClientHandler(latch, webSocketClient, clientStart));
	}
}
