package com.xms.app.mapper;

import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.Map;

/**
 * @author: renengadePISTA
 * @createDate: 2024/1/11
 */
public interface AppDataBoardMapper {

	/**
	 * update_time >= DATE_SUB(CURDATE(), INTERVAL 1 DAY) and  update_time &lt; CURDATE();
	 * @param userId
	 * @return
	 */
	@Select("SELECT  SUM(amount) num   FROM xms_reward_record WHERE `user_id` = #{userId}  and business_type in (3,4,5,11,12,13,14)  and create_time >= CURDATE()")
	BigDecimal sumRewardToday(Long userId);



}
