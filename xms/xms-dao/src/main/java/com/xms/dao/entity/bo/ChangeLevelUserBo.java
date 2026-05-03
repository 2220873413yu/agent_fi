package com.xms.dao.entity.bo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 提升等级用户bo对象
 * @author xms
 * @date 2022/4/14 15:04
 */
@Data
public class ChangeLevelUserBo {
	private Long userId;
	private Integer gameLevel;
	private Integer maxGameLevel;
	private BigDecimal validSubNum;
	private BigDecimal validUmbrellaNum;
	/**
	 * 账户余额
	 */
	private BigDecimal userAmount;
}
