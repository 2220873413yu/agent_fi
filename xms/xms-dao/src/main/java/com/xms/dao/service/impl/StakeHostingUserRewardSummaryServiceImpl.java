package com.xms.dao.service.impl;

import com.xms.dao.domain.StakeHostingUserRewardSummary;
import com.xms.dao.mapper.StakeHostingUserRewardSummaryMapper;
import com.xms.dao.service.IStakeHostingUserRewardSummaryService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * 托管用户奖励累计汇总Service业务层处理
 *
 * @author xms
 */
@Service
public class StakeHostingUserRewardSummaryServiceImpl
	extends XmsDataServiceImpl<StakeHostingUserRewardSummaryMapper, StakeHostingUserRewardSummary>
	implements IStakeHostingUserRewardSummaryService {

	/**
	 * 查询用户托管奖励累计汇总。
	 *
	 * 表结构只有奖励累计字段，不使用通用 BaseEntity 字段，因此通过专用 SQL 读取。
	 *
	 * @param userId 用户ID
	 * @return 用户托管奖励累计汇总，未初始化时返回null
	 */
	@Override
	public StakeHostingUserRewardSummary getByUserId(Long userId) {
		return baseMapper.selectByUserId(userId);
	}

	/**
	 * 初始化用户托管奖励累计汇总。
	 *
	 * 注册用户时调用，若记录已存在则忽略，所有累计金额默认为0。
	 *
	 * @param userId 用户ID
	 */
	@Override
	public void initUser(Long userId) {
		baseMapper.initUser(userId);
	}

	/**
	 * 累加用户托管极差奖累计金额。
	 *
	 * 若用户汇总记录不存在则自动初始化后累加，金额单位为USDT。
	 *
	 * @param userId 用户ID
	 * @param amount 本次极差奖金额
	 */
	@Override
	public void addDiffReward(Long userId, BigDecimal amount) {
		if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
			return;
		}
		baseMapper.addDiffReward(userId, amount);
	}

	/**
	 * 累加用户托管平级奖累计金额。
	 *
	 * 若用户汇总记录不存在则自动初始化后累加，金额单位为USDT。
	 *
	 * @param userId 用户ID
	 * @param amount 本次平级奖金额
	 */
	@Override
	public void addSameLevelReward(Long userId, BigDecimal amount) {
		if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
			return;
		}
		baseMapper.addSameLevelReward(userId, amount);
	}

	/**
	 * 累加用户托管全球分红累计金额。
	 *
	 * 若用户汇总记录不存在则自动初始化后累加，金额单位为USDT。
	 *
	 * @param userId 用户ID
	 * @param amount 本次全球分红金额
	 */
	@Override
	public void addGlobalDividend(Long userId, BigDecimal amount) {
		if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
			return;
		}
		baseMapper.addGlobalDividend(userId, amount);
	}

	/**
	 * 批量累加用户托管团队收益汇总。
	 *
	 * <p>用于定时任务批量结算直推/极差/平级奖励后的汇总落库；这里只累计极差奖和平级奖，
	 * 直推奖不计入App团队收益汇总。</p>
	 *
	 * @param list 用户团队收益增量列表，金额单位USDT
	 */
	@Override
	public void batchAddTeamRewardSummary(List<StakeHostingUserRewardSummary> list) {
		if (list == null || list.isEmpty()) {
			return;
		}
		baseMapper.batchAddTeamRewardSummary(list);
	}
}
