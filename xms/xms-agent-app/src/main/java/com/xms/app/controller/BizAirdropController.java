//package com.xms.app.controller;
//
//import com.xms.app.entity.req.*;
//import com.xms.app.entity.resp.BuyNodePlanResp;
//import com.xms.app.entity.vo.NodePlanVo;
//import com.xms.app.service.BizCardService;
//import com.xms.common.annotation.RepeatSubmit;
//import com.xms.common.core.domain.api.ResultPista;
//import com.xms.common.utils.SecurityUtils;
//import com.xms.dao.service.XmsCommonService;
//import io.swagger.annotations.Api;
//import io.swagger.annotations.ApiOperation;
//import jakarta.validation.Valid;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
///**
// * 节点相关业务
// *
// *
// * @since 2023-06-12
// */
//@Api(tags = "节点相关业务")
//@RestController
//@RequestMapping("/api/")
//public class BizAirdropController {
//
//	@Autowired
//	private BizCardService bizCardService;
//
//	@Autowired
//	private XmsCommonService xmsCommonServiceImpl;
//
//	/**
//	 * 获取节点信息
//	 *
//	 * @return 节点对象
//	 */
//	@ApiOperation(value = "获取节点信息")
//	@GetMapping(value = "/nodePlanInfo")
//	public ResultPista<List<NodePlanVo>> nodePlanInfo()  throws Exception{
//		return bizCardService.nodePlanInfo();
//	}
//
//	/**
//	 * 购买节点
//	 *
//	 * @return 购买节点
//	 */
//	@ApiOperation(value = "购买节点")
//	@PostMapping(value = "/buyNodePlan")
//	@RepeatSubmit
//	public ResultPista<BuyNodePlanResp> buyNodePlan(@Valid @RequestBody  BuyNodePlanReq req)  throws Exception{
//		return bizCardService.buyNodePlan(req, SecurityUtils.getLoginAppUser().getUserId());
//	}
//
////	/**
////	 * 创建激活订单(只有未激活的用户才能创建)
////	 *
////	 * @return 随机数
////	 */
////	@ApiOperation(value = "创建激活订单")
////	@GetMapping(value = "/createActiveOrder")
////	@RateLimiter(limitType = LimitType.IP, time = 5, count = 1)
////	public ResultPista<CreateActiveOrderResp> createActiveOrder()  throws Exception{
////		return bizCardService.createActiveOrder();
////	}
////
////
////	/**
////	 * 获取空投页面显示数据
////	 *
////	 * @return 随机数
////	 */
////	@ApiOperation(value = "获取空投页面显示数据")
////	@GetMapping(value = "/getAirdropClaimPageInfo")
////	public ResultPista<AirdropClaimPageInfoVo> getAirdropClaimPageInfo()  throws Exception{
////		return bizCardService.getAirdropClaimPageInfo();
////	}
////
////
////	/**
////	 * 领取空投
////	 *
////	 * @return 随机数
////	 */
////	@ApiOperation(value = "领取空投")
////	@PostMapping(value = "/claimAirdrop")
////	@RateLimiter(limitType = LimitType.IP, time = 5, count = 1)
////	public ResultPista<ClaimAirdropResp> claimAirdrop(@Valid @RequestBody  ClaimAirdropReq req)  throws Exception{
////		return bizCardService.claimAirdrop(req);
////	}
////
////	/**
////	 * 获取领取记录
////	 * @param lastId 没有就不传递
////	 * @return
////	 * @throws Exception
////	 */
////	@ApiOperation(value = "获取领取记录")
////	@GetMapping(value = "/claimRecordList")
////	public ResultPista<List<AirdropClaimRecordVo>> claimRecordList(Long lastId)  throws Exception{
////		return bizCardService.claimRecordList(lastId);
////	}
//
////
////	/**
////	 * 算力记录
////	 *
////	 * @return 随机数
////	 */
////	@ApiOperation(value = "算力记录")
////	@GetMapping(value = "/powerLog")
////	public ResultPista<List<RewardRecord>> powerLog(Long lastId)  throws Exception{
////		return bizCardService.powerLog(lastId);
////	}
////
////
////	/**
////	 * 购买卡片
////	 * @param req 请求参数
////	 * @return
////	 * @throws Exception
////	 */
////	@ApiOperation(value = "购买卡片")
////	@PostMapping(value = "/createCardOrder")
////	public ResultPista createCardOrder(@Valid @RequestBody  CreateCardOrderReq req)  throws Exception{
////		ResultPista resultPista = xmsCommonServiceImpl.checkMineSettleTime(1);
////		if (!resultPista.isSuccess()) {
////			throw new ServiceException(resultPista.getMsg());
////		}
////		return bizCardService.createCardOrder(req);
////	}
////
////
////	/**
////	 * 升级卡片
////	 * @param req 升级参数
////	 * @return
////	 * @throws Exception
////	 */
////	@ApiOperation(value = "升级卡片")
////	@PostMapping(value = "/upgradeCardOrder")
////	public ResultPista upgradeCardOrder(@Valid @RequestBody UpgradeCardOrderReq req)  throws Exception{
////		ResultPista resultPista = xmsCommonServiceImpl.checkMineSettleTime(1);
////		if (!resultPista.isSuccess()) {
////			throw new ServiceException(resultPista.getMsg());
////		}
////		return bizCardService.upgradeCardOrder(req);
////	}
//
////	/**
////	 * 获取销毁信息(当前日化、我的销毁数量)
////	 *
////	 * @return 返回随机数
////	 */
////	@ApiOperation(value = "获取销毁信息(当前日化、我的销毁数量)")
////	@GetMapping(value = "/destroyInfo")
////	public ResultPista<DestroyInfoBo> destroyInfo() {
////		return ResultPista.data(bizMiningService.destroyInfo());
////	}
////
////	/**
////	 * 加速释放配置
////	 * @return
////	 * @throws Exception
////	 */
////	@ApiOperation(value = "加速释放配置")
////	@GetMapping(value = "/releaseConfigList")
////	public ResultPista<List<ReleaseConfigDto>> releaseConfigList()  throws Exception{
////		return bizMiningService.releaseConfigList();
////	}
////
////	/**
////	 * 加速释放
////	 * @return
////	 * @throws Exception
////	 */
////	@ApiOperation(value = "加速释放")
////	@GetMapping(value = "/releaseOrder")
////	public ResultPista releaseOrder(ReleaseOrderReq req)  throws Exception{
////		ResultPista resultPista = xmsCommonServiceImpl.checkMineSettleTime(2);
////		if (!resultPista.isSuccess()) {
////			throw new ServiceException(resultPista.getMsg());
////		}
////		return bizMiningService.releaseOrder(req, SecurityUtils.getLoginAppUser().getUserId());
////	}
//
///*	*//**
//	 * 销毁 以u为单位
//	 * @return
//	 *//*
//	@ApiOperation(value = "销毁")
//	@PostMapping(value = "/test1")
//	@RepeatSubmit
//	public ResultPista test1()  throws Exception{
//		for (int i = 0; i < 100; i++) {
//			ThreadUtil.sleep(200, TimeUnit.MICROSECONDS);
//			CreateDestroyOrderVo req = new CreateDestroyOrderVo();
//			//随机100-500之间不能有效数
//			req.setDestroyAmount(new BigDecimal(Math.random() * 500 + 100));
//			ResultPista<CreateOrderResp> order = bizMiningService.createOrder(req);
//			DestroyCallbackBo callbackBo = new DestroyCallbackBo();
//			//生成随机hash
//			callbackBo.setHash(UUID.randomUUID().toString().replace("-", ""));
//			callbackBo.setSign("sss");
//			callbackBo.setOrderNo(order.getData().getOrderNo());
//			callbackBo.setAmount(order.getData().getUsdtValue());
//			SpringUtils.getBean(BizMiningServiceImpl.class).destroyCallback(callbackBo);
//		}
//		return ResultPista.success();
//	}*/
//
////	/**
////	 * 销毁 以u为单位
////	 * @return
////	 */
////	@ApiOperation(value = "销毁")
////	@PostMapping(value = "/destroyOrder")
////	@RepeatSubmit
////	public ResultPista<CreateOrderResp> createDestroyOrder(@Valid @RequestBody CreateDestroyOrderVo req)  throws Exception{
////		ResultPista resultPista = xmsCommonServiceImpl.checkMineSettleTime(1);
////		if (!resultPista.isSuccess()) {
////			throw new ServiceException(resultPista.getMsg());
////		}
////		return bizMiningService.createOrder(req);
////	}
////
////	/**
////	 * 销毁记录
////	 * @param pageIndex 当前页 默认1
////	 * @param pageSize 每页长度 默认20(最大20)
////	 * @return
////	 * @throws Exception
////	 */
////	@ApiOperation(value = "销毁记录")
////	@GetMapping(value = "/destroyOrderList")
////	public ResultPista<PageInfo<DestroyOrderDto>> destroyOrderList(@ApiParam(value = "当前页", required = true) @NotNull @RequestParam(defaultValue = "1") Integer pageIndex,
////																   @ApiParam(value = "每页长度", required = true) @NotNull @RequestParam(defaultValue = "20") Integer pageSize)  throws Exception{
////		return ResultPista.data(bizMiningService.destroyOrderList(pageIndex,pageSize));
////	}
////
////
////	/**
////	 * 我的利息包
////	 * @param pageIndex 当前页 默认1
////	 * @param pageSize 每页长度 默认20(最大20)
////	 * @return
////	 * @throws Exception
////	 */
////	@ApiOperation(value = "我的利息包")
////	@GetMapping(value = "/interestPacks")
////	public ResultPista<PageInfo<InterestPackDto>> getMyInterestPacks(@ApiParam(value = "当前页", required = true) @NotNull @RequestParam(defaultValue = "1") Integer pageIndex,
////																 @ApiParam(value = "每页长度", required = true) @NotNull @RequestParam(defaultValue = "20") Integer pageSize)  throws Exception{
////		return ResultPista.data(bizMiningService.getMyInterestPacks(pageIndex,pageSize));
////	}
////
////
////
////	/**
////	 * 每日释放利息记录
////	 * @param pageIndex 当前页 默认1
////	 * @param pageSize 每页长度 默认20(最大20)
////	 * @return
////	 * @throws Exception
////	 */
////	@ApiOperation(value = "每日释放利息记录")
////	@GetMapping(value = "/todayInterest")
////	public ResultPista<PageInfo<InterestStatDayDto>> todayInterest(@ApiParam(value = "当前页", required = true) @NotNull @RequestParam(defaultValue = "1") Integer pageIndex,
////															   @ApiParam(value = "每页长度", required = true) @NotNull @RequestParam(defaultValue = "20") Integer pageSize)  throws Exception{
////		return ResultPista.data(bizMiningService.todayInterest(pageIndex,pageSize));
////	}
//}
