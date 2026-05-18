package com.xms.app.client;

import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

/**
 * Polymarket WebSocket客户端常量。
 *
 * <p>旧版本这里保存数海行情订阅常量；当前客户端只用于订阅Polymarket Market Channel，
 * 监听market_resolved等市场事件并派发内部结算。</p>
 */
public interface GlobalConstant {

	/** Polymarket Market Channel地址。 */
	String POLYMARKET_MARKET_WS_URL = "wss://ws-subscriptions-clob.polymarket.com/ws/market";

	/** Polymarket Market Channel订阅类型。 */
	String POLYMARKET_MARKET_TYPE = "market";

	/** Polymarket开奖结果事件类型。 */
	String EVENT_TYPE_MARKET_RESOLVED = "market_resolved";

	/** 第三方Polymarket客户端通道组，用于后续统一关闭或排查连接。 */
	ChannelGroup THIRD_WS_CHANNEL = new DefaultChannelGroup("POLYMARKET_THIRD_WS_CHANNEL", GlobalEventExecutor.INSTANCE);
}
