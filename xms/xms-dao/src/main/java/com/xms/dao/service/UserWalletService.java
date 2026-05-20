package com.xms.dao.service;

import com.xms.dao.entity.domain.UserMoney;
import com.xms.dao.entity.vo.UpdateUserWalletVo;
import com.xms.dao.entity.vo.UserWalletLogVo;

import java.math.BigDecimal;
import java.util.List;

/**
 * 用户钱包更新服务。
 *
 * <p>封装单笔、多币种和底层批量钱包余额变更入口，钱包表变更会由Canal侧生成对应流水。</p>
 */
public interface UserWalletService {

	int updateUserMoney(UserMoney userMoney);

	int updateWallet(UpdateUserWalletVo updateUserWalletVo);

	int handerManyUserMoney(List<UserWalletLogVo> userMoneyLogList, String orderNo, Long userId, Long sourceId, Integer bizType);

	/**
	 * 替代 updateUserMoney 方法，作为与Canal中间数据监听配套的更新账户方法。
	 *
	 * @param reward 金额，正数入账、负数扣减
	 * @param orderNo 来源订单号
	 * @param userId 用户ID
	 * @param sourceId 来源ID
	 * @param bizType 业务类型
	 * @param coinType 币种类型
	 * @return 更新行数，1表示成功
	 */
	int handerUserMoney(BigDecimal reward, String orderNo, Long userId, Long sourceId, Integer bizType, Integer coinType);

	/**
	 * 更新钱包，并使用指定gtId关联后续流水记录。
	 *
	 * @param reward 金额，正数入账、负数扣减
	 * @param orderNo 来源订单号
	 * @param userId 用户ID
	 * @param sourceId 来源ID
	 * @param bizType 业务类型
	 * @param coinType 币种类型
	 * @param gtId 钱包表变更流水追踪ID
	 * @return 更新行数，1表示成功
	 */
	int handerUserMoney(BigDecimal reward, String orderNo, Long userId, Long sourceId, Integer bizType, Integer coinType, String gtId);

	UpdateUserWalletVo wrapperMoney(BigDecimal amount, String orderNo, Long userId, Long sourceId, Integer bitType, Integer coinType, String gtId);

	/**
	 * 批量增加钱包余额，不适合用于余额扣减。
	 *
	 * @param userMoneyList 钱包增量列表
	 * @return 实际更新行数
	 */
	int batchUpdateUserMoney(List<UserMoney> userMoneyList);
}
