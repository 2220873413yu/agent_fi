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
 * 托管全球分红明细对象 t_stake_hosting_global_dividend_detail
 *
 * @author xms
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "t_stake_hosting_global_dividend_detail")
public class StakeHostingGlobalDividendDetail extends BaseEntity {
	private static final long serialVersionUID = 1L;

	@TableId(type = IdType.AUTO)
	@ApiModelProperty(value = "主键ID")
	private Long id;

	@Excel(name = "批次号", sort = 1, width = 30)
	@ApiModelProperty(value = "批次号")
	private String batchNo;

	@Excel(name = "用户ID", sort = 2)
	@ApiModelProperty(value = "用户ID")
	private Long userId;

	@Excel(name = "钱包地址", sort = 3, width = 40)
	@ApiModelProperty(value = "钱包地址")
	private String account;

	@Excel(name = "奖励等级", sort = 4, dictType = "t_user_info_game_level")
	@ApiModelProperty(value = "奖励等级")
	private Integer rewardLevel;

	@Excel(name = "等级分红比例", sort = 5)
	@ApiModelProperty(value = "等级分红比例，单位%")
	private BigDecimal levelDividendRatio;

	@Excel(name = "等级奖池金额", sort = 6)
	@ApiModelProperty(value = "等级奖池金额")
	private BigDecimal levelPoolAmount;

	@Excel(name = "用户小区业绩", sort = 7)
	@ApiModelProperty(value = "用户小区业绩")
	private BigDecimal userCommunityPerformance;

	@Excel(name = "等级小区业绩总和", sort = 8)
	@ApiModelProperty(value = "等级小区业绩总和")
	private BigDecimal levelCommunityPerformance;

	@Excel(name = "分红金额", sort = 9)
	@ApiModelProperty(value = "分红金额")
	private BigDecimal rewardAmount;
}
