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
	private BizNodeService bizNodeService;

	@Autowired
	private BizStakeHostingService bizStakeHostingService;

	@Autowired
	private BizRechargeService  bizRechargeService;


	/**
	 * 充值回调
	 * @param req 请求参数
	 * @return
	 */
	@PostMapping("/afiOrder/callback")
	@Anonymous
	public ResultPista<String> rechargeCallback(@Validated @RequestBody DestroyCallbackBo req) {
		return bizRechargeService.rechargeCallback(req);
	}


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
	@PostMapping("/pledgeOrder/callback")
	@Anonymous
	public ResultPista<String> stakeHostingOrderCallback(@Validated @RequestBody StakeOrderBo req) {
		return bizStakeHostingService.orderCallback(req);
	}

	/**
	 * 提现回调
	 */
	@PostMapping("/withdrawal/callback")
	@Anonymous
	public ResultPista<String> withdrawalCallback(@Validated @RequestBody WithdrawalCallbackBo req) {
		return bizWithdrawalService.withdrawalCallback(req);
	}

}
