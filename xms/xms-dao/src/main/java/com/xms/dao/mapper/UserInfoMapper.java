package com.xms.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xms.common.core.domain.entity.SysDictData;
import com.xms.dao.entity.bo.*;
import com.xms.dao.entity.domain.UserInfo;
import com.xms.dao.entity.vo.ParentUserTaskVo;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.util.List;

/**
 * <p>
 * 用户信息表 Mapper 接口
 * </p>
 *
 *
 * @since 2023-07-25
 */
public interface UserInfoMapper extends BaseMapper<UserInfo> {

	/**
	 *
	 * @Title: getDirectUserList
	 * @param:
	 * @Description: 我的直推用户列表
	 * @return List<DirectUserBo>
	 */
	List<DirectUserBo> getDirectUserList(@Param("inviteUserCode") String inviteUserCode);

	/**
	 * 查询上级团队用户
	 * @param userId
	 * @return
	 */
	List<UserInfo> getParentUserList(@Param("userId") Long userId);


	/**
	 * 查询上级团队用户 只查询用户id 层级 等级相关
	 * @param userId
	 * @return
	 */
	List<ParentUserTaskVo> getParentUserTaskVo(@Param("userId") Long userId);

	/**
	 * 查询最近的有效上级用户
	 * @param userId 当前用户ID
	 * @param limit 返回数量
	 * @return 有效上级列表
	 */
	List<ParentUserTaskVo> getValidParentUserTaskVo(@Param("userId") Long userId, @Param("limit") Integer limit);

	/**
	 * 查询上级团队用户ID
	 * @param userId
	 * @return
	 */
	List<Long> getParentUserIdList(@Param("userId") Long userId);

	/**
	 * 查询指定层级数内的上级用户ID
	 * @param userId 当前用户ID
	 * @param distance 限制的层级数
	 * @return 指定层级内的上级用户ID列表
	 */
	List<Long> getLimitedParentUserIdList(@Param("userId") Long userId, @Param("distance") Integer distance);


	List<Parent20UserBo> get20ParentUserList(@Param("userId") Long userId);

	/**
	 * 查询下级团队用户
	 * @param userId
	 * @return
	 */
	List<UserInfo> getChildUserList(@Param("userId") Long userId);

	/**
	 * 查询团队成员（含多级，下限 500 条）
	 */
	List<UserInfo> getTeamMembersLimited(@Param("userId") Long userId, @Param("level") Integer level);

	/**
	 * 查询下级团队用户id
	 * @param userId
	 * @return
	 */
	List<Long> getChildUserIdList(@Param("userId") Long userId);

	//****************管理后台***************

	/**
	 * 查询用户信息列表
	 *
	 * @param userInfo 用户信息
	 * @return 用户信息集合
	 */
	public List<UserInfo> selectUserInfoList(UserInfo userInfo);

	/**
	 * 查询下级团队用户
	 * @param userInfo
	 * @return
	 */
	public List<UserInfo> selectChildUserInfoList(UserInfo userInfo);

	/**
	 * 统计团队用户
	 * @param userId
	 * @return
	 */
	public List<TeamUsersBo> countTodayNewTeamUsers(@Param("topUserId") Long userId);

	/**
	 * 获取所有用户
	 *
	 * @return
	 */
    List<ChangeLevelUserBo> getChangeLevelUserBo(List<Long> parentList);

	/**
	 * 获取所有合伙人用户id
	 *
	 * @return
	 */
    List<Long> queryPartnerUserId();

	/**
	 * 根据等级获取用户
	 *
	 * @return
	 */
	List<Long> getUserByLevel(@Param("level")Integer level);

	@Select("select * from sys_dict_data where status = '0' and dict_type = #{dictType} order by dict_sort asc")
	List<SysDictData> selectDictDataByType(String sxRecordType);

	@Select("SELECT COALESCE(SUM(static_change_balance), 0) AS total_static_income FROM t_w3_mining_static_log WHERE date = DATE_FORMAT(CURRENT_DATE(), '%Y%m%d');")
	BigDecimal queryTodayStaticReward();

	@Select("SELECT COALESCE(SUM(dynamic_chanage_balance), 0) AS total_dynamic_income FROM t_w3_mining_static_log WHERE date = DATE_FORMAT(CURRENT_DATE(), '%Y%m%d');")
	BigDecimal queryTodayDynamicReward();

	@Select("SELECT COALESCE(SUM(static_change_balance), 0) AS total_static_income FROM t_w3_mining_static_log")
	BigDecimal queryTotalStaticReward();

	@Select("SELECT COALESCE(SUM(dynamic_chanage_balance), 0) AS total_dynamic_income FROM t_w3_mining_static_log")
	BigDecimal queryTotalDynamicReward();

	@Insert("INSERT INTO t_w3_mining_static_log (static_change_balance, dynamic_chanage_balance, date, create_time) " +
		"VALUES (#{staticChangeBalance}, #{dynamicChangeBalance}, #{date}, CURRENT_TIMESTAMP)")
	int insertMiningStaticLog(@Param("staticChangeBalance") BigDecimal staticChangeBalance,
							   @Param("dynamicChangeBalance") BigDecimal dynamicChangeBalance,
							   @Param("date") String date);

	@Select("SELECT COALESCE(SUM(valid_num1),0) AS totalValidNum1, " +
		"COALESCE(SUM(valid_num2),0) AS totalValidNum2, " +
		"COALESCE(SUM(valid_num3),0) AS totalValidNum3 " +
		"FROM t_user_money where deleted = 0")
	UserMoneySumDTO queryUserMoneySum();

	List<UserInfo> queryNetBodyChildUser(@Param("userId")  Long userId);


	/**
	 * 获取用户算力List
	 * @return
	 */
	List<UserComputingPowerBo> userComputingPower();

	/**
	 * 获取用户等级List
	 * @return
	 */
	List<UserLevelBo> getUserLevelList();

	/**
	 * 批量获取用户信息
	 * @return
	 */
	List<BatchUserBo> getBatchUser();

	/**
	 * 首次开通OpenAI聊天时原子标记用户已扣费。
	 *
	 * <p>只有 open_ai_paid_status=0 的用户才会更新成功，调用方通过影响行数判断是否需要扣AFI。
	 * 这个条件更新用于避免同一用户并发调用开通接口时重复扣费。</p>
	 *
	 * @param userId 用户ID
	 * @return 1表示本次首次标记成功；0表示已标记或用户不存在
	 */
	@Update("UPDATE t_user_info SET open_ai_paid_status = 1, update_time = NOW() WHERE user_id = #{userId} AND open_ai_paid_status = 0 AND deleted = 0")
	int markOpenAiPaidIfUnpaid(@Param("userId") Long userId);
}
