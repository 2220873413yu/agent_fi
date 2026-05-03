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
import com.xms.common.annotation.Excel;

/**
 * 商品规则值对象 xms_diy_store_product_rule
 *
 * @author xms
 * @date 2026-04-08
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "xms_diy_store_product_rule")
public class DiyStoreProductRule extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** $column.columnComment */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 规格模板名称 */
    @Excel(name = "规格模板名称")
    private String ruleName;
    /** 规格模板名称(英文) */
    @Excel(name = "规格模板名称(英文)")
    private String ruleNameEn;
    /** 规格值JSON */
    @Excel(name = "规格值JSON")
    @ApiModelProperty(value = "规格值JSON")
    private String ruleValue;
    /** 规格值JSON(英文) */
    @Excel(name = "规格值JSON(英文)")
    @ApiModelProperty(value = "规格值JSON(英文)")
    private String ruleValueEn;


    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("ruleName", getRuleName())
            .append("ruleNameEn", getRuleNameEn())
            .append("ruleValue", getRuleValue())
            .append("ruleValueEn", getRuleValueEn())
            .append("remark", getRemark())
            .append("createTime", getCreateTime())
            .append("createBy", getCreateBy())
            .append("updateTime", getUpdateTime())
            .append("updateBy", getUpdateBy())
            .append("deleted", getDeleted())
        .toString();
    }
}
