package com.xms.dao.entity.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.xms.common.annotation.Excel;
import com.xms.common.core.domain.BaseXmsEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * <p>
 * 钱包流水表
 * </p>
 *
 * @since 2023-07-25
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_user_money_log")
@ApiModel(value = "UserMoneyLog对象", description = "钱包流水表")
public class UserMoneyLog extends BaseXmsEntity {

	@ApiModelProperty(value = "主键id")
	@TableId(value = "id", type = IdType.AUTO)
	private Integer id;

	/**
	 * 用户UID
	 */
	@ApiModelProperty(value = "用户id")
	@Excel(name = "用户UID",sort = 1)
	private Long userId;

	/**
	 * 币种1:USDT,2:DFC,3:OORT,4:锁定USDT,5:产出DFC
	 */
	@ApiModelProperty(value = "币种")
	@Excel(name = "币种",dictType = "t_user_money_log_coin_type",sort = 2)
	private Integer coinType;

	/**
	 * 变动额度
	 */
	@ApiModelProperty(value = "变动额度")
	@Excel(name = "变动额度",sort = 3)
	private BigDecimal changeBalance;

	/**
	 * 变动前余额
	 */
	@ApiModelProperty(value = "变动前余额")
	@Excel(name = "变动前余额",sort = 4)
	private BigDecimal beforeBalance;

	/**
	 * 变动后余额
	 */
	@ApiModelProperty(value = "变动后余额")
	@Excel(name = "变动后余额",sort = 4)
	private BigDecimal afterBalance;

	/**
	 * 流水号
	 */
	@ApiModelProperty(value = "流水号")
	@Excel(name = "流水号",sort = 5,width = 20)
	private String serialCode;

	/**
	 * 来源订单
	 */
	@ApiModelProperty(value = "来源订单")
	@Excel(name = "来源订单",sort = 6,width = 20)
	private String sourceCode;

	/**
	 * 来源类型
	 */
	@ApiModelProperty(value = "来源类型(1.充值 2.提现 3.推荐奖 4.级差奖 5.平级奖 6.购买套餐)")
	@Excel(name = "来源类型",dictType = "t_user_money_log_source_type",sort = 7)
	private Integer sourceType;

	@ApiModelProperty(value = "备注")
	private String remark;

	@ApiModelProperty(value = "是否删除（1:否,2:是）")
	private Integer activeFlag;
	/**
	 * binlog对应唯一ID 必填
	 */
	private String gtId;
	/**
	 * 来源用户ID
	 */
	private Long sourceId;

	/**
	 * 用户账号
	 */
	@TableField(exist = false)
	@Excel(name = "钱包地址",sort = 1, width = 40)
	private String userAccount;
}
