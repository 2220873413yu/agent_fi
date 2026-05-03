package com.xms.app.entity.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.xms.common.annotation.Excel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 商品列表
 */
@Data
public class DiyProductListDto {
	/** 主键id */
	private String id;
	/** 商品名称 */
	private String productName;
	/** 商品名称(英文) */
	private String productNameEn;
	/** 封面图 */
	private String coverImage;
	/** 封面图(英文) */
	private String coverImageEn;
	/** 规格类型 0单规格 1多规格 */
	private Integer specType;
	/** 轮播图(JSON数组) */
	private String sliderImage;
	/** 轮播图(JSON数组) */
	private String sliderImageEn;
	/** 详情图(JSON数组) */
	private String detailImage;
	/** 详情图英文 */
	private String detailImageEn;
	/** 价格(最低xx起) */
	private BigDecimal price;
	/** 总销量 */
	private String sales;
}
