package com.xms.dao.entity.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class InterestStatDayDto {
	/** 主键 */
	private Long id;
	private Long userId;
	/** 当日总利息 */
	private BigDecimal todayInterest;
	private Date createTime;
}
