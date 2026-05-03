package com.xms.app.entity.req;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 领取基金收益(后台拨付的基金订单才能领取)请求对象
 * @author xms
 * @date 2023/10/07
 */
@Data
public class ClaimFundProfitRequestReq {

	/**
	 * 基金订单id
	 */
	@NotNull
	private Long id;
}
