package com.xms.app.entity.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
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

	@NotBlank(message = "assetId cannot be blank")
	@ApiModelProperty(value = "用户选择结果对应的Polymarket asset_id/token_id，来自事件列表或市场详情的assetIds", required = true, example = "93694900555669388759405753550770573998169287228984912881955464376232163096213")
	private String assetId;

	@NotNull(message = "shareAmount cannot be null")
	@DecimalMin(value = "0.000001", message = "shareAmount must be greater than 0")
	@ApiModelProperty(value = "购买的Yes/No结果份额数量，猜中后最大兑付USDT数量等于该份额", required = true, example = "100")
	private BigDecimal shareAmount;

	@NotNull(message = "bizType cannot be null")
	@Min(value = 1, message = "bizType must be 1, 2 or 3")
	@Max(value = 3, message = "bizType must be 1, 2 or 3")
	@ApiModelProperty(value = "业务类型：1加密，2体育，3Up/Down", required = true, example = "1")
	private Integer bizType;
}
