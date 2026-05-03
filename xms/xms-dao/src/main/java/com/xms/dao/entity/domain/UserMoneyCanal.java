package com.xms.dao.entity.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.util.Date;

/**
 * <p>
 * 用户钱包表
 * </p>
 * 必须得失数据库对应的字段的，否则不得行
 *
 * @author yaojie
 * @since 2023-07-25
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserMoneyCanal {

	@ApiModelProperty(value = "用户id")
	@TableId(value = "id", type = IdType.AUTO)
	private Long id;

	@ApiModelProperty(value = "可用余额数")
	private BigDecimal valid_num1;

	@ApiModelProperty(value = "可用余额数")
	private BigDecimal valid_num2;

	@ApiModelProperty(value = "可用余额数")
	private BigDecimal valid_num3;

	@ApiModelProperty(value = "可用余额数")
	private BigDecimal valid_num4;

	@ApiModelProperty(value = "可用余额数")
	private BigDecimal valid_num5;

	@ApiModelProperty(value = "可用余额数")
	private BigDecimal valid_num6;

	@ApiModelProperty(value = "可用余额数")
	private BigDecimal valid_num7;

	@ApiModelProperty(value = "可用余额数")
	private BigDecimal valid_num8;

	@ApiModelProperty(value = "可用余额数")
	private BigDecimal valid_num9;


	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date update_time;
	/**
	 * 每次更新的唯一序号，后续可用来修正数据
	 */
	private String gt_id;
	/**
	 * 来源订单编号
	 */
	private String source_code;
	/**
	 * 来源类型(1.充值 2.提现 3.推荐奖 4.级差奖 5.平级奖 6.购买套餐 7.平台扣拨)
	 */
	private Integer source_type;
	/**
	 * 来源用户ID
	 */
	private Long source_id;

}
