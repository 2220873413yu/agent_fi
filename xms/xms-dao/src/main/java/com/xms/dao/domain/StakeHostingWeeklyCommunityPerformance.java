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
 * 托管每周新增小区业绩对象 t_stake_hosting_weekly_community_performance
 *
 * @author xms
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "t_stake_hosting_weekly_community_performance")
public class StakeHostingWeeklyCommunityPerformance extends BaseEntity {
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

	@Excel(name = "个人新增业绩", sort = 5)
	@ApiModelProperty(value = "本周个人新增业绩")
	private BigDecimal selfNewPerformance;

	@Excel(name = "团队新增业绩", sort = 6)
	@ApiModelProperty(value = "本周团队新增业绩")
	private BigDecimal teamNewPerformance;

	@Excel(name = "直推区新增合计", sort = 7)
	@ApiModelProperty(value = "本周所有直推区新增业绩合计")
	private BigDecimal totalLinePerformance;

	@Excel(name = "最大直推区新增", sort = 8)
	@ApiModelProperty(value = "本周最大直推区新增业绩")
	private BigDecimal maxLinePerformance;

	@Excel(name = "上周末小区有效积分", sort = 9)
	@ApiModelProperty(value = "上周末小区有效积分快照")
	private BigDecimal previousCommunityPerformance;

	@Excel(name = "本周末小区有效积分", sort = 10)
	@ApiModelProperty(value = "本周末小区有效积分快照")
	private BigDecimal currentCommunityPerformance;

	@Excel(name = "新增小区业绩", sort = 9)
	@ApiModelProperty(value = "本周新增小区业绩")
	private BigDecimal communityNewPerformance;

	@Excel(name = "状态", sort = 10, dictType = "t_stake_hosting_weekly_community_performance_settle_status")
	@ApiModelProperty(value = "状态 0:统计中 1:已锁定 2:已参与分红")
	private Integer settleStatus;

	@Excel(name = "分红批次号", sort = 11, width = 30)
	@ApiModelProperty(value = "后续全球分红批次号")
	private String batchNo;

	@ApiModelProperty(value = "删除标志 0正常 1删除")
	private Integer deleted;
}
