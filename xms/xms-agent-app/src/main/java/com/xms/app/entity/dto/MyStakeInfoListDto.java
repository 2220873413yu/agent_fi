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
 * 我的质押信息列表
 * @author xms
 * @date 2023/10/07
 */
@Data
public class MyStakeInfoListDto {

	/** 主键id */
	private Long id;

	/** 用户id */
	private Long userId;

	/** 质押订单号 */
	private String orderNo;

	/** 质押金额/USDT单位 */
	private BigDecimal stakeUsdtAmount;

	/** 状态 0:待链上支付确认,1:产出中,2:已出局(已完成),3:已过期未支付,4:已暂停产出 */
	private Integer status;

	/** 剩余可产出 */
	private BigDecimal remainingOutAmount;

	/** 今日收益 */
	private BigDecimal todayReward;

	/** 日利率 */
	private BigDecimal dayRatio;

	/** 出局目标产出总额 */
	private BigDecimal allOutAmount;

	/** 支付时间 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date payTime;

	/** 创建时间 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date createTime;
}
