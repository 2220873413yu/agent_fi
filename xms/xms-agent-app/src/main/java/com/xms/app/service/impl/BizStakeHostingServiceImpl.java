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
import com.xms.app.entity.vo.StopStakeHostingOrderVo;
import com.xms.app.service.BizCommonService;
import com.xms.app.service.BizStakeHostingService;
import com.xms.common.config.redis.XmsRedis;
import com.xms.common.config.redis.lock.RedisLock;
import com.xms.common.constant.RedisConstant;
import com.xms.common.constant.SysConstant;
import com.xms.common.core.domain.api.ResultPista;
import com.xms.common.exception.ServiceException;
import com.xms.common.result.ResponseCode;
import com.xms.common.utils.SecurityUtils;
import com.xms.common.utils.SignUtil;
import com.xms.dao.domain.*;
import com.xms.dao.entity.domain.UserInfo;
import com.xms.dao.service.*;
import com.xms.dao.service.impl.StakeHostingOrderServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
	private final XmsCommonService xmsCommonServiceImpl;
	private final XmsRedis xmsRedis;
	private final BizCommonService bizCommonService;

	@Autowired
	private  IStakeHostingStaticRateConfigService stakeHostingStaticRateConfigServiceImpl;

	@Value("${lq.md5Key}")
	private String md5Key;

	public BizStakeHostingServiceImpl(IStakeHostingPackageService stakeHostingPackageService,
									  IStakeHostingOrderService stakeHostingOrderService,
									  IStakeHostingAfiPledgeService stakeHostingAfiPledgeService,
									  IStakeHostingAfiAccelerateConfigService stakeHostingAfiAccelerateConfigService,
									  UserInfoService userInfoService,
									  XmsCommonService xmsCommonServiceImpl,
									  XmsRedis xmsRedis,
									  BizCommonService bizCommonService) {
		this.stakeHostingPackageService = stakeHostingPackageService;
		this.stakeHostingOrderService = stakeHostingOrderService;
		this.stakeHostingAfiPledgeService = stakeHostingAfiPledgeService;
		this.stakeHostingAfiAccelerateConfigService = stakeHostingAfiAccelerateConfigService;
		this.userInfoService = userInfoService;
		this.xmsCommonServiceImpl = xmsCommonServiceImpl;
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
		BigDecimal minDayRatio = stakeHostingStaticRateConfigServiceImpl.lambdaQuery()
			.eq(StakeHostingStaticRateConfig::getId, 1)
			.one().getStaticRate();

		BigDecimal maxDayRatio = stakeHostingStaticRateConfigServiceImpl.lambdaQuery()
			.last("limit 1")
			.orderByDesc(StakeHostingStaticRateConfig::getId)
			.one().getStaticRate();
		List<StakeHostingPackageDto> result = list.stream().map(record -> {
			StakeHostingPackageDto dto = new StakeHostingPackageDto();
			BeanUtil.copyProperties(record, dto);
			dto.setMinDayRatio(minDayRatio);
			dto.setMaxDayRatio(maxDayRatio);
			return dto;
		}).collect(Collectors.toList());
		return result;
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

	/**
	 * 创建 App 用户站内 USDT 支付托管订单。
	 *
	 * <p>请求先按当前登录用户钱包地址校验签名，签名随机数校验成功后会删除 Redis key，
	 * 再按用户维度 Redis 锁串行进入托管购买流程。新流程不再创建待支付订单，而是在 DAO 事务内
	 * 先扣减用户 `valid_num1`，扣款成功后保存已支付、产出中的托管订单，并触发生效后的异步处理。</p>
	 *
	 * @param req 创建托管订单请求，金额单位为 USDT，包含套餐ID、金额、随机数和钱包签名
	 * @param userId 当前登录用户ID
	 * @return 已支付托管订单号和托管金额快照
	 */
	@Override
	@RedisLock(value = RedisConstant.LockConstant.XMS_STAKE_APPLY, param = "#userId")
	public ResultPista<CreateStakeHostingOrderResp> createOrder(CreateStakeHostingOrderVo req, Long userId) {
		// 下单绑定当前登录用户的钱包地址，防止前端传入其他地址代签。
		UserInfo userInfo = userInfoService.lambdaQuery()
			.eq(UserInfo::getUserId, userId)
			.one();
		if (userInfo == null) {
			throw userNotFoundException();
		}

		// 钱包签名只允许使用一次；校验通过会删除随机数，配合用户锁和 RepeatSubmit 拦截重复下单。
		checkWallet(req.getRandomNum(), req.getSignature(), userInfo.getAccount(), xmsRedis);
		// 站内 USDT 支付流程在同一事务内先扣钱包再创建已支付订单，前端不再进入链上支付步骤。
		StakeHostingOrder order = stakeHostingOrderService.createUserPaidOrder(userId, req.getPackageId(), req.getAmount());

		// 返回已完成站内 USDT 支付的托管订单号和金额快照。
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
	 * 停止当前登录用户的1天自动复投托管订单。
	 *
	 * <p>App层先校验当前是否处于结算限制窗口，避免和101静态收益任务重叠；订单状态抢占、USDT退本、
	 * 业绩回退和等级重算消息由订单服务在事务内完成。</p>
	 *
	 * @param req 停止托管请求，包含托管订单ID
	 * @return success表示停止成功
	 */
	@Override
	public ResultPista<String> stop(StopStakeHostingOrderVo req) {
		ResultPista resultPista = xmsCommonServiceImpl.checkMineSettleTime();
		if (!ResultPista.isSuccess(resultPista)) {
			throw new ServiceException(resultPista.getMsg());
		}
		stakeHostingOrderService.stopUserOneDayAutoReinvestOrder(SecurityUtils.getFrontUserId(), req.getOrderId());
		return ResultPista.data("success");
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
		dto.setPrincipalReturnStatus(item.getPrincipalReturnStatus());
		dto.setPrincipalReturnTime(item.getPrincipalReturnTime());
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

	/**
	 * 处理托管订单链上支付回调。
	 *
	 * <p>生产环境会按共享 md5Key 验签；Windows 环境保留本地调试绕过。验签通过后不在 App 层直接改状态，
	 * 而是交给 DAO 订单服务按待支付状态做幂等推进，避免重复回调重复增加业绩或重复发送异步消息。</p>
	 *
	 * @param req 外部回调参数，金额单位为 USDT
	 * @return success 表示处理完成；验签失败返回签名错误
	 */
	@Override
	public ResultPista<String> orderCallback(StakeOrderBo req) {
		log.info("托管订单回调 req:{}", req);
		Map<String, Object> map = BeanUtil.beanToMap(req);
		String sign = SignUtil.getSign(map, false, false, md5Key);
		String osName = SystemUtil.getOsInfo().getName();
		// 非 Windows 环境必须验签，回调可信性依赖外部服务签名和共享密钥。
		if (!osName.toUpperCase().contains(SysConstant.OS_NAME_WINDOWS)) {
			if (!sign.equals(req.getSign())) {
				log.error("托管订单回调验签失败");
				return ResultPista.fail(ResponseCode.SIGN_VALIDATE_ERROR);
			}
		}

		// 支付状态推进、金额校验、幂等和后置异步消息均由订单服务统一处理。
		stakeHostingOrderService.confirmChainPaid(req.getOrderNo(), req.getHash(), req.getAmount());
		return ResultPista.data("success");
	}
}
