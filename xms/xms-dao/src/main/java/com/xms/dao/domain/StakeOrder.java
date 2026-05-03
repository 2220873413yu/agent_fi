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
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.xms.common.annotation.Excel;
import com.baomidou.mybatisplus.annotation.TableField;

/**
 * 质押订单对象 t_stake_order
 *
 * @author xms
 * @date 2026-03-09
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "t_stake_order")
public class StakeOrder extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 主键id */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 用户id */
    @Excel(name = "用户ID",sort = 1)
    @ApiModelProperty(value = "用户id")
    private Long userId;
    /** 质押订单号 */
    @Excel(name = "质押订单号",sort = 2,width = 30)
    @ApiModelProperty(value = "质押订单号")
    private String orderNo;
    /** 质押金额/USDT单位 */
    @Excel(name = "质押金额",sort = 3)
    @ApiModelProperty(value = "质押金额/USDT单位")
    private BigDecimal stakeUsdtAmount;
    /** 状态 0:待链上支付确认,1:产出中,2:已出局(已完成),3:已过期未支付,4:已暂停产出,5:支付了但是过期了,6:已下架 */
    @Excel(name = "订单状态",sort = 4,dictType = "t_stake_order_status")
    @ApiModelProperty(value = "状态 0:待链上支付确认,1:产出中,2:已出局(已完成),3:已过期未支付,4:已暂停产出")
    private Integer status;

	/** 商品数量 */
	@Excel(name = "商品数量",sort = 5)
    private Integer num;

	/** 业务状态 0:正常,1:暂停产出 */
    private Integer bizStatus1;
    /** 剩余可产出 */
    @Excel(name = "剩余可产出",sort = 5)
    @ApiModelProperty(value = "剩余可产出")
    private BigDecimal remainingOutAmount;
	/** 今日收益 */
	//@Excel(name = "今日收益")
	private BigDecimal todayReward;
    /** 出局目标产出总额 */
    @Excel(name = "出局目标产出总额",sort = 6)
    @ApiModelProperty(value = "出局目标产出总额")
    private BigDecimal allOutAmount;
    /** 支付时间 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "支付时间",sort = 7, width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date payTime;
    /** 支付hash */
    @Excel(name = "支付hash",sort = 8,width = 40)
    @ApiModelProperty(value = "支付hash")
    private String payHash;

	/**
	 * 本订单是否命中首单发货 0否1是
	 */
	@Excel(name = "是否首单发货",sort = 9,dictType = "t_user_info_is_valid")
	private Integer hashFirstShipOrder;
	/**
	 * 发货状态 0待发货 1已发货
	 */
	@Excel(name = "发货状态",sort = 9, readConverterExp = "0=待发货,1=已发货")
	private Integer shipStatus;

	/**
	 * 发货时间
	 */
	@Excel(name = "发货时间",sort = 9)
	private Date shipTime;
	/**
	 * 收货信息JSON快照
	 */
	@Excel(name = "收货信息JSON快照",sort = 9)
	private String receiverInfo;

	/**
	 * 物流公司
	 */
	@Excel(name = "物流公司",sort = 9)
	private String shipCompany;

	/**
	 * 物流单号
	 */
	@Excel(name = "物流单号",sort = 9)
	private String shipNo;

	/**
	 * 商品快照信息
	 */
	@Excel(name = "商品快照信息",sort = 9)
	private String productSnapshot;

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
            .append("orderNo", getOrderNo())
            .append("stakeUsdtAmount", getStakeUsdtAmount())
            .append("status", getStatus())
            .append("remainingOutAmount", getRemainingOutAmount())
            .append("allOutAmount", getAllOutAmount())
            .append("createTime", getCreateTime())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .append("payTime", getPayTime())
            .append("payHash", getPayHash())
        .toString();
    }
}
