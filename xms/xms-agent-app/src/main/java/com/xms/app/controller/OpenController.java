package com.xms.app.controller;

import com.xms.app.entity.bo.*;
import com.xms.app.entity.req.NodePackageReq;
import com.xms.app.entity.req.SwapOrderCallbackReq;
import com.xms.app.service.*;
import com.xms.common.annotation.Anonymous;
import com.xms.common.core.domain.api.ResultPista;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 回调相关
 *
 *
 * @since 2023-06-12
 */
@Api(tags = "回调相关")
@RestController
@RequestMapping("/api")
public class OpenController {

	@Autowired
	private BizWithdrawalService bizWithdrawalService;


	@Autowired
	private BizStakeService bizStakeService;

	@Autowired
	private BizNodeService bizNodeService;

	@Autowired
	private BizStakeHostingService bizStakeHostingService;

	@Autowired
	private BizRechargeService  bizRechargeService;

//	/**
//	 * df资产划转
//	 * 从旧系统的df资产划转到本系统锁定usdt资产
//	 * @param req df划转请求参数
//	 * @return
//	 */
//	@PostMapping("/notify/dfTransfer")
//	@Anonymous
//	public ResultPista<String> dfTransfer(@Validated @RequestBody DfTransferBo req) {
//		return bizRechargeService.dfTransfer(req);
//	}

	/**
	 * 充值回调
	 */
	@PostMapping("/notify/recharge")
	@Anonymous
	public ResultPista<String> rechargeCallback(@Validated @RequestBody DestroyCallbackBo req) {
		return bizRechargeService.rechargeCallback(req);
	}



//
//	/**
//	 * swap订单回调(链上进行swap的时候进行回调)
//	 */
//	@PostMapping("/notify/swapOrder")
//	@Anonymous
//	public ResultPista<String> swapOrderCallback(@Validated @RequestBody SwapOrderCallbackReq req) {
//		return bizMiningService.swapOrderCallback(req);
//	}
//
//	/**
//	 * 用户支付成功 创建激活码订单，回调接口(支付激活币)
//	 */
//	@PostMapping("/activeOrder/callback")
//	@Anonymous
//	public ResultPista<String> activeOrderCallback(@Validated @RequestBody DestroyCallbackBo req) {
//		return bizCardService.activeOrderCallback(req);
//	}
//
//	/**
//	 * 领取空投回调
//	 */
//	@PostMapping("/claimAirdrop/callback")
//	@Anonymous
//	public ResultPista<String> claimAirdropCallback(@Validated @RequestBody DestroyCallbackBo req) {
//		return bizCardService.claimAirdropCallback(req);
//	}

	/**
	 * 节点订单回调
	 */
	@PostMapping("/nodeOrder/callback")
	@Anonymous
	public ResultPista<String> nodeOrderCallback(@Validated @RequestBody StakeOrderBo req) {
		return bizNodeService.nodeOrderCallback(req);
	}

	/**
	 * 托管订单回调
	 */
	@PostMapping("/stakeHostingOrder/callback")
	@Anonymous
	public ResultPista<String> stakeHostingOrderCallback(@Validated @RequestBody StakeOrderBo req) {
		return bizStakeHostingService.orderCallback(req);
	}
//
//	/**
//	 * 质押订单回调
//	 */
//	@PostMapping("/stakeOrder/callback")
//	@Anonymous
//	public ResultPista<String> stakeOrderCallback(@Validated @RequestBody StakeOrderBo req) {
//		return bizStakeService.stakeOrderCallback(req);
//	}

	/**
	 * 提现回调
	 */
	@PostMapping("/withdrawal/callback")
	@Anonymous
	public ResultPista<String> withdrawalCallback(@Validated @RequestBody WithdrawalCallbackBo req) {
		return bizWithdrawalService.withdrawalCallback(req);
	}

}
