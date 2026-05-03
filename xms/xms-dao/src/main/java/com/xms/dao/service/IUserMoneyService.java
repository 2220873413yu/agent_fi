package com.xms.dao.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xms.dao.entity.bo.UserMoneyValidNum4Bo;
import com.xms.dao.entity.domain.UserMoney;
import com.xms.dao.entity.domain.UserMoneyLog;
import com.xms.dao.entity.vo.UpdateUserMoneyVo;
import com.xms.dao.entity.vo.UserMoneyVo;

import java.math.BigDecimal;
import java.util.List;

/**
 * 用户钱包Service接口
 *
 * @date 2023-07-31
 */
public interface IUserMoneyService extends IService<UserMoney> {

	/**
	 * 查询用户钱包列表
	 *
	 * @param userMoney 用户钱包
	 * @return 用户钱包集合
	 */
	public List<UserMoney> selectUserMoneyList(UserMoney userMoney);

	/**
	 * 更新钱包
	 *
	 * @param userMoneyVo
	 * @return
	 */
	public int updateUserMoney(UserMoneyVo userMoneyVo);

	/**
	 * 查询链信值余额大于sourceThreshold的钱包用户
	 *
	 * @param
	 * @return
	 */
    List<UserMoneyValidNum4Bo> queryGeSourceThresholdId(BigDecimal sourceThreshold);

	/**
	 *  canal 数据同步的方式处理，如果不采用，那自行处理流水。
	 *	即将废弃 新版本可采用UserWalletService.handerUserMoney 方法,或者自己写原始SQL
	 * @Title: updateUserMoney
	 * @param:
	 * @Description: 更新用户钱包
	 * @return int
	 */
	@Deprecated
	int updateUserMoney(UpdateUserMoneyVo updateUserMoneyVo);

	/**
	 * 查询用户奖励
	 *
	 * @param userId
	 * @return
	 */
	BigDecimal querySubReward(Long userId);
	BigDecimal queryIndirectReward(Long userId);

	/**
	 * 获取今日奖励
	 *
	 * @param userId
	 * @return
	 */
	BigDecimal getTodayReward(Long userId);

	/**
	 * 获取总奖励
	 *
	 * @param userId
	 * @return
	 */
	BigDecimal getTotalReward(Long userId);
}
