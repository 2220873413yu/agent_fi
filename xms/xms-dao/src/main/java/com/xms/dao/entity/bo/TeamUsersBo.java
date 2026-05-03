package com.xms.dao.entity.bo;

import lombok.Data;

import java.util.Date;

@Data
public class TeamUsersBo {
	/**
	 * 用户id
	 */
	private Long userId;

	/**
	 * 创建时间
	 */
	private Date createTime;
}
