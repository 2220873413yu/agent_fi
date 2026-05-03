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
 * 用户等级考核配置对象 t_user_level_config
 *
 * @author xms
 * @date 2026-02-26
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "t_user_level_config")
public class UserLevelConfig extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 主键id */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 等级编码 0:暂无,1:有效,2:区代理,3:县代理,4:省代理 */
    @Excel(name = "等级编码 0:暂无,1:有效,2:区代理,3:县代理,4:省代理")
    @ApiModelProperty(value = "等级编码 0:暂无,1:有效,2:区代理,3:县代理,4:省代理")
    private Integer level;
    /** 大区业绩(质押业绩) */
    @Excel(name = "大区业绩(质押业绩)")
    @ApiModelProperty(value = "大区业绩(质押业绩)")
    private BigDecimal teamPerformance;
	/**
	 * 个人业绩
	 */
	@Excel(name = "个人业绩")
    private BigDecimal performance;
    /** 小区业绩 */
    @Excel(name = "小区业绩")
    @ApiModelProperty(value = "小区业绩")
    private BigDecimal communityPerformance;
    /** 需要满足的线数量(比如2条线) */
    @Excel(name = "需要满足的线数量(比如2条线)")
    @ApiModelProperty(value = "需要满足的线数量(比如2条线)")
    private Integer requiredLegNum;
    /** 线内代理最小等级(level) */
    @Excel(name = "线内代理最小等级(level)")
    @ApiModelProperty(value = "线内代理最小等级(level)")
    private Integer legLevelMin;

	/** 每条线里需要几个该等级及以上代理 */
	@Excel(name = "每条线里需要几个该等级及以上代理")
    private Integer legLevelCount;


    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("level", getLevel())
            .append("teamPerformance", getTeamPerformance())
            .append("communityPerformance", getCommunityPerformance())
            .append("requiredLegNum", getRequiredLegNum())
            .append("legLevelMin", getLegLevelMin())
            .append("createTime", getCreateTime())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .append("deleted", getDeleted())
            .append("createBy", getCreateBy())
            .append("updateBy", getUpdateBy())
        .toString();
    }
}
