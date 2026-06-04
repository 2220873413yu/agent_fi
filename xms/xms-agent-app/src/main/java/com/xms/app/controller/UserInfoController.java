package com.xms.app.controller;


import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.github.pagehelper.PageInfo;
import com.xms.app.config.RobotConfig;
import com.xms.app.entity.dto.CurrentStakeHostingStaticRateDto;
import com.xms.app.entity.dto.GlobalDividendPoolDto;
import com.xms.app.entity.dto.MyDirectMemberDto;
import com.xms.app.entity.dto.MyTeamInfoDto;
import com.xms.app.entity.vo.*;
import com.xms.app.service.BizUserService;
import com.xms.common.annotation.Anonymous;
import com.xms.common.constant.Constants;
import com.xms.common.core.domain.api.ResultPista;
import com.xms.common.core.domain.model.xms.LoginAppUser;
import com.xms.common.utils.SecurityUtils;
import com.xms.dao.domain.StakeHostingGlobalDividendPool;
import com.xms.dao.entity.bo.BatchUserBo;
import com.xms.dao.entity.bo.UserInfoBo;
import com.xms.dao.entity.domain.UserInfo;
import com.xms.dao.service.IStakeHostingGlobalDividendPoolService;
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

import java.math.BigDecimal;
import java.util.Date;

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

	@Autowired
	private IStakeHostingGlobalDividendPoolService stakeHostingGlobalDividendPoolService;

	/**
	 * 查询分红池相关信息
	 * @return
	 */
	@ApiOperation(value = "查询分红池相关信息")
	@GetMapping(value = "/getGlobalDividendPool")
	public ResultPista<GlobalDividendPoolDto> getGlobalDividendPool() {
		GlobalDividendPoolDto resp = new GlobalDividendPoolDto();
		StakeHostingGlobalDividendPool dividendPool = stakeHostingGlobalDividendPoolService.lambdaQuery()
			.last("limit 1")
			.one();
		if(dividendPool!=null){
			resp.setBalanceAmount(dividendPool.getBalanceAmount());
		}else{
			resp.setBalanceAmount(BigDecimal.ZERO);
		}
		// 每周日 23:59:59 开奖：未过本周日结束点则返回本周日，否则返回下周日
		DateTime now = DateUtil.date();
		DateTime thisSundayEnd = DateUtil.endOfWeek(now, true);
		Date nextAwardTime = now.before(thisSundayEnd)
			? thisSundayEnd
			: DateUtil.endOfWeek(DateUtil.offsetWeek(now, 1), true);
		resp.setNextAwardTime(nextAwardTime);
		resp.setCurrentTime(now);
		return ResultPista.data(resp);
	}

	/**
	 * 生成钱包签名登录用的随机消息。
	 *
	 * <p>前端拿到随机数后发起钱包签名，登录接口再用随机数、签名和钱包地址校验是否为本人操作。</p>
	 *
	 * @param address 钱包地址，统一转为小写后参与 Redis 随机数 key
	 * @return 5 分钟内有效的钱包签名随机数
	 */
	@ApiOperation(value = "获取随机消息")
	@GetMapping(value = "/getMessage")
	public ResultPista<String> getMessage(
		@ApiParam(value = "钱包地址", required = true) @NotBlank @RequestParam String address) {
		address = address.toLowerCase();
		return ResultPista.data(bizUserService.getMessage(address));
	}


	/**
	 * 检查钱包地址是否已经注册。
	 *
	 * @param address 钱包地址
	 * @return {@code true} 表示已存在用户，{@code false} 表示可以走首次登录注册流程
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
	 * 钱包签名登录接口。
	 *
	 * <p>如果钱包地址已存在，仅校验签名并签发 Token；如果钱包地址不存在，则要求邀请码有效，
	 * 并在同一事务内创建用户、初始化钱包、写入邀请关系闭包表后再签发 Token。</p>
	 *
	 * @param loginVo 钱包地址、签名随机数、签名和首次登录邀请码
	 * @return App 登录用户和 Token 信息
	 */
	@ApiOperation(value = "登录")
	@PostMapping(value = "/login")
	public ResultPista<LoginAppUser> login(@Valid @RequestBody LoginVo loginVo) {
		loginVo.setAddress(loginVo.getAddress().toLowerCase());
		return bizUserService.login(loginVo);
	}

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

}

