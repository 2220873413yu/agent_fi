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
 * 托管套餐对象 t_stake_hosting_package
 *
 * @author xms
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "t_stake_hosting_package")
public class StakeHostingPackage extends BaseEntity {
	private static final long serialVersionUID = 1L;

	/** 主键ID */
	@TableId(type = IdType.AUTO)
	@Excel(name = "主键ID", sort = 1)
	@ApiModelProperty(value = "主键ID")
	private Long id;

	/** 套餐名称 */
	@Excel(name = "套餐名称", sort = 2)
	@ApiModelProperty(value = "套餐名称")
	private String name;

	/** 托管天数，固定为1/30/90/180/360 */
	@Excel(name = "托管天数", sort = 3)
	@ApiModelProperty(value = "托管天数，固定为1/30/90/180/360")
	private Integer days;

	/** 最低起购USDT金额 */
	@Excel(name = "最低起购USDT", sort = 4)
	@ApiModelProperty(value = "最低起购USDT金额")
	private BigDecimal minAmount;

	/** 服务费比例，单位% */
	@Excel(name = "服务费比例", sort = 5)
	@ApiModelProperty(value = "服务费比例，单位%")
	private BigDecimal serviceFeeRatio;

	/** 业绩积分系数，用于计算新增小区业绩积分 */
	@Excel(name = "业绩积分系数", sort = 6)
	@ApiModelProperty(value = "业绩积分系数，用于计算新增小区业绩积分")
	private BigDecimal performanceCoefficient;

	/** 排序 */
	@Excel(name = "排序", sort = 6)
	@ApiModelProperty(value = "排序")
	private Integer sort;

	/** 状态 0:下架 1:上架 */
	@Excel(name = "状态", sort = 7, dictType = "t_stake_hosting_package_status")
	@ApiModelProperty(value = "状态 0:下架 1:上架")
	private Integer status;
}
