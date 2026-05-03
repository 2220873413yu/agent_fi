package com.xms.app.entity.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.xms.common.annotation.Excel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 加速释放配置
 * @author xms
 * @date 2023/10/07
 */
@Data
public class ReleaseConfigDto {

	/** 主键id */
	private Long id;
	/** 加速后总释放天数（例如：120/90/60/30/10天） */
	private Integer targetDays;
	/** 所需燃料占本金比例，例如 10 = 10% */
	private BigDecimal fuelRatio;
}
