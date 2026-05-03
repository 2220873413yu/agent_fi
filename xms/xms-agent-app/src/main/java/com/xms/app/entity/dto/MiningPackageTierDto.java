package com.xms.app.entity.dto;

import com.xms.common.annotation.Excel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 矿包等级
 *
 * @author xms
 * @date 2021/05/05
 */
@Data
public class MiningPackageTierDto {
	/** 质押金额(DFC) */
	private BigDecimal stakeAmount;

	/** 日产出(DFC) */
	private BigDecimal dayReward;
}
