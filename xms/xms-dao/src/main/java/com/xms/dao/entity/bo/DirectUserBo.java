package com.xms.dao.entity.bo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 直推用户bo对象
 * @author xms
 * @date 2022/4/29 17:04
 */
@Data
public class DirectUserBo {
	/**
	 * 用户id
	 */
	private Long userId;

	/**
	 * 钱包地址
	 */
	private String userAccount;


	/**
	 * 等级
	 */
	private BigDecimal gameLevel;
}
