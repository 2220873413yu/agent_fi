package com.xms.app.entity.dto;

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
public class PolymarketOrderQuoteDto {

	@ApiModelProperty(value = "市场slug")
	private String marketSlug;

	@ApiModelProperty(value = "市场问题")
	private String marketQuestion;

	@ApiModelProperty(value = "选择结果下标")
	private Integer outcomeIndex;

	@ApiModelProperty(value = "选择结果")
	private String outcomeName;

	@ApiModelProperty(value = "下单AFI数量")
	private BigDecimal afiAmount;

	@ApiModelProperty(value = "AFI价格，单位USDT")
	private BigDecimal afiPrice;

	@ApiModelProperty(value = "AFI等值USDT")
	private BigDecimal afiUsdtAmount;

	@ApiModelProperty(value = "Polymarket outcome价格")
	private BigDecimal outcomePrice;

	@ApiModelProperty(value = "预计购买份额")
	private BigDecimal shareAmount;

	@ApiModelProperty(value = "最大兑付USDT")
	private BigDecimal maxPayoutUsdt;

	@ApiModelProperty(value = "市场结束时间")
	private Date endTime;
}
