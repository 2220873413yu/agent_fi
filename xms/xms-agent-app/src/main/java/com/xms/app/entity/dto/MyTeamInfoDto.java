package com.xms.app.entity.dto;

import com.xms.common.annotation.Excel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 我的团队数据
 */
@Data
public class MyTeamInfoDto {

	/**
	 * 团队用户数
	 */
	private Integer umbrellaNum;

	/**
	 * 直推用户数
	 */
	private Integer subNum;

	/**
	 * 我的等级
	 */
	private Integer gameLevel;

	/**
	 * 团队业绩(usdt)
	 */
	private BigDecimal umbrellaPerformance;

	/**
	 * 团队等级分布
	 */
	private List<TeamLevelDto> teams;
}
