package com.xms.app.entity.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.xms.common.annotation.Excel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CardPackageDto {


	/** 主键id */
	@TableId(type = IdType.AUTO)
	private Long id;
	/** 价格/U */
	@Excel(name = "价格/U")
	@ApiModelProperty(value = "价格/U")
	private BigDecimal price;
	/** 卡片图片 */
	@Excel(name = "卡片图片")
	@ApiModelProperty(value = "卡片图片")
	private String image;
	/** 算力 */
	private BigDecimal computingPower;

	/**
	 * 卡片级别 1:普通卡,2:白银卡,3:白金卡,4:黑金卡
	 */
	private Integer cardType;

	/**
	 * 赠送valid_num3积分比例 例如5就是5%
	 */
	private BigDecimal validNum3GiftRatio;
}
