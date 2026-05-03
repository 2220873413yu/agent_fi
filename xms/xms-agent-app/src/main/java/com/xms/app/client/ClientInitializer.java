//package com.xms.app.client;
//
//import com.xms.app.client.hander.WebSocketClientHandler;
//import com.xms.app.server.WsStockService;
//import com.xms.common.constant.SysConstant;
//import com.xms.dao.service.IMarketTradeConfigService;
//import io.netty.channel.ChannelInitializer;
//import io.netty.channel.ChannelPipeline;
//import io.netty.channel.SimpleChannelInboundHandler;
//import io.netty.channel.socket.SocketChannel;
//import io.netty.handler.codec.http.HttpClientCodec;
//import io.netty.handler.codec.http.HttpObjectAggregator;
//import io.netty.handler.ssl.SslContext;
//import io.netty.handler.timeout.IdleStateHandler;
//import lombok.AllArgsConstructor;
//import org.springframework.beans.factory.annotation.Autowired;
//
//import java.util.concurrent.CountDownLatch;
//
///**
// * netty 客户端初始化
// *
// * @author MIER
// */
//@AllArgsConstructor
//public class ClientInitializer extends ChannelInitializer<SocketChannel> {
//	private final CountDownLatch latch;
//	private final String host;
//	private final String mode;
//	private final int port;
//	private final SslContext sslCtx;
//	private final String type;
//	private SimpleChannelInboundHandler handler;
//	private final TcpClient webSocketClient;
//	private WsStockService wsStockServiceImpl;
//	private IMarketTradeConfigService marketTradeConfigServiceImpl;
//	public ClientInitializer(CountDownLatch latch, String host, int port, SslContext sslCtx, String type, TcpClient webSocketClient, String mode,
//							 WsStockService wsStockServiceImpl, IMarketTradeConfigService marketTradeConfigServiceImpl) {
//		this.latch = latch;
//		this.host = host;
//		this.port = port;
//		this.mode = mode;
//		this.sslCtx = sslCtx;
//		this.type = type;
//		this.webSocketClient = webSocketClient;
//		this.wsStockServiceImpl = wsStockServiceImpl;
//		this.marketTradeConfigServiceImpl = marketTradeConfigServiceImpl;
//	}
//
//
//	@Override
//	protected void initChannel(SocketChannel sc) {
//		//动态 handler配置支持
//		ChannelPipeline pipeline = sc.pipeline();
//		//添加wss协议支持
//		if (null != sslCtx) {
//			pipeline.addLast(sslCtx.newHandler(sc.alloc(), host, port));
//		}
//		if (SysConstant.ONE.toString().equals(type)) {
//			handler = new WebSocketClientHandler(latch, webSocketClient,wsStockServiceImpl,marketTradeConfigServiceImpl);
//			//添加心跳支持,超过90秒没有 ping 服务器就会触发 READER_IDLE 事件，进行ping服务器操作
//			pipeline.addLast(new IdleStateHandler(90, 120, 210));
//			pipeline.addLast(new HttpClientCodec(), new HttpObjectAggregator(1024 * 1024 * 10));
//			pipeline.addLast("websocketHandler", handler);
//		}
//	}
//
//}
//
