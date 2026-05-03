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

import com.xms.common.annotation.Excel;
import com.baomidou.mybatisplus.annotation.TableField;

/**
 * 每日奖励汇总对象 xms_reward_stat_day
 *
 * @author xms
 * @date 2025-11-23
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "xms_reward_stat_day")
public class RewardStatDay extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 用户id */
    @Excel(name = "用户ID",sort = 1)
    @ApiModelProperty(value = "用户id")
    private Long userId;
    /** 日期 类似 yyyymmdd */
    @Excel(name = "日期", sort = 6)
    @ApiModelProperty(value = "日期 类似 yyyymmdd")
    private Long statDate;
    /** 静态奖励 */
    @Excel(name = "静态奖励", sort = 2)
    @ApiModelProperty(value = "静态奖励")
    private BigDecimal staticAmount;
    /** 动态奖励(互动奖励) */
    @Excel(name = "互动奖励",sort = 3)
    @ApiModelProperty(value = "动态奖励(互动奖励)")
    private BigDecimal dynamicAmount;
    /** 团队收益 */
    @Excel(name = "团队收益", sort = 4)
    @ApiModelProperty(value = "团队收益")
    private BigDecimal teamAmount;
    /** 总收益 */
    @Excel(name = "总收益",sort = 5)
    @ApiModelProperty(value = "总收益")
    private BigDecimal totalAmount;

	@TableField(exist = false)
	private Date updateTime;
	@TableField(exist = false)
	private String updateBy;
	@TableField(exist = false)
	private Integer deleted;
	@TableField(exist = false)
	private String createBy;
	@TableField(exist = false)
	private String remark;

	@TableField(exist = false)
	@Excel(name = "钱包地址",sort = 1, width = 40)
	private String userAccount;

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("userId", getUserId())
            .append("statDate", getStatDate())
            .append("staticAmount", getStaticAmount())
            .append("dynamicAmount", getDynamicAmount())
            .append("teamAmount", getTeamAmount())
            .append("totalAmount", getTotalAmount())
            .append("createTime", getCreateTime())
        .toString();
    }
}
