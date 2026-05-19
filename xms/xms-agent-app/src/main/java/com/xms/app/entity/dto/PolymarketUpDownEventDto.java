package com.xms.app.entity.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * Polymarket Up/Down单个5分钟窗口精简信息。
 *
 * <p>一个窗口代表某个币种在固定5分钟内的涨跌竞猜；下单时前端使用marketSlug和assetIds中的某个assetId提交订单。</p>
 */
@Data
@Builder
@ApiModel(value = "PolymarketUpDownEventDto", description = "Polymarket Up/Down单个5分钟窗口精简信息")
public class PolymarketUpDownEventDto {

	/** 币种，例如ETH。 */
	@ApiModelProperty(value = "币种", example = "ETH")
	private String coin;

	/** Up/Down窗口标题。 */
	@ApiModelProperty(value = "Up/Down窗口标题")
	private String title;

	/** Polymarket事件slug。 */
	@ApiModelProperty(value = "Polymarket事件slug")
	private String eventSlug;

	/** 下单使用的市场slug。 */
	@ApiModelProperty(value = "下单使用的市场slug")
	private String marketSlug;

	/** 窗口开始时间，ISO字符串。 */
	@ApiModelProperty(value = "窗口开始时间，ISO字符串")
	private String startTime;

	/** 窗口结束时间，ISO字符串。 */
	@ApiModelProperty(value = "窗口结束时间，ISO字符串")
	private String endTime;

	/** 窗口开始Unix秒。 */
	@ApiModelProperty(value = "窗口开始Unix秒", example = "1779179100")
	private Long windowStartUnix;

	/** 窗口结束Unix秒。 */
	@ApiModelProperty(value = "窗口结束Unix秒", example = "1779179400")
	private Long windowEndUnix;

	/** 起始比较价格，结束价格大于等于该价格则Up赢。 */
	@ApiModelProperty(value = "起始比较价格，结束价格大于等于该价格则Up赢")
	private BigDecimal priceToBeat;

	/** 可选结果名称，通常为Up和Down。 */
	@ApiModelProperty(value = "可选结果名称，通常为Up和Down")
	private List<String> outcomes;

	/** 每个结果当前价格，顺序与outcomes一致。 */
	@ApiModelProperty(value = "每个结果当前价格，顺序与outcomes一致")
	private List<String> outcomePrices;

	/** 每个结果对应的Polymarket asset_id/token_id，顺序与outcomes一致。 */
	@ApiModelProperty(value = "每个结果对应的Polymarket asset_id/token_id，顺序与outcomes一致")
	private List<String> assetIds;

	/** 当前最佳买价。 */
	@ApiModelProperty(value = "当前最佳买价")
	private BigDecimal bestBid;

	/** 当前最佳卖价。 */
	@ApiModelProperty(value = "当前最佳卖价")
	private BigDecimal bestAsk;

	/** 最近成交价。 */
	@ApiModelProperty(value = "最近成交价")
	private BigDecimal lastTradePrice;

	/** 市场24小时成交量。 */
	@ApiModelProperty(value = "市场24小时成交量")
	private BigDecimal volume24hr;

	/** 市场流动性。 */
	@ApiModelProperty(value = "市场流动性")
	private BigDecimal liquidity;

	/** 市场是否活跃。 */
	@ApiModelProperty(value = "市场是否活跃")
	private Boolean active;

	/** 市场是否已关闭。 */
	@ApiModelProperty(value = "市场是否已关闭")
	private Boolean closed;

	/** Polymarket是否仍接受订单。 */
	@ApiModelProperty(value = "Polymarket是否仍接受订单")
	private Boolean acceptingOrders;

	/** UMA开奖结果状态，resolved表示已开奖。 */
	@ApiModelProperty(value = "UMA开奖结果状态，resolved表示已开奖")
	private String umaResolutionStatus;

	/** Polymarket原始最小下单份额。 */
	@ApiModelProperty(value = "Polymarket原始最小下单份额")
	private BigDecimal orderMinSize;

	/** Polymarket conditionId。 */
	@ApiModelProperty(value = "Polymarket conditionId")
	private String conditionId;

	/** Polymarket questionID。 */
	@ApiModelProperty(value = "Polymarket questionID")
	private String questionId;
}
