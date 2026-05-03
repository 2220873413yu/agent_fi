package com.xms.app.entity.vo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 收款信息
 * @author xms
 * @date 2023/07/08
 */
@Data
public class UserBankVo {
	private Long id;
	/**
	 * 收款方式 0:银行卡,1:BEP-20,2:TRC-20
	 */
	@NotNull
	private Integer paymentType;
	/** 持卡人姓名 */
	//@NotBlank
	private String accountName;
	/** 银行卡卡号/u地址 */
	@NotBlank
	private String accountNumber;
	/** 开户行 */
	//@NotBlank
	private String bankName;
}
