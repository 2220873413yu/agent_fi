package com.xms.app.entity.req;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 创建卡片订单请求参数
 *
 * @author xms
 * @date 2023/06/12
 */
@Data
public class CreateCardOrderReq {

	/**
	 * 购买的卡片数量
	 */
	@NotNull(message = "num error")
	@Min(1)
	private Integer num;

	/**
	 * 卡片id
	 */
	@NotNull(message = "buy error")
	private Long id;
}
