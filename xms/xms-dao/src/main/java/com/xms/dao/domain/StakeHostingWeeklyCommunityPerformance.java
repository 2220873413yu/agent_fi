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

	@Excel(name = "本周个人新增积分", sort = 5)
	@ApiModelProperty(value = "本周个人新增积分，来自本周生效的个人托管订单performance_points快照")
	private BigDecimal selfIncreasePoints;

	@Excel(name = "本周个人到期积分", sort = 6)
	@ApiModelProperty(value = "本周个人到期积分，来自本周到期完成的个人托管订单performance_points快照")
	private BigDecimal selfExpirePoints;

	@Excel(name = "本周团队新增积分", sort = 7)
	@ApiModelProperty(value = "本周团队新增积分，来自本周生效的伞下托管订单performance_points快照")
	private BigDecimal teamIncreasePoints;

	@Excel(name = "本周团队到期积分", sort = 8)
	@ApiModelProperty(value = "本周团队到期积分，来自本周到期完成的伞下托管订单performance_points快照")
	private BigDecimal teamExpirePoints;

	@Excel(name = "个人净新增积分", sort = 9)
	@ApiModelProperty(value = "本周个人净新增积分 = 本周个人新增积分 - 本周个人到期积分")
	private BigDecimal selfNewPerformance;

	@Excel(name = "团队净新增积分", sort = 10)
	@ApiModelProperty(value = "本周团队净新增积分 = 本周团队新增积分 - 本周团队到期积分")
	private BigDecimal teamNewPerformance;

	@Excel(name = "直推区有效积分合计", sort = 11)
	@ApiModelProperty(value = "本周末所有直推区有效积分合计")
	private BigDecimal totalLinePerformance;

	@Excel(name = "最大直推区有效积分", sort = 12)
	@ApiModelProperty(value = "本周末最大直推区有效积分")
	private BigDecimal maxLinePerformance;

	@Excel(name = "上周末小区有效积分", sort = 13)
	@ApiModelProperty(value = "上周末小区有效积分快照")
	private BigDecimal previousCommunityPerformance;

	@Excel(name = "本周末小区有效积分", sort = 14)
	@ApiModelProperty(value = "本周末小区有效积分快照")
	private BigDecimal currentCommunityPerformance;

	@Excel(name = "小区净新增积分", sort = 15)
	@ApiModelProperty(value = "本周小区净新增积分/全球分红权重 = 本周末小区有效积分 - 上周末小区有效积分")
	private BigDecimal communityNewPerformance;

	@Excel(name = "个人新增业绩", sort = 16)
	@ApiModelProperty(value = "本周个人新增托管业绩，单位USDT，不乘套餐积分权重")
	private BigDecimal selfIncreaseAmount;

	@Excel(name = "个人到期业绩", sort = 17)
	@ApiModelProperty(value = "本周个人到期托管业绩，单位USDT，不乘套餐积分权重")
	private BigDecimal selfExpireAmount;

	@Excel(name = "团队新增业绩", sort = 18)
	@ApiModelProperty(value = "本周团队新增托管业绩，单位USDT，不乘套餐积分权重")
	private BigDecimal teamIncreaseAmount;

	@Excel(name = "团队到期业绩", sort = 19)
	@ApiModelProperty(value = "本周团队到期托管业绩，单位USDT，不乘套餐积分权重")
	private BigDecimal teamExpireAmount;

	@Excel(name = "个人净新增业绩", sort = 20)
	@ApiModelProperty(value = "本周个人净新增托管业绩，单位USDT")
	private BigDecimal selfNetAmount;

	@Excel(name = "团队净新增业绩", sort = 21)
	@ApiModelProperty(value = "本周团队净新增托管业绩，单位USDT")
	private BigDecimal teamNetAmount;

	@Excel(name = "直推区有效业绩合计", sort = 22)
	@ApiModelProperty(value = "本周末所有直推区有效托管业绩合计，单位USDT")
	private BigDecimal totalLineAmount;

	@Excel(name = "最大直推区有效业绩", sort = 23)
	@ApiModelProperty(value = "本周末最大直推区有效托管业绩，单位USDT")
	private BigDecimal maxLineAmount;

	@Excel(name = "上周末小区有效业绩", sort = 24)
	@ApiModelProperty(value = "上周末小区有效托管业绩快照，单位USDT")
	private BigDecimal previousCommunityAmount;

	@Excel(name = "本周末小区有效业绩", sort = 25)
	@ApiModelProperty(value = "本周末小区有效托管业绩快照，单位USDT")
	private BigDecimal currentCommunityAmount;

	@Excel(name = "小区净新增业绩", sort = 26)
	@ApiModelProperty(value = "本周小区净新增托管业绩，单位USDT")
	private BigDecimal communityNewAmount;

	@Excel(name = "状态", sort = 27, dictType = "t_stake_hosting_weekly_community_performance_settle_status")
	@ApiModelProperty(value = "状态 0:统计中 1:已锁定 2:已参与分红")
	private Integer settleStatus;

	@Excel(name = "分红批次号", sort = 28, width = 30)
	@ApiModelProperty(value = "后续全球分红批次号")
	private String batchNo;

	@ApiModelProperty(value = "删除标志 0正常 1删除")
	private Integer deleted;
}
