package com.xms.dao.entity.vo;

import lombok.Data;

/**
 * 在用户结算的时候用到的vo对象
 * @Description:
 * @Author: luog
 * @Date: 2020/5/12 10:07
 */
@Data
public class ParentUserTaskVo {
	/**
	 * 用户id
	 */
	private Long userId;

	/**
	 * 等级
	 */
	private Integer gameLevel;

	/**
	 * 虚拟等级
	 */
	private Integer minGameLevel;


	/**
	 * 是否有效 0 无效 1 有效
	 */
	private Integer isValid;
}
