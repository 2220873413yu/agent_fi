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
 * 用户转账记录对象 t_user_transfer
 *
 * @author xms
 * @date 2025-03-12
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "t_user_transfer")
public class UserTransfer extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 主键id */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 转账用户ID */
    @Excel(name = "转账用户ID",sort = 1)
    private Long fromUserId;
    /** 转账用户账号 */
    @Excel(name = "转账钱包地址",sort = 1,width = 40)
    private String fromAccount;

    /** 接收用户ID */
    @Excel(name = "接收用户ID",sort = 3)
    private Long toUserId;
    /** 接收用户账号 */
    @Excel(name = "接收钱包地址",sort = 2,width = 40)
    private String toAccount;
    /** 转账订单号 */
    @Excel(name = "转账订单号",sort = 3,width = 40)
    private String code;
	/** 转账币种1:USDT,2:DFC,3:OORT,4:锁定USDT,5:产出DFC */
	@Excel(name = "转账币种",sort = 4,dictType = "t_user_money_log_coin_type")
	private Integer coinType;
    /** 转账额度 */
    @Excel(name = "转账额度",sort = 5)
    private BigDecimal changeBalance;
    /** 手续费 */
    @Excel(name = "手续费",sort = 6)
    private BigDecimal feeBalance;
    /** 手续费率 */
    @Excel(name = "手续费率%",sort = 7)
    private String feeRatio;
    /** 到账金额，用户实际收到的金额（扣除手续费后的金额） */
    @Excel(name = "到账金额",sort = 8)
    private BigDecimal actualAmount;



	/** 创建者 */
	@JsonIgnore
	@TableField(exist = false)
	private String createBy;

	/** 更新者 */
	@JsonIgnore
	@TableField(exist = false)
	private String updateBy;
	@JsonIgnore
	@TableField(exist = false)
	private Integer deleted;
	@JsonIgnore
	@TableField(exist = false)
	private Date updateTime;
	@JsonIgnore
	@TableField(exist = false)
	private String remark;
    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("fromUserId", getFromUserId())
            .append("fromAccount", getFromAccount())
            .append("toUserId", getToUserId())
            .append("toAccount", getToAccount())
            .append("code", getCode())
            .append("changeBalance", getChangeBalance())
            .append("feeBalance", getFeeBalance())
            .append("feeRatio", getFeeRatio())
            .append("actualAmount", getActualAmount())
            .append("createTime", getCreateTime())
            .append("coinType", getCoinType())
        .toString();
    }
}
