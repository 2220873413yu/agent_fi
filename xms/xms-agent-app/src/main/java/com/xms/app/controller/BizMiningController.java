//package com.xms.app.controller;
//
//import cn.hutool.core.thread.ThreadUtil;
//import com.github.pagehelper.PageInfo;
//import com.xms.app.entity.bo.DestroyCallbackBo;
//import com.xms.app.entity.bo.DestroyInfoBo;
//import com.xms.app.entity.dto.*;
//import com.xms.app.entity.vo.CreateMiningOrderVo;
//import com.xms.app.entity.vo.StakeMiningOrderVo;
//import com.xms.app.service.impl.BizMiningServiceImpl;
//import com.xms.common.utils.spring.SpringUtils;
//import com.xms.dao.entity.dto.DestroyOrderDto;
//import com.xms.dao.entity.dto.InterestPackDto;
//import com.xms.dao.entity.dto.InterestStatDayDto;
//import com.xms.app.entity.req.ReleaseOrderReq;
//import com.xms.app.entity.resp.CreateOrderResp;
//import com.xms.app.service.BizMiningService;
//import com.xms.common.annotation.RepeatSubmit;
//import com.xms.common.core.domain.api.ResultPista;
//import com.xms.common.exception.ServiceException;
//import com.xms.common.utils.SecurityUtils;
//import com.xms.dao.service.XmsCommonService;
//import io.swagger.annotations.Api;
//import io.swagger.annotations.ApiOperation;
//import io.swagger.annotations.ApiParam;
//import jakarta.validation.Valid;
//import jakarta.validation.constraints.NotNull;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.*;
//
//import java.math.BigDecimal;
//import java.util.List;
//import java.util.UUID;
//import java.util.concurrent.TimeUnit;
//
///**
// * 矿机相关 前端控制器
// *
// *
// * @since 2023-06-12
// */
//@Api(tags = "矿机相关")
//@RestController
//@RequestMapping("/api/mining")
//public class BizMiningController {
//	@Autowired
//	private BizMiningService bizMiningService;
//
//	@Autowired
//	private XmsCommonService xmsCommonServiceImpl;
//
//
//	/**
//	 * 获取矿机信息(肯能出现矿机没有上架情况数据是为空的)
//	 * @return
//	 * @throws Exception
//	 */
//	@ApiOperation(value = "矿机信息")
//	@GetMapping(value = "/miningInfo")
//	public ResultPista<MiningPackageDto> miningInfo()  throws Exception{
//		return ResultPista.data(bizMiningService.miningInfo());
//	}
//
//
//	/**
//	 * 购买矿机
//	 * @return
//	 */
//	@ApiOperation(value = "购买矿机")
//	@PostMapping(value = "/miningOrder")
//	@RepeatSubmit
//	public ResultPista<CreateOrderResp> miningOrder(@Valid @RequestBody CreateMiningOrderVo req)  throws Exception{
////		ResultPista resultPista = xmsCommonServiceImpl.checkMineSettleTime(1);
////		if (!resultPista.isSuccess()) {
////			throw new ServiceException(resultPista.getMsg());
////		}
//		return bizMiningService.miningOrder(req,SecurityUtils.getLoginAppUser().getUserId());
//	}
//
//
//	/**
//	 * 我的矿机信息 矿机总量、已经质押的矿机、今日产出dfc
//	 * @return
//	 * @throws Exception
//	 */
//	@ApiOperation(value = "我的矿机信息")
//	@GetMapping(value = "/myMiningInfo")
//	public ResultPista<MyMiningInfoDto> myMiningInfo()  throws Exception{
//		return ResultPista.data(bizMiningService.myMiningInfo());
//	}
//
//
//	/**
//	 * 我的矿机列表
//	 * @param lastId id
//	 * @param bizType 0:未质押,1:已质押,2:自提订单
//	 * @return
//	 * @throws Exception
//	 */
//	@ApiOperation(value = "我的矿机列表页面")
//	@GetMapping(value = "/myMiningList")
//	public ResultPista<List<MyMiningListDto>> myMiningList(Long lastId,Integer bizType)  throws Exception{
//		return ResultPista.data(bizMiningService.myMiningList(lastId,bizType));
//	}
//
//	/**
//	 * 获取矿机质押需要多少dfc和产出根据矿机id获取
//	 * @param id
//	 * @return
//	 * @throws Exception
//	 */
//	@ApiOperation(value = "获取矿机质押需要多少dfc和产出根据矿机id获取")
//	@GetMapping(value = "/getPackageTierById")
//	@RepeatSubmit
//	public ResultPista<MiningPackageTierDto> getPackageTierById(Long id)  throws Exception{
////		ResultPista resultPista = xmsCommonServiceImpl.checkMineSettleTime(1);
////		if (!resultPista.isSuccess()) {
////			throw new ServiceException(resultPista.getMsg());
////		}
//		return bizMiningService.getPackageTierById(id);
//	}
//	/**
//	 * 质押矿机
//	 * @return
//	 */
//	@ApiOperation(value = "质押矿机")
//	@PostMapping(value = "/stakeMiningOrder")
//	@RepeatSubmit
//	public ResultPista<CreateOrderResp> stakeMiningOrder(@Valid @RequestBody StakeMiningOrderVo req)  throws Exception{
////		ResultPista resultPista = xmsCommonServiceImpl.checkMineSettleTime(1);
////		if (!resultPista.isSuccess()) {
////			throw new ServiceException(resultPista.getMsg());
////		}
//		return bizMiningService.stakeMiningOrder(req,SecurityUtils.getLoginAppUser().getUserId());
//	}
//
//
//
//
//
//
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
