package com.xms.app.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.system.SystemUtil;
import com.xms.app.entity.bo.StakeOrderBo;
import com.xms.app.entity.dto.StakeHostingAfiAccelerateConfigDto;
import com.xms.app.entity.dto.StakeHostingAfiPledgeDto;
import com.xms.app.entity.dto.StakeHostingOrderDto;
import com.xms.app.entity.dto.StakeHostingPackageDto;
import com.xms.app.entity.resp.CreateStakeHostingOrderResp;
import com.xms.app.entity.vo.CreateStakeHostingOrderVo;
import com.xms.app.entity.vo.PledgeStakeHostingAfiVo;
import com.xms.app.service.BizCommonService;
import com.xms.app.service.BizStakeHostingService;
import com.xms.common.config.redis.XmsRedis;
import com.xms.common.constant.SysConstant;
import com.xms.common.core.domain.api.ResultPista;
import com.xms.common.exception.ServiceException;
import com.xms.common.result.ResponseCode;
import com.xms.common.utils.SecurityUtils;
import com.xms.common.utils.SignUtil;
import com.xms.dao.domain.StakeHostingAfiAccelerateConfig;
import com.xms.dao.domain.StakeHostingAfiPledge;
import com.xms.dao.domain.StakeHostingOrder;
import com.xms.dao.domain.StakeHostingPackage;
import com.xms.dao.entity.domain.UserInfo;
import com.xms.dao.service.IStakeHostingAfiAccelerateConfigService;
import com.xms.dao.service.IStakeHostingOrderService;
import com.xms.dao.service.IStakeHostingAfiPledgeService;
import com.xms.dao.service.IStakeHostingPackageService;
import com.xms.dao.service.UserInfoService;
import com.xms.dao.service.impl.StakeHostingOrderServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.xms.app.service.impl.BizUserServiceImpl.checkWallet;

/**
 * 托管业务Service实现
 */
@Service
@Slf4j
public class BizStakeHostingServiceImpl implements BizStakeHostingService {
	private final IStakeHostingPackageService stakeHostingPackageService;
	private final IStakeHostingOrderService stakeHostingOrderService;
	private final IStakeHostingAfiPledgeService stakeHostingAfiPledgeService;
	private final IStakeHostingAfiAccelerateConfigService stakeHostingAfiAccelerateConfigService;
	private final UserInfoService userInfoService;
	private final XmsRedis xmsRedis;
	private final BizCommonService bizCommonService;

	@Value("${lq.md5Key}")
	private String md5Key;

	public BizStakeHostingServiceImpl(IStakeHostingPackageService stakeHostingPackageService,
									  IStakeHostingOrderService stakeHostingOrderService,
									  IStakeHostingAfiPledgeService stakeHostingAfiPledgeService,
									  IStakeHostingAfiAccelerateConfigService stakeHostingAfiAccelerateConfigService,
									  UserInfoService userInfoService,
									  XmsRedis xmsRedis,
									  BizCommonService bizCommonService) {
		this.stakeHostingPackageService = stakeHostingPackageService;
		this.stakeHostingOrderService = stakeHostingOrderService;
		this.stakeHostingAfiPledgeService = stakeHostingAfiPledgeService;
		this.stakeHostingAfiAccelerateConfigService = stakeHostingAfiAccelerateConfigService;
		this.userInfoService = userInfoService;
		this.xmsRedis = xmsRedis;
		this.bizCommonService = bizCommonService;
	}

	/**
	 * 查询 App 托管套餐列表。
	 *
	 * App 只需要展示和创建订单相关字段，因此将数据库套餐对象转换为 DTO，
	 * 避免把状态、排序、删除标记、创建更新时间等后台字段直接返回给前端。
	 *
	 * @return 已上架托管套餐展示列表
	 */
	@Override
	public List<StakeHostingPackageDto> packageList() {
		List<StakeHostingPackage> list = stakeHostingPackageService.lambdaQuery()
			.eq(StakeHostingPackage::getStatus, 1)
			.orderByAsc(StakeHostingPackage::getSort)
			.orderByAsc(StakeHostingPackage::getDays)
			.list();
		if (CollectionUtil.isEmpty(list)) {
			return java.util.Collections.emptyList();
		}
		return list.stream().map(this::toPackageDto).collect(Collectors.toList());
	}

	/**
	 * 转换托管套餐展示 DTO。
	 *
	 * @param item 数据库托管套餐对象
	 * @return App 托管套餐展示对象
	 */
	private StakeHostingPackageDto toPackageDto(StakeHostingPackage item) {
		StakeHostingPackageDto dto = new StakeHostingPackageDto();
		dto.setId(item.getId());
		dto.setName(item.getName());
		dto.setDays(item.getDays());
		dto.setMinAmount(item.getMinAmount());
		dto.setServiceFeeRatio(item.getServiceFeeRatio());
		dto.setPerformanceCoefficient(item.getPerformanceCoefficient());
		return dto;
	}

	@Override
	public ResultPista<CreateStakeHostingOrderResp> createOrder(CreateStakeHostingOrderVo req, Long userId) {
		UserInfo userInfo = userInfoService.lambdaQuery()
			.eq(UserInfo::getUserId, userId)
			.one();
		if (userInfo == null) {
			throw userNotFoundException();
		}
		checkWallet(req.getRandomNum(), req.getSignature(), userInfo.getAccount(), xmsRedis);
		StakeHostingOrder order = stakeHostingOrderService.createUserOrder(userId, req.getPackageId(), req.getAmount());
		CreateStakeHostingOrderResp resp = new CreateStakeHostingOrderResp();
		resp.setOrderNo(order.getOrderNo());
		resp.setStakeUsdtAmount(order.getStakeUsdtAmount());
		return ResultPista.data(resp);
	}

	/**
	 * 查询我的托管订单列表。
	 *
	 * 只返回 App 展示和操作需要的订单字段，隐藏用户ID、钱包地址、周业绩/G7处理状态等内部字段。
	 *
	 * @param lastId 上一页最后一条订单ID，空表示第一页
	 * @param status 业务状态，空表示全部状态
	 * @return 当前登录用户托管订单展示列表
	 */
	@Override
	public List<StakeHostingOrderDto> orderList(Long lastId, Integer status) {
		List<StakeHostingOrder> list = stakeHostingOrderService.lambdaQuery()
			.eq(StakeHostingOrder::getUserId, SecurityUtils.getFrontUserId())
			.eq(StakeHostingOrder::getPayStatus, StakeHostingOrderServiceImpl.PAY_SUCCESS)
			.eq(status != null, StakeHostingOrder::getStatus, status)
			.lt(lastId != null, StakeHostingOrder::getId, lastId)
			.orderByDesc(StakeHostingOrder::getId)
			.last(SysConstant.PAGE_LIMIT)
			.list();
		if (CollectionUtil.isEmpty(list)) {
			return java.util.Collections.emptyList();
		}
		return list.stream().map(this::toOrderDto).collect(Collectors.toList());
	}

	/**
	 * 查询可提交 AFI 质押加速的托管订单列表。
	 *
	 * 仅返回当前登录用户产出中、30天及以上、未绑定 AFI 加速的订单，并转换为 App DTO。
	 *
	 * @param lastId 上一页最后一条订单ID，空表示第一页
	 * @return 当前登录用户可加速订单展示列表
	 */
	@Override
	public List<StakeHostingOrderDto> accelerateOrderList(Long lastId) {
		List<StakeHostingOrder> list = stakeHostingOrderService.lambdaQuery()
			.eq(StakeHostingOrder::getUserId, SecurityUtils.getFrontUserId())
			.eq(StakeHostingOrder::getStatus, StakeHostingOrderServiceImpl.STATUS_RUNNING)
			.ge(StakeHostingOrder::getPackageDays, 30)
			.eq(StakeHostingOrder::getAfiAccelerated, 0)
			.lt(lastId != null, StakeHostingOrder::getId, lastId)
			.orderByDesc(StakeHostingOrder::getId)
			.last(SysConstant.PAGE_LIMIT)
			.list();
		return CollectionUtil.isEmpty(list) ? java.util.Collections.emptyList()
			: list.stream().map(this::toOrderDto).collect(Collectors.toList());
	}

	/**
	 * 查询 AFI 质押加速配置套餐。
	 *
	 * 只返回已启用配置的质押比例和加速倍率，隐藏排序、状态、删除标记等后台字段。
	 *
	 * @return 已启用 AFI 加速配置展示列表
	 */
	@Override
	public List<StakeHostingAfiAccelerateConfigDto> afiAccelerateConfigList() {
		List<StakeHostingAfiAccelerateConfig> list = stakeHostingAfiAccelerateConfigService.lambdaQuery()
			.eq(StakeHostingAfiAccelerateConfig::getStatus, 1)
			.eq(StakeHostingAfiAccelerateConfig::getDeleted, 0)
			.orderByAsc(StakeHostingAfiAccelerateConfig::getSort)
			.orderByAsc(StakeHostingAfiAccelerateConfig::getPledgeRatio)
			.list();
		return CollectionUtil.isEmpty(list) ? java.util.Collections.emptyList()
			: list.stream().map(this::toAfiAccelerateConfigDto).collect(Collectors.toList());
	}

	/**
	 * 查询托管订单详情。
	 *
	 * 只允许查询当前登录用户自己的订单，并转换为 App DTO，避免暴露数据库内部字段。
	 *
	 * @param id 托管订单ID
	 * @return 当前登录用户托管订单详情展示对象
	 */
	@Override
	public StakeHostingOrderDto orderDetail(Long id) {
		if (id == null) {
			throw operationFailedException();
		}
		StakeHostingOrder order = stakeHostingOrderService.lambdaQuery()
			.eq(StakeHostingOrder::getId, id)
			.eq(StakeHostingOrder::getUserId, SecurityUtils.getFrontUserId())
			.eq(StakeHostingOrder::getDeleted, 0)
			.one();
		if (order == null) {
			throw operationFailedException();
		}
		return toOrderDto(order);
	}

	/**
	 * 转换托管订单展示 DTO。
	 *
	 * @param item 数据库托管订单对象
	 * @return App 托管订单展示对象
	 */
	private StakeHostingOrderDto toOrderDto(StakeHostingOrder item) {
		StakeHostingOrderDto dto = new StakeHostingOrderDto();
		dto.setId(item.getId());
		dto.setOrderNo(item.getOrderNo());
		dto.setPackageId(item.getPackageId());
		dto.setPackageName(item.getPackageName());
		dto.setPackageDays(item.getPackageDays());
		dto.setStakeUsdtAmount(item.getStakeUsdtAmount());
		dto.setServiceFeeRatio(item.getServiceFeeRatio());
		dto.setPerformanceCoefficient(item.getPerformanceCoefficient());
		dto.setPerformancePoints(item.getPerformancePoints());
		dto.setSourceType(item.getSourceType());
		dto.setPayStatus(item.getPayStatus());
		dto.setStatus(item.getStatus());
		dto.setPayHash(item.getPayHash());
		dto.setPayAmount(item.getPayAmount());
		dto.setPayTime(item.getPayTime());
		dto.setEffectiveTime(item.getEffectiveTime());
		dto.setFinishTime(item.getFinishTime());
		dto.setRunDays(item.getRunDays());
		dto.setTodayReward(item.getTodayReward());
		dto.setTotalStaticReward(item.getTotalStaticReward());
		dto.setIsReturnPrincipal(item.getIsReturnPrincipal());
		dto.setAfiAccelerated(item.getAfiAccelerated());
		dto.setLastRewardDay(item.getLastRewardDay());
		return dto;
	}

	/**
	 * 转换 AFI 加速配置展示 DTO。
	 *
	 * @param item 数据库 AFI 加速配置对象
	 * @return App AFI 加速配置展示对象
	 */
	private StakeHostingAfiAccelerateConfigDto toAfiAccelerateConfigDto(StakeHostingAfiAccelerateConfig item) {
		StakeHostingAfiAccelerateConfigDto dto = new StakeHostingAfiAccelerateConfigDto();
		dto.setId(item.getId());
		dto.setPledgeRatio(item.getPledgeRatio());
		dto.setAccelerateRate(item.getAccelerateRate());
		return dto;
	}

	@Override
	public ResultPista<StakeHostingAfiPledgeDto> pledgeAfi(PledgeStakeHostingAfiVo req) {
		Long userId = SecurityUtils.getFrontUserId();
		UserInfo userInfo = userInfoService.lambdaQuery()
			.eq(UserInfo::getUserId, userId)
			.one();
		if (userInfo == null) {
			throw userNotFoundException();
		}
		checkWallet(req.getRandomNum(), req.getSignature(), userInfo.getAccount(), xmsRedis);
		BigDecimal afiPrice = bizCommonService.getAfiPrice();
		StakeHostingAfiPledge pledge = stakeHostingAfiPledgeService.pledgeAfi(userId, req.getStakeHostingOrderId(),
			req.getAfiAccelerateConfigId(), afiPrice);
		return ResultPista.data(toAfiPledgeDto(pledge));
	}

	/**
	 * 转换 AFI 质押加速记录展示 DTO。
	 *
	 * @param item 数据库 AFI 质押记录对象
	 * @return App AFI 质押加速记录展示对象
	 */
	private StakeHostingAfiPledgeDto toAfiPledgeDto(StakeHostingAfiPledge item) {
		StakeHostingAfiPledgeDto dto = new StakeHostingAfiPledgeDto();
		dto.setId(item.getId());
		dto.setPledgeNo(item.getPledgeNo());
		dto.setStakeHostingOrderId(item.getStakeHostingOrderId());
		dto.setStakeHostingOrderNo(item.getStakeHostingOrderNo());
		dto.setStakeUsdtAmount(item.getStakeUsdtAmount());
		dto.setAfiAmount(item.getAfiAmount());
		dto.setAfiPrice(item.getAfiPrice());
		dto.setAfiUsdtAmount(item.getAfiUsdtAmount());
		dto.setPledgeRatio(item.getPledgeRatio());
		dto.setAccelerateRate(item.getAccelerateRate());
		dto.setPledgeTime(item.getPledgeTime());
		dto.setEffectiveDay(item.getEffectiveDay());
		dto.setStatus(item.getStatus());
		return dto;
	}

	/**
	 * 构建通用操作失败业务异常。
	 *
	 * App 托管接口对外不直接暴露内部业务原因，订单为空、订单不存在等通用失败统一走响应码。
	 *
	 * @return 通用操作失败异常
	 */
	private ServiceException operationFailedException() {
		return new ServiceException(ResponseCode.CODE_1002);
	}

	/**
	 * 构建用户不存在业务异常。
	 *
	 * @return 用户不存在异常
	 */
	private ServiceException userNotFoundException() {
		return new ServiceException(ResponseCode.CODE_1007);
	}

	@Override
	public ResultPista<String> orderCallback(StakeOrderBo req) {
		log.info("托管订单回调 req:{}", req);
		Map<String, Object> map = BeanUtil.beanToMap(req);
		String sign = SignUtil.getSign(map, false, false, md5Key);
		String osName = SystemUtil.getOsInfo().getName();
		if (!osName.toUpperCase().contains(SysConstant.OS_NAME_WINDOWS)) {
			if (!sign.equals(req.getSign())) {
				log.error("托管订单回调验签失败");
				return ResultPista.fail(ResponseCode.SIGN_VALIDATE_ERROR);
			}
		}
		stakeHostingOrderService.confirmChainPaid(req.getOrderNo(), req.getHash(), req.getAmount());
		return ResultPista.data("success");
	}
}
