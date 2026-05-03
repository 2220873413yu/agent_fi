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
import com.baomidou.mybatisplus.annotation.TableField;

/**
 * 商品属性对象 xms_diy_store_product_attr
 *
 * @author xms
 * @date 2026-04-08
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "xms_diy_store_product_attr")
public class DiyStoreProductAttr extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** $column.columnComment */
    @TableId(type = IdType.AUTO)
    private String id;
    /** 商品ID */
    @Excel(name = "商品ID")
    @ApiModelProperty(value = "商品ID")
    private String productId;
    /** 属性名 */
    @Excel(name = "属性名")
    @ApiModelProperty(value = "属性名")
    private String attrName;
    /** 属性值(逗号分隔) */
    @Excel(name = "属性值(逗号分隔)")
    @ApiModelProperty(value = "属性值(逗号分隔)")
    private String attrValues;


    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("productId", getProductId())
            .append("attrName", getAttrName())
            .append("attrValues", getAttrValues())
            .append("remark", getRemark())
            .append("createTime", getCreateTime())
            .append("createBy", getCreateBy())
            .append("updateTime", getUpdateTime())
            .append("updateBy", getUpdateBy())
            .append("deleted", getDeleted())
        .toString();
    }
}
