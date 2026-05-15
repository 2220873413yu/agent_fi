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
 * 托管全球分红权重快照对象 t_stake_hosting_global_dividend_weight_snapshot
 *
 * @author xms
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "t_stake_hosting_global_dividend_weight_snapshot")
public class StakeHostingGlobalDividendWeightSnapshot extends BaseEntity {
	private static final long serialVersionUID = 1L;

	@TableId(type = IdType.AUTO)
	@ApiModelProperty(value = "主键ID")
	private Long id;

	@Excel(name = "用户ID", sort = 1)
	@ApiModelProperty(value = "用户ID")
	private Long userId;

	@Excel(name = "钱包地址", sort = 2, width = 40)
	@ApiModelProperty(value = "钱包地址快照")
	private String account;

	@Excel(name = "周开始时间", sort = 3)
	@ApiModelProperty(value = "周开始时间，格式yyyyMMddHHmmss")
	private Long weekStartTime;

	@Excel(name = "周结束时间", sort = 4)
	@ApiModelProperty(value = "周结束时间，格式yyyyMMddHHmmss")
	private Long weekEndTime;

	@Excel(name = "直推区合计权重", sort = 5)
	@ApiModelProperty(value = "结算时刻所有直推区有效权重合计")
	private BigDecimal totalLineWeight;

	@Excel(name = "最大区权重", sort = 6)
	@ApiModelProperty(value = "结算时刻最大直推区有效权重")
	private BigDecimal maxLineWeight;

	@Excel(name = "小区权重", sort = 7)
	@ApiModelProperty(value = "结算时刻小区有效权重 = 直推区合计权重 - 最大区权重")
	private BigDecimal communityWeight;

	@Excel(name = "上期小区权重", sort = 8)
	@ApiModelProperty(value = "上一期结算快照的小区有效权重")
	private BigDecimal previousCommunityWeight;

	@Excel(name = "本期分红权重", sort = 9)
	@ApiModelProperty(value = "本期参与分红权重 = max(小区权重 - 上期小区权重, 0)")
	private BigDecimal dividendWeight;

	@Excel(name = "状态", sort = 10, dictType = "t_stake_hosting_global_dividend_weight_snapshot_settle_status")
	@ApiModelProperty(value = "状态 0:已快照 1:已参与分红")
	private Integer settleStatus;

	@Excel(name = "分红批次号", sort = 11, width = 30)
	@ApiModelProperty(value = "全球分红批次号")
	private String batchNo;

	@ApiModelProperty(value = "删除标志 0正常 1删除")
	private Integer deleted;
}
