package com.xms.app.entity.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Polymarket内部下单页面配置返回对象。
 *
 * <p>该对象只用于App调研页面或前端下单页读取展示配置，不参与下单金额计算；正式下单仍以后端实时校验为准。</p>
 */
@Data
@Builder
@ApiModel(value = "PolymarketOrderConfigDto", description = "Polymarket内部下单配置")
public class PolymarketOrderConfigDto {

	@ApiModelProperty(value = "最低购买份额/token数量，单位为所选结果份额", example = "10")
	private BigDecimal minShareAmount;

	@ApiModelProperty(value = "市场结束前禁止下单秒数，单位秒", example = "5")
	private Integer minSecondsBeforeEnd;

	@ApiModelProperty(value = "App接口Authorization请求头前缀", example = "App ")
	private String authorizationPrefix;
}
