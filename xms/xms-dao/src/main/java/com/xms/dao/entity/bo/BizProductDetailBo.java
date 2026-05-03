package com.xms.dao.entity.bo;

import com.xms.dao.entity.req.BizProductDetailReq;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class BizProductDetailBo {
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
	private Integer specType;
	private List<Long> specAttrIds;
	private String sales;
	private Integer stock;
	private Integer isEnabled;
	private Long sort;
	private String remark;
	private BigDecimal singleSpecPrice;
	private String singleSpecImage;
	private String singleSpecImageEn;
	private List<BizProductDetailReq.AttrValueItem> attrValueList;
}
