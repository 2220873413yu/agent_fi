package com.xms.app.entity.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TodayIncomeDto {
	private BigDecimal usdtAmount;
	private BigDecimal sgmAmount;
}
