package com.xms.app.controller;

import com.xms.app.entity.resp.CreateStakeHostingOrderResp;
import com.xms.app.entity.vo.CreateStakeHostingOrderVo;
import com.xms.app.entity.vo.PledgeStakeHostingAfiVo;
import com.xms.app.service.BizStakeHostingService;
import com.xms.common.annotation.RepeatSubmit;
import com.xms.common.core.domain.api.ResultPista;
import com.xms.common.utils.SecurityUtils;
import com.xms.dao.domain.StakeHostingAfiAccelerateConfig;
import com.xms.dao.domain.StakeHostingAfiPledge;
import com.xms.dao.domain.StakeHostingOrder;
import com.xms.dao.domain.StakeHostingPackage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 托管相关前端控制器
 */
@Api(tags = "托管相关")
@RestController
@RequestMapping("/api/stakeHosting")
public class BizStakeHostingController {
	private final BizStakeHostingService bizStakeHostingService;

	public BizStakeHostingController(BizStakeHostingService bizStakeHostingService) {
		this.bizStakeHostingService = bizStakeHostingService;
	}

	@ApiOperation(value = "托管套餐列表")
	@GetMapping("/packageList")
	public ResultPista<List<StakeHostingPackage>> packageList() {
		return ResultPista.data(bizStakeHostingService.packageList());
	}

	@ApiOperation(value = "创建托管订单")
	@PostMapping("/createOrder")
	@RepeatSubmit
	public ResultPista<CreateStakeHostingOrderResp> createOrder(@Valid @RequestBody CreateStakeHostingOrderVo req) {
		return bizStakeHostingService.createOrder(req, SecurityUtils.getLoginAppUser().getUserId());
	}

	@ApiOperation(value = "我的托管订单")
	@GetMapping("/orderList")
	public ResultPista<List<StakeHostingOrder>> orderList(Long lastId, Integer status) {
		return ResultPista.data(bizStakeHostingService.orderList(lastId, status));
	}

	@ApiOperation(value = "可加速托管订单")
	@GetMapping("/accelerateOrderList")
	public ResultPista<List<StakeHostingOrder>> accelerateOrderList(Long lastId) {
		return ResultPista.data(bizStakeHostingService.accelerateOrderList(lastId));
	}

	@ApiOperation(value = "AFI质押加速配置套餐")
	@GetMapping("/afiAccelerateConfigList")
	public ResultPista<List<StakeHostingAfiAccelerateConfig>> afiAccelerateConfigList() {
		return ResultPista.data(bizStakeHostingService.afiAccelerateConfigList());
	}

	@ApiOperation(value = "托管订单详情")
	@GetMapping("/orderDetail/{id}")
	public ResultPista<StakeHostingOrder> orderDetail(@PathVariable("id") Long id) {
		return ResultPista.data(bizStakeHostingService.orderDetail(id));
	}

	@ApiOperation(value = "提交AFI质押加速")
	@PostMapping("/pledgeAfi")
	@RepeatSubmit
	public ResultPista<StakeHostingAfiPledge> pledgeAfi(@Valid @RequestBody PledgeStakeHostingAfiVo req) {
		return bizStakeHostingService.pledgeAfi(req);
	}
}
