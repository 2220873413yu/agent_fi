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
 * 托管全球分红奖池流水对象 t_stake_hosting_global_dividend_pool_log
 *
 * @author xms
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "t_stake_hosting_global_dividend_pool_log")
public class StakeHostingGlobalDividendPoolLog extends BaseEntity {
	private static final long serialVersionUID = 1L;

	@TableId(type = IdType.AUTO)
	@ApiModelProperty(value = "主键ID")
	private Long id;

	@Excel(name = "流水单号", sort = 1, width = 30)
	@ApiModelProperty(value = "流水单号")
	private String logNo;

	@Excel(name = "奖池ID", sort = 2)
	@ApiModelProperty(value = "奖池ID")
	private Long poolId;

	@Excel(name = "奖池编码", sort = 3)
	@ApiModelProperty(value = "奖池编码")
	private String poolCode;

	@Excel(name = "流水类型", sort = 4, dictType = "t_stake_hosting_global_dividend_pool_log_flow_type")
	@ApiModelProperty(value = "流水类型 1:收入 2:支出")
	private Integer flowType;

	@Excel(name = "业务类型", sort = 5, dictType = "t_stake_hosting_global_dividend_pool_log_biz_type")
	@ApiModelProperty(value = "业务类型 1:每日服务费入池 2:后台手动增加 3:每周全球分红扣减 4:后台手动扣减")
	private Integer bizType;

	@Excel(name = "变动金额", sort = 6)
	@ApiModelProperty(value = "变动金额")
	private BigDecimal changeAmount;

	@Excel(name = "变动前余额", sort = 7)
	@ApiModelProperty(value = "变动前余额")
	private BigDecimal beforeAmount;

	@Excel(name = "变动后余额", sort = 8)
	@ApiModelProperty(value = "变动后余额")
	private BigDecimal afterAmount;

	@ApiModelProperty(value = "来源托管订单ID")
	private Long sourceOrderId;

	@Excel(name = "来源订单号", sort = 9, width = 30)
	@ApiModelProperty(value = "来源订单号")
	private String sourceOrderNo;

	@Excel(name = "来源用户ID", sort = 10)
	@ApiModelProperty(value = "来源用户ID")
	private Long sourceUserId;

	@Excel(name = "来源结算日", sort = 11)
	@ApiModelProperty(value = "来源结算日，格式yyyyMMdd")
	private Integer sourceSettlementDay;

	@Excel(name = "来源批次号", sort = 12, width = 30)
	@ApiModelProperty(value = "来源批次号")
	private String sourceBatchNo;
}
