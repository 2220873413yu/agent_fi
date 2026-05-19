package com.xms.app.entity.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Polymarket Up/Down短周期事件列表精简返回对象。
 *
 * <p>该对象只返回前端展示和选择下单结果需要的字段，不透传Polymarket原始tags、series、feeSchedule等大字段。</p>
 */
@Data
@Builder
@ApiModel(value = "PolymarketUpDownListDto", description = "Polymarket Up/Down短周期事件列表精简返回")
public class PolymarketUpDownListDto {

	/** 板块名称，固定为crypto-updown。 */
	@ApiModelProperty(value = "板块名称，固定为crypto-updown", example = "crypto-updown")
	private String section;

	/** 本次查询的币种列表。 */
	@ApiModelProperty(value = "本次查询的币种列表，例如ETH、BTC")
	private List<String> coins;

	/** 当前5分钟窗口之前查询的窗口数量。 */
	@ApiModelProperty(value = "当前5分钟窗口之前查询的窗口数量", example = "2")
	private Integer before;

	/** 当前5分钟窗口之后查询的窗口数量。 */
	@ApiModelProperty(value = "当前5分钟窗口之后查询的窗口数量", example = "6")
	private Integer after;

	/** 本次实际返回的Up/Down窗口数量。 */
	@ApiModelProperty(value = "本次实际返回的Up/Down窗口数量", example = "9")
	private Integer count;

	/** 服务端拉取数据时间。 */
	@ApiModelProperty(value = "服务端拉取数据时间")
	private String fetchedAt;

	/** Up/Down短周期窗口列表。 */
	@ApiModelProperty(value = "Up/Down短周期窗口列表")
	private List<PolymarketUpDownEventDto> events;
}
