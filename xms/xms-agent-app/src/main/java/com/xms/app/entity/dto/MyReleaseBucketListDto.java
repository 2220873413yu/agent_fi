package com.xms.app.entity.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.xms.common.annotation.Excel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 锁仓释放订单列表
 * @author xms
 * @date 2023/10/07
 */
@Data
public class MyReleaseBucketListDto {
	/** 主键id */
	private Long id;
	/** 桶内剩余天数 */
	private Integer haveDays;
	/** 桶内累计应线性释放总量 */
	private BigDecimal totalAmount;
	/** 桶内剩余待释放量 */
	private BigDecimal remainingAmount;
	/** 创建时间 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date createTime;
}
