package com.xms.app.entity.req;

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
public class PolymarketOrderReq {

	@NotBlank(message = "marketSlug cannot be blank")
	@ApiModelProperty(value = "Polymarket市场slug", required = true)
	private String marketSlug;

	@NotNull(message = "outcomeIndex cannot be null")
	@Min(value = 0, message = "outcomeIndex must be non-negative")
	@ApiModelProperty(value = "选择结果下标", required = true)
	private Integer outcomeIndex;

	@NotNull(message = "afiAmount cannot be null")
	@DecimalMin(value = "0.000001", message = "afiAmount must be greater than 0")
	@ApiModelProperty(value = "下单AFI数量", required = true)
	private BigDecimal afiAmount;
}
