package com.xms.app.entity.vo;

import com.xms.common.annotation.ValidDiyStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * trc划转到bsc
 */
@Data
public class UserMoneySwapVo {

	/**
	 * trc划转到bsc
	 */
	@NotNull
	@Max(value = 999999, message = "划转金额过大")
	private BigDecimal amount;

	/**
	 * 业务类型 0:usdt划转成为平台币,1:平台币划转成为usdt
	 */
	@NotNull
	@ValidDiyStatus(values = {0,1})
	private Integer bizType;
}
