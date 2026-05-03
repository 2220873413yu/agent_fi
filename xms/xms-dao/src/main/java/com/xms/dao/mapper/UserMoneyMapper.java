package com.xms.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xms.dao.entity.bo.UserMoneyValidNum4Bo;
import com.xms.dao.entity.domain.UserMoney;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;

/**
 * <p>
 * 用户钱包表 Mapper 接口
 * </p>
 *
 *
 * @author MIER
 * @since 2023-07-25
 */
public interface UserMoneyMapper extends BaseMapper<UserMoney> {

	/**
	 * 更新钱包
	 * @param userMoney
	 * @return
	 */
	int updateUserMoney(UserMoney userMoney);

	//****************管理后台***************

	/**
	 * 查询用户钱包列表
	 *
	 * @param userMoney 用户钱包
	 * @return 用户钱包集合
	 */
	List<UserMoney> selectUserMoneyList(UserMoney userMoney);

	@Select("select id from  t_user_money where user_id = #{userId} and active_flag = 1")
	Integer getByUserId(Long userId);

	int batchUpdateUserMoney(@Param("list") List<UserMoney> userMoneyList);

	/**
	 * 查询链信值余额大于sourceThreshold的钱包用户
	 * @param sourceThreshold
	 * @return
	 */
    List<UserMoneyValidNum4Bo> queryGeSourceThresholdId(@Param("value")BigDecimal sourceThreshold);

    BigDecimal querySubReward(@Param("userId") Long userId);

    BigDecimal queryIndirectReward(@Param("userId") Long userId);

	BigDecimal getTodayReward(@Param("userId") Long userId);

	BigDecimal getTotalReward(@Param("userId")Long userId);
}
