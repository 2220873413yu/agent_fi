package com.xms.app.entity.bo;

import com.xms.common.annotation.ValidDiyStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 充值订单回调接口
 */
@Data
public class DestroyCallbackBo {

	/**
     * 钱包地址
     */
	@NotBlank(message = "address not null")
	private String address;

    /**
     * hash
     */
    @NotBlank(message = "hash not null")
    private String hash;

	/**
	 * 订单号
	 */
	private String orderNo;

	/**
	 * 充值币种 0:afi,1:usdt
	 */
	@NotNull
	@ValidDiyStatus(values = {0,1}, message = "coinType error")
	private Integer coinType;
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
