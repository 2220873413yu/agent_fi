package com.xms.dao.entity.req;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 托管用户累计金额后台手动调整请求。
 */
@Data
public class StakeHostingUserAmountAdjustReq {
	/** 调整金额 */
	private BigDecimal amount;

	/** 备注 */
	private String remark;
}
