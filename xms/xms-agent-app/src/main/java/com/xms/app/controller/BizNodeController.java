package com.xms.app.controller;

import com.xms.app.entity.dto.*;
import com.xms.app.entity.resp.CreateOrderResp;
import com.xms.app.entity.vo.CreateNodeOrderVo;
import com.xms.app.service.BizNodeService;
import com.xms.common.annotation.RepeatSubmit;
import com.xms.common.core.domain.api.ResultPista;
import com.xms.common.utils.SecurityUtils;
import com.xms.dao.service.XmsCommonService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 节点相关 前端控制器
 *
 * @since 2023-06-12
 */
@Api(tags = "节点相关")
@RestController
@RequestMapping("/api/node")
public class BizNodeController {


	@Autowired
	private BizNodeService bizNodeService;

	@Autowired
	private XmsCommonService xmsCommonServiceImpl;

	/**
	 * 获取节点信息 没有上架的话可能为空
	 *
	 * @return
	 * @throws Exception
	 */
	@ApiOperation(value = "获取节点信息")
	@GetMapping(value = "/nodeInfo")
	public ResultPista<List<NodeInfoDTO>> nodeInfo(){
		return ResultPista.data(bizNodeService.nodeInfo());
	}


	/**
	 * 创建节点订单
	 * @return
	 */
	@ApiOperation(value = "创建节点订单")
	@PostMapping(value = "/createOrder")
	@RepeatSubmit
	public ResultPista<CreateOrderResp> createOrder(@Valid @RequestBody CreateNodeOrderVo req) throws Exception {
//		ResultPista resultPista = xmsCommonServiceImpl.checkMineSettleTime();
//		if (!resultPista.isSuccess()) {
//			throw new ServiceException(resultPista.getMsg());
//		}
		return bizNodeService.createOrder(req, SecurityUtils.getLoginAppUser().getUserId());
	}



	/**
	 * 节点购买记录
	 * @return
	 * @throws Exception
	 */
	@ApiOperation(value = "节点购买记录")
	@GetMapping(value = "/list")
	public ResultPista<List<NodePackageOrderDto>> list() throws Exception {
		return ResultPista.data(bizNodeService.list());
	}

}
