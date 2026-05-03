package com.xms.app.entity.vo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ClaimV3RewardVo {

	/**
	 * 矿机id
	 */
	@NotNull
	private Long id;
}
