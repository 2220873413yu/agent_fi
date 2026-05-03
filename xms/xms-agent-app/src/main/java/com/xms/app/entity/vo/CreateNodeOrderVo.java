package com.xms.app.entity.vo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 创建节点订单参数
 * @author xms
 * @date 2023/6/12
 */
@Data
public class CreateNodeOrderVo {
	/**
	 * 节点等级
	 */
	@NotNull(message = "节点等级不能为空")
	private Integer level;

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
	 * 用户的钱包usdt余额
	 */
	@NotNull
	private BigDecimal userAmount;
}
