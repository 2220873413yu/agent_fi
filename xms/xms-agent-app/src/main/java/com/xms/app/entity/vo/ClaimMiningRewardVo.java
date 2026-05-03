package com.xms.app.entity.vo;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class ClaimMiningRewardVo {
	/**
	 * 矿机订单id
	 */
	@NotNull
	private Long id;

	/**
	 * 周期数量
	 */
	@Positive
	@NotNull
	private Integer speedWeeks;
}
