package com.xms.dao.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 交易对象 tr_entrust
 *
 * @author admin
 * @date 2022-09-28
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MqMsgDO<T> {
	private static final long serialVersionUID = 1L;


	/**
	 * 主题 /绑定的线程ID
	 */
	private String topic;
	/**
	 * 顺序消息的 shardingKey
	 */
	private String hashKey = "shardingKey";

	/**
	 * 1 顺序  2 同步非顺序 3 异步 4 虚拟线程
	 */
	private Integer type = 1;
	/**
	 * 消息体
	 */
	private T body;
	/**
	 * 事务ID
	 */
	private String transactionId;

}
