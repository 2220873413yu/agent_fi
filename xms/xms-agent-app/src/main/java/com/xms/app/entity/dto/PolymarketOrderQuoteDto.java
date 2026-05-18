package com.xms.app.entity.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Polymarket内部订单报价预览结果。
 *
 * <p>该对象只用于展示报价，不代表最终成交；正式下单时后端会重新拉取AFI价格和Polymarket结果价格。</p>
 */
@Data
@Builder
@ApiModel(value = "PolymarketOrderQuoteDto", description = "Polymarket内部订单报价预览结果")
public class PolymarketOrderQuoteDto {

	@ApiModelProperty(value = "Polymarket市场slug", example = "will-bitcoin-hit-150k-by-december-31")
	private String marketSlug;

	@ApiModelProperty(value = "Polymarket市场问题")
	private String marketQuestion;

	@ApiModelProperty(value = "选择结果下标，对应Polymarket outcomes数组", example = "0")
	private Integer outcomeIndex;

	@ApiModelProperty(value = "选择结果名称，例如Yes、No或具体比分/候选项")
	private String outcomeName;

	@ApiModelProperty(value = "用户选择结果对应的Polymarket asset_id/token_id")
	private String assetId;

	@ApiModelProperty(value = "按购买份额和实时价格折算出的预计扣减AFI数量，单位AFI")
	private BigDecimal afiAmount;

	@ApiModelProperty(value = "AFI价格快照，单位USDT")
	private BigDecimal afiPrice;

	@ApiModelProperty(value = "预计扣减AFI按当前价格折算后的USDT金额")
	private BigDecimal afiUsdtAmount;

	@ApiModelProperty(value = "Polymarket outcome价格，范围应为0到1")
	private BigDecimal outcomePrice;

	@ApiModelProperty(value = "用户输入的购买份额，猜中后最大兑付USDT数量等于该份额")
	private BigDecimal shareAmount;

	@ApiModelProperty(value = "最大兑付USDT，等于购买份额")
	private BigDecimal maxPayoutUsdt;

	@ApiModelProperty(value = "市场结束时间")
	private Date endTime;
}
