package com.xms.app.entity.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Polymarket内部订单报价和下单请求。
 */
@Data
@ApiModel(value = "PolymarketOrderReq", description = "Polymarket内部订单报价和下单请求")
public class PolymarketOrderReq {

	@NotBlank(message = "marketSlug cannot be blank")
	@ApiModelProperty(value = "Polymarket市场slug，用于定位要购买的市场", required = true, example = "will-bitcoin-hit-150k-by-december-31")
	private String marketSlug;

	@NotNull(message = "outcomeIndex cannot be null")
	@Min(value = 0, message = "outcomeIndex must be non-negative")
	@ApiModelProperty(value = "选择结果下标，对应Polymarket outcomes数组下标，0通常表示第一个结果", required = true, example = "0")
	private Integer outcomeIndex;

	@NotNull(message = "shareAmount cannot be null")
	@DecimalMin(value = "0.000001", message = "shareAmount must be greater than 0")
	@ApiModelProperty(value = "购买的Yes/No结果份额数量，猜中后最大兑付USDT数量等于该份额", required = true, example = "100")
	private BigDecimal shareAmount;
}
