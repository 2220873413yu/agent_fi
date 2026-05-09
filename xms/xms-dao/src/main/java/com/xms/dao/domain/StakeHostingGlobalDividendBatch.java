package com.xms.dao.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.xms.common.annotation.Excel;
import com.xms.common.core.domain.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 托管全球分红批次对象 t_stake_hosting_global_dividend_batch
 *
 * @author xms
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "t_stake_hosting_global_dividend_batch")
public class StakeHostingGlobalDividendBatch extends BaseEntity {
	private static final long serialVersionUID = 1L;

	@TableId(type = IdType.AUTO)
	@ApiModelProperty(value = "主键ID")
	private Long id;

	@Excel(name = "批次号", sort = 1, width = 30)
	@ApiModelProperty(value = "批次号")
	private String batchNo;

	@Excel(name = "结算日", sort = 2)
	@ApiModelProperty(value = "结算日，格式yyyyMMdd")
	private Integer settlementDay;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@Excel(name = "周期开始时间", sort = 3, width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
	@ApiModelProperty(value = "周期开始时间")
	private Date periodStartTime;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@Excel(name = "周期结束时间", sort = 4, width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
	@ApiModelProperty(value = "周期结束时间")
	private Date periodEndTime;

	@Excel(name = "计划分红金额", sort = 5)
	@ApiModelProperty(value = "计划分红金额")
	private BigDecimal planAmount;

	@Excel(name = "实际分红金额", sort = 6)
	@ApiModelProperty(value = "实际分红金额")
	private BigDecimal actualAmount;

	@Excel(name = "参与人数", sort = 7)
	@ApiModelProperty(value = "参与人数")
	private Integer userCount;

	@Excel(name = "状态", sort = 8, dictType = "t_stake_hosting_global_dividend_batch_status")
	@ApiModelProperty(value = "状态 0:处理中 1:已完成 2:失败")
	private Integer status;
}
