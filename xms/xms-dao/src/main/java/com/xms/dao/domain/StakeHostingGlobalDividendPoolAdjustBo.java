package com.xms.dao.domain;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 托管全球分红奖池手动调账参数
 *
 * @author xms
 */
@Data
public class StakeHostingGlobalDividendPoolAdjustBo {
	@ApiModelProperty(value = "流水类型 1:收入 2:支出")
	private Integer flowType;

	@ApiModelProperty(value = "调账金额")
	private BigDecimal amount;

	@ApiModelProperty(value = "备注")
	private String remark;
}
