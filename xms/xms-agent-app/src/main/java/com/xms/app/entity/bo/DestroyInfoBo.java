package com.xms.app.entity.bo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 销毁boomai信息
 * @author xms
 * @date 2023/10/07
 */
@Data
public class DestroyInfoBo {

	/**
	 * 当前日化
	 */
	private BigDecimal dayRatio;

	/**
	 * 总销毁数量
	 */
	private BigDecimal totalDestroyAmount;
}
