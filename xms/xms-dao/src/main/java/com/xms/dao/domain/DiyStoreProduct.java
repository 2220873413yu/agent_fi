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
 * 商品对象 xms_diy_store_product
 *
 * @author xms
 * @date 2026-04-08
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "xms_diy_store_product")
public class DiyStoreProduct extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 主键id */
    @TableId(type = IdType.AUTO)
    private String id;
    /** 商品名称 */
    @Excel(name = "商品名称")
    @ApiModelProperty(value = "商品名称")
    private String productName;

	/** 商品名称(英文) */
	@Excel(name = "商品名称(英文)")
	@ApiModelProperty(value = "商品名称(英文)")
    private String productNameEn;

	/** 商品编码 */
    @Excel(name = "商品编码")
    @ApiModelProperty(value = "商品编码")
    private String productCode;

    /** 封面图 */
    @Excel(name = "封面图")
    @ApiModelProperty(value = "封面图")
    private String coverImage;

	/** 封面图(英文) */
	@Excel(name = "封面图(英文)")
	@ApiModelProperty(value = "封面图(英文)")
    private String coverImageEn;

	/** 规格类型 0单规格 1多规格 */
	@Excel(name = "规格类型",dictType = "xms_diy_store_product_spec_type")
	private Integer specType;
    /** 轮播图(JSON数组) */
    @Excel(name = "轮播图(JSON数组)")
    @ApiModelProperty(value = "轮播图(JSON数组)")
    private String sliderImage;

	/** 轮播图(JSON数组) */
	@Excel(name = "轮播图(JSON数组,英文)")
    private String sliderImageEn;

    /** 详情图(JSON数组) */
    @Excel(name = "详情图(JSON数组)")
    @ApiModelProperty(value = "详情图(JSON数组)")
    private String detailImage;

	/** 详情图 */
	@Excel(name = "详情图")
    private String detailImageEn;

    /** 价格 */
    @Excel(name = "价格")
    @ApiModelProperty(value = "价格")
    private BigDecimal price;
    /** 总销量 */
    @Excel(name = "总销量")
    @ApiModelProperty(value = "总销量")
    private String sales;
    /** 总库存冗余 -1不限 */
    @Excel(name = "总库存冗余 -1不限")
    @ApiModelProperty(value = "总库存冗余 -1不限")
    private Integer stock;
    /** 是否上架 0否1是 */
    @Excel(name = "是否上架 0否1是")
    @ApiModelProperty(value = "是否上架 0否1是")
    private Integer isEnabled;
    /** 排序 */
    @Excel(name = "排序")
    @ApiModelProperty(value = "排序")
    private Long sort;


    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("productName", getProductName())
            .append("productCode", getProductCode())
            .append("coverImage", getCoverImage())
            .append("sliderImage", getSliderImage())
            .append("detailImage", getDetailImage())
            .append("price", getPrice())
            .append("sales", getSales())
            .append("stock", getStock())
            .append("isEnabled", getIsEnabled())
            .append("sort", getSort())
            .append("remark", getRemark())
            .append("createTime", getCreateTime())
            .append("createBy", getCreateBy())
            .append("updateTime", getUpdateTime())
            .append("updateBy", getUpdateBy())
            .append("deleted", getDeleted())
        .toString();
    }
}
