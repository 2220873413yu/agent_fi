package com.xms.app.controller;

import com.xms.app.entity.dto.StakeHostingAfiAccelerateConfigDto;
import com.xms.app.entity.dto.StakeHostingAfiPledgeDto;
import com.xms.app.entity.dto.StakeHostingOrderDto;
import com.xms.app.entity.resp.CreateStakeHostingOrderResp;
import com.xms.app.entity.dto.StakeHostingPackageDto;
import com.xms.app.entity.vo.CreateStakeHostingOrderVo;
import com.xms.app.entity.vo.PledgeStakeHostingAfiVo;
import com.xms.app.service.BizStakeHostingService;
import com.xms.common.annotation.RepeatSubmit;
import com.xms.common.core.domain.api.ResultPista;
import com.xms.common.utils.SecurityUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * App托管相关接口控制器。
 *
 * <p>App Controller 对外返回 DTO 或响应对象，避免直接暴露数据库实体。</p>
 */
@Api(tags = "托管相关")
@RestController
@RequestMapping("/api/stakeHosting")
public class BizStakeHostingController {
	private final BizStakeHostingService bizStakeHostingService;

	public BizStakeHostingController(BizStakeHostingService bizStakeHostingService) {
		this.bizStakeHostingService = bizStakeHostingService;
	}

	/**
	 * 查询托管套餐列表。
	 *
	 * 返回已上架托管套餐，用于 App 展示套餐天数、最低起购金额、服务费比例和业绩积分系数。
	 *
	 * @return 托管套餐展示列表
	 */
	@ApiOperation(value = "托管套餐列表")
	@GetMapping("/packageList")
	public ResultPista<List<StakeHostingPackageDto>> packageList() {
		return ResultPista.data(bizStakeHostingService.packageList());
	}

	/**
	 * 创建托管订单。
	 *
	 * 根据当前登录用户、选择的托管套餐和托管金额创建待支付订单；请求参数包含钱包签名校验信息。
	 *
	 * @param req 创建托管订单请求，包含套餐ID、托管金额、随机数和钱包签名
	 * @return 创建成功后的托管订单号和托管金额
	 */
	@ApiOperation(value = "创建托管订单")
	@PostMapping("/createOrder")
	@RepeatSubmit
	public ResultPista<CreateStakeHostingOrderResp> createOrder(@Valid @RequestBody CreateStakeHostingOrderVo req) {
		return bizStakeHostingService.createOrder(req, SecurityUtils.getLoginAppUser().getUserId());
	}

	/**
	 * 查询我的托管订单列表。
	 *
	 * 按当前登录用户查询托管订单，支持根据业务状态过滤和基于 lastId 的向下翻页。
	 *
	 * @param lastId 上一页最后一条订单ID，空表示第一页
	 * @param status 业务状态，空表示全部状态；0未开始，1产出中，2已完成，3已暂停
	 * @return 当前登录用户托管订单展示列表
	 */
	@ApiOperation(value = "我的托管订单")
	@GetMapping("/orderList")
	public ResultPista<List<StakeHostingOrderDto>> orderList(Long lastId, Integer status) {
		return ResultPista.data(bizStakeHostingService.orderList(lastId, status));
	}
//
//	@ApiOperation(value = "可加速托管订单")
//	@GetMapping("/accelerateOrderList")
//	public ResultPista<List<StakeHostingOrderDto>> accelerateOrderList(Long lastId) {
//		return ResultPista.data(bizStakeHostingService.accelerateOrderList(lastId));
//	}

	/**
	 * 查询 AFI 质押加速配置套餐。
	 *
	 * 返回已启用的 AFI 质押比例和加速倍率，用于 App 展示可选加速档位。
	 *
	 * @return AFI 质押加速配置展示列表
	 */
	@ApiOperation(value = "AFI质押加速配置套餐")
	@GetMapping("/afiAccelerateConfigList")
	public ResultPista<List<StakeHostingAfiAccelerateConfigDto>> afiAccelerateConfigList() {
		return ResultPista.data(bizStakeHostingService.afiAccelerateConfigList());
	}

	/**
	 * 查询托管订单详情。
	 *
	 * 只能查询当前登录用户自己的托管订单，返回 App 订单详情展示字段。
	 *
	 * @param id 托管订单ID
	 * @return 托管订单详情展示对象
	 */
	@ApiOperation(value = "托管订单详情")
	@GetMapping("/orderDetail/{id}")
	public ResultPista<StakeHostingOrderDto> orderDetail(@PathVariable("id") Long id) {
		return ResultPista.data(bizStakeHostingService.orderDetail(id));
	}

	/**
	 * 提交 AFI 质押加速。
	 *
	 * 当前登录用户对指定托管订单提交 AFI 质押加速；请求参数包含托管订单ID、AFI加速配置ID和钱包签名校验信息。
	 *
	 * @param req AFI 质押加速请求，包含托管订单ID、AFI加速配置ID、随机数和钱包签名
	 * @return AFI 质押加速记录
	 */
	@ApiOperation(value = "提交AFI质押加速")
	@PostMapping("/pledgeAfi")
	@RepeatSubmit
	public ResultPista<StakeHostingAfiPledgeDto> pledgeAfi(@Valid @RequestBody PledgeStakeHostingAfiVo req) {
		return bizStakeHostingService.pledgeAfi(req);
	}
}
