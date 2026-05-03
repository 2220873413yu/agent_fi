package com.xms.app.entity.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.xms.common.annotation.Excel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 矿机套餐对象
 *
 * @author xms
 * @date 2026-02-21
 */
@Data
public class MiningPackageDto {

	/** 主键id */
	private Long id;

	/** 算力数值 */
	private String computingPower;

	/** 矿机价格 */
	private BigDecimal price;
}
