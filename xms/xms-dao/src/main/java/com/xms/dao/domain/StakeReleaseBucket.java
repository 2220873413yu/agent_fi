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
 * 质押收益线性释放对象 t_stake_release_bucket
 *
 * @author xms
 * @date 2026-02-27
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "t_stake_release_bucket")
public class StakeReleaseBucket extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 主键id */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 用户id */
    @Excel(name = "用户ID",sort = 1)
    @ApiModelProperty(value = "用户id")
    private Long userId;
	/** 订单号 */
	@Excel(name = "订单号", sort = 2, width = 30)
	private String orderNo;
    /** 线性释放天数(如270) */
    @Excel(name = "释放天数", sort = 3)
    @ApiModelProperty(value = "线性释放天数(如270)")
    private Integer linearDays;

	/** 桶内剩余天数 */
	@Excel(name = "剩余天数", sort = 4)
    private Integer haveDays;
    /** 桶内累计应线性释放总量 */
    @Excel(name = "累计应释放总量", sort = 5)
    @ApiModelProperty(value = "桶内累计应线性释放总量")
    private BigDecimal totalAmount;

    /** 桶内剩余待释放量 */
    @Excel(name = "剩余待释放量", sort = 6)
    private BigDecimal remainingAmount;

	/** 每日释放数量 */
	@Excel(name = "每日释放数量", sort = 7)
	private BigDecimal dailyReleaseAmount;
    /** 状态 0:释放中,1:已完成,2:暂停 */
    @Excel(name = "订单状态", sort = 8, dictType = "t_stake_order_status")
    @ApiModelProperty(value = "状态 0:释放中,1:已完成,2:暂停")
    private Integer status;
    /** 时间格式为:yyyymmdd,例如:20260101 */
//    @Excel(name = "时间格式为:yyyymmdd,例如:20260101")
//    @ApiModelProperty(value = "时间格式为:yyyymmdd,例如:20260101")
    private Integer startTime;
    /** 上次释放时间 格式为:yyyymmdd,例如:20260101 */
//    @Excel(name = "上次释放时间 格式为:yyyymmdd,例如:20260101")
//    @ApiModelProperty(value = "上次释放时间 格式为:yyyymmdd,例如:20260101")
    private Integer lastReleaseTime;
    /** 来源快照(json)：记录桶由哪些订单/天数/金额 */
    @Excel(name = "来源快照(json)：记录桶由哪些订单/天数/金额")
    @ApiModelProperty(value = "来源快照(json)：记录桶由哪些订单/天数/金额")
    private String sourceSnapshot;

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
            .append("userId", getUserId())
            .append("linearDays", getLinearDays())
            .append("totalAmount", getTotalAmount())
            .append("remainingAmount", getRemainingAmount())
            .append("status", getStatus())
            .append("startTime", getStartTime())
            .append("lastReleaseTime", getLastReleaseTime())
            .append("createTime", getCreateTime())
            .append("updateTime", getUpdateTime())
            .append("sourceSnapshot", getSourceSnapshot())
        .toString();
    }
}
