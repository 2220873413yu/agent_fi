package com.xms.app.entity;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class RewardVo {
	private BigDecimal fsnValue =BigDecimal.ZERO;
	private BigDecimal ftnValue =BigDecimal.ZERO;
}
