package com.xms.dao.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.xms.common.annotation.Excel;
import com.xms.common.core.domain.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * AFI质押加速配置对象 t_stake_hosting_afi_accelerate_config
 *
 * @author xms
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "t_stake_hosting_afi_accelerate_config")
public class StakeHostingAfiAccelerateConfig extends BaseEntity {
	private static final long serialVersionUID = 1L;

	/** 主键ID */
	@TableId(type = IdType.AUTO)
	@Excel(name = "主键ID", sort = 1)
	@ApiModelProperty(value = "主键ID")
	private Long id;

	/** 质押比例，单位% */
	@Excel(name = "质押比例", sort = 2)
	@ApiModelProperty(value = "AFI等值USDT / 托管USDT比例，单位%")
	private BigDecimal pledgeRatio;

	/** 加速倍率 */
	@Excel(name = "加速倍率", sort = 3)
	@ApiModelProperty(value = "加速倍率，例如1.10")
	private BigDecimal accelerateRate;

	/** 排序 */
	@Excel(name = "排序", sort = 4)
	@ApiModelProperty(value = "排序")
	private Integer sort;

	/** 状态 0:停用 1:启用 */
	@Excel(name = "状态", sort = 5, dictType = "t_stake_hosting_afi_config_status")
	@ApiModelProperty(value = "状态 0:停用 1:启用")
	private Integer status;
}
