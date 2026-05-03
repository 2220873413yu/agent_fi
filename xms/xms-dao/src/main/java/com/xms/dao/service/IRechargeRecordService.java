package com.xms.dao.service;

import com.xms.dao.domain.RechargeRecord;

import java.math.BigDecimal;
import java.util.List;

/**
 * 充值记录Service接口
 *
 * @author xms
 * @date 2025-03-12
 */
public interface IRechargeRecordService extends XmsDataService<RechargeRecord>
{

    /**
     * 查询充值记录列表
     *
     * @param rechargeRecord 充值记录
     * @return 充值记录集合
     */
    public List<RechargeRecord> selectRechargeRecordList(RechargeRecord rechargeRecord);

	/**
	 * 更新充值记录
	 * @param rechargeRecord
	 * @return
	 */
	int updateRecordById(RechargeRecord rechargeRecord);

	/**
	 * 累计充值金额
	 * @return
	 */
    BigDecimal selectTotalRechargeBalance();

	/**
	 * 今日充值金额
	 * @return
	 */
	BigDecimal selectTodayRechargeBalance();
}
