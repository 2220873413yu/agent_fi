package com.xms.app.entity.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * Polymarket普通板块事件精简信息。
 *
 * <p>一个事件通常是官网上的一组主题，例如“Bitcoin above ...”；
 * 事件下面可能包含一个或多个具体市场，列表接口只保留前8个市场用于展示和选择。</p>
 */
@Data
@Builder
@ApiModel(value = "PolymarketEventDto", description = "Polymarket普通板块事件精简信息")
public class PolymarketEventDto {

	/**
	 * Polymarket事件ID。
	 */
	@ApiModelProperty(value = "Polymarket事件ID")
	private String id;

	/**
	 * 事件标题。
	 */
	@ApiModelProperty(value = "事件标题")
	private String title;

	/**
	 * Polymarket事件slug。
	 *
	 * <p>用于标识事件本身，不等同于具体下单市场slug。</p>
	 */
	@ApiModelProperty(value = "Polymarket事件slug")
	private String slug;

	/**
	 * 事件描述。
	 *
	 * <p>来自Polymarket上游，用于前端展示规则简介。</p>
	 */
	@ApiModelProperty(value = "事件描述")
	private String description;

	/**
	 * 事件图标地址。
	 */
	@ApiModelProperty(value = "事件图标URL")
	private String icon;

	/**
	 * 事件图片地址。
	 */
	@ApiModelProperty(value = "事件图片URL")
	private String image;

	/**
	 * 事件最近24小时成交量。
	 */
	@ApiModelProperty(value = "事件24小时成交量")
	private BigDecimal volume24hr;

	/**
	 * 事件流动性。
	 */
	@ApiModelProperty(value = "事件流动性")
	private BigDecimal liquidity;

	/**
	 * 事件是否仍处于活跃状态。
	 */
	@ApiModelProperty(value = "事件是否活跃")
	private Boolean active;

	/**
	 * 事件是否已关闭。
	 */
	@ApiModelProperty(value = "事件是否已关闭")
	private Boolean closed;

	/**
	 * 事件下的市场精简列表。
	 *
	 * <p>默认最多8个。真正报价和下单时，后端仍会按市场slug实时查询完整市场详情。</p>
	 */
	@ApiModelProperty(value = "事件下的市场精简列表，默认最多8个")
	private List<PolymarketEventMarketDto> markets;
}
