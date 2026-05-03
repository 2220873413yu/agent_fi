package com.xms.dao.entity.bo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.xms.common.annotation.Excel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 提现记录Bo对象
 * @author: jiangqf
 * @date: 2020/9/7 15:05
 * @description:
 */
@Data
public class WithdrawalBo {

	/**
	 * 已完成(1,3),处理中(0,1),失败(2,4)
	 * 状态(0.待审核,1.审核成功,2.审核驳回,3:提现成功,4:打款失败)
	 */
	private Integer status;

	/**
	 * 提现单号
	 */
	private String code;

	/**
	 * 提现额度
	 */
	private BigDecimal changeBalance;

	/**
	 * 手续费额度
	 */
	private BigDecimal feeBalance;

	/**
	 * 到账日期
	 */
	private Date creditedTime;

	/**
	 * 创建时间
	 */
	private Date createTime;

	/**
	 * hash
	 */
	private String hash;
}
