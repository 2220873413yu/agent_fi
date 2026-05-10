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
 * 托管G7静态收益率区间配置对象 t_stake_hosting_static_rate_config
 *
 * @author xms
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "t_stake_hosting_static_rate_config")
public class StakeHostingStaticRateConfig extends BaseEntity {
	private static final long serialVersionUID = 1L;

	/** 主键ID */
	@TableId(type = IdType.AUTO)
	@ApiModelProperty(value = "主键ID")
	private Long id;

	/** Gsmooth下限，单位%，左闭 */
	@Excel(name = "Gsmooth下限", sort = 1)
	@ApiModelProperty(value = "Gsmooth下限，单位%，左闭")
	private BigDecimal minG;

	/** Gsmooth上限，单位%，右开，NULL表示无上限 */
	@Excel(name = "Gsmooth上限", sort = 2)
	@ApiModelProperty(value = "Gsmooth上限，单位%，右开，NULL表示无上限")
	private BigDecimal maxG;

	/** 日化静态收益率，单位% */
	@Excel(name = "日化静态收益率", sort = 3)
	@ApiModelProperty(value = "日化静态收益率，单位%")
	private BigDecimal staticRate;

	/** 排序 */
	@Excel(name = "排序", sort = 4)
	@ApiModelProperty(value = "排序")
	private Integer sort;

	/** 状态 1启用 0停用 */
	@Excel(name = "状态", sort = 5, dictType = "t_stake_hosting_static_rate_config_status")
	@ApiModelProperty(value = "状态 1启用 0停用")
	private Integer status;

	/** 删除标志 0正常 1删除 */
	@ApiModelProperty(value = "删除标志 0正常 1删除")
	private Integer deleted;
}
