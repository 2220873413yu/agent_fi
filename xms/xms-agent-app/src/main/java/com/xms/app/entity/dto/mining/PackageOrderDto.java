package com.xms.app.entity.dto.mining;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.xms.common.annotation.Excel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 基金订单历史购买记录
 * @author xms
 * @date 2023/10/07
 */
@Data
public class PackageOrderDto {
	/** 主键id */
	private Long id;
	/** 订单号 */
	private String orderNo;
	/** 购买金额(本金) */
	private BigDecimal buyPrice;
	/** 矿机天数 */
	private Integer days;
	/** 剩余天数 */
	private Integer haveDays;
	/** 运行天数 */
	private Integer runDays;
	/** 矿机类型 0:活期,1:固定 */
	private Integer type;
	/** 日利率 */
	private BigDecimal dayRatio;
	/**
	 * 累计收益
	 */
	private BigDecimal totalReward;

	/** 订单来源 0:用户购买,1:系统赠送 */
	private Integer sourceType;
	/** 最大收益 */
	private BigDecimal maxReward;
	/** 状态 0:释放中,1:已经达到最大倍数,2:已结束 */
	private Integer status;
	/** 订单每日违约金递减率(如0.5%) */
	private BigDecimal dailyPenaltyReduction;
	/** 订单违约金比例(如20%) */
	private BigDecimal penaltyRate;

	/** 当前违约金比例 */
	private BigDecimal currentPenaltyRate;
	/** 是否退本 0:否,1:是 */
	private Integer principalReturned;

	/** 创建时间 */
	private Date createTime;

	/** 退本业务状态处理 0:未退本,1:退本了处理中,2:等待退本(活期是n+1时间的),3:退本成功 */
	private Integer returnedBizStatus;
}
