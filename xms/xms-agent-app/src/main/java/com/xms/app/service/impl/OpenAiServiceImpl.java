package com.xms.app.service.impl;

import cn.hutool.core.util.IdUtil;
import com.xms.app.entity.req.OpenAiActionReq;
import com.xms.app.service.OpenAiService;
import com.xms.common.config.redis.XmsRedis;
import com.xms.common.constant.ConstantSys;
import com.xms.common.constant.ConstantType;
import com.xms.common.constant.RedisConstant;
import com.xms.common.constant.SysConstant;
import com.xms.common.exception.ServiceException;
import com.xms.common.result.ResponseCode;
import com.xms.common.utils.SecurityUtils;
import com.xms.common.utils.uuid.IDUtils;
import com.xms.dao.entity.domain.UserInfo;
import com.xms.dao.entity.domain.UserMoney;
import com.xms.dao.service.ISysParaService;
import com.xms.dao.service.IUserMoneyService;
import com.xms.dao.service.UserInfoService;
import com.xms.dao.service.UserWalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

@Service
public class OpenAiServiceImpl implements OpenAiService {

	@Autowired
	private XmsRedis xmsRedis;

	@Autowired
	private IUserMoneyService userMoneyServiceImpl;

	@Autowired
	private ISysParaService sysParaServiceImpl;

	@Autowired
	private UserWalletService userWalletServiceImpl;

	@Autowired
	private UserInfoService userInfoService;

	/**
	 * 开通当前登录用户的OpenAI聊天访问凭证。
	 *
	 * <p>用户只在首次开通时扣一次AFI。这里先用 t_user_info.open_ai_paid_status 做条件更新，
	 * 只有 open_ai_paid_status=0 的请求能更新成功并继续扣款；已经扣过费的用户只刷新Redis访问凭证。
	 * 方法开启事务，首次扣款余额不足或钱包扣减失败时，扣费标记会一起回滚。</p>
	 *
	 * @param params 开通AI请求参数，包含前端签名和随机数
	 * @return 1表示访问凭证已生成
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public int openAiAction(OpenAiActionReq params) {
		Long userId = SecurityUtils.getFrontUserId();

		// 先用条件更新抢占“首次扣费资格”，避免并发调用时重复扣AFI。
		int paidRows = userInfoService.markOpenAiPaidIfUnpaid(userId);
		if (paidRows == 1) {
			deductOpenAiAfiFee(userId);
		} else {
			// 未抢到首次扣费资格时，必须确认用户确实已扣费；避免用户不存在也被错误开通。
			UserInfo userInfo = userInfoService.lambdaQuery()
				.eq(UserInfo::getUserId, userId)
				.one();
			if (userInfo == null) {
				throw new ServiceException(ResponseCode.CODE_1007);
			}
			if (userInfo.getOpenAiPaidStatus() != 1) {
				throw new ServiceException(ResponseCode.CODE_1003);
			}
		}

		// 访问凭证仍然按当前业务放Redis一天；已扣费用户再次开通只刷新凭证，不再扣款。
		xmsRedis.set(RedisConstant.DbConstant.USER_AI_AGENT + userId, IdUtil.fastUUID(), SysConstant.ONE_LONG, TimeUnit.DAYS);
		return 1;
	}

	/**
	 * 扣减首次开通OpenAI聊天所需的AFI费用。
	 *
	 * <p>扣费金额读取系统参数 `biz_pay_afi_amount`，资产字段为用户钱包 validNum2，
	 * 钱包流水 source_type 使用现有AI开通扣费类型 type_6。</p>
	 *
	 * @param userId 当前登录用户ID
	 */
	private void deductOpenAiAfiFee(Long userId) {
		UserMoney userMoney = userMoneyServiceImpl.lambdaQuery()
			.eq(UserMoney::getId, userId)
			.one();

		BigDecimal payAfiAmount = new BigDecimal(sysParaServiceImpl.getValue(ConstantSys.biz_pay_afi_amount));
		BigDecimal validNum2 = userMoney == null || userMoney.getValidNum2() == null ? BigDecimal.ZERO : userMoney.getValidNum2();
		if (validNum2.compareTo(payAfiAmount) < 0) {
			throw new ServiceException(ResponseCode.CODE_1015);
		}

		String code = IDUtils.getSnowflakeStr();
		int count = userWalletServiceImpl.handerUserMoney(payAfiAmount.negate(), code, userId, userId,
			ConstantType.user_money_log_source_type.type_6, ConstantType.user_money_coin_type.type_2);
		if (count != 1) {
			throw new ServiceException(ResponseCode.CODE_1015);
		}
	}
}
