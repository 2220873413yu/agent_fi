package com.xms.dao.mapper;

import com.xms.dao.domain.RechargeRecord;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;

/**
 * 充值记录Mapper接口
 *
 * @author xms
 * @date 2025-03-12
 */
public interface RechargeRecordMapper extends XmsMapper<RechargeRecord>
{
    /**
     * 查询充值记录列表
     *
     * @param rechargeRecord 充值记录
     * @return 充值记录集合
     */
    public List<RechargeRecord> selectRechargeRecordList(RechargeRecord rechargeRecord);

	/**
	 * 查询累计充值余额
	 * @return
	 */
	@Select("SELECT COALESCE(SUM(recharge_amount), 0) AS total_cnt FROM t_recharge_record where status = 1")
    BigDecimal selectTotalRechargeBalance();


	/**
	 * 查询今日充值金额
	 * @return
	 */
	@Select("SELECT COALESCE(SUM(recharge_amount), 0) AS total_cnt FROM t_recharge_record where status = 1 and create_time >= CURDATE()")
	BigDecimal selectTodayRechargeBalance();
}
