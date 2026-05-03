package com.xms.app.entity.vo;

import com.xms.common.annotation.ValidDiyStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BuyNodePackageVo {

	/**
	 * 套餐id
	 */
    @NotNull
    private Integer id;


	/**
	 * 签名
	 */
	@NotBlank(message = "签名不能为空")
	private String signature;
	/**
	 * 随机数不能为空
	 */
	@NotBlank(message = "随机数不能为空")
	private String randomNum;
	/**
	 * 购买数量1
	 */
	@Positive
	private Integer buyNum=1;
	/**
	 * 只有芯片套餐才有哟
	 */
	private BigDecimal buyValue;

	/**
	 * 支付方式 默认USDT方式支付
	 * 0: USDT支付
	 * 1: ALEO支付
	 */
	@ValidDiyStatus(values = {0,1}, message = "bizType error")
	@NotNull
	private Integer paymentType = 0;
}
