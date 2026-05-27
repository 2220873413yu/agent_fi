package com.xms.dao.mapper;

import com.xms.dao.domain.DemoPledgeOrder;
import com.xms.dao.entity.domain.UserInfo;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

/**
 * 示例质押订单Mapper接口。
 */
public interface DemoPledgeOrderMapper extends XmsMapper<DemoPledgeOrder> {
	/**
	 * 查询示例质押订单列表。
	 *
	 * @param demoPledgeOrder 查询条件
	 * @return 示例质押订单集合
	 */
	List<DemoPledgeOrder> selectDemoPledgeOrderList(DemoPledgeOrder demoPledgeOrder);

	/**
	 * 增加用户个人业绩。
	 *
	 * @param userId 用户ID
	 * @param amount 增加金额，单位USDT
	 * @return 影响行数
	 */
	int increasePersonalPerformance(@Param("userId") Long userId, @Param("amount") BigDecimal amount);

	/**
	 * 增加用户直推业绩。
	 *
	 * @param userId 用户ID
	 * @param amount 增加金额，单位USDT
	 * @return 影响行数
	 */
	int increaseDirectPerformance(@Param("userId") Long userId, @Param("amount") BigDecimal amount);

	/**
	 * 批量增加上级团队业绩。
	 *
	 * @param userIds 上级用户ID列表
	 * @param amount 增加金额，单位USDT
	 * @return 影响行数
	 */
	int increaseTeamPerformance(@Param("userIds") List<Long> userIds, @Param("amount") BigDecimal amount);

	/**
	 * 查询指定用户的直推下级，用于按线重算小区业绩。
	 *
	 * @param userId 用户ID
	 * @return 直推下级用户集合
	 */
	List<UserInfo> selectDirectChildren(@Param("userId") Long userId);

	/**
	 * 更新用户小区业绩。
	 *
	 * @param userId 用户ID
	 * @param communityPerformance 小区业绩，单位USDT
	 * @return 影响行数
	 */
	int updateCommunityPerformance(@Param("userId") Long userId, @Param("communityPerformance") BigDecimal communityPerformance);
}
