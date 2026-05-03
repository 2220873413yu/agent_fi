package com.xms.app.entity.req;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 加速释放订单 req对象
 * @author xms
 * @date 2025/11/21
 */
@Data
public class ReleaseOrderReq {
	/**
	 * 选择加速释放的订单id
	 */
	private List<Long>  orderIds;

	/**
	 * 选择的加速释放配置id
	 */
	@NotNull
	private Long configId;
}
