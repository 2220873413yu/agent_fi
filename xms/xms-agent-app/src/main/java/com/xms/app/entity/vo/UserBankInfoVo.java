package com.xms.app.entity.vo;

import lombok.Data;

/**
 * 收款信息返回vo对象
 * @author xms
 * @date 2023/10/09
 */
@Data
public class UserBankInfoVo {
	/** 收款id */
	private Long id;
	/** 持卡人姓名 */
	private String accountName;
	/** 银行卡卡号 */
	private String accountNumber;
	/** 开户行 */
	private String accountOpeningBank;

	/** 收款方式 0:银行卡,1:支付宝,2:微信 */
	private Integer paymentType;

	/** vx/支付宝账户 */
	private String paymentAccount;
	/** 姓名 */
	private String paymentRealName;
	/** 二维码 */
	private String paymentQrCode;
}
