//package com.xms.app.controller;
//
//
//import cn.hutool.json.JSONUtil;
//import com.alibaba.fastjson.JSONObject;
//import com.uduncloud.sdk.domain.Trade;
//import com.xms.dao.domain.UserRechangeAddress;
//import com.xms.dao.service.WithdrawalService;
//import com.xms.common.annotation.Anonymous;
//import com.xms.common.config.UdunConfig;
//import com.xms.common.constant.ConstantStatic;
//import com.xms.common.constant.ConstantSys;
//import com.xms.common.constant.ConstantType;
//import com.xms.common.constant.SysConstant;
//import com.xms.common.exception.ServiceException;
//import com.xms.common.result.ResponseCode;
//import com.xms.common.utils.sign.Md5Utils;
//import com.xms.common.utils.spring.SpringUtils;
//import com.xms.common.utils.uuid.IDUtils;
//import com.xms.dao.domain.RechargeRecord;
//import com.xms.dao.domain.WalletTransferOrder;
//import com.xms.dao.entity.domain.UserInfo;
//import com.xms.dao.entity.domain.UserMoney;
//import com.xms.dao.entity.domain.Withdrawal;
//import com.xms.dao.service.*;
//import jakarta.servlet.http.HttpServletRequest;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.lang3.StringUtils;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.transaction.annotation.Transactional;
//import org.springframework.transaction.interceptor.TransactionAspectSupport;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.math.BigDecimal;
//import java.util.Date;
//
///**
// * udun 控制器
// */
//@RestController
//@RequestMapping("/udun")
//@Slf4j
//public class UdunController {
//
//	private final String uniqueIPrefix = "wdwByUdun";
//	@Autowired
//	private IRechargeRecordService rechargeService;
//
//	@Autowired
//	private WithdrawalService withdrawalService;
//
//	@Autowired
//	private IWalletTransferOrderService walletTransferOrderService;
//
//	@Autowired
//	private UdunConfig udunConfig;
//
//	@Autowired
//	private  UserWalletService userWalletServiceImpl;
//
//	@Autowired
//	private UserInfoService userInfoServiceImpl;
//
//	@Autowired
//	private IUserRechangeAddressService userRechargeAddressService;
//
//	/**
//	 * U盾回调地址
//	 *
//	 * @param request
//	 * @param timestamp
//	 * @param nonce
//	 * @param sign
//	 * @param body
//	 */
//	@PostMapping("/callback")
//	@Anonymous
//	public void callback(HttpServletRequest request, String timestamp, String nonce, String sign, String body) throws Exception {
//		log.info("U盾 - callack, {}, {}, {}, {}", timestamp, nonce, sign, body);
//
//		if (StringUtils.isNotBlank(body)) {
//			try {
//				String key = udunConfig.getKey();
//				String s = body + key + nonce + timestamp;
//				if (StringUtils.isNotBlank(s)) {
//					String md = Md5Utils.hash(s);
//					log.info("验签加密后：{}", md);
//					if (sign.equalsIgnoreCase(md)) {
//						log.info("验签成功");
//					} else {
//						log.info("验签失败");
//						return;
//					}
//				} else {
//					return;
//				}
//			} catch (Exception e) {
//				log.error("验签失败", e);
//				throw e;
//			}
//			Trade trade = JSONObject.toJavaObject(JSONObject.parseObject(body), Trade.class);
//			log.info("U盾 - callack, trade:{}", trade);
//			//插入表结构，分为两种类型，充币 1，提币 2 tradeType
//			if (trade.getTradeType() == 1) {
//				//充值
//				SpringUtils.getBean(UdunController.class).addRecharge(trade);
//			} else if (trade.getTradeType() == 2) {
//				//转账
//				log.info("转账：{}", trade.getBusinessId());
//				Withdrawal withdrawal = withdrawalService.lambdaQuery().eq(Withdrawal::getCode, trade.getBusinessId()).one();
//				if (withdrawal == null) {
//					log.warn("转账不存在：{}", trade.getBusinessId());
//					return;
//				}
//				if (withdrawal.getStatus() != 1) {
//					log.warn("该笔转账状态不和谐哟：{}", trade.getBusinessId());
//					return;
//				}
//				//都是从5 变更为其他状态,  2 3 4 是终态, 1 是中态
//				if (trade.getStatus() == 1) {//成功
//					log.info("不处理的 审核成功：{}", trade.getBusinessId());
//				} else if (trade.getStatus() == 2) { //失败
//					log.info("审核驳回：{}", trade.getBusinessId());
//					boolean update = withdrawalService.lambdaUpdate()
//						.eq(Withdrawal::getCode, withdrawal.getCode())
//						.eq(Withdrawal::getStatus, SysConstant.ONE)
//						.set(Withdrawal::getStatus, SysConstant.FOUR)
//						.set(Withdrawal::getUpdateTime, new Date())
//						.update();
//					if(!update){
//						throw new ServiceException("更新U盾提现审核状态失败");
//					}
//
//
//					int i = userWalletServiceImpl.handerUserMoney(withdrawal.getChangeBalance(), withdrawal.getCode(), withdrawal.getUserId(), withdrawal.getUserId()
//						, ConstantType.user_money_log_source_type.type_5, withdrawal.getCoinType());
//					if (i != 1) {
//						throw new ServiceException("更新钱包失败");
//					}
//
//					if(withdrawal.getFeeBalance().compareTo(BigDecimal.ZERO)>0){
//						//退还提现手续费
//						i = userWalletServiceImpl.handerUserMoney(withdrawal.getFeeBalance(), withdrawal.getCode(), withdrawal.getUserId(), withdrawal.getUserId()
//							, ConstantType.user_money_log_source_type.type_4, withdrawal.getCoinType());
//						if (i != 1) {
//							throw new ServiceException("更新钱包失败");
//						}
//					}
//				} else if (trade.getStatus() == 3) { //交易成功
//					boolean update = withdrawalService.lambdaUpdate()
//						.eq(Withdrawal::getCode, withdrawal.getCode())
//						.eq(Withdrawal::getStatus, SysConstant.ONE)
//						.set(Withdrawal::getStatus, SysConstant.FIVE)
//						.set(Withdrawal::getUpdateTime, new Date())
//						.update();
//					if(!update){
//						throw new ServiceException("更新U盾提现审核状态失败");
//					}
//
//				} else if (trade.getStatus() == 4) { //交易失败
//					log.info("交易失败：{}", trade.getBusinessId());
//					boolean update = withdrawalService.lambdaUpdate()
//						.eq(Withdrawal::getCode, withdrawal.getCode())
//						.eq(Withdrawal::getStatus, SysConstant.ONE)
//						.set(Withdrawal::getStatus, SysConstant.SIX)
//						.set(Withdrawal::getUpdateTime, new Date())
//						.update();
//					if(!update){
//						throw new ServiceException("更新U盾提现审核状态失败");
//					}
//
//
//					int i = userWalletServiceImpl.handerUserMoney(withdrawal.getChangeBalance(), withdrawal.getCode(), withdrawal.getUserId(), withdrawal.getUserId()
//						, ConstantType.user_money_log_source_type.type_5, withdrawal.getCoinType());
//					if (i != 1) {
//						throw new ServiceException("更新钱包失败");
//					}
//
//					if(withdrawal.getFeeBalance().compareTo(BigDecimal.ZERO)>0){
//						//退还提现手续费
//						i = userWalletServiceImpl.handerUserMoney(withdrawal.getFeeBalance(), withdrawal.getCode(), withdrawal.getUserId(), withdrawal.getUserId()
//							, ConstantType.user_money_log_source_type.type_4, withdrawal.getCoinType());
//						if (i != 1) {
//							throw new ServiceException("更新钱包失败");
//						}
//					}
//				}
//			}
//		} else {
//			throw new ServiceException("body is null");
//		}
//	}
//
//	@Transactional(rollbackFor = Exception.class)
//	public void addRecharge(Trade trade) {
//		Integer chaiId;
//		if(trade.getMainCoinType().equals(udunConfig.getMainBepCoinType())){
//			chaiId = 2510;
//			if(!trade.getCoinType().equals(udunConfig.getUSDTBEP20())){
//				//只能充值trx上面的usdt
//				log.info("不支持的币种类型->trade:{}", JSONUtil.toJsonStr(trade));
//				throw new ServiceException("不支持的币种类型");
//			}
//		}else{
//			log.info("不支持的链类型->trade:{}", JSONUtil.toJsonStr(trade));
//			throw new ServiceException("不支持的币种类型");
//		}
//		UserRechangeAddress rechangeAddress = userRechargeAddressService.lambdaQuery()
//			.eq(UserRechangeAddress::getAddress, trade.getAddress())
//			.eq(UserRechangeAddress::getChainId,chaiId)
//			.last("limit 1")
//			.one();
//		if(rechangeAddress == null){
//			log.info("用户不存在->trade:{}", JSONUtil.toJsonStr(trade));
//			return;
//		}
//
//			//查询交易hash是否存在
//			RechargeRecord queryRecord = rechargeService.lambdaQuery()
//				.eq(RechargeRecord::getTxId, trade.getTxId())
//				.one();
//			if (queryRecord != null) {
//				log.info("交易hash已存在->trade:{}", JSONUtil.toJsonStr(trade));
//				return;
//			}
//
//			BigDecimal amount = trade.getAmount().divide(BigDecimal.valueOf(10).pow(trade.getDecimals()),
//				ConstantStatic.newScale, ConstantStatic.roundingModeNew);
//			log.info("充值实际金额 amount:{}",amount);
//			amount =amount.setScale(ConstantStatic.newScale,ConstantStatic.roundingModeNew);
//			log.info("充值金额截取小数点之后 amount:{}",amount);
//			if(amount.compareTo(BigDecimal.ZERO)<=0){
//				log.info("异常充值金额 amount:{}",amount);
//				throw new ServiceException("u盾充值金额错误");
//			}
//
//			RechargeRecord rechargeRecord = RechargeRecord.builder()
//				.userId(rechangeAddress.getUserId())
//				.orderNo(IDUtils.getSnowflake().nextIdStr())
//				.txId(trade.getTxId())
//				.status(SysConstant.ONE)
//				.rechargeAmount(amount.setScale(ConstantStatic.newScale,ConstantStatic.roundingModeNew))
//				.build();
//			boolean save = rechargeService.save(rechargeRecord);
//			if (!save) {
//				throw new ServiceException(ResponseCode.CODE_1002);
//			}
//
//			//到账USDT
//			int i = userWalletServiceImpl.handerUserMoney(rechargeRecord.getRechargeAmount(), rechargeRecord.getOrderNo(), rechargeRecord.getUserId(),
//				rechargeRecord.getUserId(), ConstantType.user_money_log_source_type.type_1,ConstantType.user_money_coin_type.type_1);
//			if (i != 1) {
//				TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
//				throw new ServiceException(ResponseCode.CODE_1002);
//			}
//	}
//
//
//}
//
