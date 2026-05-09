package com.xms.app.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.system.SystemUtil;
import com.github.pagehelper.PageInfo;
import com.uduncloud.sdk.domain.ResultMsg;
import com.xms.app.entity.bo.WithdrawalCallbackBo;
import com.xms.app.entity.bo.WithdrawalConfigBo;
import com.xms.app.entity.req.JuNotifyReq;
import com.xms.app.entity.resp.WithdrawalInfo;
import com.xms.app.entity.resp.WithdrawalSummaryResp;
import com.xms.app.entity.vo.UserBankInfoVo;
import com.xms.app.entity.vo.UserBankVo;
import com.xms.app.entity.vo.WithdrawalVo;
import com.xms.app.handler.CustomException;
import com.xms.app.service.BizWithdrawalService;
import com.xms.common.config.redis.XmsRedis;
import com.xms.common.core.domain.AjaxResult;
import com.xms.common.mq.dynamic.AsyncDynamicOrderSettlementService;
import com.xms.common.mq.dynamic.OrderMsgDO;
import com.xms.common.notify.AsyncTelegramMessageService;
import com.xms.common.notify.TelegramMessageDTO;
import com.xms.common.notify.TelegramNotifyService;
import com.xms.common.utils.*;
import com.xms.common.utils.googleUtil.GoogleAuthenticator;
import com.xms.common.utils.spring.SpringUtils;
import com.xms.dao.domain.SwapOrder;
import com.xms.dao.domain.WithdrawalConfig;
import com.xms.dao.entity.bo.WithdrawalBo;
import com.xms.dao.service.WithdrawalService;
import com.xms.common.config.redis.lock.RedisLock;
import com.xms.common.constant.*;
import com.xms.common.core.domain.api.ResultPista;
import com.xms.common.exception.ServiceException;
import com.xms.common.result.ResponseCode;
import com.xms.common.utils.uuid.IDUtils;
import com.xms.dao.entity.domain.UserInfo;
import com.xms.dao.entity.domain.UserMoney;
import com.xms.dao.entity.domain.Withdrawal;
import com.xms.dao.service.*;
import com.xms.dao.service.impl.WithdrawalServiceImpl;
import lombok.extern.slf4j.Slf4j;
import net.dreamlu.mica.core.result.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.util.*;

import static com.xms.app.service.impl.BizUserServiceImpl.checkWallet;


@Service
@Slf4j
public class BizWithdrawalServiceImpl implements BizWithdrawalService {
	@Autowired
	private WithdrawalService withdrawalService;

	@Autowired
	private IUserMoneyService userMoneyServiceImpl;

	@Autowired
	private ISysParaService sysParaServiceImpl;

	@Autowired
	private UserInfoService userInfoService;

	@Autowired
	private UserWalletService userWalletServiceImpl;


	@Autowired
	private XmsRedis xmsRedis;

	@Autowired
	private IWithdrawalConfigService withdrawalConfigService;

	@Autowired
	private AsyncDynamicOrderSettlementService asyncDynamicOrderSettlementServiceImpl;


	@Autowired(required = false)
	private TelegramNotifyService telegramNotifyService;

	@Autowired
	private AsyncTelegramMessageService asyncTelegramMessageService;

	@Value("${telegram.chat-id:}")
	private String telegramChatId;



	@Value("${lq.md5Key}")
	private String md5Key;

	@Override
	public ResultPista<WithdrawalConfigBo> getWithdrawalConfig(Integer coinType) {
		WithdrawalConfig withdrawalConfig = withdrawalConfigService.lambdaQuery()
			.eq(WithdrawalConfig::getCoinType, coinType)
			.one();
		WithdrawalConfigBo result = new WithdrawalConfigBo();
		result.setWithdrawOpen(withdrawalConfig.getWithdrawOpen());
		result.setMinWithdrawAmount(withdrawalConfig.getMinWithdrawAmount());
		result.setFeeRatio(withdrawalConfig.getFeeRatio());
		return ResultPista.data(result);
	}

	@Override
	public void updateJuStatus(JuNotifyReq req) {
		//查询提现单号是否存在
		Withdrawal withdrawal = withdrawalService.lambdaQuery().eq(Withdrawal::getCode, req.getOrderId()).one();
		if (withdrawal == null) {
			log.warn("提现单号不存在：{}", req.getOrderId());
			return;
		}
		if (withdrawal.getStatus() != 1) {
			log.warn("提现单号已处理完成：{} status {}", req.getOrderId(), req.getStatus());
			return;
		}
		if (req.getStatus() == 1) {
			boolean update = withdrawalService.lambdaUpdate()
				.set(Withdrawal::getStatus, 3)
				.set(Withdrawal::getUpdateTime, new Date())
				.set(Withdrawal::getCreditedTime, new Date())
				.eq(Withdrawal::getId, withdrawal.getId())
				.eq(Withdrawal::getStatus, 1).update();
			if (!update) {
				throw new ServiceException(ResponseCode.CODE_1003);
			}
		} else if (req.getStatus() == 2) {
			finalWithdrawFail(withdrawal);
		}

	}

	private void finalWithdrawFail(Withdrawal withdraw) {
		withdrawalFail(withdraw);
		boolean update = withdrawalService.lambdaUpdate()
			.set(Withdrawal::getStatus, 4)
			.set(Withdrawal::getUpdateTime, new Date())
			.eq(Withdrawal::getId, withdraw.getId())
			.eq(Withdrawal::getStatus, 1).update();
		if (!update) {
			throw new ServiceException(ResponseCode.CODE_1003);
		}
	}

	/**
	 * @return void
	 * @Title: withdrawalFail
	 * @param:
	 * @Description: 提现失败，返还资金
	 */
	public void withdrawalFail(Withdrawal withdrawal) {
		int i = userWalletServiceImpl.handerUserMoney(withdrawal.getChangeBalance(), withdrawal.getCode(),
			withdrawal.getUserId(), withdrawal.getUserId()
			, ConstantType.user_money_log_source_type.type_26, withdrawal.getCoinType());
		if (i != 1) {
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			throw new ServiceException("回退失败");
		}

		//退还手续费
		i = userWalletServiceImpl.handerUserMoney(withdrawal.getFeeBalance(), withdrawal.getCode(), withdrawal.getUserId(), withdrawal.getUserId()
			, ConstantType.user_money_log_source_type.type_32,ConstantType.user_money_coin_type.type_3);
		if (i != 1) {
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			throw new ServiceException("FEE 回退失败");
		}
	}

	/**
	 * 保存收款信息
	 *
	 * @param req
	 */
	@Override
	public void bindUserBank(UserBankVo req) {

	}

	/**
	 * 解绑收款信息
	 */
	@Override
	public void unBindUserBank(Long id) {
	}


	/**
	 * 查询收款信息
	 *
	 * @return
	 */
	@Override
	public UserBankInfoVo getUserBank(Integer type) {
		return null;
	}


	/**
	 * 提现
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	@RedisLock(value = RedisConstant.LockConstant.XMS_WITHDRAW_APPLY, param = "#userId")
	public int addWithdrawal(WithdrawalVo req, Long userId){

		req.setChangeBalance(req.getChangeBalance().setScale(ConstantStatic.newScale, ConstantStatic.roundingModeNew));
		//查询提现信息
		WithdrawalConfig withdrawalConfig = withdrawalConfigService.lambdaQuery()
			.eq(WithdrawalConfig::getCoinType, req.getCoinType())
			.one();
		if(withdrawalConfig == null){
			throw new ServiceException(ResponseCode.CODE_1003);
		}

		if(withdrawalConfig.getWithdrawOpen().equals(SysConstant.TWO)){
			throw new ServiceException(ResponseCode.CODE_1108);
		}

		//查询用户
		UserInfo userInfo = userInfoService.lambdaQuery().eq(UserInfo::getUserId, userId).one();

		//用户维度提现开关
		if (userInfo.getWithdrawalOpenOrClose().equals(SysConstant.ONE)) {
			throw new ServiceException(ResponseCode.CODE_1108);
		}

		//提现额度最小判断
		if (req.getChangeBalance().compareTo( withdrawalConfig.getMinWithdrawAmount()) < 0) {
			throw new ServiceException(String.format(ResponseCode.CODE_1109.getMsg(),
				withdrawalConfig.getMinWithdrawAmount().stripTrailingZeros().toPlainString()), ResponseCode.CODE_1109.getCode());
		}

		//验签，随机数
		checkWallet(req.getRandomNum(), req.getSignature(), userInfo.getAccount(), xmsRedis);

		UserMoney userMoney = userMoneyServiceImpl.lambdaQuery()
			.eq(UserMoney::getId, userId)
			.one();
		if(req.getCoinType().equals(1)){
			if (userMoney.getValidNum1().compareTo(req.getChangeBalance()) < 0) {
				throw new ServiceException(ResponseCode.CODE_1015);
			}
		}else if(req.getCoinType().equals(2)){
			if (userMoney.getValidNum2().compareTo(req.getChangeBalance()) < 0) {
				throw new ServiceException(ResponseCode.CODE_1015);
			}
		}else if(req.getCoinType().equals(3)){
			if (userMoney.getValidNum3().compareTo(req.getChangeBalance()) < 0) {
				throw new ServiceException(ResponseCode.CODE_1015);
			}
		}else if(req.getCoinType().equals(5)){
			if (userMoney.getValidNum5().compareTo(req.getChangeBalance()) < 0) {
				throw new ServiceException(ResponseCode.CODE_1015);
			}
		}else{
			throw new ServiceException(ResponseCode.CODE_1002);
		}

		BigDecimal ratio = withdrawalConfig.getFeeRatio()
			.divide(SysConstant.BAIFENBI, ConstantStatic.newScale, ConstantStatic.roundingModeNew);

		//手续费
		BigDecimal fee = req.getChangeBalance().multiply(ratio)
			.setScale(ConstantStatic.newScale, ConstantStatic.roundingModeNew);
		if (fee.compareTo(BigDecimal.ZERO) < 0) {
			//手续费设置有误
			fee = BigDecimal.ZERO;
		}

		//手续费大于提现额度
		if(fee.compareTo(req.getChangeBalance()) >= 0){
			throw new ServiceException(ResponseCode.CODE_1070);
		}

		//订单号
		String code = IDUtils.getSnowflakeStr();
		//扣款提现额度
		int count = userWalletServiceImpl.handerUserMoney(req.getChangeBalance().negate(), code, userId, userId, ConstantType.user_money_log_source_type.type_4,
			req.getCoinType());
		if (count != 1) {
			throw new ServiceException(ResponseCode.CODE_1015);
		}

		Integer status = ConstantType.withdrawal_status.type_0;

		//提现免审核配置
		BigDecimal auditAmount = withdrawalConfig.getWithdrawLimit();
		Integer auditCount = withdrawalConfig.getDailyFreeAuditCount();
		if(req.getChangeBalance().compareTo(auditAmount)<=0) {
			status = ConstantType.withdrawal_status.type_3;
			//查询累计提现多少钱
			List<Withdrawal> withdrawalList = withdrawalService.lambdaQuery()
				.eq(Withdrawal::getUserId, userId)
				.eq(Withdrawal::getCoinType, req.getCoinType())
				.apply("create_time >= CURDATE()")
				.select(Withdrawal::getChangeBalance)
				.list();
			if (CollectionUtil.isNotEmpty(withdrawalList)) {
				BigDecimal totalWithdrawal = BigDecimal.ZERO;
				for (Withdrawal withdrawal : withdrawalList) {
					totalWithdrawal = totalWithdrawal.add(withdrawal.getChangeBalance());
				}

				if (totalWithdrawal.add(req.getChangeBalance()).compareTo(auditAmount) > 0) {
					//超过了单日提现总额
					status = ConstantType.withdrawal_status.type_0;
				}

				if (withdrawalList.size() >= auditCount) {
					//判断提现次数超过了当日
					status = ConstantType.withdrawal_status.type_0;
				}
			}

			if(auditCount<=0){
				status = ConstantType.withdrawal_status.type_0;
			}
		}


		//新增提现记录
		Withdrawal withdrawal = Withdrawal.builder().userId(userId).code(code)
			.changeBalance(req.getChangeBalance())
			.bizStatus(0)
			.feeBalance(fee)
			.feeRatio(withdrawalConfig.getFeeRatio())
			.status(status)
			.coinType(req.getCoinType())
			.accountNo(userInfo.getAccount())
			.build();
		boolean save = withdrawalService.save(withdrawal);
		if (!save) {
			throw new ServiceException(ResponseCode.CODE_1002);
		}

		//如果status=1就直接发送提现请求
		if(withdrawal.getStatus().equals(ConstantType.withdrawal_status.type_3)){
			//发送到合约(内扣)
			String tokenName= "USDT";
			Map<String, Object> formParams = new HashMap<>();
			formParams.put("orderId", withdrawal.getCode());
			formParams.put("userAddress", withdrawal.getAccountNo());
			formParams.put("token", tokenName);
			BigDecimal finalAmount = withdrawal.getChangeBalance().subtract(withdrawal.getFeeBalance());
			formParams.put("amount", finalAmount.stripTrailingZeros().toPlainString());
			String sign = SignUtil.getSign(formParams, false, false, md5Key);
			formParams.put("sign", sign);
			log.info("提现业务完整参数 param:{},sign:{}", formParams,sign);
			SpringUtils.getBean(WithdrawalServiceImpl.class)
				.sendWithdrawalRequest(formParams);
		}

		// 提现通知放到事务提交后异步入队，避免影响提单主流程。
		sendWithdrawalTelegramAsync(withdrawal);

		return 1;
	}

	private void sendWithdrawalTelegramAsync(Withdrawal withdrawal) {
		// 这里只入 Redis Stream，Telegram HTTP 发送交给独立消费者慢慢处理。
		String amountText = withdrawal.getChangeBalance().stripTrailingZeros().toPlainString();
		String auditText = withdrawal.getStatus().equals(ConstantType.withdrawal_status.type_3)
			? "已自动审核"
			: "需人工审核";
		String text = String.format("备注:%s，发起提现,金额%sUSDT,%s", withdrawal.getAccountNo(), amountText, auditText);
		TelegramMessageDTO telegramMessageDTO = new TelegramMessageDTO();
		telegramMessageDTO.setChatId(telegramChatId);
		telegramMessageDTO.setText(text);
		asyncTelegramMessageService.sendMessage(telegramMessageDTO);
	}



	/**
	 * 提现记录
	 * @param pageIndex 当前页 默认1
	 * @param pageSize 每页长度 默认20(最大20)
	 * @return
	 */
	@Override
	public PageInfo<WithdrawalBo> listWithdrawRecord(Integer coinType, Integer pageIndex, Integer pageSize,Long userId) {
		return withdrawalService.listWithdrawRecord(coinType ,pageIndex, pageSize, userId);
	}

	/**
	 * 提现回调
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public ResultPista<String> withdrawalCallback(WithdrawalCallbackBo req) {

		log.info("提现回调 req:{}", req);
		/*if(!StrUtil.isBlank(req.getHash())){
			if(req.getHash().length()>255){
				throw new ServiceException("hash长度不能超过255");
			}
		}

		// 将 RechargeCallbackBo 对象转换为 Map
		Map<String, Object> map = BeanUtil.beanToMap(req);
		String sign = SignUtil.getSign(map, false, false, md5Key);
		String osName = SystemUtil.getOsInfo().getName();
		if (!osName.toUpperCase().contains(SysConstant.OS_NAME_WINDOWS)) {
			if (!sign.equals(req.getSign())) {
				log.error("验签失败");
				return ResultPista.fail("验签失败");
			}
		}



		Withdrawal withdrawal = withdrawalService.lambdaQuery()
			.eq(Withdrawal::getCode, req.getRequestId())
			.one();
		if(withdrawal == null || !withdrawal.getStatus().equals(ConstantType.withdrawal_status.type_1)){
			throw new ServiceException("提现订单已经被处理了");
		}

		Integer status =req.getSuccess().equals(false)? 4:3;
		boolean update = withdrawalService.lambdaUpdate()
			.eq(Withdrawal::getId, withdrawal.getId())
			.eq(Withdrawal::getStatus, ConstantType.withdrawal_status.type_1)
			.set(Withdrawal::getStatus, status)
			.set(req.getSuccess(),Withdrawal::getCreditedTime, new Date())
			.set(Withdrawal::getUpdateTime, new Date())
			.set(Withdrawal::getRemark, req.getHash())
			//审核成功
			.set(req.getSuccess() == true, Withdrawal::getCreditedTime, new Date())
			.update();
		if(!update){
			throw new ServiceException("提现订单已经被处理了");
		}

		if(req.getSuccess().equals(false)){
			//退还提现金额
			int i = userWalletServiceImpl.handerUserMoney(withdrawal.getChangeBalance(), withdrawal.getCode(), withdrawal.getUserId(), withdrawal.getUserId()
				, ConstantType.user_money_log_source_type.type_5, withdrawal.getCoinType());
			if (i != 1) {
				throw new ServiceException(ResponseCode.CODE_1002);
			}
		}else{
			//提现成功、后续业务逻辑分配
			TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
				@Override
				public void afterCommit() {
					List<OrderMsgDO> orderMsgDOList = new ArrayList<>();
					OrderMsgDO orderMsgDO = new OrderMsgDO();
					orderMsgDO.setId(withdrawal.getId().longValue());
					orderMsgDO.setBizType(2);
					orderMsgDOList.add(orderMsgDO);
					asyncDynamicOrderSettlementServiceImpl.sendMessage(orderMsgDOList);
				}
			});
		}*/

		return ResultPista.data("success");
	}

	/**
	 * 提现汇总（总提现/今日提现/待处理）
	 */
	@Override
	public WithdrawalSummaryResp withdrawalSummary(Long userId) {
		WithdrawalSummaryResp resp = new WithdrawalSummaryResp();
		BigDecimal totalWithdrawal = withdrawalService.lambdaQuery()
			.eq(Withdrawal::getUserId, userId)
			.eq(Withdrawal::getStatus, ConstantType.withdrawal_status.type_3)
			.select(Withdrawal::getChangeBalance)
			.list().stream()
			.map(Withdrawal::getChangeBalance)
			.reduce(BigDecimal.ZERO, BigDecimal::add);

		resp.setTotalWithdrawal(totalWithdrawal);

		BigDecimal todayWithdrawal = withdrawalService.lambdaQuery()
			.eq(Withdrawal::getUserId, userId)
			//.eq(Withdrawal::getChainId, Integer.valueOf(DateUtil.format(DateUtil.date(), "yyyyMMdd")))
			.eq(Withdrawal::getStatus, ConstantType.withdrawal_status.type_3)
			.select(Withdrawal::getChangeBalance)
			.list().stream()
			.map(Withdrawal::getChangeBalance)
			.reduce(BigDecimal.ZERO, BigDecimal::add);
		resp.setTodayWithdrawal(todayWithdrawal);

		BigDecimal pendingAmount = withdrawalService.lambdaQuery()
			.eq(Withdrawal::getUserId, userId)
			.in(Withdrawal::getStatus, ConstantType.withdrawal_status.type_0,ConstantType.withdrawal_status.type_1)
			.select(Withdrawal::getChangeBalance)
			.list().stream()
			.map(Withdrawal::getChangeBalance)
			.reduce(BigDecimal.ZERO, BigDecimal::add);
		resp.setPendingAmount(pendingAmount);
		return resp;
	}
}
