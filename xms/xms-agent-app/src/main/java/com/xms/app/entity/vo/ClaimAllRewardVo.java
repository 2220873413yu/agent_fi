package com.xms.app.entity.vo;

import com.xms.common.annotation.ValidDiyStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ClaimAllRewardVo {
	/**
	 * 0:活期利息,1:固定矿机利息,2:活期余额宝,3:固定矿机余额宝,4:活期本金,5:固定矿机本金
	 */
	@NotNull
	@ValidDiyStatus(values = {0, 1, 2, 3, 4, 5})
	private Integer type;

	/**
	 * 领取利息的时候传递(不传默认领取fsn代币),2:FTN,3:FSN
	 */
	private Integer coinType;
}
