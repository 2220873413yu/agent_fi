package com.xms.app.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.hutool.system.SystemUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.xms.app.entity.bo.ComputingPowerBo;
import com.xms.app.entity.bo.TeamViewBO;
import com.xms.app.entity.req.BindInviteUserReq;
import com.xms.app.handler.CustomException;
import com.xms.common.constant.ExternalApiConstant;
import com.xms.app.entity.bo.CoinInfoBo;
import com.xms.app.entity.bo.UserAssetInfoBo;
import com.xms.app.entity.dto.CurrentStakeHostingStaticRateDto;
import com.xms.app.entity.dto.MyDirectMemberDto;
import com.xms.app.entity.dto.MyTeamInfoDto;
import com.xms.app.entity.dto.MyTeamMemberDto;
import com.xms.app.entity.dto.MyTeamMemberPageDto;
import com.xms.app.entity.dto.TeamLevelDto;
import com.xms.app.entity.req.BindEmailVo;
import com.xms.app.entity.vo.*;
import com.xms.app.util.AliyunSenMailUtil;
import com.xms.app.util.TLSSigAPIv2;
import com.xms.app.entity.req.UserBaseInfoVo;
import com.xms.app.service.BizUserService;
import com.xms.common.config.redis.lock.RedisLock;
import com.xms.common.constant.*;
import com.xms.common.exception.ServiceException;
import com.xms.common.mq.dynamic.AsyncDynamicOrderSettlementService;
import com.xms.common.mq.dynamic.OrderMsgDO;
import com.xms.common.utils.*;
import com.xms.common.utils.ip.IpUtils;
import com.xms.dao.domain.*;
import com.xms.dao.entity.bo.BatchUserBo;
import com.xms.dao.entity.domain.UserMoneyLog;
import com.xms.dao.entity.dto.TeamDestroyStatDto;
import com.xms.dao.service.*;
import com.xms.dao.mapper.UserInfoMapper;
import com.xms.common.config.redis.XmsRedis;
import com.xms.common.core.domain.api.ResultPista;
import com.xms.common.core.domain.model.xms.LoginAppUser;
import com.xms.common.result.ResponseCode;
import com.xms.common.utils.sign.Md5Utils;
import com.xms.common.utils.spring.SpringUtils;
import com.xms.dao.entity.domain.UserInfo;
import com.xms.dao.entity.domain.UserMoney;
import com.xms.dao.entity.domain.UserRelation;
import com.xms.dao.service.impl.StakeHostingOrderServiceImpl;
import com.xms.dao.service.impl.UserInfoServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class BizUserServiceImpl implements BizUserService {

	private static final String SDK_APP_ID = "1721002266";
	private static final String SECRET_KEY = "01acd6bad44c551dd72e3fad285275586c060dd32ef483d8f9958d3eae5ede46"; // 需要替换为真实密钥
	private static final String ADMIN_IDENTIFIER = "administrator"; // App管理员账号
	private static final BigDecimal PLACEHOLDER_STATIC_RATE_PERCENT = new BigDecimal("0.5000");
	private static final BigDecimal PURE_STATIC_RATE_BEFORE_RETURN_PERCENT = new BigDecimal("0.5000");
	private static final BigDecimal PURE_STATIC_RATE_AFTER_RETURN_PERCENT = new BigDecimal("0.2000");


	@Autowired
	private UserInfoService userInfoServiceImpl;

	@Autowired
	private UserInfoMapper userInfoMapper;


	@Autowired
	private INodePackageOrderService nodePackageOrderService;


	@Autowired
	private AsyncDynamicOrderSettlementService asyncDynamicOrderSettlementServiceImpl;


	@Autowired
	private UserRelationService userRelationService;

	@Autowired
	private IUserMoneyService userMoneyService;

	@Autowired
	private UserMoneyLogService userMoneyLogService;

	@Autowired
	private AppTokenService appTokenService;

	@Autowired
	private ISysParaService sysParaServiceImpl;

	@Autowired
	private IUserLevelConfigService userLevelConfigService;

	@Autowired
	private IRewardRecordService rewardRecordService;

	@Autowired
	private IStakeHostingUserRewardSummaryService stakeHostingUserRewardSummaryService;

	@Autowired
	private IStakeHostingDailyTeamPerformanceService stakeHostingDailyTeamPerformanceService;

	@Autowired
	private IStakeHostingOrderService stakeHostingOrderService;


	@Autowired
	private XmsRedis xmsRedis;

	@Autowired
	private IEmailConfigService emailConfigService;

	@Autowired
	private Environment environment;


	private TLSSigAPIv2 tlsSigAPIv2 = new TLSSigAPIv2(Long.parseLong(SDK_APP_ID), SECRET_KEY);

	@NotNull
	private static ResultPista<Object> registerIM(UserInfo userInfo) {
		//调用第三方接口注册到腾讯im里面去
		String faceUrl ="https://www.sigmapro.cc/profile/upload/2025/08/19/img_v3_02pa_9933ac6e-35b5-443e-878d-16e50f89998g_20250819111743A004.png";
		// 1. 生成UserSig - 必须使用管理员账号生成，因为需要管理员权限调用API
		String userSig = "eJwtzF0LgjAYBeD-sltD5nR*QVcRtoiwD4gup5vxZs41V0TRf8-Uy-Ocw-mg4*bgPqVBKSIuRrMhg5DKQgUDc9GAgs4ablszDTpRc61BoNSLiIcxIWE4NvKlwcjeKaUEYzyqheZvEY1pEHs0mF7g0v8X*9K5PSrYLZNszVo-YB17r5gCf5uVzj1ZxHl*ste6lOc5*v4AUiQ0uQ__";

		// 2. 构建请求URL - 新加坡数据中心
		String random = String.valueOf(RandomUtil.randomLong(100000000, 999999999));
		String url = String.format(
			"https://adminapisgp.im.qcloud.com/v4/im_open_login_svc/account_import?sdkappid=%s&identifier=%s&usersig=%s&random=%s&contenttype=json",
			SDK_APP_ID, ADMIN_IDENTIFIER, userSig, random  // identifier必须是管理员账号
		);

		// 3. 构建请求体
		JSONObject requestBody = new JSONObject();
		//设置昵称
		if(StrUtil.isNotBlank(userInfo.getAccount())){
			requestBody.put("Nick", userInfo.getAccount());
		}else{
			requestBody.put("Nick", userInfo.getAccount());
		}

		//设置头像
		if(StrUtil.isNotBlank(userInfo.getAvatar())){
			faceUrl = userInfo.getAvatar();
		}
		requestBody.put("UserID", userInfo.getAccount());

		requestBody.put("FaceUrl", faceUrl);

		log.info("腾讯IM导入账号请求: URL={}, Body={}", url, requestBody.toString());
		// 4. 发送HTTP请求
		String response = HttpUtil.createPost(url)
			.header("Content-Type", "application/json")
			.body(requestBody.toString())
			.timeout(30000)
			.execute()
			.body();

		log.info("腾讯IM导入账号响应: {}", response);

		// 5. 解析响应
		if (StrUtil.isNotBlank(response)) {
			JSONObject responseJson = JSONUtil.parseObj(response);

			if ("OK".equals(responseJson.getStr("ActionStatus")) &&
				responseJson.getInt("ErrorCode") == 0) {
				return ResultPista.success();
			} else if ("FAIL".equals(responseJson.getStr("ActionStatus")) &&
				responseJson.getInt("ErrorCode") == 70399) {
				throw new ServiceException(ResponseCode.CODE_1103);
			}else{
				throw new ServiceException(ResponseCode.CODE_1103);
			}
		} else {
			throw new ServiceException(ResponseCode.CODE_1116);
		}
	}

	/**
	 * 获取token
	 *
	 * @param getUser
	 * @param appTokenService
	 * @param tokenPrefix
	 * @return
	 */
	static ResultPista<LoginAppUser> getLoginAppUserResult(UserInfo getUser, AppTokenService appTokenService, String tokenPrefix) {
		LoginAppUser loginAppUser = new LoginAppUser();
		loginAppUser.setUserId(getUser.getUserId());
		loginAppUser.setClientId(tokenPrefix);
		loginAppUser.setUserCode(getUser.getUserCode());
		String token = appTokenService.createToken(loginAppUser);
		loginAppUser.setToken(token);
		loginAppUser.setRegAddress(getUser.getAccount());
		return ResultPista.data(loginAppUser);
	}

	/**
	 * @param account    账号
	 * @param code       验证码
	 * @param verifyType 验证码 业务类型 1:注册,2:绑定邮箱,3:提现,4:修改密码,5:忘记密码
	 */
	public static void verifyCode(String account, String code, Integer verifyType, String uuid, XmsRedis xmsRedis, ISysParaService sysParaServiceImpl) {
		//校验code
		String key = StringUtils.join(RedisConstant.CAPTCHA_SMS, account, RedisConstant.SEPARATOR, verifyType, uuid);
		verifyCode(code, xmsRedis, sysParaServiceImpl, key);
	}

	private static void verifyCode(String code, XmsRedis xmsRedis, ISysParaService sysParaServiceImpl, String key) {
		String realCode = xmsRedis.get(key);
		if (!code.equals(realCode)) {
			String value = sysParaServiceImpl.getValue(SysConstant.VERIFY_CODE_OFF);
			if (!value.equals("1")) {
				throw new ServiceException(ResponseCode.VALIDATE_CODE_ERROR);
			}
		}
		xmsRedis.del(key);
	}

	/**
	 * 验证钱包签名
	 *
	 * @param randomNum
	 * @param signature
	 * @param address
	 */
	public static void checkWallet(String randomNum, String signature, String address, XmsRedis xmsRedis) {
		address = address.toLowerCase();
		String osName = SystemUtil.getOsInfo().getName();
		if (osName.toUpperCase().contains(SysConstant.OS_NAME_WINDOWS)) {
			return;
		}

		if (!xmsRedis.hasKey(ConstantStatic.USER_RANDOM + address + randomNum)) {
			throw new ServiceException(ResponseCode.RANDOM_NOT_EXIT);
		}
		//boolean validate = MetaMaskUtil.validate(signature, randomNum, address);
		boolean validate = MetaMaskUtil.verify(randomNum, signature, address);
		if (!validate) {
			throw new ServiceException(ResponseCode.SIGN_VALIDATE_ERROR);
		}
		xmsRedis.del(ConstantStatic.USER_RANDOM + address + randomNum);
	}

	@Override
	public List<UserMoneyLog> powerDataList(Long lastId) {
		List<UserMoneyLog> userMoneyLogList = userMoneyLogService.lambdaQuery()
			.eq(UserMoneyLog::getUserId, SecurityUtils.getLoginAppUser().getUserId())
			.eq(UserMoneyLog::getCoinType, 2)
			.in(UserMoneyLog::getSourceType, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
			.lt(Func.isNotEmpty(lastId), UserMoneyLog::getId, lastId)
			.orderByDesc(UserMoneyLog::getId)
			.last(SysConstant.PAGE_LIMIT)
			.list();
		return userMoneyLogList;
	}

	@Override
	public ComputingPowerBo computingPowerData() {
		ComputingPowerBo result = new ComputingPowerBo();
		Long userId = SecurityUtils.getLoginAppUser().getUserId();
		UserInfo userInfo = userInfoServiceImpl.lambdaQuery()
			.eq(UserInfo::getUserId, userId)
			.one();

		result.setGlobalTotalPower(BigDecimal.ZERO);
		result.setTodayReward(userMoneyService.getTodayReward(userId));
		result.setTotalReward(userMoneyService.getTotalReward(userId));
		return result;
	}

	@Override
	public TeamViewBO getTeamView(Long userId) {
		UserInfo userInfo = userInfoServiceImpl.lambdaQuery()
			.eq(UserInfo::getUserId, userId)
			.one();
		TeamViewBO teamViewBO = new TeamViewBO();
		//直推业绩

		teamViewBO.setSubPerformance(userInfo.getSubPerformance());
		//小区业绩
		teamViewBO.setCommunityPerformance(userInfo.getCommunityPerformance());
		//团队业绩
		teamViewBO.setUmbrellaPerformance(userInfo.getUmbrellaPerformance());
		//邀请人数
		userInfo.getUmbrellaNum();
		teamViewBO.setUmbrellaNum(userInfo.getUmbrellaNum());
		teamViewBO.setSubNum(userInfo.getSubNum());
		//直推奖励
		teamViewBO.setSubReward(userMoneyService.querySubReward(userId));
		teamViewBO.setIndirectReward(userMoneyService.queryIndirectReward(userId));
		//间推奖励
		return teamViewBO;
	}

	/**
	 * 绑定邀请人
	 * @param req 邀请信息
	 * @return
	 */
	@Override
	@RedisLock(value = RedisConstant.LockConstant.USER_LOGIN, param = "#req.userId")
	@Transactional(rollbackFor = Exception.class)
	public ResultPista bindInviteUser(BindInviteUserReq req) {
		return ResultPista.success();
	}

	/**
	 * 我的直推用户
	 * @return
	 */
	@Override
	public PageInfo<MyDirectMemberDto> listSubMembers(Integer pageIndex, Integer pageSize,Integer gameLevel) {
		if(pageIndex == null){
			pageIndex =1;
		}
		if(pageSize == null || pageSize <= 0 || pageSize > 2000){
			pageSize =10;
		}
		PageHelper.startPage(pageIndex, pageSize);
		List<UserInfo> userInfoList = userInfoServiceImpl.lambdaQuery()
			.eq(UserInfo::getInviteUserId, SecurityUtils.getLoginAppUser().getUserId())
			.select(UserInfo::getAccount, UserInfo::getCreateTime, UserInfo::getUserId, UserInfo::getUmbrellaNodePerformance, UserInfo::getSubUmbrellaNodePerformance,
				UserInfo::getMinGameLevel, UserInfo::getGameLevel, UserInfo::getCommunityPerformance,
				UserInfo::getSubNum, UserInfo::getUmbrellaNum, UserInfo::getUmbrellaPerformance,UserInfo::getPerformance,
				UserInfo::getNodeLevel,UserInfo::getNodeTeamPerformance,UserInfo::getSubNodePerformance)
			.list();

		PageInfo<UserInfo> userInfoPageInfo = new PageInfo<>(userInfoList);
		List<MyDirectMemberDto> result =new ArrayList<>();
		if(CollectionUtil.isNotEmpty(userInfoList)){
			//查询单个人买的业绩
			Set<Long> directIds = userInfoList.stream().map(UserInfo::getUserId).collect(Collectors.toSet());
			Map<Long, BigDecimal> userPerformanceMap = nodePackageOrderService.lambdaQuery()
				.in(NodePackageOrder::getUserId, directIds)
				.select(NodePackageOrder::getUserId, NodePackageOrder::getOrderValueUsdt)
				.list().stream().collect(Collectors.toMap(NodePackageOrder::getUserId, NodePackageOrder::getOrderValueUsdt, (k1, k2) -> k2));

			result = userInfoList
				.stream().map(record -> {
					MyDirectMemberDto entity = new MyDirectMemberDto();
					entity.setUserId(record.getUserId());
					entity.setAccount(record.getAccount());
					entity.setGameLevel(record.getGameLevel()>record.getMinGameLevel()?record.getGameLevel():record.getMinGameLevel());
					entity.setNodeLevel(record.getNodeLevel());
					entity.setSubNum(record.getSubNum());
					entity.setUmbrellaNodePerformance(userPerformanceMap.getOrDefault(record.getUserId(),BigDecimal.ZERO).add(record.getUmbrellaNodePerformance()));
					entity.setSubUmbrellaNodePerformance(record.getSubUmbrellaNodePerformance());
					entity.setUmbrellaNum(record.getUmbrellaNum());
					entity.setNodeTeamPerformance(record.getNodeTeamPerformance());
					entity.setSubNodePerformance(record.getSubNodePerformance());
//				entity.setPerformance(record.getPerformance());
//				entity.setUmbrellaPerformance(record.getUmbrellaPerformance());
					entity.setCreateTime(record.getCreateTime());
					//entity.setCommunityPerformance(record.getCommunityPerformance());
					return entity;
				}).collect(Collectors.toList());
		}


		PageInfo<MyDirectMemberDto> pageInfo = new PageInfo<>();
		BeanUtil.copyProperties(userInfoPageInfo, pageInfo);
		pageInfo.setList(result);
		return pageInfo;
	}


	/**
	 * 我的团队数据
	 * @return
	 */
	@Override
	public List<MyDirectMemberDto> listMyDirectMembers() {
//		Long userId = SecurityUtils.getLoginAppUser().getUserId();
//		List<UserInfo> childUsers = userInfoMapper.getTeamMembersLimited(userId,null);
//		if (CollectionUtil.isEmpty(childUsers)) {
//			return Collections.emptyList();
//		}
//		return childUsers.stream().map(childInfo -> {
//			MyDirectMemberDto entity = new MyDirectMemberDto();
//			//用户id
//			entity.setUserId(childInfo.getUserId());
//			//直推人数
//			entity.setSubNum(childInfo.getSubNum());
//			//团队人数
//			entity.setUmbrellaNum(childInfo.getUmbrellaNum());
//			//团队有效人数
//			entity.setValidUmbrellaNum(childInfo.getValidUmbrellaNum());
//			//个人节点
//			entity.setPerformance(childInfo.getPerformance());
//			//团队节点
//			entity.setUmbrellaPerformance(childInfo.getUmbrellaPerformance());
//			entity.setAccount(childInfo.getAccount());
//			entity.setGameLevel(childInfo.getGameLevel());
//			entity.setCreateTime(childInfo.getCreateTime());
//			return entity;
//		}).collect(Collectors.toList());
		return null;
	}

	/**
	 * 我的团队数据 总成员、直推人数、团队销毁usdt、等级
	 * @param lastId lastId
	 * @param distance 层级
	 * @param level level
	 * @return
	 */
	@Override
	public MyTeamMemberPageDto listMyTeamMembers(Long lastId, Integer distance,Integer level) {
		Long userId = SecurityUtils.getLoginAppUser().getUserId();
		List<Long> levelUserIds = null;
		if (level != null) {
			levelUserIds = userInfoServiceImpl.lambdaQuery()
				.eq(UserInfo::getGameLevel, level)
				.select(UserInfo::getUserId)
				.list().stream()
				.map(UserInfo::getUserId)
				.filter(Objects::nonNull)
				.collect(Collectors.toList());
			if (CollectionUtil.isEmpty(levelUserIds)) {
				MyTeamMemberPageDto empty = new MyTeamMemberPageDto();
				empty.setTotal(0L);
				empty.setRecords(Collections.emptyList());
				return empty;
			}
		}

		LambdaQueryWrapper<UserRelation> countWrapper = new LambdaQueryWrapper<>();
		countWrapper.eq(UserRelation::getParUserId, userId)
			.eq(UserRelation::getActiveFlag, 1)
			.gt(UserRelation::getDistance, 0);
		if (distance != null) {
			countWrapper.eq(UserRelation::getDistance, distance);
		}
		if (levelUserIds != null) {
			countWrapper.in(UserRelation::getPosUserId, levelUserIds);
		}
		long total = userRelationService.count(countWrapper);

		LambdaQueryWrapper<UserRelation> pageWrapper = new LambdaQueryWrapper<>();
		pageWrapper.eq(UserRelation::getParUserId, userId)
			.eq(UserRelation::getActiveFlag, 1)
			.gt(UserRelation::getDistance, 0);
		if (distance != null) {
			pageWrapper.eq(UserRelation::getDistance, distance);
		} else {
			pageWrapper.orderByAsc(UserRelation::getDistance);
		}
		if (levelUserIds != null) {
			pageWrapper.in(UserRelation::getPosUserId, levelUserIds);
		}
		if (lastId != null) {
			UserRelation lastRelation = userRelationService.lambdaQuery()
				.eq(UserRelation::getParUserId, userId)
				.eq(UserRelation::getPosUserId, lastId)
				.eq(UserRelation::getActiveFlag, 1)
				.select(UserRelation::getId)
				.orderByDesc(UserRelation::getId)
				.last("limit 1")
				.one();
			if (lastRelation != null) {
				pageWrapper.lt(UserRelation::getId, lastRelation.getId());
			}
		}
		pageWrapper.orderByDesc(UserRelation::getId).last(SysConstant.PAGE_LIMIT);

		List<UserRelation> relationList = userRelationService.list(pageWrapper);
		if (CollectionUtil.isEmpty(relationList)) {
			MyTeamMemberPageDto empty = new MyTeamMemberPageDto();
			empty.setTotal(total);
			empty.setRecords(Collections.emptyList());
			return empty;
		}

		List<Long> childUserIds = relationList.stream()
			.map(UserRelation::getPosUserId)
			.filter(Objects::nonNull)
			.distinct()
			.collect(Collectors.toList());
		if (CollectionUtil.isEmpty(childUserIds)) {
			MyTeamMemberPageDto empty = new MyTeamMemberPageDto();
			empty.setTotal(total);
			empty.setRecords(Collections.emptyList());
			return empty;
		}

		LambdaQueryChainWrapper<UserInfo> childQuery = userInfoServiceImpl.lambdaQuery()
			.in(UserInfo::getUserId, childUserIds)
			.select(UserInfo::getUserId, UserInfo::getAccount, UserInfo::getGameLevel,
				UserInfo::getUmbrellaPerformance,UserInfo::getCreateTime);
		if (level != null) {
			childQuery.eq(UserInfo::getGameLevel, level);
		}
		List<UserInfo> childUsers = childQuery.list();
		Map<Long, UserInfo> childUserMap = childUsers.stream()
			.collect(Collectors.toMap(UserInfo::getUserId, Function.identity(), (a, b) -> a));

		List<MyTeamMemberDto> result = new ArrayList<>(relationList.size());
		for (UserRelation relation : relationList) {
			UserInfo child = childUserMap.get(relation.getPosUserId());
			if (child == null) {
				continue;
			}
			MyTeamMemberDto dto = new MyTeamMemberDto();
			dto.setUserId(child.getUserId());
			dto.setAccount(child.getAccount());
			dto.setGameLevel(child.getGameLevel());
			dto.setUmbrellaPerformance(child.getUmbrellaPerformance());
			dto.setCreateTime(child.getCreateTime());
			dto.setDistance(relation.getDistance());
			result.add(dto);
		}
		MyTeamMemberPageDto dto = new MyTeamMemberPageDto();
		dto.setTotal(total);
		dto.setRecords(result);
		return dto;
	}

	/**
	 * 我的团队数据
	 *
	 * 根据当前用户的真实等级、赠送等级、后台管理等级取最大等级，并查询下一档等级配置，
	 * 返回个人托管和团队托管升级进度；收益字段当前只汇总全球分红，团队收益和间推收益按确认口径返回0。
	 *
	 * @param userId
	 * @return 我的团队页面展示数据，金额单位为USDT，进度单位为%
	 */
	@Override
	public MyTeamInfoDto myTeamInfo(Long userId) {
		UserInfo userInfo = userInfoServiceImpl.lambdaQuery()
			.eq(UserInfo::getUserId, userId)
			.one();
		if (userInfo == null) {
			throw new ServiceException("用户不存在");
		}

		int currentLevel = effectiveLevel(userInfo);
		int targetLevel = currentLevel >= 9 ? 9 : currentLevel + 1;
		UserLevelConfig targetConfig = userLevelConfigService.lambdaQuery()
			.eq(UserLevelConfig::getLevel, targetLevel)
			.last("limit 1")
			.one();

		BigDecimal selfHostingAmount = defaultAmount(userInfo.getPerformance());
		BigDecimal teamHostingAmount = defaultAmount(userInfo.getUmbrellaPerformance());
		BigDecimal targetSelfHostingAmount = targetConfig == null ? BigDecimal.ZERO : defaultAmount(targetConfig.getPerformance());
		BigDecimal targetTeamHostingAmount = targetConfig == null ? BigDecimal.ZERO : defaultAmount(targetConfig.getCommunityPerformance());
		StakeHostingUserRewardSummary rewardSummary = stakeHostingUserRewardSummaryService.getByUserId(userId);
		BigDecimal diffRewardAmount = rewardSummary == null ? BigDecimal.ZERO : defaultAmount(rewardSummary.getDiffRewardAmount());
		BigDecimal sameLevelRewardAmount = rewardSummary == null ? BigDecimal.ZERO : defaultAmount(rewardSummary.getSameLevelRewardAmount());
		BigDecimal globalDividendAmount = rewardSummary == null ? BigDecimal.ZERO : defaultAmount(rewardSummary.getGlobalDividendAmount());

		MyTeamInfoDto dto = new MyTeamInfoDto();
		dto.setCurrentLevel(currentLevel);
		dto.setTargetLevel(targetLevel);
		dto.setSelfHostingAmount(selfHostingAmount);
		dto.setTargetSelfHostingAmount(targetSelfHostingAmount);
		dto.setSelfHostingNeedAmount(needAmount(selfHostingAmount, targetSelfHostingAmount));
		dto.setSelfHostingProgress(progressPercent(selfHostingAmount, targetSelfHostingAmount));
		dto.setTeamHostingAmount(teamHostingAmount);
		dto.setTargetTeamHostingAmount(targetTeamHostingAmount);
		dto.setTeamHostingNeedAmount(needAmount(teamHostingAmount, targetTeamHostingAmount));
		dto.setTeamHostingProgress(progressPercent(teamHostingAmount, targetTeamHostingAmount));
		dto.setTeamUserCount(userInfo.getUmbrellaNum());
		dto.setDirectUserCount(userInfo.getSubNum());
		dto.setTeamRewardAmount(diffRewardAmount.add(sameLevelRewardAmount));
		dto.setGlobalDividendAmount(globalDividendAmount);
		dto.setTeamTotalHostingAmount(teamHostingAmount);
		dto.setSelfTotalHostingAmount(selfHostingAmount);
		return dto;
	}

	/**
	 * 查询当前用户今日托管基础静态日利率。
	 *
	 * <p>该接口用于App简单展示，不发放收益。查询时会先补齐当天G7快照，
	 * 只返回今日G值和当前命中的基础静态日利率。</p>
	 *
	 * @param userId 用户ID
	 * @return 当前G值和托管基础静态日利率，单位均为%
	 */
	@Override
	public CurrentStakeHostingStaticRateDto currentStakeHostingStaticRate(Long userId) {
		UserInfo userInfo = userInfoServiceImpl.lambdaQuery()
			.eq(UserInfo::getUserId, userId)
			.eq(UserInfo::getDeleted, 0)
			.one();
		if (userInfo == null) {
			throw new ServiceException("用户不存在");
		}
		Integer rewardDay = Integer.valueOf(DateUtil.format(DateUtil.date(), "yyyyMMdd"));
		CurrentStakeHostingStaticRateDto dto = new CurrentStakeHostingStaticRateDto();
		dto.setRewardDay(rewardDay);

		// 查询展示利率前先补齐G7快照，前端只需要今日G值和当前命中的基础日利率。
		stakeHostingDailyTeamPerformanceService.prepareDailySnapshots(rewardDay, Collections.singletonList(userId));
		StakeHostingDailyTeamPerformance snapshot = stakeHostingDailyTeamPerformanceService.getCalculatedSnapshot(userId, rewardDay);
		dto.setGDay(snapshot == null ? BigDecimal.ZERO : scaleRate(snapshot.getGDay()));

		// 后台长期指定收益率优先级最高；G值仍取当天快照中的G_day用于展示。
		if (userInfo.getStakeHostingStaticRate() != null && userInfo.getStakeHostingStaticRate().compareTo(BigDecimal.ZERO) > 0) {
			dto.setCurrentStaticRate(scaleRate(userInfo.getStakeHostingStaticRate()));
			return dto;
		}
		if (!hasG7NewPerformanceSnapshot(snapshot)) {
			dto.setCurrentStaticRate(scaleRate(getPureStaticRatePercent(userId)));
			return dto;
		}
		if (snapshot.getBaseStaticRate() == null) {
			dto.setCurrentStaticRate(PLACEHOLDER_STATIC_RATE_PERCENT);
			return dto;
		}
		dto.setCurrentStaticRate(scaleRate(snapshot.getBaseStaticRate()));
		return dto;
	}
	/**
	 * 判断用户当天是否存在可用于G7计算的团队新增业绩。
	 *
	 * <p>今日新增为0但昨日新增大于0时会产生负增长，仍需要按G7快照展示，不能走未推广纯静态。</p>
	 *
	 * @param snapshot G7每日团队新增业绩快照
	 * @return true表示应按G7快照展示
	 */
	private boolean hasG7NewPerformanceSnapshot(StakeHostingDailyTeamPerformance snapshot) {
		if (snapshot == null) {
			return false;
		}
		BigDecimal previousTeamNewAmount = snapshot.getPreviousTeamTvl() == null ? BigDecimal.ZERO : snapshot.getPreviousTeamTvl();
		BigDecimal currentTeamNewAmount = snapshot.getCurrentTeamTvl() == null ? BigDecimal.ZERO : snapshot.getCurrentTeamTvl();
		return previousTeamNewAmount.compareTo(BigDecimal.ZERO) > 0 || currentTeamNewAmount.compareTo(BigDecimal.ZERO) > 0;
	}

	/**
	 * 获取未推广纯静态展示收益率。
	 *
	 * <p>如果用户存在未回本的产出中托管订单，展示回本前0.5%；如果产出中订单均已回本，展示回本后0.2%。
	 * 用户没有产出中托管订单时，按回本前基础档0.5%展示。</p>
	 *
	 * @param userId 用户ID
	 * @return 纯静态收益率，单位%
	 */
	private BigDecimal getPureStaticRatePercent(Long userId) {
		long runningCount = stakeHostingOrderService.lambdaQuery()
			.eq(StakeHostingOrder::getUserId, userId)
			.eq(StakeHostingOrder::getPayStatus, StakeHostingOrderServiceImpl.PAY_SUCCESS)
			.eq(StakeHostingOrder::getStatus, StakeHostingOrderServiceImpl.STATUS_RUNNING)
			.eq(StakeHostingOrder::getDeleted, 0)
			.count();
		if (runningCount <= 0) {
			return PURE_STATIC_RATE_BEFORE_RETURN_PERCENT;
		}
		long unreturnedCount = stakeHostingOrderService.lambdaQuery()
			.eq(StakeHostingOrder::getUserId, userId)
			.eq(StakeHostingOrder::getPayStatus, StakeHostingOrderServiceImpl.PAY_SUCCESS)
			.eq(StakeHostingOrder::getStatus, StakeHostingOrderServiceImpl.STATUS_RUNNING)
			.eq(StakeHostingOrder::getDeleted, 0)
			.and(wrapper -> wrapper.isNull(StakeHostingOrder::getIsReturnPrincipal)
				.or()
				.ne(StakeHostingOrder::getIsReturnPrincipal, 1))
			.count();
		return unreturnedCount > 0 ? PURE_STATIC_RATE_BEFORE_RETURN_PERCENT : PURE_STATIC_RATE_AFTER_RETURN_PERCENT;
	}

	/**
	 * 统一格式化收益率百分比。
	 *
	 * @param ratePercent 收益率，单位%
	 * @return 保留4位小数的收益率；空值返回0
	 */
	private BigDecimal scaleRate(BigDecimal ratePercent) {
		return defaultAmount(ratePercent).setScale(4, RoundingMode.HALF_UP);
	}

	/**
	 * 获取用户托管展示等级。
	 *
	 * 当前 App 团队页面按真实等级、赠送等级、后台管理等级三者的最大值展示用户等级，
	 * 空等级按0处理。
	 *
	 * @param userInfo 用户信息
	 * @return 最大有效等级编码
	 */
	private int effectiveLevel(UserInfo userInfo) {
		return Math.max(userInfo.getGameLevel(), Math.max(userInfo.getMinGameLevel(), userInfo.getAdminGameLevel()));
	}

	/**
	 * 计算升级还需金额。
	 *
	 * 当前值已达到或超过目标值时返回0，避免页面展示负数。
	 *
	 * @param current 当前金额
	 * @param target 目标金额
	 * @return 还需金额，单位USDT
	 */
	private BigDecimal needAmount(BigDecimal current, BigDecimal target) {
		BigDecimal need = defaultAmount(target).subtract(defaultAmount(current));
		return need.compareTo(BigDecimal.ZERO) > 0 ? need : BigDecimal.ZERO;
	}

	/**
	 * 计算升级进度百分比。
	 *
	 * 目标值为空或小于等于0时返回0；当前值超过目标值时封顶100。
	 *
	 * @param current 当前金额
	 * @param target 目标金额
	 * @return 进度百分比，单位%
	 */
	private BigDecimal progressPercent(BigDecimal current, BigDecimal target) {
		BigDecimal targetAmount = defaultAmount(target);
		if (targetAmount.compareTo(BigDecimal.ZERO) <= 0) {
			return BigDecimal.ZERO;
		}
		BigDecimal progress = defaultAmount(current)
			.multiply(new BigDecimal("100"))
			.divide(targetAmount, 2, RoundingMode.HALF_UP);
		return progress.compareTo(new BigDecimal("100")) > 0 ? new BigDecimal("100") : progress;
	}

	private BigDecimal defaultAmount(BigDecimal amount) {
		return amount == null ? BigDecimal.ZERO : amount;
	}


	/**
	 * 初始化用户托管奖励累计汇总。
	 *
	 * 新注册用户默认生成一条0金额汇总记录，方便 App 团队页直接读取团队收益和全球分红；
	 * 方法内部使用 insert ignore，重复调用不会覆盖历史累计值。
	 *
	 * @param userId 用户ID
	 */
	private void initStakeHostingRewardSummary(Long userId) {
		stakeHostingUserRewardSummaryService.initUser(userId);
	}

	/**
	 * 构建团队等级分布（含全网对比数据）
	 */
	private List<TeamLevelDto> buildTeamLevelDistribution(Long userId) {
		List<Long> teamUserIds = userRelationService.lambdaQuery()
			.eq(UserRelation::getParUserId, userId)
			.eq(UserRelation::getActiveFlag, 1)
			.gt(UserRelation::getDistance, 0)
			.select(UserRelation::getPosUserId)
			.list()
			.stream()
			.map(UserRelation::getPosUserId)
			.filter(Objects::nonNull)
			.distinct()
			.collect(Collectors.toList());

		Map<Integer, Integer> teamCountMap = new HashMap<>();
		if (CollectionUtil.isNotEmpty(teamUserIds)) {
			List<Map<String, Object>> teamRows = userInfoServiceImpl.getBaseMapper().selectMaps(
				new QueryWrapper<UserInfo>()
					.in("user_id", teamUserIds)
					.select("game_level", "COUNT(1) AS cnt")
					.groupBy("game_level")
			);
			fillLevelCount(teamRows, teamCountMap);
		}

		Map<Integer, Integer> globalCountMap = new HashMap<>();
		List<Map<String, Object>> globalRows = userInfoServiceImpl.getBaseMapper().selectMaps(
			new QueryWrapper<UserInfo>()
				.select("game_level", "COUNT(1) AS cnt")
				.groupBy("game_level")
		);
		fillLevelCount(globalRows, globalCountMap);

		Set<Integer> levelSet = new TreeSet<>();
		levelSet.addAll(teamCountMap.keySet());
		levelSet.addAll(globalCountMap.keySet());

		List<TeamLevelDto> result = new ArrayList<>(levelSet.size());
		for (Integer level : levelSet) {
			TeamLevelDto dto = new TeamLevelDto();
			dto.setGameLevel(level);
			dto.setTeamCount(teamCountMap.getOrDefault(level, 0));
			dto.setGlobalCount(globalCountMap.getOrDefault(level, 0));
			result.add(dto);
		}
		return result;
	}

	private void fillLevelCount(List<Map<String, Object>> rows, Map<Integer, Integer> targetMap) {
		if (CollectionUtil.isEmpty(rows)) {
			return;
		}
		for (Map<String, Object> row : rows) {
			Object levelObj = row.get("game_level");
			Object countObj = row.get("cnt");
			if (!(levelObj instanceof Number) || !(countObj instanceof Number)) {
				continue;
			}
			Integer level = ((Number) levelObj).intValue();
			Integer count = ((Number) countObj).intValue();
			targetMap.put(level, count);
		}
	}


	/**
	 * 计算涨跌幅：(current - last) / last * 100
	 */
	private BigDecimal calcChangeRate(BigDecimal current, BigDecimal last) {
		if (current == null || last == null || last.compareTo(BigDecimal.ZERO) <= 0) {
			return BigDecimal.ZERO;
		}
		return current
			.subtract(last)
			.divide(last, ConstantStatic.newScale, ConstantStatic.roundingModeNew)
			.multiply(new BigDecimal("100"));
	}

	@Override
	@RedisLock(value = RedisConstant.LockConstant.USER_LOGIN, param = "#loginVo.address")
	@Transactional(rollbackFor = Exception.class)
	public ResultPista<LoginAppUser> login(LoginVo loginVo) {
		//验签，随机数
		checkWallet(loginVo.getRandomNum(), loginVo.getSignature(), loginVo.getAddress(), xmsRedis);
		UserInfo userInfo;
		//根据注册钱包地址查询
		userInfo = userInfoServiceImpl.lambdaQuery().eq(UserInfo::getAccount, loginVo.getAddress()).one();
		if (userInfo == null) {
			UserInfo inviteUser;
			if (StringUtils.isBlank(loginVo.getInviteCode())) {
				throw new ServiceException(ResponseCode.CODE_1010);
			} else {
				inviteUser = userInfoServiceImpl.lambdaQuery().eq(UserInfo::getAccount, loginVo.getInviteCode()).one();
			}
			if (inviteUser == null) {
				throw new ServiceException(ResponseCode.CODE_1010);
			}
			//查询上级团队用户
			List<UserRelation> urList = userRelationService.getParentList(inviteUser.getUserId());
			List<Long> parentUserIds = urList.stream().map(UserRelation::getParUserId).collect(Collectors.toList());
			//父级链
			String parentChain;
			if (StringUtils.isBlank(inviteUser.getParentChain())) {
				parentChain = String.valueOf(inviteUser.getUserId());
			} else {
				parentChain = inviteUser.getParentChain() + "," + inviteUser.getUserId();
			}
			//新增用户
			userInfo = UserInfo.builder()
				//账号
				.account(loginVo.getAddress())
				//用户编码
				.userCode(RandomUtil.randomNumbers(10))
				.gameLevel(SysConstant.ZERO)
				//保底等级
				.minGameLevel(SysConstant.ZERO)
				//用户名密码 存的是md5然后盐加密之后的密码
				.inviteUserId(inviteUser.getUserId())
				.inviteUserCode(inviteUser.getUserCode())
				.isValid(SysConstant.ZERO)
				.subNum(SysConstant.ZERO)
				.validSubNum(SysConstant.ZERO)
				.umbrellaNum(SysConstant.ZERO)
				.validUmbrellaNum(SysConstant.ZERO)
				.performance(BigDecimal.ZERO)
				.umbrellaPerformance(BigDecimal.ZERO)
				.parentChain(parentChain)
				.withdrawalOpenOrClose(SysConstant.TWO)
				.status(SysConstant.ONE)
				.build();
			boolean res = userInfoServiceImpl.save(userInfo);
			if (!res) {
				TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
				throw new ServiceException(ResponseCode.CODE_1002);
			}
			initStakeHostingRewardSummary(userInfo.getUserId());

			//更新上级直推人数
			userInfoServiceImpl.lambdaUpdate().setSql(" sub_num = sub_num + 1 ")
				.eq(UserInfo::getUserId, inviteUser.getUserId()).update();
			if (parentUserIds.size() > 0) {
				//更新上级团队人数
				userInfoServiceImpl.lambdaUpdate().setSql(" umbrella_num = umbrella_num + 1")
					.in(UserInfo::getUserId, parentUserIds).update();
			}
			//创建新系统钱包
			UserMoney userMoney = UserMoney.builder().id(userInfo.getUserId()).build();
			userMoneyService.save(userMoney);

			//新增关系表
			List<UserRelation> dataList = Lists.newArrayList();
			UserRelation ur = UserRelation.builder().parUserId(userInfo.getUserId())
				.posUserId(userInfo.getUserId()).distance(0).build();
			dataList.add(ur);//新增自己
			for (UserRelation temp : urList) {
				//限制最多200层
				if (temp.getDistance() + 1 > 200) {
					throw new ServiceException(ResponseCode.CODE_1064);
				}
				UserRelation urPar = UserRelation.builder().parUserId(temp.getParUserId())
					.posUserId(userInfo.getUserId()).distance(temp.getDistance() + 1).build();
				dataList.add(urPar);
			}

			// 批量插入
			boolean b = userRelationService.saveBatch(dataList);
			if (!b) {
				TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
				throw new ServiceException(ResponseCode.CODE_1003);
			}

		} else {
			if (!userInfo.getStatus().equals(SysConstant.ONE)) {
				throw new ServiceException(ResponseCode.CODE_401);
			}
		}

		//记录用户登录IP地址
		recordUserLoginIp(userInfo);
		//删除随机数验证
		xmsRedis.del(ConstantStatic.USER_RANDOM + loginVo.getAddress());
		return getLoginAppUserResult(userInfo, appTokenService, Constants.TOKEN_APP_PREFIX);
	}

	@Override
	public ResultPista<LoginAppUser> login(BatchUserBo req) {

		UserInfo userInfo;
		//根据注册钱包地址查询
		userInfo = userInfoServiceImpl.lambdaQuery().eq(UserInfo::getAccount, req.getWalletAddress()).one();
		if (userInfo == null) {
			UserInfo inviteUser;
			if (StringUtils.isBlank(req.getParentWalletAddress())) {
				throw new ServiceException("邀请用户不存在");
			} else {
				inviteUser = userInfoServiceImpl.lambdaQuery().eq(UserInfo::getAccount, req.getParentWalletAddress()).one();
			}
			if (inviteUser == null) {
				throw new ServiceException("邀请用户不存在"+req);
			}
			//查询上级团队用户
			List<UserRelation> urList = userRelationService.getParentList(inviteUser.getUserId());
			List<Long> parentUserIds = urList.stream().map(UserRelation::getParUserId).collect(Collectors.toList());
			//父级链
			String parentChain;
			if (StringUtils.isBlank(inviteUser.getParentChain())) {
				parentChain = String.valueOf(inviteUser.getUserId());
			} else {
				parentChain = inviteUser.getParentChain() + "," + inviteUser.getUserId();
			}
			//新增用户
			userInfo = UserInfo.builder()
				//账号
				.account(req.getWalletAddress())
				//用户编码
				.userCode(RandomUtil.randomNumbers(10))
				.gameLevel(SysConstant.ZERO)
				//保底等级
				.minGameLevel(SysConstant.ZERO)
				//用户名密码 存的是md5然后盐加密之后的密码
				.inviteUserId(inviteUser.getUserId())
				.inviteUserCode(inviteUser.getUserCode())
				.isValid(SysConstant.ZERO)
				.subNum(SysConstant.ZERO)
				.validSubNum(SysConstant.ZERO)
				.umbrellaNum(SysConstant.ZERO)
				.validUmbrellaNum(SysConstant.ZERO)
				.performance(BigDecimal.ZERO)
				.umbrellaPerformance(BigDecimal.ZERO)
				.parentChain(parentChain)
				.withdrawalOpenOrClose(SysConstant.TWO)
				.status(SysConstant.ONE)
				.build();
			boolean res = userInfoServiceImpl.save(userInfo);
			if (!res) {
				TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
				throw new ServiceException(ResponseCode.CODE_1002);
			}
			initStakeHostingRewardSummary(userInfo.getUserId());

			//更新上级直推人数
			userInfoServiceImpl.lambdaUpdate().setSql(" sub_num = sub_num + 1 ")
				.eq(UserInfo::getUserId, inviteUser.getUserId()).update();
			if (parentUserIds.size() > 0) {
				//更新上级团队人数
				userInfoServiceImpl.lambdaUpdate().setSql(" umbrella_num = umbrella_num + 1")
					.in(UserInfo::getUserId, parentUserIds).update();
			}
			//创建新系统钱包
			UserMoney userMoney = UserMoney.builder().id(userInfo.getUserId()).build();
			userMoneyService.save(userMoney);

			//新增关系表
			List<UserRelation> dataList = Lists.newArrayList();
			UserRelation ur = UserRelation.builder().parUserId(userInfo.getUserId())
				.posUserId(userInfo.getUserId()).distance(0).build();
			dataList.add(ur);//新增自己
			for (UserRelation temp : urList) {
				//限制最多200层
				if (temp.getDistance() + 1 > 200) {
					throw new ServiceException(ResponseCode.CODE_1064);
				}
				UserRelation urPar = UserRelation.builder().parUserId(temp.getParUserId())
					.posUserId(userInfo.getUserId()).distance(temp.getDistance() + 1).build();
				dataList.add(urPar);
			}

			// 批量插入
			boolean b = userRelationService.saveBatch(dataList);
			if (!b) {
				TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
				throw new ServiceException(ResponseCode.CODE_1003);
			}

		} else {
			if (!userInfo.getStatus().equals(SysConstant.ONE)) {
				throw new ServiceException(ResponseCode.CODE_401);
			}
		}

		return ResultPista.success();
	}

	/**
	 * 记录用户登录IP地址
	 * <p>
	 * 该方法会将用户每次登录的IP地址和时间记录到用户信息中，
	 * 最多保留最近5次的登录IP记录，格式为"时间/IP地址"。
	 * </p>
	 *
	 * @param userInfo 用户信息对象，包含用户的基本信息和登录IP记录
	 */
	private void recordUserLoginIp(UserInfo userInfo) {
		//记录登录ip
		String resIp = IpUtils.getIpAddr(ServletUtils.getRequest());
		if(StrUtil.isBlank(userInfo.getLastLoginIp())){
			userInfoServiceImpl.lambdaUpdate()
				.eq(UserInfo::getUserId, userInfo.getUserId())
				.set(UserInfo::getLastLoginIp, DateUtil.now()+"/"+resIp)
				.update();
		}else{
			List<String> resIpList = StrUtil.split(userInfo.getLastLoginIp(), ',')
				.stream()
				.map(String::trim) // 去掉空格
				.filter(s -> !s.isEmpty())
				.collect(Collectors.toList());
			resIpList.add(DateUtil.now()+"/"+resIp);
			// 保持最多5个IP记录，如果超过则移除最旧的（列表开头的）
			if(resIpList.size() > 5) {
				resIpList = resIpList.subList(resIpList.size() - 5, resIpList.size());
			}
			String resIpListStr = String.join(",", resIpList);
			userInfoServiceImpl.lambdaUpdate()
				.eq(UserInfo::getUserId, userInfo.getUserId())
				.set(UserInfo::getLastLoginIp, resIpListStr)
				.update();
		}
	}

	@Override
	public String getMessage(String address) {
		String radom = IdUtil.randomUUID();
		xmsRedis.set(ConstantStatic.USER_RANDOM + address + radom, radom, SysConstant.FIVE_LONG, TimeUnit.MINUTES);
		log.info(" address:{},radom:{} ", address, radom);
		return radom;
	}

	/**
	 * 注册
	 * @param req
	 * @return
	 * @throws Exception
	 */
	@Override
	public ResultPista register(RegisterSmsVo req) throws Exception {
		//账号校验
		verifyAccount(req.getAccount());
		Long count = userInfoServiceImpl.lambdaQuery()
			.eq(UserInfo::getAccount, req.getAccount())
			.count();
		if(count>0){
			throw new ServiceException(ResponseCode.CODE_1103);
		}

		//校验邮箱格式是否正确
		if (!Validator.isEmail(req.getEmail())) {
			throw new ServiceException(ResponseCode.CODE_1215);
		}
		 count = userInfoServiceImpl.lambdaQuery()
			.eq(UserInfo::getEmail, req.getEmail())
			.count();
		if(count>0){
			//throw new ServiceException(ResponseCode.CODE_1214);
		}

		UserInfo inviteUserInfo = userInfoServiceImpl.lambdaQuery()
			.eq(UserInfo::getUserCode, req.getInviteUserCode())
			.eq(UserInfo::getDeleted,0)
			.one();
		if(inviteUserInfo == null){
			throw new ServiceException(ResponseCode.CODE_1104);
		}
		//校验验证码是否正确
		verifyCode(req.getEmail(), req.getCode(), SysConstant.ONE, req.getUuid(), xmsRedis, sysParaServiceImpl);
		//注册
		return ResultPista.data(SpringUtils.getBean(BizUserServiceImpl.class).realRegister(req,inviteUserInfo));
	}

	@Override
	public void bindEmail(BindEmailVo req) {
	/*	//校验邮箱格式是否正确
		if (!Validator.isEmail(req.getEmail())) {
			throw new ServiceException(ResponseCode.CODE_1215);
		}

		long count = userInfoServiceImpl.lambdaQuery()
			.eq(UserInfo::getEmail, req.getEmail())
			.count();
		if(count>0){
			throw new ServiceException(ResponseCode.CODE_1214);
		}

		//校验验证码是否正确
		verifyCode(req.getEmail(), req.getCode(), SysConstant.TWO, req.getUuid(), xmsRedis, sysParaServiceImpl);
		UserInfo queryUserInfo = userInfoServiceImpl.lambdaQuery()
			.eq(UserInfo::getUserId, SecurityUtils.getLoginAppUser().getUserId())
			.select(UserInfo::getEmail,UserInfo::getUserId)
			.one();
		if(StrUtil.isNotBlank(queryUserInfo.getEmail())){
			throw new ServiceException(ResponseCode.CODE_1216);
		}
		//绑定
		boolean update = userInfoServiceImpl.lambdaUpdate()
			.eq(UserInfo::getUserId, SecurityUtils.getLoginAppUser().getUserId())
			.set(UserInfo::getEmail, req.getEmail())
			.update();
		if(!update){
			throw new ServiceException(ResponseCode.CODE_1002);
		}*/
	}

	/**
	 * 验证账号是否正确
	 *
	 * @param account
	 * @return
	 */
	private void verifyAccount(String account) {
		if(!isValidAccount(account)){
			//throw new ServiceException(ResponseCode.CODE_1102);
		}
	}

	/**
	 * 验证账号是否为6-16位数字和字母组合
	 *
	 * @param account
	 * @return
	 */
	private boolean isValidAccount(String account) {
		// 允许纯字母或者数字和字母组合
		return account.matches("^[a-zA-Z]{6,16}$") ||
			account.matches("^(?=.*[0-9])(?=.*[a-zA-Z])[0-9a-zA-Z]{6,16}$");
	}

	/**
	 * 注册
	 *
	 * @param req
	 * @param inviteUserInfo  邀请用户
	 * @return
	 * @throws Exception
	 */
	@Transactional(rollbackFor = Exception.class)
	public ResultPista realRegister(RegisterSmsVo req,UserInfo inviteUserInfo) throws Exception {
		//登录密码
		String loginSale = RandomUtil.randomString(8);
		String loginPwd = Md5Utils.hash(req.getLoginPwd() + loginSale);

		//父级链
		String parentChain;
		if (StringUtils.isBlank(inviteUserInfo.getParentChain())) {
			parentChain = String.valueOf(inviteUserInfo.getUserId());
		} else {
			parentChain = inviteUserInfo.getParentChain() + "," + inviteUserInfo.getUserId();
		}
		UserInfo userInfo = UserInfo.builder()
			//账号
			.account(req.getAccount())
			.email(req.getEmail())
			//用户编码
			.userCode(GenUUID.getCode(6))
			.gameLevel(SysConstant.ZERO)
			//保底等级
			.minGameLevel(SysConstant.ZERO)
			.inviteUserId(inviteUserInfo.getUserId())
			.inviteUserCode(inviteUserInfo.getUserCode())
			.isValid(SysConstant.ZERO)
			.subNum(SysConstant.ZERO)
			.validSubNum(SysConstant.ZERO)
			.umbrellaNum(SysConstant.ZERO)
			.validUmbrellaNum(SysConstant.ZERO)
			.performance(BigDecimal.ZERO)
			.umbrellaPerformance(BigDecimal.ZERO)
			.parentChain(parentChain)
			.withdrawalOpenOrClose(SysConstant.TWO)
			.status(SysConstant.ONE)
			.build();

		//查询上级团队用户
		List<UserRelation> urList = userRelationService.getParentList(inviteUserInfo.getUserId());
		List<Long> parentUserIds = urList.stream().map(UserRelation::getParUserId).collect(Collectors.toList());

		try {
			boolean res = userInfoServiceImpl.save(userInfo);
			if (!res) {
				TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
				throw new ServiceException(ResponseCode.CODE_1002);
			}
		}catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(ResponseCode.CODE_1002);
		}
		initStakeHostingRewardSummary(userInfo.getUserId());
		//更新上级直推人数
		userInfoServiceImpl.lambdaUpdate().setSql(" sub_num = sub_num + 1 ")
			.eq(UserInfo::getUserId, inviteUserInfo.getUserId()).update();
		if (parentUserIds.size() > 0) {
			//更新上级团队人数
			userInfoServiceImpl.lambdaUpdate().setSql(" umbrella_num = umbrella_num + 1")
				.in(UserInfo::getUserId, parentUserIds).update();
		}
		//创建新系统钱包
		UserMoney userMoney = UserMoney.builder().id(userInfo.getUserId()).build();
		userMoneyService.save(userMoney);

		//新增关系表
		List<UserRelation> dataList = Lists.newArrayList();
		UserRelation ur = UserRelation.builder().parUserId(userInfo.getUserId())
			.posUserId(userInfo.getUserId()).distance(0).build();
		dataList.add(ur);//新增自己
		for (UserRelation temp : urList) {
			//限制最多100层
			if (temp.getDistance() + 1 > 200) {
				throw new ServiceException(ResponseCode.CODE_1064);
			}
			UserRelation urPar = UserRelation.builder().parUserId(temp.getParUserId())
				.posUserId(userInfo.getUserId()).distance(temp.getDistance() + 1).build();
			dataList.add(urPar);
		}
		// 批量插入
		userRelationService.saveBatch(dataList);
		return registerIM(userInfo);
	}

	/**
	 * 退出登录
	 *
	 * @param request
	 */
	@Override
	public ResultPista logout(HttpServletRequest request) {
		LoginAppUser loginUser = appTokenService.getLoginUser(request);
		if (com.xms.common.utils.StringUtils.isNotNull(loginUser)) {
			// 删除用户缓存记录
			appTokenService.delLoginUser(loginUser.getClientId(), loginUser.getUserId().toString());
		}
		return ResultPista.success();
	}

	/**
	 * 修改用户基础信息
	 * @param req
	 */
	@Override
	public void updateBaseInfo(UserBaseInfoVo req) {
		if(StringUtils.isNotBlank(req.getNickName()) || StringUtils.isNotBlank(req.getAvatar())){
			userInfoServiceImpl.lambdaUpdate()
				.eq(UserInfo::getUserId, SecurityUtils.getLoginAppUser().getUserId())
				.set(StringUtils.isNotBlank(req.getNickName()),UserInfo::getAccount, req.getNickName())
				.set(StringUtils.isNotBlank(req.getAvatar()),UserInfo::getAvatar, req.getAvatar())
				.update();
			UserInfo userInfo = userInfoServiceImpl.lambdaQuery()
				.eq(UserInfo::getUserId, SecurityUtils.getLoginAppUser().getUserId())
				.one();
			//获取域名
			userInfo.setAvatar(sysParaServiceImpl.getValue(ConstantSys.biz_image_domain)+userInfo.getAvatar());
			registerIM(userInfo);
		}
	}




	/**
	 * 发送邮箱验证码
	 * @param req
	 * @return
	 * @throws Exception
	 */
	@Override
	public ResultPista sendMesAuthCode(MesAuthCodeVo req) throws Exception{
		String account = req.getEmail();
		Integer bizType = req.getBizType();
		String uuid = IdUtil.fastUUID();
		String code = RandomUtil.randomNumbers(SysConstant.SIX);
		//发送邮箱验证码
		//0.1 校验邮箱格式
		if (!Validator.isEmail(account)) {
			return ResponseCode.getR(ResponseCode.CODE_1215);
		}
		//0.2 只有当 bizType 不等于 1 且不等于 2 时，才校验邮箱是否已经注册
		if (!bizType.equals(1) && !bizType.equals(2)) {
			// 校验邮箱是否已经注册
			Long count = userInfoServiceImpl.lambdaQuery()
				.eq(UserInfo::getEmail, account)
				.count();
			if (count <= 0) {
				return ResponseCode.getR(ResponseCode.CODE_1007);
			}
		}
		//获取可以用的邮箱
		List<EmailConfig> emailList = xmsRedis.get(RedisConstant.GOOGLE_EMAIL_LIST, () -> emailConfigService.lambdaQuery()
			.eq(EmailConfig::getEnable, 1).list(), RedisConstant.DAY_EXPIRE_TIME, TimeUnit.DAYS);
		if(CollectionUtil.isEmpty(emailList)){
			//throw new ServiceException(ResponseCode.CODE_1213);
		}

		xmsRedis.set(StringUtils.join(RedisConstant.CAPTCHA_SMS, req.getEmail(), RedisConstant.SEPARATOR, bizType, uuid), code, 120L, TimeUnit.SECONDS);
		//随机获取一个邮箱
		EmailConfig selectedEmail = RandomUtil.randomEle(emailList);
		AliyunSenMailUtil.MailInfo mailInfo = new AliyunSenMailUtil.MailInfo();
		mailInfo.setUsername(selectedEmail.getEmail());
		mailInfo.setPassword(selectedEmail.getAppAuthPassword());
		mailInfo.setToUser(account);
		mailInfo.setSubject("sigmaPro");
		mailInfo.setContent(MessageFormat.format("尊敬的客户您好，您本次的验证码为：{0}", code));
		if (!SystemUtil.getOsInfo().getName().toUpperCase().contains(ConstantStatic.OS_NAME_WINDOWS)){
			AliyunSenMailUtil.sendMail(mailInfo);
		}
		return ResultPista.data(uuid);
	}

	/**
 * 标准化字符串：去除多余空格，转小写
 */
private String normalizeString(String str) {
    if (str == null) {
        return "";
    }
    // 将多个连续空格替换为单个空格，去除首尾空格，转小写
    return str.trim().replaceAll("\\s+", " ").toLowerCase();
}
}
