package com.xms.app.entity.vo;

import com.xms.common.annotation.ValidDiyStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ClaimDayRewardVo {
	/**
	 * 矿机id
	 */
	@NotNull
	private Long id;

	/**
	 * 领取方式 2:FTN,3:FSN
	 */
	@ValidDiyStatus(values = {2,3})
	private Integer coinType;

	/**
	 * 领取数量
	 */
	private BigDecimal balance;

}
