package com.xms.app.entity.bo;

import com.xms.common.annotation.ValidDiyStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * df资产划转参数
 * @author xms
 * @date 2023/06/12
 */
@Data
public class DfTransferBo {
	/**
	 * 划出钱包地址
	 */
	@NotBlank(message = "address not null")
	private String fromAddress;

	/**
	 * 划入钱包地址
	 */
	@NotBlank(message = "address not null")
	private String toAddress;

	/**
	 * orderNo
	 */
	@NotBlank(message = "orderNo not null")
	private String orderNo;

	/**
	 * 签名
	 */
	@NotBlank(message = "sign not null")
	private String sign;

	/**
	 * 充值代币数量
	 */
	@NotNull(message = "amount not null")
	private BigDecimal amount;
}
