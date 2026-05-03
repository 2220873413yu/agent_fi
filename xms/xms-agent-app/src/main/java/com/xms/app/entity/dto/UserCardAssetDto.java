package com.xms.app.entity.dto;

import com.xms.common.annotation.Excel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 卡片持有信息对象 t_user_card_asset
 *
 * @author xms
 * @date 2025-12-05
 */
@Data
public class UserCardAssetDto {

	private Integer num;
	/** 价格/U */
	private BigDecimal price;
	/** 算力 */
	private BigDecimal computingPower;
	/** 卡片类型 1:普通卡,2:白银卡,3白金卡,4:黑金卡 */
	private Integer cardType;

	/**
	 * 持有算力
	 */
	private BigDecimal holdingPower;
}
