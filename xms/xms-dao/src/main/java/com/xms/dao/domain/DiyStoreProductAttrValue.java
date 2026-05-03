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
 * 商品属性值(SKU)对象 xms_diy_store_product_attr_value
 *
 * @author xms
 * @date 2026-04-08
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "xms_diy_store_product_attr_value")
public class DiyStoreProductAttrValue extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** $column.columnComment */
    @TableId(type = IdType.AUTO)
    private String id;
    /** 商品ID */
    @Excel(name = "商品ID")
    @ApiModelProperty(value = "商品ID")
    private String productId;
    /** 商品属性索引值(attr_value|attr_value[|...]) */
    @Excel(name = "商品属性索引值(attr_value|attr_value[|...])")
    @ApiModelProperty(value = "商品属性索引值(attr_value|attr_value[|...])")
    private String sku;
    /** 属性对应库存 */
    @Excel(name = "属性对应库存")
    @ApiModelProperty(value = "属性对应库存")
    private String stock;
    /** 销量 */
    @Excel(name = "销量")
    @ApiModelProperty(value = "销量")
    private String sales;
    /** 销售价 */
    @Excel(name = "销售价")
    @ApiModelProperty(value = "销售价")
    private BigDecimal price;
    /** 图片 */
    @Excel(name = "图片")
    private String image;
	/** 图片(英文) */
	@Excel(name = "图片(英文)")
    private String imageEn;
    /** 唯一值(可放规则签名) */
    @Excel(name = "唯一值(可放规则签名)")
    @ApiModelProperty(value = "唯一值(可放规则签名)")
    private String codeUnique;
    /** 成本价 */
    @Excel(name = "成本价")
    @ApiModelProperty(value = "成本价")
    private BigDecimal cost;
    /** 商品条码 */
    @Excel(name = "商品条码")
    @ApiModelProperty(value = "商品条码")
    private String barCode;
    /** 重量(可选) */
    @Excel(name = "重量(可选)")
    @ApiModelProperty(value = "重量(可选)")
    private BigDecimal weight;
    /** 体积(可选) */
    @Excel(name = "体积(可选)")
    @ApiModelProperty(value = "体积(可选)")
    private BigDecimal volume;
    /** 扩展字段1(保留) */
    @Excel(name = "扩展字段1(保留)")
    @ApiModelProperty(value = "扩展字段1(保留)")
    private BigDecimal brokerage;
    /** 扩展字段2(保留) */
    @Excel(name = "扩展字段2(保留)")
    @ApiModelProperty(value = "扩展字段2(保留)")
    private BigDecimal brokerageTwo;
    /** 拼团价(保留) */
    @Excel(name = "拼团价(保留)")
    @ApiModelProperty(value = "拼团价(保留)")
    private BigDecimal pinkPrice;
    /** 拼团库存(保留) */
    @Excel(name = "拼团库存(保留)")
    @ApiModelProperty(value = "拼团库存(保留)")
    private Long pinkStock;
    /** 秒杀价(保留) */
    @Excel(name = "秒杀价(保留)")
    @ApiModelProperty(value = "秒杀价(保留)")
    private BigDecimal seckillPrice;
    /** 秒杀库存(保留) */
    @Excel(name = "秒杀库存(保留)")
    @ApiModelProperty(value = "秒杀库存(保留)")
    private Long seckillStock;
    /** 积分(保留) */
    @Excel(name = "积分(保留)")
    @ApiModelProperty(value = "积分(保留)")
    private Integer integral;

	@TableField(exist = false)
	private String createBy;

	@TableField(exist = false)
	private String updateBy;

	@TableField(exist = false)
	private String remark;

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("productId", getProductId())
            .append("sku", getSku())
            .append("stock", getStock())
            .append("sales", getSales())
            .append("price", getPrice())
            .append("image", getImage())
            .append("codeUnique", getCodeUnique())
            .append("cost", getCost())
            .append("barCode", getBarCode())
            .append("weight", getWeight())
            .append("volume", getVolume())
            .append("brokerage", getBrokerage())
            .append("brokerageTwo", getBrokerageTwo())
            .append("pinkPrice", getPinkPrice())
            .append("pinkStock", getPinkStock())
            .append("seckillPrice", getSeckillPrice())
            .append("seckillStock", getSeckillStock())
            .append("integral", getIntegral())
            .append("createTime", getCreateTime())
            .append("updateTime", getUpdateTime())
            .append("deleted", getDeleted())
        .toString();
    }
}
