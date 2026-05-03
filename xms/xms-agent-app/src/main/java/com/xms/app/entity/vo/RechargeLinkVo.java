package com.xms.app.entity.vo;

import lombok.Data;

/**
 * 充值vo对象
 * @author xms
 * @date 2023/07/07
 */
@Data
public class RechargeLinkVo {
	/** 主键id */
	private Long id;
	/** 收款类型(0:银行卡,1:BEP-20,2:TRC-20,3:) */
	private Integer type;
	/** 持卡人 */
	private String accountName;
	/** 银行卡号/usdt地址 */
	private String accountNo;
	/** 开户行/网络名称 */
	private String bankName;
}
