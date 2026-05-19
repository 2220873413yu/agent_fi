package com.xms.app.entity.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * Polymarket事件列表里的市场精简信息。
 *
 * <p>市场是用户真正选择Yes/No或其他结果并创建内部订单的对象。
 * 这里不返回完整上游字段，只保留列表展示、状态判断和选择结果所需字段。</p>
 */
@Data
@Builder
@ApiModel(value = "PolymarketEventMarketDto", description = "Polymarket事件列表市场精简信息")
public class PolymarketEventMarketDto {

	/**
	 * Polymarket市场ID。
	 */
	@ApiModelProperty(value = "Polymarket市场ID")
	private String id;

	/**
	 * 市场问题文案。
	 *
	 * <p>例如“Will Bitcoin hit $150k by June 30, 2026?”。</p>
	 */
	@ApiModelProperty(value = "市场问题文案")
	private String question;

	/**
	 * Polymarket市场slug。
	 *
	 * <p>这是内部报价、下单、查询市场详情和订单聚合使用的核心市场标识。</p>
	 */
	@ApiModelProperty(value = "Polymarket市场slug，下单和查询详情使用该字段")
	private String slug;

	/**
	 * 市场所有结果名称。
	 *
	 * <p>常见为Yes/No，也可能是比分、区间等多结果选项。</p>
	 */
	@ApiModelProperty(value = "市场所有结果名称，例如Yes/No或多个比分选项")
	private List<String> outcomes;

	/**
	 * 每个结果对应的当前价格。
	 *
	 * <p>顺序与outcomes一致，价格通常在0到1之间。正式下单时后端会重新读取实时价格。</p>
	 */
	@ApiModelProperty(value = "每个结果对应的当前价格，顺序与outcomes一致，范围通常为0到1")
	private List<String> outcomePrices;

	/**
	 * 每个结果对应的Polymarket asset_id/token_id。
	 *
	 * <p>顺序与outcomes、outcomePrices一致。前端下单时传该字段里的assetId，
	 * 后端会实时查询市场详情并用assetId反查当前结果下标，避免只靠数组下标导致选错结果。</p>
	 */
	@ApiModelProperty(value = "每个结果对应的Polymarket asset_id/token_id，顺序与outcomes和outcomePrices一致")
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
	 * 市场最近24小时成交量。
	 */
	@ApiModelProperty(value = "24小时成交量")
	private BigDecimal volume24hr;

	/**
	 * 市场流动性。
	 */
	@ApiModelProperty(value = "流动性")
	private BigDecimal liquidity;

	/**
	 * 市场结束时间。
	 *
	 * <p>ISO时间字符串，前端可用于倒计时和禁用临近结束的下单按钮。</p>
	 */
	@ApiModelProperty(value = "市场结束时间，ISO时间字符串")
	private String endDate;
	private String groupItemTitle;

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
	 *
	 * <p>resolved表示市场已经有明确开奖结果。</p>
	 */
	@ApiModelProperty(value = "UMA开奖结果状态，例如resolved表示已开奖")
	private String umaResolutionStatus;

	/**
	 * Polymarket conditionId。
	 *
	 * <p>后续链上或CLOB深度对接时使用，当前列表只作为调试和识别字段返回。</p>
	 */
	@ApiModelProperty(value = "Polymarket conditionId，后续链上或CLOB深度对接时使用")
	private String conditionId;

	/**
	 * Polymarket最小下单份额。
	 *
	 * <p>这是上游市场字段；平台内部最低下单限制仍以后端订单配置接口为准。</p>
	 */
	@ApiModelProperty(value = "Polymarket最小下单份额")
	private BigDecimal orderMinSize;
}
