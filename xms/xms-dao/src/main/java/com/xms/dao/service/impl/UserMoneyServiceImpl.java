package com.xms.dao.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xms.common.constant.ConstantType;
import com.xms.common.exception.ServiceException;
import com.xms.common.utils.SecurityUtils;
import com.xms.common.utils.uuid.IDUtils;
import com.xms.dao.entity.bo.UserMoneyValidNum4Bo;
import com.xms.dao.entity.domain.UserMoney;
import com.xms.dao.entity.vo.UpdateUserMoneyVo;
import com.xms.dao.entity.vo.UserMoneyLogVo;
import com.xms.dao.entity.vo.UserMoneyVo;
import com.xms.dao.mapper.UserMoneyMapper;
import com.xms.dao.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.math.BigDecimal;
import java.util.List;

/**
 * 用户钱包Service业务层处理
 *
 * @date 2023-07-31
 */
@Service
public class UserMoneyServiceImpl extends ServiceImpl<UserMoneyMapper, UserMoney> implements IUserMoneyService {

	@Autowired
	private UserWalletService userWalletServiceImpl;

	/**
	 * 查询用户钱包列表
	 *
	 * @param userMoney 用户钱包
	 * @return 用户钱包
	 */
	@Override
	public List<UserMoney> selectUserMoneyList(UserMoney userMoney) {
		List<UserMoney> userMonies = baseMapper.selectUserMoneyList(userMoney);
		return userMonies;
	}

	/**
	 * 更新钱包
	 *
	 * @param userMoneyVo
	 * @return
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public int updateUserMoney(UserMoneyVo userMoneyVo) {

		String orderCode =IDUtils.getSnowflakeStr();
		int i = userWalletServiceImpl.handerUserMoney(userMoneyVo.getChangeBalance(), orderCode, userMoneyVo.getId(),
			SecurityUtils.getUserId(), ConstantType.user_money_log_source_type.type_28, userMoneyVo.getCoinType());
		if (i == 0) {
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			throw new ServiceException("更新资产异常");
		}
		return 1;
	}

	/**
	 * 查询链信值余额大于sourceThreshold的钱包用户
	 * @param sourceThreshold
	 * @return
	 */
	@Override
	public List<UserMoneyValidNum4Bo> queryGeSourceThresholdId(BigDecimal sourceThreshold) {
		return baseMapper.queryGeSourceThresholdId(sourceThreshold);
	}


	/**
	 * canal 数据同步的方式处理，如果不采用，那自行处理流水。
	 * 即将废弃 新版本可采用UserWalletService.handerUserMoney 方法,或者自己写原始SQL
	 *
	 * @param updateUserMoneyVo
	 * @return int
	 * @Title: updateUserMoney
	 * @param:
	 * @Description: 更新用户钱包
	 */
	@Override
	public int updateUserMoney(UpdateUserMoneyVo updateUserMoneyVo) {
		//批量新增流水对象
		UserMoney userMoney = UserMoney.builder().id(updateUserMoneyVo.getUserId()).build();
		List<UserMoneyLogVo> userMoneyLogList = updateUserMoneyVo.getUserMoneyLogList();
		for (UserMoneyLogVo userMoneyLogVo : userMoneyLogList) {
			if (userMoneyLogVo.getChangeBalance().compareTo(BigDecimal.ZERO) == 0) {
				continue;
			}
			if (updateUserMoneyVo.getSourceType() == null) {
				updateUserMoneyVo.setSourceType(userMoneyLogVo.getSourceType());
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
			}

		}
		//更新钱包
		userMoney.setGtId(IDUtils.getSnowflake().nextIdStr());
		userMoney.setSourceCode(updateUserMoneyVo.getSourceCode());
		userMoney.setSourceId(updateUserMoneyVo.getSourceId() == null ? updateUserMoneyVo.getUserId() : updateUserMoneyVo.getSourceId());
		userMoney.setSourceType(updateUserMoneyVo.getSourceType());
		int i = this.baseMapper.updateUserMoney(userMoney);
		if (i != 1) {
			return 0;
		}
		return 1;
	}

	@Override
	public BigDecimal querySubReward(Long userId) {
		return baseMapper.querySubReward(userId);
	}

	@Override
	public BigDecimal queryIndirectReward(Long userId) {
		return baseMapper.queryIndirectReward(userId);
	}

	@Override
	public BigDecimal getTodayReward(Long userId) {
		return baseMapper.getTodayReward(userId);
	}

	@Override
	public BigDecimal getTotalReward(Long userId) {
		return baseMapper.getTotalReward(userId);
	}
}
