package com.xms.dao.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.xms.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import java.math.BigDecimal;
import java.util.Date;

import com.xms.common.annotation.Excel;

/**
 * 矿机订单对象 t_mining_package_order
 *
 * @author xms
 * @date 2026-02-23
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "t_mining_package_order")
public class MiningPackageOrder extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 主键id */
    @TableId(type = IdType.AUTO)
	@Excel(name = "矿机编号",sort = 1)
    private Long id;
    /** 矿机编号 */

    @ApiModelProperty(value = "矿机编号")
    private String miningNo;
    /** 订单号 */
    @Excel(name = "订单号",sort = 2,width = 30)
    @ApiModelProperty(value = "订单号")
    private String orderNo;
	/**
	 * 主订单号 业务场景:一次性可以买多个矿机标识哪几个订单是同一个时间点买的
	 */
	@Excel(name = "主订单号",sort = 3,width = 30)
	private String masterOrderNo;

    /** 用户id */
    @Excel(name = "用户ID",sort = 4)
    private Long userId;

	/** 支付方式 1:USDT,2:DFC */
	@Excel(name = "支付方式",sort = 5,dictType = "t_mining_package_order_pay_type")
	private Integer payType;

	/** 订单价值多少usdt */
	@Excel(name = "订单价值多少usdt",sort = 6)
	private BigDecimal orderValueUsdt;

	/** 支付USDT金额 */
	@Excel(name = "支付USDT金额",sort = 7)
    private BigDecimal payValidNum1;
	/** 支付DFC金额 */
	@Excel(name = "支付DFC金额",sort = 8)
    private BigDecimal payValidNum2;
	/** 支付锁定USDT金额 */
	@Excel(name = "支付锁定USDT金额",sort = 9)
    private BigDecimal payValidNum4;
	/** dfc的价格 */
	@Excel(name = "dfc的价格",sort = 10)
	private BigDecimal dfcPrice;

    /** 运行天数 */
    @Excel(name = "运行天数",sort = 11)
    @ApiModelProperty(value = "运行天数")
    private Integer runDays;
    /** 订单来源 0:购买,1:后台拨付 */
    @Excel(name = "订单来源",sort = 12, dictType = "t_mining_package_order_source_type")
    @ApiModelProperty(value = "订单来源 0:购买,1:后台拨付")
    private Integer sourceType;
    /** 每日收益 */
    @Excel(name = "每日收益",sort = 13)
    @ApiModelProperty(value = "每日收益")
    private BigDecimal dayReward;

	/** 质押dfc数量 */
	@Excel(name = "质押金额",sort = 14)
	private BigDecimal stakeDfcAmount;
    /** 累计收益 */
    @Excel(name = "累计收益",sort = 15)
    @ApiModelProperty(value = "累计收益")
    private BigDecimal totalReward;
    /** 状态 0:未质押,1:启动中,2:正在产币,3:暂停 */
    @Excel(name = "质押状态", dictType = "t_mining_package_order_status",sort = 16)
    private Integer status;
    /** 购买矿机业务状态 0:未处理,1:已处理 */
    @Excel(name = "购买矿机业务状态", dictType = "t_user_info_is_valid",sort = 17)
    private Integer bizStatus;
    /** 质押矿机业务状态 0:未处理,1:已处理 */
    @Excel(name = "质押矿机业务状态", dictType = "t_user_info_is_valid",sort = 18)
    private Integer bizStatus1;

	/** 质押类型 1:托管,2:自提 */
	@Excel(name = "质押类型",sort = 19,dictType = "t_mining_package_order_stake_type")
	private Integer stakeType;
	/** 质押时间 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@Excel(name = "质押时间", dateFormat = "yyyy-MM-dd HH:mm:ss",sort = 20)
	private Date stakeDate;

	/** 质押启动期剩余天数 */
	@Excel(name = "启动期剩余天数",sort = 21)
	private Integer stakeStartupRemainingDays;

	/**
	 * 是否发货 0:否,1:是
	 */
	@Excel(name = "是否发货",dictType = "t_user_info_is_valid",sort = 22)
	private Integer shippingStatus;

	/**
	 * 物流公司名称
	 */
	@Excel(name = "物流公司名称",sort = 23,width = 30)
	private String shippingCompany;
	/**
	 * 物流单号
	 */
	@Excel(name = "物流单号",sort = 24,width = 30)
	private String trackingNo;

	/** 矿机算力 */
	@Excel(name = "矿机算力",sort = 25)
	private BigDecimal computingPower;

	@TableField(exist = false)
	private String createBy;

	@TableField(exist = false)
	private String updateBy;

	@TableField(exist = false)
	private Integer deleted;

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("miningNo", getMiningNo())
            .append("orderNo", getOrderNo())
            .append("userId", getUserId())
            .append("runDays", getRunDays())
            .append("sourceType", getSourceType())
            .append("dayReward", getDayReward())
            .append("totalReward", getTotalReward())
            .append("createTime", getCreateTime())
            .append("updateTime", getUpdateTime())
            .append("status", getStatus())
            .append("bizStatus", getBizStatus())
            .append("bizStatus1", getBizStatus1())
        .toString();
    }
}
