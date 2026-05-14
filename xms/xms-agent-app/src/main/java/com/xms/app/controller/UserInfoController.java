package com.xms.app.controller;


import cn.hutool.core.util.StrUtil;
import com.github.pagehelper.PageInfo;
import com.xms.app.config.RobotConfig;
import com.xms.app.entity.dto.CurrentStakeHostingStaticRateDto;
import com.xms.app.entity.dto.MyDirectMemberDto;
import com.xms.app.entity.dto.MyTeamInfoDto;
import com.xms.app.entity.vo.*;
import com.xms.app.service.BizUserService;
import com.xms.common.annotation.Anonymous;
import com.xms.common.constant.Constants;
import com.xms.common.core.domain.api.ResultPista;
import com.xms.common.core.domain.model.xms.LoginAppUser;
import com.xms.common.utils.SecurityUtils;
import com.xms.dao.entity.bo.BatchUserBo;
import com.xms.dao.entity.bo.UserInfoBo;
import com.xms.dao.entity.domain.UserInfo;
import com.xms.dao.service.UserInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

/**
 * 用户信息表 前端控制器
 */
@Api(tags = "用户信息")
@RestController
@RequestMapping("/userinfo")
@Slf4j
public class UserInfoController {

	@Autowired
	private UserInfoService userInfoService;

	@Autowired
	private BizUserService bizUserService;

	@Autowired
	private HttpServletRequest request;




//	/**
//	 * 获取算力页面展示数据
//	 *
//	 * @return 返回随机数
//	 */
//	@ApiOperation(value = "获取算力页面展示数据")
//	@GetMapping(value = "/computingPowerData")
//	public ResultPista<ComputingPowerBo> computingPowerData() {
//		return ResultPista.data(bizUserService.computingPowerData());
//	}
//
//	/**
//	 * 获取算力奖励产出列表
//	 * @param lastId
//	 * @return
//	 */
//	@ApiOperation(value = "获取算力奖励产出列表")
//	@GetMapping(value = "/powerDataList")
//	public ResultPista<List<UserMoneyLog>> powerDataList(Long lastId) {
//		return ResultPista.data(bizUserService.powerDataList(lastId));
//	}

//	/**
//	 * 获取用户资产信息
//	 *
//	 * @return 用户资产信息 DTO
//	 */
//	@ApiOperation(value = "获取用户资产信息")
//	@GetMapping(value = "/getUserAssetInfo")
//	public ResultPista<UserAssetInfoBo> getUserAssetInfo() {
//		Long userId = SecurityUtils.getLoginAppUser().getUserId();
//		// 空实现占位，后续可在 BizUserServiceImpl 中补充具体资产统计逻辑
//		return ResultPista.data(bizUserService.getUserAssetInfo(userId));
//	}



	/**
	 * 获取随机消息
	 *
	 * @param address 钱包地址
	 * @return 返回随机数
	 */
	@ApiOperation(value = "获取随机消息")
	@GetMapping(value = "/getMessage")
	public ResultPista<String> getMessage(
		@ApiParam(value = "钱包地址", required = true) @NotBlank @RequestParam String address) {
		address = address.toLowerCase();
		return ResultPista.data(bizUserService.getMessage(address));
	}


	/**
	 * 检查钱包地址是否注册过,返回false是没注册过,true注册过
	 * @param address 钱包地址
	 * @return
	 */
	@ApiOperation(value = "检查账号是否注册过")
	@Anonymous
	@GetMapping("/checkAddress")
	public ResultPista<Boolean> checkAddress(@NotBlank @RequestParam String address) {
		if(StrUtil.isBlank(address)){
			return ResultPista.fail("钱包地址不能为空");
		}
		UserInfo one = userInfoService.lambdaQuery().eq(UserInfo::getAccount, address).one();
		if (one == null) {
			return ResultPista.data(false);
		}
		return ResultPista.data(true);
	}



	/**
	 * 登录接口
	 *
	 * @param address
	 * @return
	 */
	@ApiOperation(value = "登录")
	@GetMapping(value = "/getToken")
	@Anonymous
	public ResultPista<LoginAppUser> getToken(String address) {
		return bizUserService.getToken(address);
	}


	/**
	 * 登录接口
	 *
	 * @param loginVo
	 * @return
	 */
	@ApiOperation(value = "登录")
	@PostMapping(value = "/login")
	public ResultPista<LoginAppUser> login(@Valid @RequestBody LoginVo loginVo) {
		loginVo.setAddress(loginVo.getAddress().toLowerCase());
		return bizUserService.login(loginVo);
	}

//	/**
//	 * 登录接口
//	 *
//	 * @return
//	 */
//	@ApiOperation(value = "登录")
//	@Anonymous
//	@PostMapping(value = "/batchRegister")
//	@Transactional(rollbackFor = Exception.class)
//	public ResultPista batchRegister() {
//		List<BatchUserBo> batchUserList = userInfoService.getBatchUser();
//		for (BatchUserBo batchUserBo : batchUserList) {
//			log.info("开始注册用户:{}", batchUserBo.getWalletAddress());
//			bizUserService.login(batchUserBo);
//		}
//		return ResultPista.success();
//	}


//	/**
//	 * 查询全网节点信息
//	 *
//	 * @return
//	 */
//	@ApiOperation(value = "查询全网节点信息")
//	@GetMapping(value = "/getTotalNode")
//	public ResultPista<Integer> getTotalNode() {
//		List<IdoOrder> idoOrderList = idoOrderService.lambdaQuery()
//			.eq(IdoOrder::getBizStatus, 2)
//			.select(IdoOrder::getShares)
//			.list();
//		Integer totalNode = 0;
//		if(CollectionUtil.isNotEmpty(idoOrderList)){
//			for (IdoOrder idoOrder : idoOrderList) {
//				totalNode =totalNode + idoOrder.getShares();
//			}
//		}
//		return ResultPista.data(totalNode);
//	}


	/**
	 * 查询用户详情
	 *
	 * @return
	 */
	@ApiOperation(value = "用户详情")
	@GetMapping(value = "/getUserInfo")
	public ResultPista<UserInfoBo> getUserInfo() {
		UserInfoBo userInfoBo = userInfoService.getUserInfo(SecurityUtils.getLoginAppUser().getUserId());

		return ResultPista.data(userInfoBo);
	}

	/**
	 * 退出登录
	 *
	 * @return
	 */
	@ApiOperation(value = "退出登录")
	@GetMapping(value = "/logout")
	public ResultPista<String> logout() {
		return bizUserService.logout(request);
	}


	/**
	 * 查询我的直推用户信息
	 * @param pageIndex pageindex
	 * @param pageSize pageSize
	 * @param gameLevel 用户等级
	 * @return
	 */
	@ApiOperation(value = "我的直推用户信息")
	@GetMapping("/listSubMembers")
	public ResultPista<PageInfo<MyDirectMemberDto>> listSubMembers(Integer pageIndex, Integer pageSize,Integer gameLevel) {
		return ResultPista.data(bizUserService.listSubMembers(pageIndex, pageSize,gameLevel));
	}


	/**
	 * 我的团队数据
	 *
	 * @return
	 */
	@ApiOperation(value = "我的团队数据")
	@GetMapping(value = "/myTeamInfo")
	public ResultPista<MyTeamInfoDto> myTeamInfo() {
		return ResultPista.data(bizUserService.myTeamInfo(SecurityUtils.getLoginAppUser().getUserId()));
	}

	/**
	 * 查询当前托管静态日利率。
	 *
	 * 查询当前登录用户今日G值和当前托管基础静态日利率。
	 *
	 * @return 今日G值和当前托管基础静态日利率
	 */
	@ApiOperation(value = "查询当前托管静态日利率")
	@GetMapping(value = "/currentStakeHostingStaticRate")
	public ResultPista<CurrentStakeHostingStaticRateDto> currentStakeHostingStaticRate() {
		return ResultPista.data(bizUserService.currentStakeHostingStaticRate(SecurityUtils.getLoginAppUser().getUserId()));
	}

	/**
	 * 我的团队用户信息
	 * @return
	 */
//	@ApiOperation(value = "我的团队用户信息")
//	@GetMapping("/listMyDirectMembers")
//	public ResultPista<List<MyDirectMemberDto>> listMyDirectMembers() {
//		return ResultPista.data(bizUserService.listMyDirectMembers());
//	}






//	/**
//	 * 我的团队数据 总成员、直推人数、团队销毁usdt、等级
//	 * @param lastId lastId
//	 * @param distance 层级
//	 * @param level 等级
//	 * @return
//	 */
//	@ApiOperation(value = "我的团队数据")
//	@GetMapping("/teamMembers")
//	public ResultPista<MyTeamMemberPageDto> listMyTeamMembers(Long lastId,Integer distance,Integer level) {
//		return ResultPista.data(bizUserService.listMyTeamMembers(lastId,distance,level));
//	}
//



//	/**
//	 * 我的团队页面
//	 *
//	 * @return
//	 */
//	@ApiOperation(value = "我的团队页面")
//	@GetMapping(value = "/getTeamInfo")
//	public ResultPista<List<TeamOverviewDto>> getMyTeamOverview() {
//		return ResultPista.data(bizUserService.getMyTeamOverview(SecurityUtils.getLoginAppUser().getUserId()));
//	}

//	/**
//	 * 查询用户收益信息
//	 *
//	 * @return
//	 */
//	@ApiOperation(value = "查询用户收益信息")
//	@GetMapping(value = "/getIncomeSummary")
//	public ResultPista<UserIncomeSummaryVo> getIncomeSummary() {
//		return ResultPista.data(bizUserService.getIncomeSummary(SecurityUtils.getLoginAppUser().getUserId()));
//	}







//	/**
//	 * 修改用户基础信息
//	 * @param req
//	 * @return
//	 */
//	@ApiOperation(value = "修改用户基础信息")
//	@PostMapping(value = "/updateBaseInfo")
//	public ResultPista updateBaseInfo(@Valid @RequestBody UserBaseInfoVo req) {
//		bizUserService.updateBaseInfo(req);
//		return ResultPista.success();
//	}


	//	/**
//	 * 查询用户业绩信息
//	 *
//	 * @return
//	 */
//	@ApiOperation(value = "查询用户业绩信息")
//	@GetMapping(value = "/getTeamView")
//	public ResultPista<TeamViewBO> getTeamView() {
//		TeamViewBO result = bizUserService.getTeamView(SecurityUtils.getLoginAppUser().getUserId());
//		return ResultPista.data(result);
//	}


}

