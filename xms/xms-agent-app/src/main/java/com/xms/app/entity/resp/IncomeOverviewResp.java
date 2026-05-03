package com.xms.app.entity.resp;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 收益概览（一次返回今日/本周/本月/累计等卡片数据）
 */
@Data
public class IncomeOverviewResp {

	/**
	 * 各时间维度的收益详情（1:今日 2:本周 3:本月 4:累计）
	 */
	private List<IncomeScopeDetail> scopes;

	@Data
	public static class IncomeScopeDetail {
		/**
		 * 统计范围类型：1-今日 2-本周 3-本月 4-累计
		 */
		private Integer scopeType;

		/**
		 * 该范围的收益总额
		 */
		private BigDecimal amount;

		/**
		 * 收益类型分布
		 */
		private List<IncomeTypePortion> typePortions;
	}

	@Data
	public static class IncomeTypePortion {
		/**
		 * 类型：1-静态 2-动态 3-团队 等
		 */
		private Integer type;

		/**
		 * 金额
		 */
		private BigDecimal amount;

		/**
		 * 占比（百分数，比如 2.5 表示 2.5%）
		 */
		private BigDecimal ratio;
	}
}

