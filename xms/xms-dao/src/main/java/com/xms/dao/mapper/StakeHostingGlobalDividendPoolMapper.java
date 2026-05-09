package com.xms.dao.mapper;

import com.xms.dao.domain.StakeHostingGlobalDividendPool;
import org.apache.ibatis.annotations.Select;

/**
 * 托管全球分红奖池Mapper接口
 *
 * @author xms
 */
public interface StakeHostingGlobalDividendPoolMapper extends XmsMapper<StakeHostingGlobalDividendPool> {
	@Select("select id, pool_code, balance_amount, total_income_amount, total_expense_amount, last_income_time, last_expense_time, create_by, create_time, update_by, update_time, remark, deleted from t_stake_hosting_global_dividend_pool where deleted = 0 order by id asc limit 1 for update")
	StakeHostingGlobalDividendPool selectOneForUpdate();
}
