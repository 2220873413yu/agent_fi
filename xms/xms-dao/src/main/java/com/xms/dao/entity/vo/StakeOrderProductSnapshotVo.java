package com.xms.dao.entity.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 质押订单商品快照（下单时固化，避免后续商品变更影响历史订单展示）
 */
@Data
public class StakeOrderProductSnapshotVo {
	/** 商品ID */
	private String productId;
	/** 商品编码 */
	private String productCode;
	/** 规格类型：0单规格 1多规格 */
	private Integer specType;
	/** 成交价（下单时选中SKU价格） */
	private BigDecimal dealPrice;
	/** 币种（如：USDT） */
	private String currency;

	/** 商品名称（中文） */
	private String productNameZh;
	/** 商品名称（英文） */
	private String productNameEn;
	/** 商品封面图（中文） */
	private String productCoverImageZh;
	/** 商品封面图（英文） */
	private String productCoverImageEn;
	/** 商品轮播图（中文，通常为JSON/逗号分隔字符串） */
	private String productSliderImagesZh;
	/** 商品轮播图（英文，通常为JSON/逗号分隔字符串） */
	private String productSliderImagesEn;
	/** 商品详情图（中文，通常为JSON/逗号分隔字符串） */
	private String productDetailImagesZh;
	/** 商品详情图（英文，通常为JSON/逗号分隔字符串） */
	private String productDetailImagesEn;

	/** SKU ID */
	private String skuId;
	/** SKU唯一编码 */
	private String skuCodeUnique;
	/** SKU规格描述（中文） */
	private String skuTextZh;
	/** SKU规格描述（英文） */
	private String skuTextEn;
	/** SKU图片（中文） */
	private String skuImageZh;
	/** SKU图片（英文） */
	private String skuImageEn;
}

