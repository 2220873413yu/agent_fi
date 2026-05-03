package com.xms.dao.entity.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * <p>
 * 钱包流水表
 * </p>
 *
 * @author MIER
 * @since 2023-07-25
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserWalletLogVo {

	@ApiModelProperty(value = "币种(对应钱包)")
	private Integer coinType;

	@ApiModelProperty(value = "变动额度-正负")
	private BigDecimal changeBalance;

}
