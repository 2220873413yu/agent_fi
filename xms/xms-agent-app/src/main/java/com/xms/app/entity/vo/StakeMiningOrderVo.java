package com.xms.app.entity.vo;

import com.xms.common.annotation.ValidDiyStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 质押矿机 vo对象
 */
@Data
public class StakeMiningOrderVo {

	/**
	 * 矿机id
	 */
	@NotNull
	private Long id;

	/**
	 * 质押类型 1:托管,2:自提
	 */
	@NotNull
	@ValidDiyStatus(values = {1,2})
	private Integer type;

	/**
	 * 收货地址id
	 */
	private Long addressId;
}
