package com.xms.app.entity.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 全球分红池金额
 */
@Data
public class GlobalDividendPoolDto {
	private BigDecimal balanceAmount;
	/** 下次开奖时间（每周日 23:59:59） */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date nextAwardTime;
	/** 当前时间 */
	private Date currentTime;
}
