package com.xms.app.entity.vo;

import com.xms.common.annotation.Excel;
import lombok.Data;

@Data
public class WithdrawalAddressVo {
	/** bep-20提现地址 */
	private String bepWithdrawalAddress;

	/** trc-20提现地址 */
	private String trcWithdrawalAddress;
}
