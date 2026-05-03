package com.xms.app.entity.vo;

import com.xms.common.annotation.ValidDiyStatus;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 转账vo参数
 *
 * @author xms
 * @date 2023/07/07
 */
@Data
public class TransferOrderVo {
	/**
	 * 转账金额,转账金额最多保留2位小数点
	 */
	@NotNull
	@Positive
	private BigDecimal amount;

	/**
	 * 转账币种 1:USDT,2:DFC,3:OORT,4:锁定USDT,5:产出DFC
	 */
	@NotNull
	@ValidDiyStatus(values = {1,2,3,4,5}, message = "bizType error")
	private Integer coinType;

	/**
	 * 收款人钱包地址
	 */
	@NotBlank
	private String toUserAddress;

	/**
	 * 签名
	 */
	@ApiModelProperty(value = "签名")
	private String signature;

	/**
	 * 随机数不能为空
	 */
	@NotBlank(message = "随机数不能为空")
	private String randomNum;

}
