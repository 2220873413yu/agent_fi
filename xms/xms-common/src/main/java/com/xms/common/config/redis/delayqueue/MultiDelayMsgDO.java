package com.xms.common.config.redis.delayqueue;


import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 并发发送延迟消息，导致mq压力过大，以及资源浪费的解决方案
 *
 * @author MIER
 */
@Data
public class MultiDelayMsgDO<T> implements Serializable {
	/**
	 * 消息体,可NULL
	 */
	private T msgBody;
	/**
	 * 记录延迟时间的集合 单位：毫秒，分切多个字段，每个用户处理的方式时机不一样，切片目的，保证并发消息的延迟业务闭环逻辑不一致.默认切割30分钟
	 * 10s 10s 10s 15s 15s  1m 1m 2m 5m 10m 10m
	 *
	 */
	// private List<Long> delayMills = new ArrayList<>(Arrays.asList(10000L, 10000L, 10000L, 15000L, 15000L, 60000L, 60000L, 120000L, 300000L, 600000L, 600000L));
	/**
	 * 设置三分钟分片
	 * 10s 10s 10s 15s 15s 1m  1m
	 */
	private List<Long> delayMills = new ArrayList<>(Arrays.asList(10000L, 10000L, 10000L, 15000L, 15000L, 60000L, 60000L));

	public MultiDelayMsgDO() {
	}

	public MultiDelayMsgDO(T msgBody, List<Long> delayMills) {
		this.msgBody = msgBody;
		this.delayMills = delayMills;
	}

	public MultiDelayMsgDO(T msgBody) {
		this.msgBody = msgBody;
	}


	public static <T> MultiDelayMsgDO<T> of(T msgBody, Long... delayMills) {
		return new MultiDelayMsgDO<>(msgBody, Arrays.asList(delayMills));
	}


	/**
	 * 获取并移除下一个延迟时间  单位：毫秒
	 * 返回队列中的第一个延迟消息
	 */
	public Long removeNextDelay() {
		return delayMills.remove(0);
	}


	/**
	 * 判断是否还有下一个延迟时间
	 */
	public boolean hasNextDelay() {
		return !delayMills.isEmpty();
	}

}
