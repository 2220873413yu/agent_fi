package com.xms.dao.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author: GT63S
 * @createDate: 2024/7/9
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RewardPublisherEvent {
	/**
	 * 订单ID
	 */
	private String orderId;
	/**
	 * 来源用户ID
	 */
	private Long sourceId;
	/**
	 * 目标用户id 集合
	 */
	private Long[] targetUserIds;
	/**
	 * 1 分享销售直推
	 */
	private Integer bizType;
	/**
	 * 要计算的数量
	 */
	private BigDecimal amount;


}
