package com.xms.app.entity.dto;

import com.xms.common.annotation.Excel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 获取奖金记录DTO
 * @author xms
 * @date 2022/5/27 17:09
 */
@Data
public class RewardRecordDto {

	/**
	 * id
	 */
	private Long id;

	/**
	 * 数量
	 */
	private BigDecimal amount;

	/**
	 * 来源类型
	 */
	private Integer sourceType;

	/** 来源订单号 */
	private String sourceOrderCode;

	/** 来源用户 */
	private Long sourceUserId;

	/**
	 * 创建时间
	 */
	private Date createTime;

	/**
	 * 结算时币种price
	 */
	private BigDecimal realTimePrice;
}
