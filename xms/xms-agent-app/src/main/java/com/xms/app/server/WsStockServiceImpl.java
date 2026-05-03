package com.xms.app.server;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.xms.common.config.redis.XmsRedis;
import com.xms.common.constant.*;
import com.xms.common.core.domain.model.LoginUser;
import com.xms.common.core.domain.model.xms.LoginAppUser;
import com.xms.common.result.ResponseCode;
import com.xms.common.thread.ExecutorRegionKit;
import com.xms.common.utils.Func;
import com.xms.common.utils.StringUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.yeauty.pojo.Session;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

/**
 * ws 消息业务处理器
 *
 * @author: renengadePISTA
 * @createDate: 2023/10/19
 */
@Service
@AllArgsConstructor
@Slf4j
public class WsStockServiceImpl implements WsStockService {
	private final XmsRedis xmsRedis;
	private final Environment environment;

	/**
	 * token校验
	 *
	 * @param session
	 * @param token
	 * @param loginType
	 */
	@Override
	public void authTokenUser(Session session, String token, String loginType) {
		try {
			if (StringUtils.isNotEmpty(token) && token.startsWith(Constants.TOKEN_PREFIX)) {
				token = token.replace(Constants.TOKEN_PREFIX, "");
			}
			//根据传递过来的类型，解析token
			String secret;
			Claims claims;
			Long userId;

				secret = environment.getProperty("websocket.server.adminSecret");
				claims = Jwts.parserBuilder().setSigningKey(secret).build().parseClaimsJws(token).getBody();
				String uuid = (String) claims.get(Constants.LOGIN_APP_USER_KEY);
				String userKey = CacheConstants.LOGIN_APP_TOKEN_KEY + Constants.TOKEN_APP_PREFIX+uuid;
			LoginAppUser user = xmsRedis.get(userKey);
				if (Func.isAllEmpty(user)) {
					log.error("ws server channel 鉴权的token:{},鉴权 fail", token);
					session.close();
					return;
				}
				userId = user.getUserId();
			GlobalConstant.GLOBAL_USER_CHANNEL.handlerRead(userChannels -> {
				Session tempSession = userChannels.get(userId);
/*				if (tempSession != null) {
					log.warn("ws server channel {} 你的账号当前在另一台设备/地点登录，你被迫下线", userId);
					JSONObject receiptMsg = new JSONObject();
					receiptMsg.put(GlobalConstant.DATA, "你的账号当前在另一台设备/地点登录，你被迫下线");
					receiptMsg.put(GlobalConstant.CODE, HttpStatus.UNAUTHORIZED);
					tempSession.sendText(JSONObject.toJSONString(receiptMsg));
					GlobalConstant.GLOBAL_USER_SESSION.remove(tempSession);
					tempSession.close();
				}*/
				session.setAttribute("userId", userId);
				//登陆端的类型
				session.setAttribute(Constants.LOGIN_TYPE, (String) claims.get(Constants.CLIENT_ID));
				userChannels.put(userId, session);
				GlobalConstant.GLOBAL_USER_SESSION.put(session, userId);
			});

		} catch (Exception e) {
			log.error("ws server channel 鉴权的token:{},无法预知异常，鉴权 fail", token);
			e.printStackTrace();
			session.close();
		}
	}

	@Override
	@Async("asyncVirtualExecutor")
	public void handerSub(Session session, String message) {
		JSONObject msg = JSONObject.parseObject(message);
		if (GlobalConstant.PING.equals(msg.getString(GlobalConstant.ACTION))) {
			msg.put(GlobalConstant.ACTION, GlobalConstant.PONG);
			//发送到客户端ws
			session.sendText(JSON.toJSONString(msg));
			return;
		}
		//订阅或者取消的主题，多个以英文逗号分隔
		String topic = msg.getString(GlobalConstant.SYMBOLS);
		String loginType = session.getAttribute(Constants.LOGIN_TYPE);
		if (topic == null) {
			topic = msg.getString(GlobalConstant.SYMBOLS_ADMIN);
		}
		String[] strTopics = topic.split(",");
		if (strTopics.length < 1) {
			return;
		}
		//处理订阅/取消
		if (GlobalConstant.SUBSCRIBE.equals(msg.getString(GlobalConstant.ACTION))) {
			handlerSubChannel(session, strTopics, loginType);
		} else if (GlobalConstant.DELSUBSCRIBE.equals(msg.get(GlobalConstant.ACTION))) {
			//由于这个订阅可能还有其他通道订阅，故移除订阅时，暂时不发送到第三方ws 服务端。
			handerDelSubChannel(session, strTopics);
		} else if (GlobalConstant.SUBSCRIBEADMIN.equalsIgnoreCase(msg.getString(GlobalConstant.ACTION))) {
			handlerSubChannel(session, strTopics, loginType);
		} else if (GlobalConstant.DELSUBSCRIBEADMIN.equalsIgnoreCase(msg.getString(GlobalConstant.ACTION))) {
			handerDelSubChannel(session, strTopics);
		} else if (GlobalConstant.PING.equalsIgnoreCase(msg.getString(GlobalConstant.ACTION))) {
			msg.put(GlobalConstant.ACTION, GlobalConstant.PONG);
		}
		//回执成功
		msg.put("status", true);
		//发送到客户端ws
		session.sendText(JSON.toJSONString(msg));

	}

	/**
	 * 取消订阅
	 *
	 * @param session
	 * @param strTopics
	 */
	private void handerDelSubChannel(Session session, String[] strTopics) {
		for (String strTopic : strTopics) {
			GlobalConstant.GLOBAL_GROUP_SUB_CHANNEL.compute(strTopic, (key, value) -> {
				if (value != null) {
					//频道组，可自动移除，也可手动移除
					value.remove(session.channel());
					//该组通道确定完成以后，才能处理
					if (value.isEmpty()) {
						return null;
					}
					return value;
				}
				return null;
			});
		}
	}

	/**
	 * 添加订阅
	 *
	 * @param session
	 * @param strTopics
	 * @param loginType
	 */
	private void handlerSubChannel(Session session, String[] strTopics, String loginType) {
		JSONObject data;
		for (String strTopic : strTopics) {
			GlobalConstant.GLOBAL_GROUP_SUB_CHANNEL.compute(strTopic, (key, value) -> {
				if (value == null || value.isEmpty()) {
					ChannelGroup temp = new DefaultChannelGroup(strTopic, GlobalEventExecutor.INSTANCE);
					temp.add(session.channel());
//					//推盘口数据
//					String o = xmsRedis.get(RedisConstant.DbConstant.HOME_O_DATA + strTopic);
//					if (o != null) {
//						session.channel().writeAndFlush(new TextWebSocketFrame(o));
//					}
					return temp;
				} else {
					value.add(session.channel());
//					String o = xmsRedis.get(RedisConstant.DbConstant.HOME_O_DATA + strTopic);
//					if (o != null) {
//						session.channel().writeAndFlush(new TextWebSocketFrame(o));
//					}
					return value;
				}
			});


		}
	}

	/**
	 * 初始化处理订阅消息
	 *
	 * @param session
	 */
	private void asyncInitTradeMsg(Session session) {
		Date date = new Date();
		Long userId = session.getAttribute("userId");
//		Assets assets = GlobalConstant.GLOBAL_USER_ASSETS.get(userId);
//		if (assets == null) {
//			JSONObject msg = new JSONObject();
//			//可用预付款
//			msg.put("type", "traderNotify");
//			msg.put("timestamp", date.getTime());
//			//推送
//			session.sendText(JSONObject.toJSONString(msg));;
//		}else {
//			JSONObject msg = new JSONObject();
//			//可用预付款
//			msg.put("assets", assets);
//			msg.put("timestamp", date.getTime());
//			Map<String, PositionManager> positionManagerMap = GlobalConstant.GLOBAL_USER_TRADING_PAIRS.get(userId);
//			if (positionManagerMap != null) {
//				msg.put("orderInfo", positionManagerMap);
//			}
//			//推送
//			session.sendText(JSONObject.toJSONString(msg));
//		}

	}

	/**
	 * @param symbols 主题交易对
	 * @param msg     推送的消息
	 */
	@Override
	public void sendGroupBySymbol(String symbols, String msg) {
		ChannelGroup channelGroup = GlobalConstant.GLOBAL_GROUP_SUB_CHANNEL.get(symbols);
		if (channelGroup == null) {
			return;
		}
		channelGroup.writeAndFlush(new TextWebSocketFrame(msg));
		//		String[] list = Func.split(symbols, ",");
//		for (String topic : list) {
//			ChannelGroup channelGroup = GlobalConstant.GLOBAL_GROUP_SUB_CHANNEL.get(topic);
//			if (channelGroup == null) {
//				return;
//			}
//			channelGroup.writeAndFlush(new TextWebSocketFrame(msg));
//		}

	}

	/**
	 * @param userId 用户ID
	 * @param msg    推送的消息
	 */
	@Override
	public void sendByUserId(Long userId, String msg) {
		Session session = GlobalConstant.GLOBAL_USER_CHANNEL.get(userId);
		if (session == null) {
			return;
		}
		session.sendText(msg);
	}

//	/**
//	 * 代码 | 市场 | 最新时间 | 档位 | 卖一价 | 卖一量 | ... | 档位 | 买一价 | 买一量 | ...
//	 *
//	 * @param messageExt
//	 */
//	@SneakyThrows
//	@Override
//	public void onMessage(MessageExt messageExt) {
//
//		String body = new String(messageExt.getBody(), StandardCharsets.UTF_8);
//		// log.info("消息 {} body {}", MqQueueConstant.PANKOU_QUEUE, body);
//		JSONObject jsonData = JSON.parseObject(body);
//		//主题格式 市场|类型|数据类型|代码
//		String string = jsonData.getString("marketType") + "|1|" + GlobalConstant.STOCK_O + "|" + jsonData.getString("symbol");
//		wsStockServiceImpl.sendGroupBySymbol(string, body);
//		xmsRedis.set(RedisConstant.DbConstant.HOME_O_DATA + string, body);
//	}

	/**
	 * 当连接上后初始化数据
	 */
	@Override
	public void sendByUserOpen(Session session) {
		//推送app初始化数据哟
		ExecutorRegionKit.getExecutorRegion().getUserVirtualThreadExecutor(session.getAttribute("userId")).execute(() -> asyncInitTradeMsg(session));
	}

}
