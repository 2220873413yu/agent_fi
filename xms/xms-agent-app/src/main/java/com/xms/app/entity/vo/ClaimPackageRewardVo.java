package com.xms.app.entity.vo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ClaimPackageRewardVo {
	/**
	 * 债券订单id
	 */
	@NotNull
	private Long id;
}
