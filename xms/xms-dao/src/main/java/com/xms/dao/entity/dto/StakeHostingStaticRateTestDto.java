package com.xms.dao.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 托管静态日利率测试结果。
 *
 * <p>仅用于核对 101 静态收益任务命中的基础日利率，不代表实际发放金额；
 * 实际发放还会继续叠加 AFI 加速、服务费和团队奖励等逻辑。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StakeHostingStaticRateTestDto {
	/** 托管订单ID */
	private Long orderId;

	/** 托管订单号 */
	private String orderNo;

	/** 用户ID */
	private Long userId;

	/** 托管USDT金额 */
	private BigDecimal stakeUsdtAmount;

	/** 用户长期指定托管静态收益率，单位% */
	private BigDecimal stakeHostingStaticRate;

	/** 昨日伞下团队新增托管金额，单位USDT；字段名沿用 previous_team_tvl */
	private BigDecimal previousTeamTvl;

	/** 当日伞下团队新增托管金额，单位USDT；字段名沿用 current_team_tvl */
	private BigDecimal currentTeamTvl;

	/** 当日伞下团队新增托管金额，单位USDT */
	private BigDecimal teamNewAmount;

	/** 当日伞下团队到期托管金额，单位USDT；当前不参与G7静态日利率 */
	private BigDecimal teamExpiredAmount;

	/** 单日团队新增业绩增长率，单位% */
	private BigDecimal gDay;

	/** 最近最多7天团队新增业绩平均增长率，单位% */
	private BigDecimal gSmooth;

	/** G7快照命中的基础静态收益率，单位% */
	private BigDecimal baseStaticRate;

	/** 最终命中的基础静态日利率，单位% */
	private BigDecimal finalStaticRate;

	/** 命中来源：指定收益率、未推广规则、G7快照、快照缺失兜底 */
	private String rateSource;

	/** 命中原因说明 */
	private String remark;
}
