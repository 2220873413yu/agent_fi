package com.xms.app.entity.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.xms.common.annotation.Excel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 质押信息
 * @author xms
 * @date 2023/6/12
 */
@Data
public class StakeInfoDTO {

	/** 最低质押 */
	private BigDecimal stakeUnitAmountMin;
	/** 最高质押 */
	private BigDecimal maxStakeAmount;
	/** 静态日利率 例如: 1就是1% */
	private BigDecimal staticRatio;
	/** 出局倍数 */
	private BigDecimal exitMultiplier;
}
