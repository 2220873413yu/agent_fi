package com.xms.dao.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.xms.common.annotation.Excel;
import com.xms.common.core.domain.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 充值记录对象 t_recharge_record
 *
 * @author xms
 * @date 2025-03-12
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "t_recharge_record")
public class RechargeRecord extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 主键id */
    @TableId(type = IdType.AUTO)
    private String id;
    /** 用户ID */
    @Excel(name = "用户ID", sort = 1)
    private Long userId;

    /** 充值订单号 */
    @Excel(name = "订单号",sort = 2, width = 30)
    private String orderNo;

	/**
	 * 充值金额
	 */
	@Excel(name = "充值金额",sort = 3)
	private BigDecimal rechargeAmount;

	/**
	 * 币种类型 币种1:USDT,2:DFC,3:OORT
	 */
	@Excel(name = "币种",sort = 4, dictType = "t_user_money_log_coin_type")
	private Integer coinType;


    /** 状态(0:等待充值,1:充值成功 2:已过期)(废弃) */
    @Excel(name = "充值状态",dictType = "t_recharge_record_status",sort = 5)
    private Integer status;

	@Excel(name = "交易hash",sort = 6, width = 30)
	private String txId;

	/**
	 * 钱包地址
	 */
	@Excel(name = "钱包地址",sort = 2, width = 40)
	private String remark;

	@TableField(exist = false)
	@JsonIgnore
	private String createBy;

	@TableField(exist = false)
	@JsonIgnore
	private String updateBy;

	@TableField(exist = false)
	@JsonIgnore
	private Integer deleted;
    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("userId", getUserId())
            .append("orderNo", getOrderNo())
            .append("rechargeAmount", getRechargeAmount())
            .append("status", getStatus())
            .append("remark", getRemark())
            .append("createTime", getCreateTime())
            .append("createBy", getCreateBy())
            .append("updateTime", getUpdateTime())
            .append("updateBy", getUpdateBy())
            .append("deleted", getDeleted())
        .toString();
    }
}
