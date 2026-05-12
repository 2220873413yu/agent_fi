package com.xms.dao.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.xms.common.annotation.Excel;
import com.xms.common.core.domain.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 托管G7每日团队新增业绩与收益率快照对象 t_stake_hosting_daily_team_performance
 *
 * @author xms
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "t_stake_hosting_daily_team_performance")
public class StakeHostingDailyTeamPerformance extends BaseEntity {
	private static final long serialVersionUID = 1L;

	/** 主键ID */
	@TableId(type = IdType.AUTO)
	@ApiModelProperty(value = "主键ID")
	private Long id;

	/** 用户ID */
	@Excel(name = "用户ID", sort = 1)
	@ApiModelProperty(value = "用户ID")
	private Long userId;

	/** 钱包地址快照 */
	@Excel(name = "钱包地址", sort = 2, width = 40)
	@ApiModelProperty(value = "钱包地址快照")
	private String account;

	/** 统计日期，格式yyyyMMdd */
	@Excel(name = "统计日期", sort = 3)
	@ApiModelProperty(value = "统计日期，格式yyyyMMdd")
	private Integer statDay;

	/** 当天伞下团队新增托管USDT金额 */
	@Excel(name = "团队新增USDT", sort = 4)
	@ApiModelProperty(value = "当天伞下团队新增托管USDT金额")
	private BigDecimal teamNewAmount;

	/** 当天伞下团队到期托管USDT金额，当前不参与G7静态日利率 */
	@Excel(name = "团队到期USDT", sort = 5)
	@ApiModelProperty(value = "当天伞下团队到期托管USDT金额，当前不参与G7静态日利率")
	private BigDecimal teamExpiredAmount;

	/** 昨日伞下团队新增托管USDT金额，字段名沿用 previous_team_tvl */
	@Excel(name = "昨日团队新增USDT", sort = 6)
	@ApiModelProperty(value = "昨日伞下团队新增托管USDT金额，字段名沿用 previous_team_tvl")
	private BigDecimal previousTeamTvl;

	/** 当日伞下团队新增托管USDT金额，字段名沿用 current_team_tvl */
	@Excel(name = "当日团队新增USDT", sort = 7)
	@ApiModelProperty(value = "当日伞下团队新增托管USDT金额，字段名沿用 current_team_tvl")
	private BigDecimal currentTeamTvl;

	/** 单日增长率，单位% */
	@Excel(name = "单日增长率", sort = 8)
	@ApiModelProperty(value = "单日增长率，单位%")
	@JsonProperty("gDay")
	private BigDecimal gDay;

	/** 最近最多7天滚动平均增长率，单位% */
	@Excel(name = "G7滚动增长率", sort = 9)
	@ApiModelProperty(value = "最近最多7天滚动平均增长率，单位%")
	@JsonProperty("gSmooth")
	private BigDecimal gSmooth;

	/** 命中基础静态收益率，单位% */
	@Excel(name = "基础静态收益率", sort = 10)
	@ApiModelProperty(value = "命中基础静态收益率，单位%")
	private BigDecimal baseStaticRate;

	/** 收益率来源 0未计算 1G7区间 2指定收益率 3未推广特殊规则 */
	@Excel(name = "收益率来源", sort = 11)
	@ApiModelProperty(value = "收益率来源 0未计算 1G7区间 2指定收益率 3未推广特殊规则")
	private Integer rateSource;

	/** 计算状态 0未计算 1已计算 */
	@Excel(name = "计算状态", sort = 12)
	@ApiModelProperty(value = "计算状态 0未计算 1已计算")
	private Integer calcStatus;

	/** 删除标志 0正常 1删除 */
	@ApiModelProperty(value = "删除标志 0正常 1删除")
	private Integer deleted;
}
