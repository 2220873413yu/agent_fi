package com.xms.dao.domain;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.xms.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import java.math.BigDecimal;
import com.xms.common.annotation.Excel;
import com.baomidou.mybatisplus.annotation.TableField;

/**
 * 提现配置对象 t_withdrawal_config
 *
 * @author xms
 * @date 2026-02-06
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "t_withdrawal_config")
public class WithdrawalConfig extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 主键id */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 币种 1:USDT,2:DFC,3:OORT */
    @Excel(name = "币种 1:USDT,2:AFI")
    @ApiModelProperty(value = "币种 1:USDT,2:DFC,3:OORT")
    private Integer coinType;

    /** 提现开关(1:开,2:关) */
    @Excel(name = "提现开关(1:开,2:关)")
    @ApiModelProperty(value = "提现开关(1:开,2:关)")
    private Integer withdrawOpen;

    /** 最小提现金额 */
    @Excel(name = "最小提现金额")
    @ApiModelProperty(value = "最小提现金额")
    private BigDecimal minWithdrawAmount;

	/**
	 * 提现手续费率
	 */
	@Excel(name = "提现手续费率")
    private BigDecimal feeRatio;


    /** 单日免审核次数 */
    @Excel(name = "单日免审核次数")
    @ApiModelProperty(value = "单日免审核次数")
    private Integer dailyFreeAuditCount;

    /** 单日提现额度 */
    @Excel(name = "单日提现额度")
    @ApiModelProperty(value = "提现额度")
    private BigDecimal withdrawLimit;

	@TableField(exist = false)
	private String updateBy;
	@TableField(exist = false)
	private String createBy;
	@TableField(exist = false)
	private Integer deleted;

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("coinType", getCoinType())
            .append("withdrawOpen", getWithdrawOpen())
            .append("minWithdrawAmount", getMinWithdrawAmount())
            .append("dailyFreeAuditCount", getDailyFreeAuditCount())
            .append("withdrawLimit", getWithdrawLimit())
            .append("createTime", getCreateTime())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
        .toString();
    }
}
