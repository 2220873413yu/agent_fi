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
    /** 等级编码 0:暂无,1:F1,2:F2,3:F3,4:F4,5:F5,6:F6,7:F7,8:F8,9:F9 */
    @Excel(name = "等级编码", dictType = "t_user_info_game_level")
    @ApiModelProperty(value = "等级编码 0:暂无,1:F1,2:F2,3:F3,4:F4,5:F5,6:F6,7:F7,8:F8,9:F9")
    private Integer level;
    /** 大区业绩(历史字段，本次托管等级考核暂不使用) */
    @Excel(name = "大区业绩(历史字段)")
    @ApiModelProperty(value = "大区业绩(历史字段，本次托管等级考核暂不使用)")
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
	/** 团队奖励比例，单位% */
	@Excel(name = "团队奖励比例")
	@ApiModelProperty(value = "团队奖励比例，单位%")
	private BigDecimal teamRewardRatio;
	/** 全球手续费分红比例，单位% */
	@Excel(name = "全球手续费分红比例")
	@ApiModelProperty(value = "全球手续费分红比例，单位%")
	private BigDecimal globalFeeDividendRatio;
    /** 需要满足的线数量(历史字段，本次托管等级考核暂不使用) */
    @Excel(name = "需要满足的线数量(历史字段)")
    @ApiModelProperty(value = "需要满足的线数量(历史字段，本次托管等级考核暂不使用)")
    private Integer requiredLegNum;
    /** 线内代理最小等级(历史字段，本次托管等级考核暂不使用) */
    @Excel(name = "线内代理最小等级(历史字段)")
    @ApiModelProperty(value = "线内代理最小等级(历史字段，本次托管等级考核暂不使用)")
    private Integer legLevelMin;

	/** 每条线里需要几个该等级及以上代理(历史字段，本次托管等级考核暂不使用) */
	@Excel(name = "线内等级人数(历史字段)")
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
