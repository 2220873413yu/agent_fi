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
 * 托管奖励结算明细对象 t_stake_hosting_reward_settlement
 *
 * @author xms
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "t_stake_hosting_reward_settlement")
public class StakeHostingRewardSettlement extends BaseEntity {
	private static final long serialVersionUID = 1L;

	@TableId(type = IdType.AUTO)
	@ApiModelProperty(value = "主键ID")
	private Long id;

	@Excel(name = "结算单号", sort = 1, width = 30)
	@ApiModelProperty(value = "结算单号")
	private String settlementNo;

	@ApiModelProperty(value = "源托管订单ID")
	private Long sourceOrderId;

	@Excel(name = "源订单号", sort = 2, width = 30)
	@ApiModelProperty(value = "源订单号")
	private String sourceOrderNo;

	@Excel(name = "源用户ID", sort = 3)
	@ApiModelProperty(value = "源用户ID")
	private Long sourceUserId;

	@Excel(name = "接收用户ID", sort = 4)
	@ApiModelProperty(value = "接收用户ID")
	private Long receiveUserId;

	@Excel(name = "奖励类型", sort = 5, dictType = "t_stake_hosting_reward_settlement_reward_type")
	@ApiModelProperty(value = "奖励类型 1:静态服务费结算 2:直推奖 3:极差奖 4:平级奖 5:平台沉淀")
	private Integer rewardType;

	@Excel(name = "奖励等级", sort = 6, dictType = "t_user_info_game_level")
	@ApiModelProperty(value = "奖励等级")
	private Integer rewardLevel;

	@Excel(name = "奖励基数", sort = 7)
	@ApiModelProperty(value = "奖励基数")
	private BigDecimal rewardBaseAmount;

	@Excel(name = "奖励比例", sort = 8)
	@ApiModelProperty(value = "奖励比例，单位%")
	private BigDecimal rewardRatio;

	@Excel(name = "奖励金额", sort = 9)
	@ApiModelProperty(value = "奖励金额")
	private BigDecimal rewardAmount;

	@Excel(name = "静态毛收益", sort = 10)
	@ApiModelProperty(value = "静态毛收益")
	private BigDecimal grossStaticReward;

	@Excel(name = "服务费比例", sort = 11)
	@ApiModelProperty(value = "服务费比例，单位%")
	private BigDecimal serviceFeeRatio;

	@Excel(name = "服务费金额", sort = 12)
	@ApiModelProperty(value = "服务费金额")
	private BigDecimal serviceFeeAmount;

	@Excel(name = "静态净收益", sort = 13)
	@ApiModelProperty(value = "静态净收益")
	private BigDecimal netStaticReward;

	@Excel(name = "到账状态", sort = 14, dictType = "t_stake_hosting_reward_settlement_arrival_status")
	@ApiModelProperty(value = "到账状态 0:未到账 1:已到账")
	private Integer arrivalStatus;

	@Excel(name = "未到账原因", sort = 15, dictType = "t_stake_hosting_reward_settlement_skip_reason")
	@ApiModelProperty(value = "未到账原因")
	private Integer skipReason;

	@Excel(name = "结算日期", sort = 16)
	@ApiModelProperty(value = "结算日期，格式yyyyMMdd")
	private Integer settlementDay;
}
