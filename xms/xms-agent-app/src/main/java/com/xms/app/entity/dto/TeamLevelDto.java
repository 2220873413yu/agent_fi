package com.xms.app.entity.dto;

import lombok.Data;

/**
 * 团队等级数据
 */
@Data
public class TeamLevelDto {

	/**
	 * 等级
	 */
	private Integer gameLevel;

	/**
	 * 团队该等级人数
	 */
	private Integer teamCount;

	/**
	 * 全网该等级人数
	 */
	private Integer globalCount;
}
