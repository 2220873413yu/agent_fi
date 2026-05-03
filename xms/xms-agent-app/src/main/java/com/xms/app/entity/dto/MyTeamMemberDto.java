package com.xms.app.entity.dto;

import com.xms.common.annotation.Excel;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 我的团队成员
 */
@Data
public class MyTeamMemberDto {

	/**
	 * 用户id
	 */
	private Long userId;

	/**
	 * 钱包地址
	 */
	private String account;

	/**
	 * 用户等级
	 */
	private Integer gameLevel;

	/**
	 * 我的销毁(销毁boomai)
	 */
	private BigDecimal performanceV1;

	/**
	 * 团队销毁(usdt)
	 */
	private BigDecimal umbrellaPerformance;

	/**
	 * 团队销毁(boomai)
	 */
	private BigDecimal umbrellaPerformanceV1;

	/**
	 * 加入时间
	 */
	private Date createTime;

	/**
	 * 层级
	 */
	private Integer distance;
}
