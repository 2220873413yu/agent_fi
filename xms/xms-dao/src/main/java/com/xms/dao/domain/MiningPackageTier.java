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
 * 矿机质押区间配置对象 t_mining_package_tier
 *
 * @author xms
 * @date 2026-02-23
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "t_mining_package_tier")
public class MiningPackageTier extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 主键id */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 区间起始(含) */
    @Excel(name = "区间起始(含)")
    @ApiModelProperty(value = "区间起始(含)")
    private Integer startIndex;
    /** 区间结束(含) */
    @Excel(name = "区间结束(含)")
    @ApiModelProperty(value = "区间结束(含)")
    private Integer endIndex;
    /** 质押金额(DFC) */
    @Excel(name = "质押金额(DFC)")
    @ApiModelProperty(value = "质押金额(DFC)")
    private BigDecimal stakeAmount;
    /** 日产出(DFC) */
    @Excel(name = "日产出(DFC)")
    @ApiModelProperty(value = "日产出(DFC)")
    private BigDecimal dayReward;

	/** 创建者 */
	@TableField(exist = false)
	private String createBy;
	/** 更新者 */
	@TableField(exist = false)
	private String updateBy;

	@TableField(exist = false)
	private Integer deleted;

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("startIndex", getStartIndex())
            .append("endIndex", getEndIndex())
            .append("stakeAmount", getStakeAmount())
            .append("dayReward", getDayReward())
            .append("createTime", getCreateTime())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
        .toString();
    }
}
