package com.xms.app.entity.req;

import com.xms.common.annotation.ValidDiyStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpgradeCardOrderReq {

	@NotNull
	@Min(1)
	private Integer num;
	/**
	 * 要升级的卡片等级
	 */
	@NotNull
	@ValidDiyStatus(values = {1, 2, 3})
	private Integer cardLevel;

	/**
	 * 升级后的卡片等级
	 */
	@NotNull
	@ValidDiyStatus(values = {2, 3, 4})
	private Integer upgradeLevel;
}
