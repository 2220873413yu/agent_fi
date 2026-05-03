package com.xms.dao.entity.req;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class BizProductDetailReq {
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
	private BigDecimal price;
	/** 规格类型 0单规格 1多规格 */
	private Integer specType;
	/** 选择的规格模板ID */
	private List<Long> specAttrIds;
	private String sales;
	private Integer stock;
	private Integer isEnabled;
	private Long sort;
	private String remark;
	/** 单规格价格（前端临时字段） */
	private BigDecimal singleSpecPrice;
	/** 单规格图片（前端临时字段） */
	private String singleSpecImage;
	/** 单规格图片（英文） */
	private String singleSpecImageEn;
	/** SKU列表 */
	private List<AttrValueItem> attrValueList;

	@Data
	public static class AttrValueItem {
		private String id;
		private String sku;
		private String stock;
		private String sales;
		private BigDecimal price;
		private String image;
		private String imageEn;
		private String codeUnique;
	}
}
