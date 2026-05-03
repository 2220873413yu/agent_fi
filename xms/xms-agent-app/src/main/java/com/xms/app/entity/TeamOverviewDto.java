package com.xms.app.entity;

import com.xms.common.annotation.Excel;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TeamOverviewDto {

	/**
	 * 昵称
	 */
	private String nickName;

	/**
	 * 头像
	 */
	private String avatar;

	/**
	 * 用户编号
	 */
	private String userCode;

	/**
	 * 等级
	 */
	private Integer gameLevel;
	/**
	 * 团队业绩
	 */
	private BigDecimal umbrellaPerformance;

	/**
	 * 是否下过单 0=否,1=是
	 */
	private Integer hasOrdered;
}
