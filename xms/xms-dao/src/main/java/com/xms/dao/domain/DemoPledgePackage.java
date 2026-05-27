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
 * 示例质押套餐对象 t_demo_pledge_package。
 *
 * <p>该对象只用于初始化项目的标准业务样板，不代表真实生产质押规则。</p>
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "t_demo_pledge_package")
public class DemoPledgePackage extends BaseEntity {
	private static final long serialVersionUID = 1L;

	/** 主键ID */
	@TableId(type = IdType.AUTO)
	@Excel(name = "主键ID", sort = 1)
	@ApiModelProperty(value = "主键ID")
	private Long id;

	/** 套餐名称 */
	@Excel(name = "套餐名称", sort = 2)
	@ApiModelProperty(value = "套餐名称")
	private String packageName;

	/** 质押USDT金额 */
	@Excel(name = "质押USDT金额", sort = 3)
	@ApiModelProperty(value = "质押USDT金额")
	private BigDecimal pledgeUsdtAmount;

	/** 释放天数 */
	@Excel(name = "释放天数", sort = 4)
	@ApiModelProperty(value = "释放天数")
	private Integer releaseDays;

	/** 日利率，单位% */
	@Excel(name = "日利率(%)", sort = 5)
	@ApiModelProperty(value = "日利率，单位%")
	private BigDecimal dailyRate;

	/** 状态 0:停用 1:启用 */
	@Excel(name = "状态", sort = 6, dictType = "t_demo_pledge_package_status")
	@ApiModelProperty(value = "状态 0:停用 1:启用")
	private Integer status;

	/** 排序 */
	@Excel(name = "排序", sort = 7)
	@ApiModelProperty(value = "排序")
	private Integer sort;
}
