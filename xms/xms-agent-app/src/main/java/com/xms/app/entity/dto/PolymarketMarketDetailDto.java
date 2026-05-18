package com.xms.app.entity.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * Polymarket市场详情精简返回对象。
 *
 * <p>用于前端刷新单个市场详情。该对象只保留展示、状态判断和结果价格需要的字段；
 * 正式报价和下单仍由后端内部实时查询Polymarket原始市场详情，不信任前端传回的价格或token。</p>
 */
@Data
@Builder
@ApiModel(value = "PolymarketMarketDetailDto", description = "Polymarket市场详情精简返回")
public class PolymarketMarketDetailDto {

	/**
	 * 本次请求Polymarket Gamma的源地址。
	 */
	@ApiModelProperty(value = "本次请求的Polymarket Gamma源地址，用于调试排查")
	private String sourceUrl;

	/**
	 * 本次接口返回数据的获取时间。
	 */
	@ApiModelProperty(value = "本次数据获取时间")
	private String fetchedAt;

	/**
	 * 查询入参里的市场slug。
	 */
	@ApiModelProperty(value = "查询入参里的市场slug")
	private String slug;

	/**
	 * 市场所属事件ID。
	 */
	@ApiModelProperty(value = "市场所属事件ID")
	private String eventId;

	/**
	 * 市场所属事件slug。
	 */
	@ApiModelProperty(value = "市场所属事件slug")
	private String eventSlug;

	/**
	 * 市场所属事件标题。
	 */
	@ApiModelProperty(value = "市场所属事件标题")
	private String eventTitle;

	/**
	 * 市场所属事件描述。
	 */
	@ApiModelProperty(value = "市场所属事件描述")
	private String eventDescription;

	/**
	 * 市场所属事件图标URL。
	 */
	@ApiModelProperty(value = "市场所属事件图标URL")
	private String eventIcon;

	/**
	 * 市场所属事件图片URL。
	 */
	@ApiModelProperty(value = "市场所属事件图片URL")
	private String eventImage;

	/**
	 * Polymarket市场ID。
	 */
	@ApiModelProperty(value = "Polymarket市场ID")
	private String marketId;

	/**
	 * 市场问题文案。
	 */
	@ApiModelProperty(value = "市场问题文案")
	private String question;

	/**
	 * Polymarket市场slug。
	 */
	@ApiModelProperty(value = "Polymarket市场slug")
	private String marketSlug;

	/**
	 * 市场规则描述。
	 */
	@ApiModelProperty(value = "市场规则描述")
	private String description;

	/**
	 * Polymarket conditionId。
	 */
	@ApiModelProperty(value = "Polymarket conditionId")
	private String conditionId;

	/**
	 * Polymarket questionID。
	 */
	@ApiModelProperty(value = "Polymarket questionID")
	private String questionId;

	/**
	 * 市场所有结果名称。
	 */
	@ApiModelProperty(value = "市场所有结果名称，例如Yes/No或多个比分选项")
	private List<String> outcomes;

	/**
	 * 每个结果对应的当前价格，顺序与outcomes一致。
	 */
	@ApiModelProperty(value = "每个结果对应的当前价格，顺序与outcomes一致")
	private List<String> outcomePrices;

	/**
	 * 每个结果对应的Polymarket asset_id/token_id，顺序与outcomes一致。
	 */
	@ApiModelProperty(value = "每个结果对应的Polymarket asset_id/token_id，顺序与outcomes一致")
	private List<String> assetIds;

	/**
	 * 当前最佳买价。
	 */
	@ApiModelProperty(value = "当前最佳买价")
	private BigDecimal bestBid;

	/**
	 * 当前最佳卖价。
	 */
	@ApiModelProperty(value = "当前最佳卖价")
	private BigDecimal bestAsk;

	/**
	 * 最近成交价。
	 */
	@ApiModelProperty(value = "最近成交价")
	private BigDecimal lastTradePrice;

	/**
	 * 买卖价差。
	 */
	@ApiModelProperty(value = "买卖价差")
	private BigDecimal spread;

	/**
	 * 市场最近24小时成交量。
	 */
	@ApiModelProperty(value = "24小时成交量")
	private BigDecimal volume24hr;

	/**
	 * 市场最近一周成交量。
	 */
	@ApiModelProperty(value = "一周成交量")
	private BigDecimal volume1wk;

	/**
	 * 市场最近一个月成交量。
	 */
	@ApiModelProperty(value = "一个月成交量")
	private BigDecimal volume1mo;

	/**
	 * 市场流动性。
	 */
	@ApiModelProperty(value = "流动性")
	private BigDecimal liquidity;

	/**
	 * 市场是否活跃。
	 */
	@ApiModelProperty(value = "市场是否活跃")
	private Boolean active;

	/**
	 * 市场是否已关闭。
	 */
	@ApiModelProperty(value = "市场是否已关闭")
	private Boolean closed;

	/**
	 * Polymarket是否仍接受订单。
	 */
	@ApiModelProperty(value = "Polymarket是否仍接受订单")
	private Boolean acceptingOrders;

	/**
	 * UMA开奖结果状态。
	 */
	@ApiModelProperty(value = "UMA开奖结果状态，例如resolved表示已开奖")
	private String umaResolutionStatus;

	/**
	 * 市场结束时间，ISO时间字符串。
	 */
	@ApiModelProperty(value = "市场结束时间，ISO时间字符串")
	private String endDate;

	/**
	 * Polymarket最小下单份额。
	 */
	@ApiModelProperty(value = "Polymarket最小下单份额")
	private BigDecimal orderMinSize;
}
