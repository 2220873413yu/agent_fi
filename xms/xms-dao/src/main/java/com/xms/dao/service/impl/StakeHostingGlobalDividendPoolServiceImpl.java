package com.xms.dao.service.impl;

import com.xms.common.exception.ServiceException;
import com.xms.common.utils.uuid.IDUtils;
import com.xms.dao.domain.StakeHostingGlobalDividendPool;
import com.xms.dao.domain.StakeHostingGlobalDividendPoolLog;
import com.xms.dao.mapper.StakeHostingGlobalDividendPoolMapper;
import com.xms.dao.service.IStakeHostingGlobalDividendPoolLogService;
import com.xms.dao.service.IStakeHostingGlobalDividendPoolService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 托管全球分红奖池Service业务层处理
 *
 * @author xms
 */
@Service
public class StakeHostingGlobalDividendPoolServiceImpl extends XmsDataServiceImpl<StakeHostingGlobalDividendPoolMapper, StakeHostingGlobalDividendPool> implements IStakeHostingGlobalDividendPoolService {
	public static final String DEFAULT_POOL_CODE = "STAKE_HOSTING_GLOBAL_DIVIDEND";
	public static final int FLOW_TYPE_INCOME = 1;
	public static final int FLOW_TYPE_EXPENSE = 2;
	public static final int BIZ_TYPE_DAILY_SERVICE_FEE = 1;
	public static final int BIZ_TYPE_MANUAL_INCOME = 2;
	public static final int BIZ_TYPE_WEEKLY_GLOBAL_DIVIDEND = 3;
	public static final int BIZ_TYPE_MANUAL_EXPENSE = 4;

	private final IStakeHostingGlobalDividendPoolLogService poolLogService;

	public StakeHostingGlobalDividendPoolServiceImpl(IStakeHostingGlobalDividendPoolLogService poolLogService) {
		this.poolLogService = poolLogService;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public StakeHostingGlobalDividendPool getOrInitPool() {
		StakeHostingGlobalDividendPool pool = lambdaQuery()
			.eq(StakeHostingGlobalDividendPool::getPoolCode, DEFAULT_POOL_CODE)
			.last("limit 1")
			.one();
		if (pool != null) {
			return pool;
		}
		pool = new StakeHostingGlobalDividendPool();
		pool.setPoolCode(DEFAULT_POOL_CODE);
		pool.setBalanceAmount(BigDecimal.ZERO);
		pool.setTotalIncomeAmount(BigDecimal.ZERO);
		pool.setTotalExpenseAmount(BigDecimal.ZERO);
		pool.setRemark("托管全球分红奖池");
		save(pool);
		return pool;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public StakeHostingGlobalDividendPool adjustPool(Integer flowType, BigDecimal amount, String remark, String operator) {
		if (flowType == null || (flowType != FLOW_TYPE_INCOME && flowType != FLOW_TYPE_EXPENSE)) {
			throw new ServiceException("流水类型不正确");
		}
		if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
			throw new ServiceException("调账金额必须大于0");
		}
		getOrInitPool();
		StakeHostingGlobalDividendPool pool = baseMapper.selectOneForUpdate();
		if (pool == null) {
			throw new ServiceException("全球分红奖池初始化失败");
		}
		Date now = new Date();
		BigDecimal beforeAmount = pool.getBalanceAmount() == null ? BigDecimal.ZERO : pool.getBalanceAmount();
		BigDecimal totalIncomeAmount = pool.getTotalIncomeAmount() == null ? BigDecimal.ZERO : pool.getTotalIncomeAmount();
		BigDecimal totalExpenseAmount = pool.getTotalExpenseAmount() == null ? BigDecimal.ZERO : pool.getTotalExpenseAmount();
		BigDecimal afterAmount;
		int bizType;
		if (flowType == FLOW_TYPE_INCOME) {
			afterAmount = beforeAmount.add(amount);
			totalIncomeAmount = totalIncomeAmount.add(amount);
			bizType = BIZ_TYPE_MANUAL_INCOME;
			pool.setLastIncomeTime(now);
		} else {
			if (beforeAmount.compareTo(amount) < 0) {
				throw new ServiceException("奖池余额不足，不能扣减");
			}
			afterAmount = beforeAmount.subtract(amount);
			totalExpenseAmount = totalExpenseAmount.add(amount);
			bizType = BIZ_TYPE_MANUAL_EXPENSE;
			pool.setLastExpenseTime(now);
		}
		pool.setBalanceAmount(afterAmount);
		pool.setTotalIncomeAmount(totalIncomeAmount);
		pool.setTotalExpenseAmount(totalExpenseAmount);
		pool.setUpdateBy(operator);
		pool.setUpdateTime(now);
		updateById(pool);

		StakeHostingGlobalDividendPoolLog log = new StakeHostingGlobalDividendPoolLog();
		log.setLogNo(IDUtils.getSnowflakeStr());
		log.setPoolId(pool.getId());
		log.setPoolCode(pool.getPoolCode());
		log.setFlowType(flowType);
		log.setBizType(bizType);
		log.setChangeAmount(amount);
		log.setBeforeAmount(beforeAmount);
		log.setAfterAmount(afterAmount);
		log.setCreateBy(operator);
		log.setCreateTime(now);
		log.setRemark(remark);
		poolLogService.save(log);
		return pool;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public StakeHostingGlobalDividendPool incomeDailyServiceFee(Integer settlementDay, BigDecimal amount, String operator) {
		if (settlementDay == null) {
			throw new ServiceException("服务费入池结算日不能为空");
		}
		if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
			return getOrInitPool();
		}
		getOrInitPool();
		StakeHostingGlobalDividendPool pool = baseMapper.selectOneForUpdate();
		if (pool == null) {
			throw new ServiceException("全球分红奖池初始化失败");
		}
		Date now = new Date();
		BigDecimal beforeAmount = pool.getBalanceAmount() == null ? BigDecimal.ZERO : pool.getBalanceAmount();
		BigDecimal totalIncomeAmount = pool.getTotalIncomeAmount() == null ? BigDecimal.ZERO : pool.getTotalIncomeAmount();
		BigDecimal afterAmount = beforeAmount.add(amount);
		pool.setBalanceAmount(afterAmount);
		pool.setTotalIncomeAmount(totalIncomeAmount.add(amount));
		pool.setLastIncomeTime(now);
		pool.setUpdateBy(operator);
		pool.setUpdateTime(now);
		updateById(pool);

		StakeHostingGlobalDividendPoolLog log = new StakeHostingGlobalDividendPoolLog();
		log.setLogNo(IDUtils.getSnowflakeStr());
		log.setPoolId(pool.getId());
		log.setPoolCode(pool.getPoolCode());
		log.setFlowType(FLOW_TYPE_INCOME);
		log.setBizType(BIZ_TYPE_DAILY_SERVICE_FEE);
		log.setChangeAmount(amount);
		log.setBeforeAmount(beforeAmount);
		log.setAfterAmount(afterAmount);
		log.setSourceSettlementDay(settlementDay);
		log.setCreateBy(operator);
		log.setCreateTime(now);
		log.setRemark("101任务每日托管服务费累计入池");
		poolLogService.save(log);
		return pool;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public StakeHostingGlobalDividendPool expenseWeeklyDividend(String batchNo, BigDecimal amount, String operator) {
		if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
			return getOrInitPool();
		}
		getOrInitPool();
		StakeHostingGlobalDividendPool pool = baseMapper.selectOneForUpdate();
		if (pool == null) {
			throw new ServiceException("全球分红奖池初始化失败");
		}
		Date now = new Date();
		BigDecimal beforeAmount = pool.getBalanceAmount() == null ? BigDecimal.ZERO : pool.getBalanceAmount();
		if (beforeAmount.compareTo(amount) < 0) {
			throw new ServiceException("奖池余额不足，不能发放全球分红");
		}
		BigDecimal totalExpenseAmount = pool.getTotalExpenseAmount() == null ? BigDecimal.ZERO : pool.getTotalExpenseAmount();
		BigDecimal afterAmount = beforeAmount.subtract(amount);
		pool.setBalanceAmount(afterAmount);
		pool.setTotalExpenseAmount(totalExpenseAmount.add(amount));
		pool.setLastExpenseTime(now);
		pool.setUpdateBy(operator);
		pool.setUpdateTime(now);
		updateById(pool);

		StakeHostingGlobalDividendPoolLog log = new StakeHostingGlobalDividendPoolLog();
		log.setLogNo(IDUtils.getSnowflakeStr());
		log.setPoolId(pool.getId());
		log.setPoolCode(pool.getPoolCode());
		log.setFlowType(FLOW_TYPE_EXPENSE);
		log.setBizType(BIZ_TYPE_WEEKLY_GLOBAL_DIVIDEND);
		log.setChangeAmount(amount);
		log.setBeforeAmount(beforeAmount);
		log.setAfterAmount(afterAmount);
		log.setSourceBatchNo(batchNo);
		log.setCreateBy(operator);
		log.setCreateTime(now);
		log.setRemark("102任务每周全球分红扣减奖池");
		poolLogService.save(log);
		return pool;
	}
}
