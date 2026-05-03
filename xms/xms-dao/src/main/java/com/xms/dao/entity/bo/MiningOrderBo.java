package com.xms.dao.entity.bo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MiningOrderBo {
	private Long id;
	private Long userId;
	private BigDecimal reward;
}
