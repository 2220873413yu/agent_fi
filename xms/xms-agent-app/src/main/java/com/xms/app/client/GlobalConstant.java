package com.xms.app.client;

import io.netty.channel.ChannelId;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author: GT63S
 * @createDate: 2024/7/30
 */
public interface GlobalConstant {

	String PING = "ping";
	String PONG = "pong";
	/**
	 * 订阅
	 */
	String SUBSCRIBE = "subscribe";

	String UNSUBSCRIBE = "unsubscribe";

	/**
	 * 第三方客户端的通道组 bian
	 */
	ChannelGroup THIRD_WS_CHANNEL = new DefaultChannelGroup("THIRD_WS_CHANNEL", GlobalEventExecutor.INSTANCE);

	/**
	 * 同源通道 全局通道订阅集合Channel，采用读写锁
	 * key 订阅的主题 格式：证券市场|证券类型|数据类型|证券代码，
	 * login
	 * u: 用户名
	 * <p>
	 * sign: 签名
	 * <p>
	 * stamp: 时间戳
	 * <p>
	 * 登录服务器验证信息
	 * subscribe
	 * market: 订阅市场代码，多个以逗号分割。如:SH,SZ
	 * <p>
	 * wlist: 产品白名单过滤【可选参数】，多个以逗号分割。如:SH600(A股过滤),SZ000002(单个产品过滤)。最多可指定50个
	 * <p>
	 * blist: 产品黑名单过滤【可选参数】，多个以逗号分割。如:SH600(A股过滤),SZ000002(单个产品过滤)。最多可指定50个
	 * <p>
	 * 订阅市场产品信息
	 * ping
	 * u: 用户名
	 * <p>
	 * sign: 签名
	 * <p>
	 * stamp: 时间戳
	 * <p>
	 * 通过心跳机制判断连接是否断开，可每60秒发送一次心跳数据
	 * unsubscribe
	 * market: 市场代码，多个以逗号分割
	 */
	List<String> GLOBAL_GROUP_CHANNEL = new ArrayList<>(16);
	String FUN_CODE = "fun_code";



	/**
	 * 全局体力初始化过程
	 */
	Integer[] INIT_STATUS = new Integer[1];

}
