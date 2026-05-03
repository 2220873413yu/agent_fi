//package com.xms.app.controller;
//
//import com.github.pagehelper.PageInfo;
//import com.xms.app.entity.dto.*;
//import com.xms.app.entity.resp.CreateOrderResp;
//import com.xms.app.entity.resp.CreateStakeOrderResp;
//import com.xms.app.entity.vo.CreateMiningOrderVo;
//import com.xms.app.entity.vo.CreateStakeOrderVo;
//import com.xms.app.entity.vo.StakeMiningOrderVo;
//import com.xms.app.service.BizMiningService;
//import com.xms.app.service.BizStakeService;
//import com.xms.common.annotation.RepeatSubmit;
//import com.xms.common.core.domain.api.ResultPista;
//import com.xms.common.exception.ServiceException;
//import com.xms.common.utils.SecurityUtils;
//import com.xms.dao.entity.dto.DestroyOrderDto;
//import com.xms.dao.service.XmsCommonService;
//import io.swagger.annotations.Api;
//import io.swagger.annotations.ApiOperation;
//import io.swagger.annotations.ApiParam;
//import jakarta.validation.Valid;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
///**
// * 质押相关 前端控制器
// *
// * @since 2023-06-12
// */
//@Api(tags = "质押相关")
//@RestController
//@RequestMapping("/api/stake")
//public class BizStaKeController {
//
//	@Autowired
//	private BizStakeService bizStakeService;
//
//	@Autowired
//	private XmsCommonService xmsCommonServiceImpl;
//
//	/**
//	 * 获取质押信息 没有上架的话可能为空
//	 *
//	 * @return
//	 * @throws Exception
//	 */
//	@ApiOperation(value = "获取质押信息")
//	@GetMapping(value = "/stakeInfo")
//	public ResultPista<StakeInfoDTO> stakeInfo() throws Exception {
//		return ResultPista.data(bizStakeService.stakeInfo());
//	}
//
//	/**
//	 * 商品列表
//	 * @return
//	 * @throws Exception
//	 */
//	@ApiOperation(value = "商品列表")
//	@GetMapping(value = "/diyProductList")
//	public ResultPista<List<DiyProductListDto>> diyProductList() throws Exception {
//		return ResultPista.data(bizStakeService.diyProductList());
//	}
//
//	/**
//	 * 获取商品详情
//	 * @param productId
//	 * @return
//	 * @throws Exception
//	 */
//	@ApiOperation(value = "获取商品详情")
//	@GetMapping(value = "/diyProductDetail")
//	public ResultPista<DiyProductDetailDto> diyProductDetail(Long productId) throws Exception {
//		return ResultPista.data(bizStakeService.diyProductDetail(productId));
//	}
//
//	/**
//	 * 创建质押订单
//	 *
//	 * @return
//	 */
//	@ApiOperation(value = "创建质押订单")
//	@PostMapping(value = "/createStakeOrder")
//	@RepeatSubmit
//	public ResultPista<CreateStakeOrderResp> createStakeOrder(@Valid @RequestBody CreateStakeOrderVo req) throws Exception {
//		ResultPista resultPista = xmsCommonServiceImpl.checkMineSettleTime();
//		if (!resultPista.isSuccess()) {
//			throw new ServiceException(resultPista.getMsg());
//		}
//		return bizStakeService.createStakeOrder(req, SecurityUtils.getLoginAppUser().getUserId());
//	}
//
//
//	/**
//	 * 发货订单列表
//	 * @param bizType (不传递查询所有的) 0:待发货,1:已发货
//	 * @return
//	 * @throws Exception
//	 */
//	@ApiOperation(value = "发货订单列表")
//	@GetMapping(value = "/myProductOrderList")
//	public ResultPista<List<MyProductOrderDto>> myProductOrderList(Integer bizType) throws Exception {
//		return ResultPista.data(bizStakeService.myProductOrderList(bizType));
//	}
//
//	/**
//	 * 质押订单记录列表
//	 *
//	 * @param lastId
//	 * @param status 1:产出中,2:已出局
//	 * @return
//	 * @throws Exception
//	 */
//	@ApiOperation(value = "质押订单记录")
//	@GetMapping(value = "/myStakeInfoList")
//	public ResultPista<List<MyStakeInfoListDto>> destroyOrderList(Long lastId, Integer status) throws Exception {
//		return ResultPista.data(bizStakeService.destroyOrderList(lastId, status));
//	}
//
//}
