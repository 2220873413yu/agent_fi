package com.xms.app.entity.vo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 充值vo对象
 * @author xms
 * @date 2023/07/07
 */
@Data
public class RechargeVo {
	/** 充值数量,最多保留2位小数 */
	@NotNull
	@Positive
	private BigDecimal cnt;
	/** 收款方式id */
	@NotNull
	private Long id;
	/** 交易凭证 */
	@NotBlank
	private String voucherImage;
}
