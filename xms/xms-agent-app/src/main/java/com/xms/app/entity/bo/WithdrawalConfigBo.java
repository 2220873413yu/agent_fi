package com.xms.app.entity.bo;

import com.xms.common.annotation.Excel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 提现配置bo对象
 */
@Data
public class WithdrawalConfigBo {
	/** 提现开关(1:开,2:关) */
	private Integer withdrawOpen;

	/** 最小提现金额 */
	private BigDecimal minWithdrawAmount;

	/** 手续费率(例如:10表示10%) */
	private BigDecimal feeRatio;
}
