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
 * 示例质押等级配置对象 t_demo_pledge_level_config。
 *
 * <p>该配置只用于示例质押模块，等级升级同时考核个人业绩、团队业绩和小区业绩。</p>
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "t_demo_pledge_level_config")
public class DemoPledgeLevelConfig extends BaseEntity {
	private static final long serialVersionUID = 1L;

	/** 主键ID */
	@TableId(type = IdType.AUTO)
	@Excel(name = "主键ID", sort = 1)
	@ApiModelProperty(value = "主键ID")
	private Long id;

	/** 等级编码 0:暂无,1:F1,2:F2... */
	@Excel(name = "等级", sort = 2, dictType = "t_user_info_game_level")
	@ApiModelProperty(value = "等级编码 0:暂无,1:F1,2:F2...")
	private Integer level;

	/** 个人托管业绩门槛 */
	@Excel(name = "个人业绩", sort = 3)
	@ApiModelProperty(value = "个人托管业绩门槛")
	private BigDecimal performance;

	/** 团队托管业绩门槛 */
	@Excel(name = "团队业绩", sort = 4)
	@ApiModelProperty(value = "团队托管业绩门槛")
	private BigDecimal teamPerformance;

	/** 小区托管业绩门槛 */
	@Excel(name = "小区业绩", sort = 5)
	@ApiModelProperty(value = "小区托管业绩门槛")
	private BigDecimal communityPerformance;
}
