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
     * 签名
     */
    @NotBlank(message = "sign not null")
    private String sign;

//	/**
//     * 币种类型 2:afi
//     */
//	@ValidDiyStatus(values = {2})
//	@NotNull(message = "coinType not null")
//	private Integer coinType;

    /**
     * 充值代币数量
     */
    @NotNull(message = "amount not null")
    private BigDecimal amount;
}
