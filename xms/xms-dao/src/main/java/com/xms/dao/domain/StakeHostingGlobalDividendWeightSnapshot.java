package com.xms.dao.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.xms.common.annotation.Excel;
import com.xms.common.core.domain.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Stake hosting global dividend weekly weight snapshot.
 *
 * <p>One row represents one user's global dividend weight at the weekly 102 settlement moment.
 * The dividend task uses {@code dividendWeight}, not the mutable current weight on {@code t_user_info},
 * to allocate the global dividend pool.</p>
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "t_stake_hosting_global_dividend_weight_snapshot")
public class StakeHostingGlobalDividendWeightSnapshot extends BaseEntity {
	private static final long serialVersionUID = 1L;

	@TableId(type = IdType.AUTO)
	@ApiModelProperty(value = "Primary key")
	private Long id;

	@Excel(name = "用户ID", sort = 1)
	@ApiModelProperty(value = "User ID")
	private Long userId;

	@Excel(name = "钱包地址", sort = 2, width = 40)
	@ApiModelProperty(value = "Wallet account snapshot")
	private String account;

	@Excel(name = "周开始时间", sort = 3)
	@ApiModelProperty(value = "Week start time, format yyyyMMddHHmmss")
	private Long weekStartTime;

	@Excel(name = "周结束时间", sort = 4)
	@ApiModelProperty(value = "Week end time, format yyyyMMddHHmmss")
	private Long weekEndTime;

	@Excel(name = "个人权重", sort = 5)
	@ApiModelProperty(value = "Current self global dividend weight")
	private BigDecimal selfWeight;

	@Excel(name = "团队权重", sort = 6)
	@ApiModelProperty(value = "Current umbrella global dividend weight")
	private BigDecimal umbrellaWeight;

	@Excel(name = "本期小区权重", sort = 7)
	@ApiModelProperty(value = "Current community global dividend weight")
	private BigDecimal communityWeight;

	@Excel(name = "上期小区权重", sort = 8)
	@ApiModelProperty(value = "Previous weekly community global dividend weight")
	private BigDecimal previousCommunityWeight;

	@Excel(name = "本期分红权重", sort = 9)
	@ApiModelProperty(value = "Dividend weight = max(communityWeight - previousCommunityWeight, 0)")
	private BigDecimal dividendWeight;

	@Excel(name = "状态", sort = 10, dictType = "t_stake_hosting_global_dividend_weight_snapshot_settle_status")
	@ApiModelProperty(value = "Settle status 0:not participated 1:participated")
	private Integer settleStatus;

	@Excel(name = "分红批次", sort = 11, width = 30)
	@ApiModelProperty(value = "Global dividend batch number")
	private String batchNo;
}
