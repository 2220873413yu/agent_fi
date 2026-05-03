package com.xms.app.server;

import com.xms.app.server.locks.MapWithLock;
import com.xms.common.constant.RedisConstant;
import com.xms.common.constant.SysConstant;
import com.xms.common.thread.ExecutorRegion;
import com.xms.common.thread.ExecutorRegionKit;
import io.netty.channel.group.ChannelGroup;
import org.yeauty.pojo.Session;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: GT63S
 * @createDate: 2024/7/30
 */
public interface GlobalConstant{

	/**
	 * NonBlockingHashMap 无所队列 不支持单个key的复合操作,要这个复合操作必须得用ConcurrentHashMap.每秒数百万(复合操作). 不涉及复合操作,推荐NonBlockingHashMap,读写更高
	 * 全局用户通道集合  string    session  userId通道 ,  用户多登陆通道处理
	 */
	Map<Session, Long> GLOBAL_USER_SESSION = new ConcurrentHashMap<>(RedisConstant.SECONDS_EXPIRE_TIME.intValue() * 3);
	/**
	 * 全局用户通道集合  string  userId  session 通道  用户单通道处理
	 * 全局通道订阅集合Channel，
	 */
	MapWithLock<Long, Session> GLOBAL_USER_CHANNEL = new MapWithLock<>(new HashMap<>(RedisConstant.SECONDS_EXPIRE_TIME.intValue() * 3));

	/**
	 * 全局通道订阅集合Channel， 一个主题 订阅多个用户
	 * key 订阅的主题 格式：证券市场|证券类型|数据类型|证券代码 多个以英文逗号分隔，value 订阅的通道ID集合 // 订阅用户资产的id集合，多个以英文逗号分隔
	 */
	Map<String, ChannelGroup> GLOBAL_GROUP_SUB_CHANNEL = new ConcurrentHashMap<>(RedisConstant.SECONDS_EXPIRE_TIME.intValue() * 3);

	// /**
	//  * 每个seesion 单独定制的主题，多-> 多 / 多对1，一个用户对应一个主题/一个用户对应多个主题
	//  */
	// Map<Session, String> GLOBAL_USER_SUB_CHANNEL = new NonBlockingHashMap<>(RedisConstant.SECONDS_EXPIRE_TIME.intValue() * 3);

	/**
	 * 全局体力初始化过程
	 */
	Integer[] INIT_STATUS = new Integer[]{0};


	/**
	 * 带单员下面所属的用户，再添加用户上架时。先全部进去，假设都能充足， 如果用户不满足，那么再一个个移除
	 * key 用户ID,  value: map<DOCUMENTARY_1, List<Long>>
	 * 其中value的key格式：代表这个带单人该笔带单ID  List<Long> 就是带党员下面所有的跟单用户
	 */
	Map<Long, Map<String, List<Long>>> GLOBAL_USER_TRADING_DOCUMENTARY_IDS = new ConcurrentHashMap<>(RedisConstant.SECONDS_EXPIRE_TIME.intValue() * 3);
	String PING = "ping";
	String PONG = "pong";
	/**
	 * 订阅
	 */
	String SUBSCRIBE = "SUBSCRIBE";
	String ACTION = "action";
	String PAIR = "pair";
	String PRICE = "price";
	/**
	 * 取消
	 */
	String DELSUBSCRIBE = "DELSUBSCRIBE";
	String SUBSCRIBEADMIN = "SUBSCRIBEADMIN";
	String DELSUBSCRIBEADMIN = "DELSUBSCRIBEADMIN";


	/**
	 * 主题 app
	 */
	String SYMBOLS = "symbols";
	/**
	 * 后台/代理订阅主题
	 */
	String SYMBOLS_ADMIN = "symbolsName";


	String THIRD_WEBSOCKET = "third-websocket";
	String FUN_CODE = "fun_code";

	/**
	 * Q:行情数据
	 */
	String STOCK_Q = "Q";
	/**
	 * O:盘口数据
	 */
	String STOCK_O = "O";
	String STOCK_E = "e";
	String STOCK_S = "s";
	String TRADE_E = "E";
	String TRADE_P = "p";
	/**
	 * T:逐笔成交
	 */
	String STOCK_T = "T";
	/**
	 * B:港股经济盘
	 */
	String STOCK_B = "B";
	String CODE = "code";
	String DATA = "data";
	/**
	 * 用户自己的余额
	 */
	String USER_BALANCE = "User|balance";
}
