package com.xms.dao.service;

import com.xms.dao.entity.domain.UserMoney;
import com.xms.dao.entity.vo.UpdateUserWalletVo;
import com.xms.dao.entity.vo.UserWalletLogVo;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author: renengadePISTA
 * @createDate: 2023/8/24
 */
public interface UserWalletService {


	int updateUserMoney(UserMoney userMoney);

	int updateWallet(UpdateUserWalletVo updateUserWalletVo);

	int handerManyUserMoney(List<UserWalletLogVo> userMoneyLogList, String orderNo, Long userId, Long sourceId, Integer bizType);

	/**
	 * 替代 updateUserMoney 方法，作为与canal中间数据监听配套的更新账户方法
	 *
	 * @param reward   金额
	 * @param orderNo  订单类型
	 * @param userId   用户IDU
	 * @param sourceId 来源ID
	 * @param bizType  业务类型
	 * @param bizType  业务类型
	 * @return coinType 币种类型
	 */
	int handerUserMoney(BigDecimal reward, String orderNo, Long userId, Long sourceId, Integer bizType, Integer coinType);

	/**
	 * 更新钱包
	 * @param reward 更新的余额
	 * @param orderNo 来源
	 * @param userId 用户id
	 * @param sourceId 来源哪个用户Id
	 * @param bizType 业务类型
	 * @param coinType 货币类型
	 * @param gtId 映射的id号对应产生记录的订单号
	 * @return
	 */
	int handerUserMoney(BigDecimal reward, String orderNo, Long userId, Long sourceId, Integer bizType, Integer coinType,String gtId);


	UpdateUserWalletVo wrapperMoney(BigDecimal amount, String orderNo, Long userId, Long sourceId, Integer bitType, Integer coinType, String gtId);


	/**
	 * 批量增加，不适合减少
	 * @param userMoneyList
	 * @return
	 */
	int batchUpdateUserMoney(List<UserMoney> userMoneyList);
}
