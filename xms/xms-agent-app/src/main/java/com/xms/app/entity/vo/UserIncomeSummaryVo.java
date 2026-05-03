package com.xms.app.entity.vo;

import com.xms.common.annotation.Excel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 用户收益信息vo对象
 * @author xms
 * @date 2023/04/05
 */
@Data
public class UserIncomeSummaryVo {

	/** 获得基金利息(静态收益) */
	private BigDecimal sourceType21Balance;
	/** 推荐奖 */
	private BigDecimal sourceType23Balance;
	/** 团队奖 */
	private BigDecimal sourceType24Balance;
}
