package com.xms.app.entity.vo;

import com.xms.common.annotation.ValidDiyStatus;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 购买矿机
 */
@Data
public class CreateMiningOrderVo {
	/**
	 * 矿机id
	 */
	@NotNull
	private Long id;

	/**
	 * 签名
	 */
	@ApiModelProperty(value = "签名")
	@NotBlank
	private String signature;

	/**
	 * 随机数不能为空
	 */
	@NotBlank(message = "随机数不能为空")
	private String randomNum;

	/**
	 * 购买数量
	 */
	@NotNull
	@Positive
	private Integer num;

	/**
	 * 支付方式 1:USDT,2:DFC
	 */
	@NotNull
	@ValidDiyStatus(values = {1,2})
	private Integer payType;
}
