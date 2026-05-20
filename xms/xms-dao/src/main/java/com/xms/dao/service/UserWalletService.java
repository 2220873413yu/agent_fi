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
	 * 批量处理多个用户的钱包入账/出账变更。
	 *
	 * <p>调用方传入标准钱包更新VO，方法内部统一转换为UserMoney增量并批量落库；
	 * 适合奖励、结算等多用户批量场景，保留gtId/sourceCode/sourceType/sourceId追踪字段。</p>
	 *
	 * @param walletUpdates 标准钱包更新VO集合，每个元素对应一个用户一次钱包变更
	 * @return 实际更新的钱包行数
	 */
	int handerBatchUserMoney(List<UpdateUserWalletVo> walletUpdates);

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
