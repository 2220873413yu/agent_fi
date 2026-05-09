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
 * 托管全球分红奖池对象 t_stake_hosting_global_dividend_pool
 *
 * @author xms
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "t_stake_hosting_global_dividend_pool")
public class StakeHostingGlobalDividendPool extends BaseEntity {
	private static final long serialVersionUID = 1L;

	@TableId(type = IdType.AUTO)
	@ApiModelProperty(value = "主键ID")
	private Long id;

	@Excel(name = "奖池编码", sort = 1)
	@ApiModelProperty(value = "奖池编码")
	private String poolCode;

	@Excel(name = "当前余额", sort = 2)
	@ApiModelProperty(value = "当前余额")
	private BigDecimal balanceAmount;

	@Excel(name = "累计收入", sort = 3)
	@ApiModelProperty(value = "累计收入")
	private BigDecimal totalIncomeAmount;

	@Excel(name = "累计支出", sort = 4)
	@ApiModelProperty(value = "累计支出")
	private BigDecimal totalExpenseAmount;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@Excel(name = "最近收入时间", sort = 5, width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
	@ApiModelProperty(value = "最近收入时间")
	private Date lastIncomeTime;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@Excel(name = "最近支出时间", sort = 6, width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
	@ApiModelProperty(value = "最近支出时间")
	private Date lastExpenseTime;
}
