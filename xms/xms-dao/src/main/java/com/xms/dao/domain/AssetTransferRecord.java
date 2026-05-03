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
 * DF划转记录对象 t_asset_transfer_record
 *
 * @author xms
 * @date 2026-02-25
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "t_asset_transfer_record")
public class AssetTransferRecord extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 主键id */
    @TableId(type = IdType.AUTO)
    private String id;
    /** 用户ID */
    @Excel(name = "用户ID", sort = 1)
    @ApiModelProperty(value = "用户ID")
    private Long userId;
    /** 划出地址 */
    @Excel(name = "划出地址", sort = 2, width = 40)
    @ApiModelProperty(value = "划出地址")
    private String fromAddress;
    /** 划入地址 */
    @Excel(name = "划入地址", sort = 3, width = 40)
    @ApiModelProperty(value = "划入地址")
    private String toAddress;
    /** 第三方来源订单号 */
    @Excel(name = "第三方来源订单号", sort = 4, width = 30)
    @ApiModelProperty(value = "第三方来源订单号")
    private String sourceOrderNo;
    /** 币种 1:USDT,2:DFC,3:OORT,4:锁定USDT */
    //@Excel(name = "币种", sort = 5)
    private Integer coinType;
    /** 订单号 */
    @Excel(name = "订单号", sort = 5, width = 30)
    @ApiModelProperty(value = "订单号")
    private String orderNo;
    /** 划转金额 */
    @Excel(name = "划转金额", sort = 6)
    @ApiModelProperty(value = "划转金额")
    private BigDecimal rechargeAmount;
    /** 到账金额 */
    @Excel(name = "到账金额", sort = 7)
    @ApiModelProperty(value = "到账金额")
    private BigDecimal arrivalAmount;



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
            .append("userId", getUserId())
            .append("fromAddress", getFromAddress())
            .append("toAddress", getToAddress())
            .append("sourceOrderNo", getSourceOrderNo())
            .append("coinType", getCoinType())
            .append("orderNo", getOrderNo())
            .append("rechargeAmount", getRechargeAmount())
            .append("remark", getRemark())
            .append("arrivalAmount", getArrivalAmount())
            .append("createTime", getCreateTime())
            .append("updateTime", getUpdateTime())
        .toString();
    }
}
