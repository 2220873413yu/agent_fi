package com.xms.app.entity.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.xms.common.annotation.Excel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 空投领取记录
 *
 * @author xms
 * @date 2023/04/05
 */
@Data
public class AirdropClaimRecordVo {

	/** 主键 */
	private Long id;

	/** 领取流水号/锁定号 */
	private String claimNo;

	/** 1:支付中,2:成功,3:超时,4:超时但支付 */
	private Integer status;

	/** 本次应发放的代币数量 */
	private BigDecimal tokenAmount;

	/** 链上交易哈希 */
	private String txHash;

	/** 支付金额 */
	private BigDecimal payAmount;

	/** 创建时间 */
	private Date createTime;

	/** 终态时间(领取时间) */
	private Date completedAt;
}
