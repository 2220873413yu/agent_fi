package com.xms.app.entity.dto;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.xms.common.annotation.Excel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 质押订单奖励dto对象
 * @author: liuya
 * @date: 2023/1/5
 */
@Data
public class OrderRewardDto {

	/** id */
	private Long id;

	/** 静态收益数量 */
	private BigDecimal amount;

	/** 订单号 */
	private String orderCode;

	/** 用户id */
	private Long userId;

	/** 创建时间 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date createTime;
}
