package com.xms.web.mapper;

import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;

/**
 * @author: renengadePISTA
 * @createDate: 2024/1/11
 */
public interface ScheduleTaskMapper {

	@Select("SELECT SUM(amount) FROM `xms_reward_record` WHERE business_type=11 and TO_DAYS(create_time)= TO_DAYS(NOW())")
	BigDecimal sumNum();

}
