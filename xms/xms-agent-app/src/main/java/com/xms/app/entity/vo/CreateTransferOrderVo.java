package com.xms.app.entity.vo;

import com.xms.common.annotation.ValidDiyStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import org.checkerframework.checker.units.qual.min;

import java.math.BigDecimal;

/**
 * 创建质押订单
 */
@Data
public class CreateTransferOrderVo {

	/**
	 * 质押套餐id
	 */
	@NotNull
	private Long id;

	/**
	 * 质押金额
	 */
	@NotNull
	private BigDecimal amount;

	/**
	 * 谷歌验证码
	 */
	@NotNull
	private String googleCode;
}
