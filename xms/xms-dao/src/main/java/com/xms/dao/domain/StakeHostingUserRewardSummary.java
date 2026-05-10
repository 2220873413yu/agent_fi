package com.xms.dao.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.xms.common.core.domain.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 托管用户奖励累计汇总对象 t_stake_hosting_user_reward_summary
 *
 * @author xms
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "t_stake_hosting_user_reward_summary")
public class StakeHostingUserRewardSummary extends BaseEntity {
	private static final long serialVersionUID = 1L;

	@TableId
	@ApiModelProperty(value = "用户ID")
	private Long userId;

	@ApiModelProperty(value = "托管极差奖累计")
	private BigDecimal diffRewardAmount;

	@ApiModelProperty(value = "托管平级奖累计")
	private BigDecimal sameLevelRewardAmount;

	@ApiModelProperty(value = "托管全球分红累计，可后续使用")
	private BigDecimal globalDividendAmount;
}
