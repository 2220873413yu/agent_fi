package com.xms.app.entity;

import com.xms.common.annotation.ValidDiyStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;


/**
 * @author: renengadePISTA
 * @createDate: 2023/5/30
 */
@Data
public class OrderReq {
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
	 * 下单金额
	 */
	@NotBlank(message = "下单金额不能为空")
	@Positive
	private String amount;


}
