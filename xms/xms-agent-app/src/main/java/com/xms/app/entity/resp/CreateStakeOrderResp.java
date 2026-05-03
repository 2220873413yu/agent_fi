package com.xms.app.entity.resp;

import com.xms.common.annotation.Excel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 创建质押订单返回信息
 */
@Data
public class CreateStakeOrderResp {
	/** 质押订单号 */
	private String orderNo;
	/** 质押金额/USDT单位 */
	private BigDecimal stakeUsdtAmount;
}
