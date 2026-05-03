package com.xms.dao.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
 * 质押套餐对象 t_stake_product
 *
 * @author xms
 * @date 2026-03-09
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "t_stake_product")
public class StakeProduct extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 主键id */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 质押名称 */
    @Excel(name = "质押名称")
    @ApiModelProperty(value = "质押名称")
    private String name;
    /** 销量(废弃) */
    @Excel(name = "销量")
    @ApiModelProperty(value = "销量")
    private Integer sales;
    /** 最低质押 */
    @Excel(name = "最低质押")
    @ApiModelProperty(value = "最低质押")
    private BigDecimal stakeUnitAmountMin;
    /** 最高质押 */
    @Excel(name = "最高质押")
    @ApiModelProperty(value = "最高质押")
    private BigDecimal maxStakeAmount;
    /** 静态日利率 例如: 1就是1% */
    @Excel(name = "静态日利率 例如: 1就是1%")
    @ApiModelProperty(value = "静态日利率 例如: 1就是1%")
    private BigDecimal staticRatio;
    /** 出局倍数 */
    @Excel(name = "出局倍数")
    @ApiModelProperty(value = "出局倍数")
    private BigDecimal exitMultiplier;
    /** 是否上架 0:否,1:是 */
    @Excel(name = "是否上架 0:否,1:是")
    @ApiModelProperty(value = "是否上架 0:否,1:是")
    private Integer isEnabled;

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
            .append("name", getName())
            .append("sales", getSales())
            .append("stakeUnitAmountMin", getStakeUnitAmountMin())
            .append("maxStakeAmount", getMaxStakeAmount())
            .append("staticRatio", getStaticRatio())
            .append("exitMultiplier", getExitMultiplier())
            .append("isEnabled", getIsEnabled())
            .append("createTime", getCreateTime())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
        .toString();
    }
}
