package com.xms.app.server.server;


import com.xms.app.server.GlobalConstant;
import com.xms.app.server.WsStockService;
import com.xms.common.utils.Func;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.timeout.IdleStateEvent;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.yeauty.annotation.*;
import org.yeauty.pojo.Session;

import java.io.IOException;

/**
 * 在端点类上加上@ServerEndpoint、@Component注解，并在相应的方法上加上@OnOpen、@OnClose、@OnError、@OnMessage注解（不想关注某个事件可不添加对应的注解）：
 * 当ServerEndpointExporter类通过Spring配置进行声明并被使用，它将会去扫描带有@ServerEndpoint注解的类
 * 被注解的类将被注册成为一个WebSocket端点 所有的配置项都在这个注解的属性中 ( 如:@ServerEndpoint("/ws") )
 * readerIdleTimeSeconds 与IdleStateHandler中的readerIdleTimeSeconds一致，并且当它不为0时，将在pipeline中添加IdleStateHandler,
 *
 * @author MIER
 */
@Slf4j
@ConditionalOnProperty(value = "websocket.enable", havingValue = "true", matchIfMissing = true)
@ServerEndpoint(path = "/ws/{loginType}/{token}", host = "${websocket.server.host}", port = "${websocket.server.port}",
	allIdleTimeSeconds = "${websocket.server.allIdleTimeSeconds}", writerIdleTimeSeconds = "${websocket.server.writerIdleTimeSeconds}",
	readerIdleTimeSeconds = "${websocket.server.readerIdleTimeSeconds}",
	optionSoBacklog = "${websocket.server.soBacklog}", childOptionSoKeepalive = "${websocket.server.childOptionSoKeepalive}",
	workerLoopGroupThreads = "${websocket.server.workerLoopGroupThreads}")
public class XmsWsServerHandler {
	@Resource
	private WsStockService wsStockServiceImpl;


	/**
	 *
	 * wss://www.sigmase.com/ws/app/{用户的token(通过登录接口获取)}
	 * 当有新的连接进入时，对该方法进行回调 注入参数的类型:Session、HttpHeaders...
	 * 支持类型
	 * // Session session, HttpHeaders headers, @RequestParam String req, @RequestParam MultiValueMap<String, Object> reqMap,
	 * // @PathVariable("token") String token, @PathVariable("token1") String token1, @PathVariable Map<String, Object> pathMap
	 * // session.setSubprotocols("stomp");
	 *
	 * @param session
	 * @param token
	 */
	@BeforeHandshake
	public void handshake(Session session, @PathVariable("token") String token, @PathVariable("loginType") String loginType) {
		//  BY RENEGADE PISTA: 2023/10/18  处理鉴权
		log.debug("ws server channel 鉴权的token:{}", token);
		// 获取请求携带的令牌
		if (Func.isAllEmpty(token)) {
			session.close();
			return;
		}
		wsStockServiceImpl.authTokenUser(session, token, loginType);
	}

	/**
	 * 当有新的WebSocket连接完成时，对该方法进行回调 注入参数的类型:Session、HttpHeaders...
	 *
	 * @param session
	 */
	@OnOpen
	public void onOpen(Session session, @PathVariable("token") String token) {
		GlobalConstant.GLOBAL_USER_CHANNEL.put(session.getAttribute("userId"), session);
		//触发在线
		//wsStockServiceImpl.sendByUserOpen(session);
		log.info("ws server channel new connection coming :{} 此刻在线人人数：{}", session.channel(), GlobalConstant.GLOBAL_USER_CHANNEL.size());
	}

	/**
	 * 当有WebSocket连接关闭时，对该方法进行回调 注入参数的类型:Session
	 *
	 * @param session
	 * @throws IOException
	 */
	@OnClose
	public void onClose(Session session) throws IOException {
		Long userId = session.getAttribute("userId");
		log.info("ws server  channelInactive 触发，其中用户：{} 通道关闭！{}", userId, session.channel().isActive());
		Long aLong = GlobalConstant.GLOBAL_USER_SESSION.get(session);
		if (aLong != null) {
			//String loginType = session.getAttribute(Constants.LOGIN_TYPE);
			sendUserOnline(session, "loginType", userId);

		}
	}

	/**
	 * 当有WebSocket抛出异常时，对该方法进行回调 注入参数的类型:Session、Throwable
	 *
	 * @param session
	 * @param throwable
	 */
	@OnError
	public void onError(Session session, Throwable throwable) {
		throwable.printStackTrace();
		Long aLong = GlobalConstant.GLOBAL_USER_SESSION.get(session);
		if (aLong != null) {
			//String loginType = session.getAttribute(Constants.LOGIN_TYPE);
			Long userId = session.getAttribute("userId");
			sendUserOnline(session, "", userId);
		}
	}

	private void sendUserOnline(Session session, String loginType, Long userId) {
		// if (!loginType.endsWith(Constants.LOGIN_ADMIN)) {
		// 	JSONObject msg = new JSONObject();
		// 	msg.put("userId", userId);
		// 	msg.put("onLineState", false);
		// 	wsStockServiceImpl.sendGroupBySymbol(userId + ExchangeConstants.SYS_USER_WALLET, JSONObject.toJSONString(msg));
		// }
		GlobalConstant.GLOBAL_USER_SESSION.remove(session);
		GlobalConstant.GLOBAL_USER_CHANNEL.remove(userId);
	}

	/**
	 * 当接收到字符串消息时，对该方法进行回调 注入参数的类型:Session、String
	 *
	 * @param session
	 * @param message
	 */
	@OnMessage
	public void onMessage(Session session, String message) {
		//if (message.equalsIgnoreCase(GlobalConstant.PING)) {
		if (message.equalsIgnoreCase("ping")) {
			//session.sendText(GlobalConstant.PONG);
			session.sendText("pong");
			return;
		}
		log.debug("userId {} ws server TextWebSocketFrame 类型文本接收数据：{}", session.getAttribute("userId"), message);
		//处理通道订阅/取消的干活
		wsStockServiceImpl.handerSub(session, message);
	}

	/**
	 * 当接收到二进制消息时，对该方法进行回调 注入参数的类型:Session、byte[]
	 *
	 * @param session
	 * @param bytes
	 */
	@OnBinary
	public void onBinary(Session session, byte[] bytes) {
		log.info("ws server BinaryWebSocketFrame数据：" + Func.toStr(bytes));
	}

	/**
	 * 心跳事件
	 *
	 * @param session
	 * @param evt
	 */
	@OnEvent
	public void onEvent(Session session, Object evt) {
		if (evt instanceof IdleStateEvent) {
			IdleStateEvent idleStateEvent = (IdleStateEvent) evt;
			switch (idleStateEvent.state()) {
				case READER_IDLE:
					log.debug("server 读空闲");
					break;
				case WRITER_IDLE:
					log.debug("server 写空闲");
					break;
				case ALL_IDLE:
					log.debug("server 两者空闲");
					session.channel().writeAndFlush(new PingWebSocketFrame());
					break;
				default:
					break;
			}
		}
	}

}
