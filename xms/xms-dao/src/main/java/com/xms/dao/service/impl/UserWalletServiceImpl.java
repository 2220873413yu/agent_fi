package com.xms.dao.service.impl;

import com.google.common.collect.Lists;
import com.xms.common.config.redis.XmsRedis;
import com.xms.common.constant.ConstantType;
import com.xms.common.constant.RedisConstant;
import com.xms.common.utils.uuid.IDUtils;
import com.xms.dao.entity.domain.UserMoney;
import com.xms.dao.entity.vo.UpdateUserWalletVo;
import com.xms.dao.entity.vo.UserWalletLogVo;
import com.xms.dao.mapper.UserMoneyMapper;
import com.xms.dao.service.UserWalletService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author: renengadePISTA
 * @createDate: 2023/8/24
 */
@Service
@Slf4j
@AllArgsConstructor
public class UserWalletServiceImpl implements UserWalletService {
	private final UserMoneyMapper userMoneyMapper;



	@Override
	public int updateUserMoney(UserMoney userMoney) {
		return userMoneyMapper.updateUserMoney(userMoney);
	}


	@Override
	public int updateWallet(UpdateUserWalletVo updateUserWalletVo) {
		UserMoney userMoney = UserMoney.builder().id(updateUserWalletVo.getUserId()).build();
		List<UserWalletLogVo> userMoneyLogList = updateUserWalletVo.getUserWalletLogList();
		for (UserWalletLogVo userMoneyLogVo : userMoneyLogList) {
			applyWalletChange(userMoney, userMoneyLogVo);
		}
		//更新钱包
		userMoney.setGtId(updateUserWalletVo.getGtId() == null ? IDUtils.getSnowflake().nextIdStr() : updateUserWalletVo.getGtId());
		userMoney.setSourceCode(updateUserWalletVo.getSourceCode());
		userMoney.setSourceId(updateUserWalletVo.getSourceId() == null ? updateUserWalletVo.getUserId() : updateUserWalletVo.getSourceId());
		userMoney.setSourceType(updateUserWalletVo.getSourceType());
		int i = userMoneyMapper.updateUserMoney(userMoney);
		if (i != 1) {
			return 0;
		}
		return 1;
	}

	/**
	 * 批量处理标准钱包更新VO。
	 *
	 * <p>该方法用于奖励、结算等批量入账场景，调用方不再直接组装UserMoney；
	 * 这里统一把coinType/changeBalance映射到validNumX，并保留gtId/sourceCode/sourceType/sourceId供Canal流水追踪。</p>
	 *
	 * @param walletUpdates 标准钱包更新VO集合，每个元素对应一个用户一次钱包变更
	 * @return 实际更新的钱包行数
	 */
	@Override
	public int handerBatchUserMoney(List<UpdateUserWalletVo> walletUpdates) {
		if (walletUpdates == null || walletUpdates.isEmpty()) {
			return 0;
		}
		List<UserMoney> userMoneyList = Lists.newArrayList();
		for (UpdateUserWalletVo updateUserWalletVo : walletUpdates) {
			UserMoney userMoney = UserMoney.builder().id(updateUserWalletVo.getUserId()).build();
			List<UserWalletLogVo> userWalletLogList = updateUserWalletVo.getUserWalletLogList();
			if (userWalletLogList == null || userWalletLogList.isEmpty()) {
				continue;
			}
			boolean hasChange = false;
			for (UserWalletLogVo userWalletLogVo : userWalletLogList) {
				hasChange = applyWalletChange(userMoney, userWalletLogVo) || hasChange;
			}
			if (!hasChange) {
				continue;
			}
			userMoney.setGtId(updateUserWalletVo.getGtId() == null ? IDUtils.getSnowflake().nextIdStr() : updateUserWalletVo.getGtId());
			userMoney.setSourceCode(updateUserWalletVo.getSourceCode());
			userMoney.setSourceId(updateUserWalletVo.getSourceId() == null ? updateUserWalletVo.getUserId() : updateUserWalletVo.getSourceId());
			userMoney.setSourceType(updateUserWalletVo.getSourceType());
			userMoneyList.add(userMoney);
		}
		if (userMoneyList.isEmpty()) {
			return 0;
		}
		return batchUpdateUserMoney(userMoneyList);
	}

	@Override
	public int handerManyUserMoney(List<UserWalletLogVo> userMoneyLogList, String orderNo, Long userId, Long sourceId, Integer bizType) {
		UpdateUserWalletVo updateUserMoneyVo = UpdateUserWalletVo.builder()
			.userId(userId)
			.gtId(IDUtils.getSnowflakeStr())
			.sourceId(sourceId)
			.sourceCode(orderNo)
			.sourceType(bizType)
			.userWalletLogList(userMoneyLogList).build();
		return updateWallet(updateUserMoneyVo);
	}

	@Override
	public int handerUserMoney(BigDecimal reward, String orderNo, Long userId, Long sourceId, Integer bizType, Integer coinType) {
		//更新钱包
		UserWalletLogVo userMoneyLogVo = UserWalletLogVo.builder()
			.coinType(coinType)
			.changeBalance(reward).build();
		List<UserWalletLogVo> userMoneyLogList = Lists.newArrayList();
		userMoneyLogList.add(userMoneyLogVo);
		UpdateUserWalletVo updateUserMoneyVo = UpdateUserWalletVo.builder()
			.userId(userId)
			.gtId(IDUtils.getSnowflake(coinType).nextIdStr())
			.sourceId(sourceId)
			.sourceCode(orderNo)
			.sourceType(bizType)
			.userWalletLogList(userMoneyLogList).build();
		return updateWallet(updateUserMoneyVo);
	}

	/**
	 * @param reward   金额
	 * @param orderNo  订单类型
	 * @param userId   用户IDU
	 * @param sourceId 来源ID
	 * @param bizType  业务类型
	 * @param bizType  业务类型
	 * @return coinType 币种类型
	 */
	@Override
	public int handerUserMoney(BigDecimal reward, String orderNo, Long userId, Long sourceId, Integer bizType, Integer coinType, String gtId) {
		//更新钱包
		UpdateUserWalletVo updateUserMoneyVo = wrapperMoney(reward, orderNo, userId, sourceId, bizType, coinType, gtId);
		return updateWallet(updateUserMoneyVo);
	}


	@Override
	public UpdateUserWalletVo wrapperMoney(BigDecimal amount, String orderNo, Long userId, Long sourceId, Integer bitType, Integer coinType, String gtId) {
		UserWalletLogVo userMoneyLogVo = UserWalletLogVo.builder()
			.coinType(coinType)
			.changeBalance(amount).build();
		List<UserWalletLogVo> userMoneyLogList = Lists.newArrayList();
		userMoneyLogList.add(userMoneyLogVo);
		return UpdateUserWalletVo.builder()
			.userId(userId)
			.gtId(gtId)
			.sourceId(sourceId)
			.sourceCode(orderNo)
			.sourceType(bitType)
			.userWalletLogList(userMoneyLogList).build();
	}

	/**
	 * 批量增加，不适合减少
	 *
	 * @param userMoneyList
	 * @return
	 */
	@Override
	public int batchUpdateUserMoney(List<UserMoney> userMoneyList) {
		return userMoneyMapper.batchUpdateUserMoney(userMoneyList);
	}

	/**
	 * 把标准钱包流水VO中的币种和变动金额映射到UserMoney的validNum字段。
	 *
	 * <p>返回值用于批量场景判断本次更新是否有实际金额变动；金额为0时跳过，不产生钱包更新。</p>
	 *
	 * @param userMoney 待写入数据库的钱包增量对象
	 * @param userMoneyLogVo 单个币种的钱包变动
	 * @return true表示已写入一个非零金额的钱包字段
	 */
	private boolean applyWalletChange(UserMoney userMoney, UserWalletLogVo userMoneyLogVo) {
		if (userMoneyLogVo.getChangeBalance().compareTo(BigDecimal.ZERO) == 0) {
			return false;
		}
		if (userMoneyLogVo.getCoinType() == ConstantType.user_money_coin_type.type_1) {
			userMoney.setValidNum1(userMoneyLogVo.getChangeBalance());
		} else if (userMoneyLogVo.getCoinType() == ConstantType.user_money_coin_type.type_2) {
			userMoney.setValidNum2(userMoneyLogVo.getChangeBalance());
		} else if (userMoneyLogVo.getCoinType() == ConstantType.user_money_coin_type.type_3) {
			userMoney.setValidNum3(userMoneyLogVo.getChangeBalance());
		} else if (userMoneyLogVo.getCoinType() == ConstantType.user_money_coin_type.type_4) {
			userMoney.setValidNum4(userMoneyLogVo.getChangeBalance());
		} else if (userMoneyLogVo.getCoinType() == ConstantType.user_money_coin_type.type_5) {
			userMoney.setValidNum5(userMoneyLogVo.getChangeBalance());
		} else if (userMoneyLogVo.getCoinType() == ConstantType.user_money_coin_type.type_6) {
			userMoney.setValidNum6(userMoneyLogVo.getChangeBalance());
		} else if (userMoneyLogVo.getCoinType() == ConstantType.user_money_coin_type.type_7) {
			userMoney.setValidNum7(userMoneyLogVo.getChangeBalance());
		} else if (userMoneyLogVo.getCoinType() == ConstantType.user_money_coin_type.type_8) {
			userMoney.setValidNum8(userMoneyLogVo.getChangeBalance());
		} else if (userMoneyLogVo.getCoinType() == ConstantType.user_money_coin_type.type_9) {
			userMoney.setValidNum9(userMoneyLogVo.getChangeBalance());
		} else {
			return false;
		}
		return true;
	}
}
