package com.xms.app.controller;


import com.github.pagehelper.PageInfo;
import com.xms.app.entity.PtbPriceDto;
import com.xms.app.entity.bo.MyAssetsBo;
import com.xms.app.entity.dto.CardUpgradeLogDto;
import com.xms.app.entity.dto.RewardRecordDto;
import com.xms.app.entity.dto.UserCardAssetDto;
import com.xms.app.entity.req.RewardRecordQueryReq;
import com.xms.app.entity.resp.IncomeOverviewResp;
import com.xms.app.entity.dto.TodayIncomeDto;
import com.xms.app.entity.resp.IncomeInfoResp;
import com.xms.app.entity.vo.UserMoneySwapVo;
import com.xms.app.service.BizCardService;
import com.xms.app.service.BizUserMoneyService;
import com.xms.common.annotation.RepeatSubmit;
import com.xms.common.constant.ConstantStatic;
import com.xms.common.constant.SysConstant;
import com.xms.common.core.domain.api.ResultPista;
import com.xms.common.core.domain.entity.SysDictData;
import com.xms.common.exception.ServiceException;
import com.xms.common.result.ResponseCode;
import com.xms.common.utils.CollectionUtil;
import com.xms.common.utils.SecurityUtils;
import com.xms.dao.domain.*;
import com.xms.dao.entity.bo.RewardRecordBo;
import com.xms.dao.entity.bo.UserMoneyBo;
import com.xms.dao.entity.bo.UserMoneyLogBo;
import com.xms.dao.entity.domain.UserMoneyLog;
import com.xms.dao.service.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 用户钱包表 前端控制器
 */
@Api(tags = "用户钱包")
@RestController
@RequestMapping("/usermoney")
public class UserMoneyController {

	@Autowired
	private UserMoneyLogService userMoneyLogService;

	@Autowired
	private BizUserMoneyService bizUserMoneyService;


	/**
	 * 查询钱包余额
	 *
	 * @return
	 */
	@ApiOperation(value = "查询钱包余额")
	@GetMapping(value = "getUserMoney")
	public ResultPista<UserMoneyBo> getUserMoney() {
		UserMoneyBo userMoney = bizUserMoneyService.getUserMoney(SecurityUtils.getLoginAppUser().getUserId());
		return ResultPista.data(userMoney);
	}

//	/**
//	 * 查询我的资产
//	 *
//	 * @return
//	 */
//	@ApiOperation(value = "查询我的资产")
//	@GetMapping(value = "myAssets")
//	public ResultPista<MyAssetsBo> myAssets() {
//		return ResultPista.data(bizUserMoneyService.myAssets());
//	}


//
//	/**
//	 * 查询卡片购买升级记录(默认查询10条)
//	 * @param cardType
//	 * @return
//	 */
//	@ApiOperation(value = "查询卡片购买升级记录")
//	@GetMapping(value = "/getCardLog")
//	public ResultPista<List<CardUpgradeLogDto>>  getCardLog(Integer cardType,Long lastId){
//		cardType = cardType == null ? 1 : cardType;
//		return ResultPista.data(bizCardService.getCardLog(cardType,lastId));
//	}
//	/**
//	 * 查询我的卡片资产根据卡片类型
//	 * @return
//	 */
//	@ApiOperation(value = "查询我的卡片资产根据卡片类型")
//	@GetMapping(value = "/cardAssetByType")
//	public ResultPista<UserCardAssetDto> cardAssetByType(Integer cardType) {
//		UserCardAssetDto dto = new UserCardAssetDto();
//		cardType = cardType == null ? 1 : cardType;
//		CardPackage cardPackage = cardPackageService.lambdaQuery()
//			.eq(CardPackage::getCardType, cardType)
//			.one();
//		//算力
//		dto.setComputingPower(cardPackage.getComputingPower());
//		//价格
//		dto.setPrice(cardPackage.getPrice());
//		//查询算力
//		List<CardOrder> cardOrderList = cardOrderService.lambdaQuery()
//			.eq(CardOrder::getCardType, cardType)
//			.eq(CardOrder::getUserId, SecurityUtils.getLoginAppUser().getUserId())
//			.eq(CardOrder::getStatus, 0)
//			.select(CardOrder::getExtraComputingPower,CardOrder::getComputingPower)
//			.list();
//		BigDecimal holdingPower = BigDecimal.ZERO;
//		if(CollectionUtil.isNotEmpty(cardOrderList)){
//			for (CardOrder cardOrder : cardOrderList) {
//				holdingPower = holdingPower.add(cardOrder.getComputingPower().add(cardOrder.getExtraComputingPower()));
//			}
//		}
//		dto.setHoldingPower(holdingPower);
//		UserCardAsset cardAsset = userCardAssetService.lambdaQuery()
//			.eq(UserCardAsset::getId, SecurityUtils.getLoginAppUser().getUserId())
//			.one();
//
//		//设置持有卡片数量
//		if(cardType.equals(1)){
//			dto.setNum(cardAsset.getCardLevel1());
//		}else if(cardType.equals(2)){
//			dto.setNum(cardAsset.getCardLevel2());
//		}else if(cardType.equals(3)){
//			dto.setNum(cardAsset.getCardLevel3());
//		}else if(cardType.equals(4)){
//			dto.setNum(cardAsset.getCardLevel4());
//		}
//
//		return ResultPista.data(dto);
//	}
//
//	/**
//	 * 查询我的卡片资产
//	 * @return
//	 */
//	@ApiOperation(value = "查询我的卡片资产")
//	@GetMapping(value = "/cardAsset")
//	public ResultPista<UserCardAsset> cardAsset() {
//		UserCardAsset cardAsset = userCardAssetService.lambdaQuery()
//			.eq(UserCardAsset::getId, SecurityUtils.getLoginAppUser().getUserId())
//			.one();
//		return ResultPista.data(cardAsset);
//	}

//	/**
//	 * 奖金记录(收益记录)
//	 * @param pageIndex 当前页 默认1
//	 * @param pageSize 每页长度 默认20(最大20)
//	 * @param bizType 业务类型 -1:查询全部,1:静态收益,2:动态收益,3:团队奖励
//	 * @param dateType 时间类型-1:查询全部,1:今日,2:本周,3:本月
//	 * @return
//	 */
//	@ApiOperation(value = "奖金记录(收益记录)")
//	@GetMapping(value = "/rewardList")
//	public ResultPista<PageInfo<RewardRecordBo>> rewardList(
//		@ApiParam(value = "当前页", required = true) @NotNull @RequestParam(defaultValue = "1") Integer pageIndex,
//		@ApiParam(value = "每页长度", required = true) @NotNull @RequestParam(defaultValue = "20") Integer pageSize,
//		@RequestParam(required = false) Integer bizType,@RequestParam(required = false) Integer dateType) {
//		pageSize = pageSize >= 20 ? SysConstant.TWENTY : pageSize;
//		return ResultPista.data(bizUserMoneyService.rewardList(pageIndex, pageSize, SecurityUtils.getLoginAppUser().getUserId(), bizType,dateType));
//	}

//	/**
//	 * 查询奖金记录
//	 * @param req
//	 * @return
//	 */
//	@ApiOperation(value = "查询奖金记录")
//	@PostMapping(value = "/rewardRecord")
//	public ResultPista<List<RewardRecordDto>> rewardRecord(@Valid @RequestBody RewardRecordQueryReq req) {
//		return ResultPista.data(bizUserMoneyService.rewardRecord(req.getLastId(),req.getSourceTypes()));
//	}


//	/**
//	 * 收益卡片/类型分布切换
//	 * 一次性返回收益卡片 + 类型分布（今日/本周/本月/总）
//	 * @return
//	 */
//	@ApiOperation(value = "收益统计概览")
//	@GetMapping(value = "/incomeOverview")
//	public ResultPista<IncomeOverviewResp> incomeOverview() {
//		return ResultPista.data(bizUserMoneyService.incomeOverview());
//	}

//	/**
//	 * 查询今日收益
//	 * @return
//	 */
//	@ApiOperation(value = "查询今日收益")
//	@GetMapping(value = "/todayIncome")
//	public ResultPista<TodayIncomeDto> todayIncome() {
//		return ResultPista.data(bizUserMoneyService.todayIncome(SecurityUtils.getLoginAppUser().getUserId()));
//	}

//	/**
//	 * 查询收益明细
//	 * @param lastId
//	 * @return
//	 */
//	@ApiOperation(value = "查询收益明细")
//	@GetMapping(value = "/incomeDetailsList")
//	public ResultPista<List<UserMoneyLog>> incomeDetailsList(Long lastId) {
//		return ResultPista.data(userMoneyLogService.incomeDetailsList(lastId, SecurityUtils.getLoginAppUser().getUserId()));
//	}


//	/**
//	 * 查询平台币最近7天价格
//	 * @return
//	 */
//	@ApiOperation(value = "查询平台币最近7天价格")
//	@GetMapping(value = "/queryPtbLast7Price")
//	public ResultPista<List<PtbPriceDto>> queryPtbLast7Price() {
//		return bizUserMoneyService.queryPtbLast7Price();
//	}



//	/**
//	 * 闪兑
//	 *
//	 * @param req
//	 * @return
//	 */
//	@ApiOperation(value = "闪兑")
//	@RepeatSubmit
//	@PostMapping(value = "/swap")
//	public ResultPista swap(@Valid @RequestBody UserMoneySwapVo req) {
//		ResultPista resultPista = xmsCommonServiceImpl.checkMineSettleTime();
//		if (!resultPista.isSuccess()) {
//			throw new ServiceException(resultPista.getMsg());
//		}
//
//		if(req.getAmount().compareTo(BigDecimal.ZERO)<=0){
//			return ResultPista.fail(ResponseCode.CODE_108);
//		}
//		req.setAmount(req.getAmount().setScale(ConstantStatic.newScale, ConstantStatic.roundingModeNew));
//		bizUserMoneyService.swap(req, SecurityUtils.getLoginAppUser().getUserId());
//		return ResultPista.success();
//	}

//	/**
//	 * 闪兑记录
//	 *
//	 * @param lastId   当前记录最后一个ID
//	 * @return
//	 */
//	@ApiOperation("闪兑记录")
//	@GetMapping("/swapRecord")
//	public ResultPista<List<FlashExchangeRecord>> swapRecord(Long lastId) {
//		return bizUserMoneyService.swapRecord(lastId);
//	}


	/**
	 * 钱包流水列表
	 *
	 * @param pageIndex  当前页 默认10
	 * @param pageSize   每页长度 默认20(最大20)
	 * @param coinType   币种 1:USDT,2:DFC,3:OORT,4:锁定USDT,5:产出DFC
	 * @param sourceType 来源类型 xxx
	 * @return
	 */
	@ApiOperation(value = "钱包流水列表")
	@GetMapping(value = "/getUserMoneyLogList")
	public ResultPista<PageInfo<UserMoneyLogBo>> getUserMoneyLogList(
		@ApiParam(value = "当前页", required = true) @NotNull @RequestParam(defaultValue = "1") Integer pageIndex,
		@ApiParam(value = "每页长度", required = true) @NotNull @RequestParam(defaultValue = "20") Integer pageSize,
		@ApiParam(value = "币种") @RequestParam(required = false) Integer coinType,
		@ApiParam(value = "来源类型") @RequestParam(required = false) Integer sourceType) {
		pageSize = pageSize >= 20 ? SysConstant.TWENTY : pageSize;
		return ResultPista.data(userMoneyLogService.getUserMoneyLogList(pageIndex, pageSize, SecurityUtils.getLoginAppUser().getUserId(), coinType, sourceType));
	}

	/**
	 * 根据类型获取明细业务类型
	 *
	 * @param dictType 字典类型：账单流水  t_user_money_log_source_type
	 * @return
	 */
	@ApiOperation("根据类型获取明细业务类型")
	@GetMapping("/listRecordBiz")
	public ResultPista<List<SysDictData>> listRecordBiz(@RequestParam String dictType) {
		return userMoneyLogService.listRecordBiz(dictType);
	}
}

