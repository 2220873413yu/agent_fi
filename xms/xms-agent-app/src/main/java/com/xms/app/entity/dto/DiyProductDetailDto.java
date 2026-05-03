package com.xms.app.entity.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 获取商品详情
 */
@Data
public class DiyProductDetailDto {
	private String id;
	private String productName;
	private String productNameEn;
	private String productCode;
	private String coverImage;
	private String coverImageEn;
	private String sliderImage;
	private String sliderImageEn;
	private String detailImage;
	private String detailImageEn;
	private Integer specType;
	private BigDecimal price;
	private String sales;

	/**
	 * 规格维度列表（如：颜色、尺寸）
	 */
	private List<SpecItem> specList = new ArrayList<>();

	/**
	 * SKU 组合列表（如：黑色|128G）
	 */
	private List<SkuItem> skuList = new ArrayList<>();

	/**
	 * 默认选中的 codeUnique（前端首屏可直接渲染价格/图片）
	 */
	private String defaultCodeUnique;

	@Data
	public static class SpecItem {
		private String nameZh;
		private String nameEn;
		private List<String> valuesZh = new ArrayList<>();
		private List<String> valuesEn = new ArrayList<>();
	}

	@Data
	public static class SkuItem {
		private String codeUnique;
		private String sku;
		private BigDecimal price;
		private String image;
		private String imageEn;
		private String stock;
		private String sales;
		private List<String> valuesZh = new ArrayList<>();
		private List<String> valuesEn = new ArrayList<>();
	}
}
