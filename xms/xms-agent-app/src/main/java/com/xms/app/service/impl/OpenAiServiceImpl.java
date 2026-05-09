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
import com.xms.dao.entity.domain.UserMoney;
import com.xms.dao.service.ISysParaService;
import com.xms.dao.service.IUserMoneyService;
import com.xms.dao.service.UserWalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
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



	@Override
	public int openAiAction(OpenAiActionReq params) {
		// todo   BY RENEGADE PISTA: 2026/5/6  这里需要调用合约接口，扣除用户余额，开通AI权限
		// 1. 验证支付密码/鉴权
		// 2. 调用合约接口，扣除用户余额，开通AI权限
		// 3. 生成访问凭证，存储到Redis中，设置过期时间
		// 4. 返回访问凭证给前端
		// 注意：以上步骤需要保证原子性，可以使用分布式事务或者消息队列来实现
		//实现扣款逻辑
		//扣款提现额度
		Long userId = SecurityUtils.getFrontUserId();
		UserMoney userMoney = userMoneyServiceImpl.lambdaQuery()
			.eq(UserMoney::getId, userId)
			.one();

		BigDecimal payAfiAmount = new BigDecimal(sysParaServiceImpl.getValue(ConstantSys.biz_pay_afi_amount));
		if(userMoney.getValidNum2().compareTo(payAfiAmount)<0){
			throw new ServiceException(ResponseCode.CODE_1015);
		}
		//订单号
		String code = IDUtils.getSnowflakeStr();
		int count = userWalletServiceImpl.handerUserMoney(payAfiAmount.negate(), code, userId, userId,
			ConstantType.user_money_log_source_type.type_6, ConstantType.user_money_coin_type.type_2);
		if (count != 1) {
			throw new ServiceException(ResponseCode.CODE_1015);
		}
		//获取临时凭证
		xmsRedis.set(RedisConstant.DbConstant.USER_AI_AGENT + userId, IdUtil.fastUUID(), SysConstant.ONE_LONG, TimeUnit.DAYS);
		return 1;
	}
}
