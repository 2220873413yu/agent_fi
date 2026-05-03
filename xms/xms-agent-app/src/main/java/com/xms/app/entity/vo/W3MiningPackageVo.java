package com.xms.app.entity.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.xms.common.annotation.Excel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class W3MiningPackageVo {
	/** 套餐id */
	private Long id;
	/** 套餐类型 0:活期,1:定期 */
	private Integer type;
	/** 套餐有效期天数 */
	private Integer day;
	/** 日利率 */
	private BigDecimal dayRatio;
	/** 收益倍数 */
	private BigDecimal multipliedValue;
	/** 最少购买金额限制 */
	private BigDecimal minBuyPrice;
	/** USDT支付占比 */
	private BigDecimal usdtRatio;
	/** FTN支付占比 */
	private BigDecimal wfRatio;
	/** 是否上架 0:否,1:是 */
	private Integer status;
}
