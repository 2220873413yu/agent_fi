package com.xms.app.entity.vo;

import com.xms.common.annotation.ValidDiyStatus;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateStakeOrderVo {

	/**
	 * 签名
	 */
	@NotBlank
	private String signature;

	/**
	 * 随机数不能为空
	 */
	@NotBlank(message = "随机数不能为空")
	private String randomNum;

	/**
	 * 商品id
	 */
	@NotNull
	private Long productId;

	/**
	 * 购买数量
	 */
	@NotNull
	private Integer num;

	/**
	 * 收货地址id
	 */
	private Long addressId;

	/**
	 * sku编码
	 */
	@NotBlank
	private String codeUnique;

	/**
	 * 用户的钱包usdt余额
	 */
	@NotNull
	private BigDecimal userAmount;

}
