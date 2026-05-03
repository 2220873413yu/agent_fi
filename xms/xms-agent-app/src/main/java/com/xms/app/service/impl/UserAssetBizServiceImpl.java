//package com.xms.app.service.impl;
//
//import cn.hutool.system.SystemUtil;
//import com.xms.app.entity.OrderReq;
//import com.xms.app.handler.CustomException;
//import com.xms.app.service.RechargeService;
//import com.xms.app.service.UserAssetBizService;
//import com.xms.dao.service.UserInfoService;
//import com.xms.common.config.redis.XmsRedis;
//import com.xms.common.constant.ConstantStatic;
//import com.xms.common.constant.ConstantType;
//import com.xms.common.constant.SysConstant;
//import com.xms.common.core.domain.api.ResultPista;
//import com.xms.common.exception.ServiceException;
//import com.xms.common.result.ResponseCode;
//import com.xms.common.utils.Func;
//import com.xms.common.utils.Kv;
//import com.xms.common.utils.MetaMaskUtil;
//import com.xms.common.utils.SecurityUtils;
//import com.xms.common.utils.uuid.IDUtils;
//import com.xms.dao.domain.TokenContractConfig;
//import com.xms.dao.entity.domain.Recharge;
//import com.xms.dao.entity.domain.UserInfo;
//import com.xms.dao.service.ITokenContractConfigService;
//import com.xms.dao.service.XmsCommonService;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import java.math.BigDecimal;
//import java.util.List;
//
///**
// * 用户资产实现类
// */
//@Service
//@Slf4j
//public class UserAssetBizServiceImpl implements UserAssetBizService {
//
//
//	@Autowired
//	private UserInfoService userInfoServiceImpl;
//
//	@Autowired
//	private RechargeService rechargeServiceImpl;
//	@Autowired
//	private XmsRedis xmsRedis;
//	@Autowired
//	private ITokenContractConfigService contractConfigServiceImpl;
//
//	@Autowired
//	private XmsCommonService xmsCommonServiceImpl;
//
//
//	static void checkWallet(String randomNum, String signature, String address, XmsRedis xmsRedis) {
//		String osName = SystemUtil.getOsInfo().getName();
//		if (osName.toUpperCase().contains(SysConstant.OS_NAME_WINDOWS)) {
//			return;
//		}
//		if (!xmsRedis.hasKey(ConstantStatic.USER_RANDOM + address + randomNum)) {
//			throw new CustomException(ResponseCode.RANDOM_NOT_EXIT);
//		}
//		boolean validate = MetaMaskUtil.validate(signature, randomNum, address);
//		if (!validate) {
//			throw new CustomException(ResponseCode.SIGN_VALIDATE_ERROR);
//		}
//		xmsRedis.del(ConstantStatic.USER_RANDOM + address + randomNum);
//	}
//
//	@Override
//	public ResultPista<List<Recharge>> listRechargeRecord(Long lastId, Integer coinType) {
//		List<Recharge> list = rechargeServiceImpl.lambdaQuery().eq(Recharge::getUserId, SecurityUtils.getFrontUserId())
//			.lt(Func.isNotEmpty(lastId), Recharge::getId, lastId)
//			.eq(Func.isNotEmpty(coinType), Recharge::getCoinType, coinType)
//			.orderByDesc(Recharge::getId).last(SysConstant.PAGE_LIMIT)
//			.list();
//		if (!list.isEmpty()) {
//			List<Integer> res = list.stream().map(Recharge::getCoinType).toList();
//			List<TokenContractConfig> configs = contractConfigServiceImpl.lambdaQuery().in(TokenContractConfig::getId, res).list();
//			for (Recharge recharge : list) {
//				recharge.setRemark("USDT");
//				for (TokenContractConfig config : configs) {
//					if (config.getId().equals((long) recharge.getCoinType())) {
//						recharge.setRemark(config.getName());
//						break;
//					}
//				}
//			}
//		}
//		return ResultPista.data(list);
//	}
//
//	@Override
//	public ResultPista rechargeAction(OrderReq req) {
//		ResultPista resultPista = xmsCommonServiceImpl.checkMineSettleTime();
//		if (!resultPista.isSuccess()) {
//			throw new ServiceException(resultPista.getMsg());
//		}
//		UserInfo userInfo = userInfoServiceImpl.getById(SecurityUtils.getFrontUserId());
//		checkWallet(req.getRandomNum(), req.getSignature(), userInfo.getRegAddress(), xmsRedis);
//		BigDecimal rate = BigDecimal.ONE;
//		Integer CoinType =req.getBizType();
//		if (req.getBizType() > 1) {
//			TokenContractConfig config = contractConfigServiceImpl.getById(req.getBizType());
//			if (config == null || config.getDeleted().equals(SysConstant.ONE)) {
//				throw new ServiceException(ResponseCode.CODE_1016);
//			}
//			rate = config.getExchangeRate();
//		}
//
//		//coinType ==0 特殊处理 充值ALEO
//		if(CoinType==SysConstant.ZERO){
//			CoinType = ConstantType.user_money_coin_type.type_3;
//		}
//		Recharge rechargeOrder = Recharge.builder()
//			.userId(SecurityUtils.getFrontUserId())
//			.fromAddress(userInfo.getRegAddress())
//			//币种类型 1=u,0=ALEO
//			.coinType(CoinType)
//			.orderNo(IDUtils.getSnowflakeStr())
//			.status(SysConstant.ZERO)
//			.rate(rate)
//			.cnt(new BigDecimal(req.getAmount()))
//			.build();
//		boolean res = rechargeServiceImpl.save(rechargeOrder);
//		if (res) {
//			return ResultPista.data(Kv.create().set("orderNo", rechargeOrder.getOrderNo()).set("bizType", req.getBizType()).set("amount", req.getAmount()));
//		}
//		return ResultPista.fail();
//	}
//
//	@Override
//	public ResultPista<BigDecimal> getAleoPrice() {
//		return null;
//	}
//}
