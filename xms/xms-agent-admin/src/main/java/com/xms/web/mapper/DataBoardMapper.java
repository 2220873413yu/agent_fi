package com.xms.web.mapper;

import com.xms.web.domain.LineDataDO;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author: renengadePISTA
 * @createDate: 2024/1/11
 */
public interface DataBoardMapper {



	@Select("SELECT  COALESCE(SUM(change_balance), 0)  num   FROM t_withdrawal WHERE `status` = 5  ")
	BigDecimal countWdwNum();


	@Select("SELECT  COALESCE(SUM(change_balance), 0)  num FROM t_withdrawal WHERE `status` = 5  AND TO_DAYS(create_time) =TO_DAYS(NOW())")
	BigDecimal countWdwNumToday();

	@Select("SELECT sum(performance) FROM `t_user_info` ")
	BigDecimal countPerformanceNum();

	@Select("SELECT sum(token_amount) FROM `xms_mining_order` WHERE type=1 and  TO_DAYS(create_time) =TO_DAYS(NOW())  ")
	BigDecimal countPerformanceNumToday();


	@Select("select sum(amount*real_time_price) from xms_reward_record   where   TO_DAYS(create_time) =TO_DAYS(NOW())")
	BigDecimal sumYesReward();

	@Select("select sum(amount*real_time_price) from xms_reward_record where     business_type in (5,9,11) ")
	BigDecimal sumDynamicReward();

	@Select("select sum(amount*real_time_price) from xms_reward_record where     business_type =12  ")
	BigDecimal sumStaticReward();

	@Select("SELECT sum(capital) FROM `xms_mining_order` WHERE source_type=1 and TO_DAYS(create_time) =TO_DAYS(NOW())  ")
	BigDecimal countPerformanceNumTodayReal();

	@Select("SELECT sum(capital) FROM `xms_mining_order` WHERE source_type=1 and  TO_DAYS(create_time) =TO_DAYS(NOW())-1  ")
	BigDecimal countPerformanceNumYesReal();


	@Select("select sum(amount*real_time_price) from xms_reward_record where     business_type in (5,9,11)  AND TO_DAYS(create_time) =TO_DAYS(NOW())-1")
	BigDecimal sumDynamicRewardYes();

	@Select("select sum(amount*real_time_price) from xms_reward_record where     business_type =12   AND TO_DAYS(create_time) =TO_DAYS(NOW())-1 ")
	BigDecimal sumStaticRewardYes();

	@Select("select sum(amount*real_time_price) from xms_reward_record where      TO_DAYS(create_time) =TO_DAYS(NOW())-1 ")
	BigDecimal sumTotalRewardYes();

	List<LineDataDO> listUserGroupTotal();

	List<LineDataDO> getLineWdwChartData();

	@Select("SELECT sum(change_balance) expectedRewardData  FROM t_withdrawal WHERE create_time >= CURDATE() AND status =1 ")
	BigDecimal getLineWdwChartDataToday();

	List<LineDataDO> listRewardGroupTotal();



	@Select("SELECT sum(amount) expectedRewardDataV2  FROM xms_reward_record WHERE create_time >= CURDATE() ")
	BigDecimal listRewardGroupTotalToday();
}
