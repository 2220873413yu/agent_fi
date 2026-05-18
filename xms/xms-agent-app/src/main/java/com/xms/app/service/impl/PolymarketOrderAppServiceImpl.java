package com.xms.app.service.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.xms.app.client.LongLiveClientStart;
import com.xms.app.entity.dto.PolymarketOrderDto;
import com.xms.app.entity.dto.PolymarketOrderQuoteDto;
import com.xms.app.entity.req.PolymarketOrderReq;
import com.xms.app.service.BizCommonService;
import com.xms.app.service.PolymarketOrderAppService;
import com.xms.app.service.PolymarketService;
import com.xms.common.config.redis.XmsRedis;
import com.xms.common.config.redis.lock.RedisLock;
import com.xms.common.constant.ConstantSys;
import com.xms.common.constant.ConstantType;
import com.xms.common.constant.RedisConstant;
import com.xms.common.core.domain.api.ResultPista;
import com.xms.common.exception.ServiceException;
import com.xms.common.result.ResponseCode;
import com.xms.common.thread.ExecutorRegionKit;
import com.xms.common.utils.uuid.IDUtils;
import com.xms.dao.domain.PolymarketMarket;
import com.xms.dao.domain.PolymarketOrder;
import com.xms.dao.entity.domain.UserInfo;
import com.xms.dao.entity.domain.UserMoney;
import com.xms.dao.service.IPolymarketMarketService;
import com.xms.dao.service.IPolymarketOrderService;
import com.xms.dao.service.ISysParaService;
import com.xms.dao.service.IUserMoneyService;
import com.xms.dao.service.UserInfoService;
import com.xms.dao.service.UserWalletService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 处理App端AFI支付的Polymarket平台内部订单。
 *
 * <p>本服务不会向Polymarket真实CLOB订单簿下单。创建订单时只冻结AFI价格、Polymarket结果价格和市场JSON快照；
 * 市场结束并resolved后，再按快照订单和Polymarket最终结果做平台内部兑付。</p>
 */
@Slf4j
@Service
public class PolymarketOrderAppServiceImpl implements PolymarketOrderAppService {

	private static final int STATUS_PENDING = 0;
	private static final int MIN_SECONDS_BEFORE_END = 5;
	private static final int MONEY_SCALE = 6;
	private static final int SHARE_SCALE = 8;
	private static final int BIZ_TYPE_CRYPTO = 1;
	private static final int BIZ_TYPE_SPORTS = 2;
	private static final int BIZ_TYPE_UP_DOWN = 3;
	private static final String UP_DOWN_SLUG_MARK = "-updown-5m-";
	private static final BigDecimal DEFAULT_MIN_ORDER_AFI_AMOUNT = new BigDecimal("10");
	private static final long WS_SUBSCRIBED_MARKET_DEFAULT_TTL_SECONDS = TimeUnit.DAYS.toSeconds(30);
	private static final long WS_SUBSCRIBED_MARKET_AFTER_END_TTL_SECONDS = TimeUnit.DAYS.toSeconds(7);

	private final PolymarketService polymarketService;
	private final BizCommonService bizCommonService;
	private final IPolymarketOrderService polymarketOrderService;
	private final IPolymarketMarketService polymarketMarketService;
	private final IUserMoneyService userMoneyService;
	private final UserWalletService userWalletService;
	private final UserInfoService userInfoService;
	private final ISysParaService sysParaServiceImpl;
	private final LongLiveClientStart polymarketWebSocketClient;
	private final XmsRedis xmsRedis;

	public PolymarketOrderAppServiceImpl(PolymarketService polymarketService,
										 BizCommonService bizCommonService,
										 IPolymarketOrderService polymarketOrderService,
										 IPolymarketMarketService polymarketMarketService,
										 IUserMoneyService userMoneyService,
										 UserWalletService userWalletService,
										 UserInfoService userInfoService,
										 ISysParaService sysParaServiceImpl,
										 LongLiveClientStart polymarketWebSocketClient,
										 XmsRedis xmsRedis) {
		this.polymarketService = polymarketService;
		this.bizCommonService = bizCommonService;
		this.polymarketOrderService = polymarketOrderService;
		this.polymarketMarketService = polymarketMarketService;
		this.userMoneyService = userMoneyService;
		this.userWalletService = userWalletService;
		this.userInfoService = userInfoService;
		this.sysParaServiceImpl = sysParaServiceImpl;
		this.polymarketWebSocketClient = polymarketWebSocketClient;
		this.xmsRedis = xmsRedis;
	}

	/**
	 * 生成内部订单报价预览。
	 *
	 * <p>只读取当前AFI价格和Polymarket结果价格，不扣钱包、不写订单。正式下单时仍会重新拉取实时价格。</p>
	 *
	 * @param req 报价请求，shareAmount表示用户计划购买的Yes/No结果份额数量
	 * @return 基于当前结果价格计算出的报价快照
	 */
	@Override
	public PolymarketOrderQuoteDto quote(PolymarketOrderReq req) {
		MarketPriceSnapshot snapshot = buildMarketPriceSnapshot(req);
		return PolymarketOrderQuoteDto.builder()
			.marketSlug(snapshot.marketSlug)
			.marketQuestion(snapshot.marketQuestion)
			.outcomeIndex(snapshot.outcomeIndex)
			.outcomeName(snapshot.outcomeName)
			.assetId(snapshot.assetId)
			.afiAmount(snapshot.afiAmount)
			.afiPrice(snapshot.afiPrice)
			.afiUsdtAmount(snapshot.afiUsdtAmount)
			.outcomePrice(snapshot.outcomePrice)
			.shareAmount(snapshot.shareAmount)
			.maxPayoutUsdt(snapshot.maxPayoutUsdt)
			.endTime(snapshot.endTime)
			.build();
	}

	/**
	 * 从系统参数读取最低AFI下单数量。
	 *
	 * <p>参数缺失、空值、非数字或小于等于0时，使用默认值10 AFI，避免配置异常导致任意小额下单。</p>
	 *
	 * @return 最低AFI下单数量
	 */
	@Override
	public BigDecimal getMinAfiOrderAmount() {
		String value = sysParaServiceImpl.getValue(ConstantSys.POLYMARKET_MIN_ORDER_AFI_AMOUNT);
		if (StrUtil.isBlank(value)) {
			return DEFAULT_MIN_ORDER_AFI_AMOUNT;
		}
		try {
			BigDecimal minAmount = new BigDecimal(value.trim()).setScale(MONEY_SCALE, RoundingMode.DOWN);
			return minAmount.compareTo(BigDecimal.ZERO) > 0 ? minAmount : DEFAULT_MIN_ORDER_AFI_AMOUNT;
		} catch (NumberFormatException e) {
			return DEFAULT_MIN_ORDER_AFI_AMOUNT;
		}
	}

	/**
	 * 创建平台内部Polymarket订单并扣减用户AFI余额。
	 *
	 * <p>即使前端已经请求过报价，这里也会重新拉取AFI价格和Polymarket价格，防止前端提交过期价格或篡改价格。</p>
	 *
	 * @param req 下单请求，shareAmount表示购买份额；后端按实时价格折算并从AFI钱包validNum2扣减
	 * @param userId 当前App用户ID
	 * @return 已创建的内部订单快照
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	@RedisLock(value = RedisConstant.LockConstant.POLYMARKET_ORDER_CREATE, param = "#userId")
	public ResultPista<PolymarketOrderDto> create(PolymarketOrderReq req, Long userId) {
		// 下单主流程保持同步强一致：扣AFI、写内部订单、写市场聚合必须在同一个事务里完成。
		// WebSocket刷新只是实时监听增强，放到事务提交后执行，失败也不影响订单创建和资金扣减结果。
		// 步骤1：先做本地参数和重复下单校验；同一用户同一市场临时只允许保留一笔正常订单。
		validateRequest(req);
		validateUserMarketNotOrdered(req.getMarketSlug(), userId);
		// 步骤2：重新生成价格快照，保证下单使用后端实时价格，不使用前端报价结果。
		MarketPriceSnapshot snapshot = buildMarketPriceSnapshot(req);
		// 步骤3：读取用户与钱包余额，确认用户存在且AFI可用余额足够。
		UserInfo userInfo = userInfoService.lambdaQuery().eq(UserInfo::getUserId, userId).one();
		if (userInfo == null) {
			throw new ServiceException(ResponseCode.CODE_1001);
		}
		UserMoney userMoney = userMoneyService.lambdaQuery().eq(UserMoney::getId, userId).one();
		if (userMoney.getValidNum2().compareTo(snapshot.afiAmount) < 0) {
			throw new ServiceException(ResponseCode.CODE_1015);
		}

		// 步骤4：事务内先扣AFI并写钱包流水；后续订单入库失败会整体回滚。
		String orderNo = IDUtils.getSnowflakeStr();
		int rows = userWalletService.handerUserMoney(snapshot.afiAmount.negate(), orderNo, userId, userId,
			ConstantType.user_money_log_source_type.type_40, ConstantType.user_money_coin_type.type_2);
		if (rows != 1) {
			throw new ServiceException(ResponseCode.CODE_1015);
		}

		// 步骤5：保存内部订单，记录下单时的市场、价格、份额和最大兑付快照。
		PolymarketOrder order = buildOrder(orderNo, userId, userInfo.getAccount(), snapshot);
		try {
			if (!polymarketOrderService.save(order)) {
				throw new ServiceException(ResponseCode.CODE_1292);
			}
		} catch (DuplicateKeyException e) {
			// 唯一索引兜底并发重复下单：同一用户同一市场只能保留一笔正常订单。
			throw new ServiceException(ResponseCode.CODE_1297);
		}
		// 步骤6：同事务累加市场级聚合金额，结算任务后续按市场批量处理订单。
		if (!polymarketMarketService.upsertOrderAggregate(buildMarketAggregate(snapshot))) {
			throw new ServiceException(ResponseCode.CODE_1293);
		}
		// 步骤7：事务提交后投递市场延迟结算消息；数据库市场状态仍由Quartz兜底扫描。
		sendMarketSettleDelayAfterCommit(snapshot);
		// 步骤8：事务提交后按marketSlug做Redis去重，只有该市场首次触发时才刷新WebSocket订阅。
		refreshWebSocketSubscribeAfterCommit(snapshot);
		return ResultPista.data(toDto(order, false));
	}

	/**
	 * 查询当前用户订单列表。
	 *
	 * <p>列表接口会隐藏原始JSON快照，避免页面列表返回过大；详情接口才返回快照。</p>
	 *
	 * @param lastId 可选游标，只返回ID更小的订单
	 * @param status 可选订单状态
	 * @param userId 当前App用户ID
	 * @return 不包含原始JSON快照的订单列表
	 */
	@Override
	public List<PolymarketOrderDto> myOrders(Long lastId, Integer status, Long userId) {
		List<PolymarketOrder> orders = polymarketOrderService.lambdaQuery()
			.eq(PolymarketOrder::getUserId, userId)
			.eq(PolymarketOrder::getDeleted, 0)
			.eq(status != null, PolymarketOrder::getStatus, status)
			.lt(lastId != null, PolymarketOrder::getId, lastId)
			.orderByDesc(PolymarketOrder::getId)
			.last("limit 20")
			.list();
		return orders.stream().map(order -> toDto(order, false)).collect(Collectors.toList());
	}

	/**
	 * 查询当前用户单笔订单详情。
	 *
	 * @param orderNo 平台内部订单号
	 * @param userId 当前App用户ID
	 * @return 包含下单/结算市场快照的订单详情
	 */
	@Override
	public PolymarketOrderDto detail(String orderNo, Long userId) {
		if (StrUtil.isBlank(orderNo)) {
			throw new ServiceException(ResponseCode.CODE_1294);
		}
		PolymarketOrder order = polymarketOrderService.lambdaQuery()
			.eq(PolymarketOrder::getOrderNo, orderNo.trim())
			.eq(PolymarketOrder::getUserId, userId)
			.eq(PolymarketOrder::getDeleted, 0)
			.one();
		if (order == null) {
			throw new ServiceException(ResponseCode.CODE_1295);
		}
		return toDto(order, true);
	}

	/**
	 * 派发市场级Polymarket结算。
	 *
	 * <p>App侧不再保留订单轮询结算实现，统一委托DAO层按市场状态派发；真正订单结算由后台手动入口或后续延迟队列处理。</p>
	 *
	 * @param limit 本批最多派发的市场数
	 * @return 成功改为结算中的市场数量
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public int settlePendingOrders(Integer limit) {
		// App侧不再维护一套订单轮询结算逻辑，统一委托DAO层的市场级派发逻辑，避免两套规则不一致。
		return polymarketOrderService.settlePendingOrders(limit);
	}

	/**
	 * 构建下单或报价使用的价格快照。
	 *
	 * <p>该方法会实时读取Polymarket市场、校验是否可下单、读取AFI价格，并按用户输入的购买份额反算USDT成本和应扣AFI数量。</p>
	 *
	 * @param req 报价或下单请求
	 * @return 报价和创建订单共用的不可变业务快照
	 */
	private MarketPriceSnapshot buildMarketPriceSnapshot(PolymarketOrderReq req) {
		// 这里统一生成报价/下单快照：正式下单时仍重新拉实时价格，不信任前端传回来的报价。
		// Polymarket的outcomes、outcomePrices、clobTokenIds三个数组按同一个outcomeIndex对应。
		// outcomes[outcomeIndex]是结果名称，outcomePrices[outcomeIndex]是成交价，clobTokenIds[outcomeIndex]是asset_id/token_id。
		// 步骤1：先做本地请求参数校验，避免无效参数触发外部请求。
		validateRequest(req);
		// 步骤2：实时读取Polymarket市场详情，并校验市场仍可交易。
		JSONObject marketWrapper;
		try {
			marketWrapper = polymarketService.getMarketBySlug(req.getMarketSlug().trim());
		} catch (Exception e) {
			throw new ServiceException(ResponseCode.CODE_1285);
		}
		if (marketWrapper == null) {
			throw new ServiceException(ResponseCode.CODE_1285);
		}
		JSONObject market = marketWrapper.getJSONObject("market");
		if (market == null) {
			throw new ServiceException(ResponseCode.CODE_1285);
		}
		validateMarketOpen(market);

		// 步骤3：解析结果名称与结果价格，outcomeIndex必须同时落在两个数组范围内。
		JSONArray outcomes = parseJsonArray(market.getString("outcomes"), "outcomes");
		JSONArray outcomePrices = parseJsonArray(market.getString("outcomePrices"), "outcomePrices");
		JSONArray clobTokenIds = parseJsonArray(market.getString("clobTokenIds"), "clobTokenIds");
		if (req.getOutcomeIndex() >= outcomes.size() || req.getOutcomeIndex() >= outcomePrices.size() || req.getOutcomeIndex() >= clobTokenIds.size()) {
			throw new ServiceException(ResponseCode.CODE_1286);
		}
		String outcomeName = outcomes.getString(req.getOutcomeIndex());
		if (StrUtil.isBlank(outcomeName)) {
			throw new ServiceException(ResponseCode.CODE_1286);
		}
		String assetId = clobTokenIds.getString(req.getOutcomeIndex());
		if (StrUtil.isBlank(assetId)) {
			throw new ServiceException(ResponseCode.CODE_1286);
		}
		BigDecimal outcomePrice;
		try {
			outcomePrice = outcomePrices.getBigDecimal(req.getOutcomeIndex());
		} catch (Exception e) {
			throw new ServiceException(ResponseCode.CODE_1287);
		}
		if (outcomePrice == null || outcomePrice.compareTo(BigDecimal.ZERO) <= 0) {
			throw new ServiceException(ResponseCode.CODE_1287);
		}

		// 步骤4：用户输入的是购买份额，先按结果价格计算USDT成本，再用实时AFI价格反算应扣AFI数量。
		BigDecimal shareAmount = req.getShareAmount().setScale(SHARE_SCALE, RoundingMode.DOWN);
		if (shareAmount.compareTo(BigDecimal.ZERO) <= 0) {
			throw new ServiceException(ResponseCode.CODE_1294);
		}
		BigDecimal rawAfiPrice = bizCommonService.getAfiPrice();
		if (rawAfiPrice == null) {
			throw new ServiceException(ResponseCode.CODE_1288);
		}
		BigDecimal afiPrice = rawAfiPrice.setScale(MONEY_SCALE, RoundingMode.DOWN);
		if (afiPrice.compareTo(BigDecimal.ZERO) <= 0) {
			throw new ServiceException(ResponseCode.CODE_1288);
		}
		if (outcomePrice.compareTo(BigDecimal.ONE) > 0) {
			throw new ServiceException(ResponseCode.CODE_1287);
		}
		BigDecimal afiUsdtAmount = shareAmount.multiply(outcomePrice).setScale(MONEY_SCALE, RoundingMode.DOWN);
		if (afiUsdtAmount.compareTo(BigDecimal.ZERO) <= 0) {
			throw new ServiceException(ResponseCode.CODE_1287);
		}
		// AFI数量向上保留6位，避免除法舍入导致实际扣款低于购买份额所需USDT成本。
		BigDecimal afiAmount = afiUsdtAmount.divide(afiPrice, MONEY_SCALE, RoundingMode.UP);
		if (afiAmount.compareTo(BigDecimal.ZERO) <= 0) {
			throw new ServiceException(ResponseCode.CODE_1287);
		}
		BigDecimal minAfiAmount = getMinAfiOrderAmount();
		if (afiAmount.compareTo(minAfiAmount) < 0) {
			throw new ServiceException(ResponseCode.CODE_1291);
		}

		// 步骤5：组装快照，后续报价直接返回，正式下单会把这些字段落到订单表。
		MarketPriceSnapshot snapshot = new MarketPriceSnapshot();
		snapshot.marketWrapperJson = marketWrapper.toJSONString();
		snapshot.market = market;
		snapshot.marketSlug = StrUtil.blankToDefault(market.getString("slug"), req.getMarketSlug().trim());
		snapshot.bizType = req.getBizType();
		snapshot.marketId = market.getString("id");
		snapshot.conditionId = market.getString("conditionId");
		snapshot.marketQuestion = market.getString("question");
		snapshot.eventSlug = firstEventString(market, "slug");
		snapshot.eventTitle = firstEventString(market, "title");
		snapshot.outcomeIndex = req.getOutcomeIndex();
		snapshot.outcomeName = outcomeName;
		snapshot.assetId = assetId;
		snapshot.assetIdsJson = clobTokenIds.toJSONString();
		snapshot.outcomesJson = outcomes.toJSONString();
		snapshot.afiAmount = afiAmount;
		snapshot.afiPrice = afiPrice;
		snapshot.afiUsdtAmount = afiUsdtAmount;
		snapshot.outcomePrice = outcomePrice;
		snapshot.shareAmount = shareAmount;
		snapshot.maxPayoutUsdt = shareAmount;
		snapshot.endTime = parseEndTime(market);
		return snapshot;
	}

	/**
	 * 校验报价/下单请求参数。
	 *
	 * <p>这里只校验本地必填和购买份额大于0；最低AFI数量需要先按实时价格折算，在价格快照计算阶段校验。</p>
	 *
	 * @param req 报价或下单请求
	 */
	private void validateRequest(PolymarketOrderReq req) {
		if (req == null || StrUtil.isBlank(req.getMarketSlug())) {
			throw new ServiceException(ResponseCode.CODE_1294);
		}
		if (req.getOutcomeIndex() == null || req.getOutcomeIndex() < 0) {
			throw new ServiceException(ResponseCode.CODE_1294);
		}
		if (req.getShareAmount() == null || req.getShareAmount().compareTo(BigDecimal.ZERO) <= 0) {
			throw new ServiceException(ResponseCode.CODE_1294);
		}
		if (req.getBizType() == null || (req.getBizType() != BIZ_TYPE_CRYPTO
			&& req.getBizType() != BIZ_TYPE_SPORTS && req.getBizType() != BIZ_TYPE_UP_DOWN)) {
			throw new ServiceException(ResponseCode.CODE_1294);
		}
		// Up/Down短周期市场的slug有固定标识，避免前端入口传错类型导致后台统计不准。
		if (req.getMarketSlug().trim().contains(UP_DOWN_SLUG_MARK) && req.getBizType() != BIZ_TYPE_UP_DOWN) {
			throw new ServiceException(ResponseCode.CODE_1294);
		}
	}

	/**
	 * 校验当前用户是否已经购买过同一个Polymarket市场。
	 *
	 * <p>当前业务临时限制为“同一用户同一marketSlug只能下一笔正常订单”，不区分Yes/No或多结果选项；
	 * 数据库唯一索引用于并发兜底，这里负责在进入价格查询和扣款前给前端明确业务提示。</p>
	 *
	 * @param marketSlug Polymarket市场slug，来自下单请求
	 * @param userId 当前App用户ID
	 */
	private void validateUserMarketNotOrdered(String marketSlug, Long userId) {
		boolean exists = polymarketOrderService.lambdaQuery()
			.eq(PolymarketOrder::getUserId, userId)
			.eq(PolymarketOrder::getMarketSlug, marketSlug.trim())
			.eq(PolymarketOrder::getDeleted, 0)
			.exists();
		if (exists) {
			throw new ServiceException(ResponseCode.CODE_1297);
		}
	}

	/**
	 * 校验Polymarket市场是否仍允许创建平台内部订单。
	 *
	 * <p>这里是后端最终保护：即使前端按钮没有及时禁用，也不能对已关闭、未激活、停止接单或结束前最后5秒内的市场下单。</p>
	 *
	 * @param market Polymarket Gamma返回的市场对象
	 */
	private void validateMarketOpen(JSONObject market) {
		if (Boolean.TRUE.equals(market.getBoolean("closed")) || Boolean.FALSE.equals(market.getBoolean("active"))) {
			throw new ServiceException(ResponseCode.CODE_1289);
		}
		Boolean acceptingOrders = market.getBoolean("acceptingOrders");
		if (Boolean.FALSE.equals(acceptingOrders)) {
			throw new ServiceException(ResponseCode.CODE_1289);
		}
		Date endTime = parseEndTime(market);
		if (endTime != null && endTime.getTime() - System.currentTimeMillis() < MIN_SECONDS_BEFORE_END * 1000L) {
			// 短周期Up/Down市场波动很快，结束前最后5秒禁止内部下单，避免价格快照和结算结果贴得过近。
			throw new ServiceException(ResponseCode.CODE_1290);
		}
	}

	/**
	 * 根据价格快照构建待入库的内部订单。
	 *
	 * @param orderNo 平台内部订单号
	 * @param userId 用户ID
	 * @param account 用户钱包地址快照
	 * @param snapshot 下单时价格和市场快照
	 * @return 可直接保存的订单实体
	 */
	private PolymarketOrder buildOrder(String orderNo, Long userId, String account, MarketPriceSnapshot snapshot) {
		Date now = new Date();
		PolymarketOrder order = PolymarketOrder.builder()
			.orderNo(orderNo)
			.userId(userId)
			.account(account)
			.eventSlug(snapshot.eventSlug)
			.eventTitle(snapshot.eventTitle)
			.marketSlug(snapshot.marketSlug)
			.bizType(snapshot.bizType)
			.marketId(snapshot.marketId)
			.conditionId(snapshot.conditionId)
			.marketQuestion(snapshot.marketQuestion)
			.outcomeIndex(snapshot.outcomeIndex)
			.outcomeName(snapshot.outcomeName)
			.assetId(snapshot.assetId)
			.afiAmount(snapshot.afiAmount)
			.afiPrice(snapshot.afiPrice)
			.afiUsdtAmount(snapshot.afiUsdtAmount)
			.outcomePrice(snapshot.outcomePrice)
			.shareAmount(snapshot.shareAmount)
			.maxPayoutUsdt(snapshot.maxPayoutUsdt)
			.endTime(snapshot.endTime)
			.status(STATUS_PENDING)
			.payoutUsdtAmount(BigDecimal.ZERO.setScale(MONEY_SCALE, RoundingMode.DOWN))
			.payoutAfiPrice(BigDecimal.ZERO.setScale(MONEY_SCALE, RoundingMode.DOWN))
			.payoutAfiAmount(BigDecimal.ZERO.setScale(SHARE_SCALE, RoundingMode.DOWN))
			.orderSnapshotJson(snapshot.marketWrapperJson)
			.deleted(0)
			.build();
		order.setCreateTime(now);
		order.setUpdateTime(now);
		return order;
	}

	/**
	 * 根据下单快照构建市场聚合增量。
	 *
	 * <p>该对象用于按marketSlug插入或累加市场表，只统计市场总下单次数和总金额，不统计每个outcome的金额。</p>
	 *
	 * @param snapshot 下单时市场和价格快照
	 * @return 可直接upsert的市场聚合增量
	 */
	private PolymarketMarket buildMarketAggregate(MarketPriceSnapshot snapshot) {
		// 市场聚合是订单的强一致附属数据，必须跟订单同事务写入，不能丢到延迟队列异步补。
		// WebSocket刷新订阅依赖这里写入的asset_ids_json，后续结算也会按market_slug批量处理订单。
		Date now = new Date();
		PolymarketMarket market = PolymarketMarket.builder()
			.marketSlug(snapshot.marketSlug)
			.marketId(snapshot.marketId)
			.conditionId(snapshot.conditionId)
			.eventSlug(snapshot.eventSlug)
			.eventTitle(snapshot.eventTitle)
			.marketQuestion(snapshot.marketQuestion)
			.assetIdsJson(snapshot.assetIdsJson)
			.outcomesJson(snapshot.outcomesJson)
			.endTime(snapshot.endTime)
			.status(STATUS_PENDING)
			.orderCount(1)
			.totalAfiAmount(snapshot.afiAmount)
			.totalUsdtAmount(snapshot.afiUsdtAmount)
			.totalShareAmount(snapshot.shareAmount)
			.totalPayoutUsdtAmount(BigDecimal.ZERO.setScale(MONEY_SCALE, RoundingMode.DOWN))
			.totalPayoutAfiAmount(BigDecimal.ZERO.setScale(SHARE_SCALE, RoundingMode.DOWN))
			.marketSnapshotJson(snapshot.marketWrapperJson)
			.deleted(0)
			.build();
		market.setCreateTime(now);
		market.setUpdateTime(now);
		return market;
	}

	/**
	 * 预留订单事务提交后的市场结算延迟消息投递入口。
	 *
	 * <p>当前阶段延迟队列消费者尚未接入，所以下单后不真实发送Redisson消息。后续接入队列时，应在事务提交后投递marketSlug；
	 * 即使消息丢失，后台Quartz仍会扫描市场表兜底派发。</p>
	 *
	 * @param snapshot 下单时市场和结束时间快照
	 */
	private void sendMarketSettleDelayAfterCommit(MarketPriceSnapshot snapshot) {
		if (snapshot.endTime == null || StrUtil.isBlank(snapshot.marketSlug)) {
			return;
		}
		// TODO 后续接入Redisson延迟队列后，在事务提交后投递snapshot.marketSlug，让队列消费者统一调用processSettlingMarket发奖。
	}

	/**
	 * 在订单事务提交后尝试刷新Polymarket WebSocket订阅。
	 *
	 * <p>市场聚合表和订单在同一个事务内写入，提交后再做Redis SETNX去重，确保新市场第一次下单后能尽快进入
	 * market_resolved监听；同一个marketSlug后续订单不再重复触发全量订阅刷新。</p>
	 *
	 * @param snapshot 下单时的市场快照，用于获取marketSlug和endTime计算Redis过期时间
	 */
	private void refreshWebSocketSubscribeAfterCommit(MarketPriceSnapshot snapshot) {
		if (snapshot == null || StrUtil.isBlank(snapshot.marketSlug)) {
			return;
		}
		// 只刷新订阅，不发奖、不修改市场状态；真正结算仍由市场派发和processSettlingMarket兜底复核。
		// 放在afterCommit是为了保证刷新订阅时，市场表已经能查到本次下单写入的asset_ids_json。
		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
			@Override
			public void afterCommit() {
				submitWebSocketRefreshTask(snapshot);
			}
		});
	}

	/**
	 * 使用项目统一虚拟线程池异步执行Redis去重和WebSocket订阅刷新。
	 *
	 * <p>刷新订阅是轻量级实时性增强动作，不能阻塞下单主流程。Redis异常或WebSocket当前不可用都不影响订单创建；
	 * WebSocket重连全量订阅和Quartz到期扫描仍然是兜底。</p>
	 *
	 * @param snapshot 下单时的市场快照
	 */
	private void submitWebSocketRefreshTask(MarketPriceSnapshot snapshot) {
		ExecutorRegionKit.getExecutorRegion().getUserVirtualThreadExecutor(1)
			.executeTry(() -> {
				if (!markMarketSubscribeRefreshNeeded(snapshot)) {
					return;
				}
				polymarketWebSocketClient.refreshSubscribeAfterOrderCreated();
			});
	}

	/**
	 * 用Redis SETNX判断该marketSlug是否已经触发过WebSocket订阅刷新。
	 *
	 * <p>Redis这里只是减少重复刷新，不作为市场是否存在、是否结算的依据。Redis key丢失最多导致后续订单多刷新一次，
	 * 不会影响资金、订单和结算状态。</p>
	 *
	 * @param snapshot 下单时的市场快照
	 * @return true表示本次拿到首次刷新资格；false表示已刷新过或Redis不可用
	 */
	private boolean markMarketSubscribeRefreshNeeded(MarketPriceSnapshot snapshot) {
		String redisKey = RedisConstant.POLYMARKET_WS_SUBSCRIBED_MARKET + snapshot.marketSlug;
		long ttlSeconds = calculateSubscribeRefreshTtlSeconds(snapshot.endTime);
		try {
			Boolean success = xmsRedis.getRedisTemplate()
				.opsForValue()
				.setIfAbsent(redisKey, "1", ttlSeconds, TimeUnit.SECONDS);
			if (Boolean.TRUE.equals(success)) {
				log.info("Polymarket市场首次触发WebSocket订阅刷新，marketSlug={}, ttlSeconds={}", snapshot.marketSlug, ttlSeconds);
				return true;
			}
			log.info("Polymarket市场已触发过WebSocket订阅刷新，本次跳过，marketSlug={}", snapshot.marketSlug);
			return false;
		} catch (Exception e) {
			log.warn("Polymarket市场WebSocket订阅刷新Redis去重失败，marketSlug={}, error={}", snapshot.marketSlug, e.getMessage());
			return false;
		}
	}

	/**
	 * 计算WebSocket订阅去重key的过期时间。
	 *
	 * <p>有结束时间时保留到市场结束后7天，方便结算排查且避免Redis长期堆积；没有结束时间时使用默认30天。</p>
	 *
	 * @param endTime Polymarket市场结束时间，可能为空
	 * @return Redis key过期秒数
	 */
	private long calculateSubscribeRefreshTtlSeconds(Date endTime) {
		if (endTime == null) {
			return WS_SUBSCRIBED_MARKET_DEFAULT_TTL_SECONDS;
		}
		long secondsUntilEnd = (endTime.getTime() - System.currentTimeMillis()) / 1000L;
		long ttlSeconds = secondsUntilEnd + WS_SUBSCRIBED_MARKET_AFTER_END_TTL_SECONDS;
		return ttlSeconds > 0 ? ttlSeconds : WS_SUBSCRIBED_MARKET_AFTER_END_TTL_SECONDS;
	}

	/**
	 * 将订单实体转换为App返回对象。
	 *
	 * @param order 订单实体
	 * @param includeSnapshots 是否返回原始JSON快照；列表页通常不返回，详情页返回
	 * @return App订单DTO
	 */
	private PolymarketOrderDto toDto(PolymarketOrder order, boolean includeSnapshots) {
		PolymarketOrderDto dto = new PolymarketOrderDto();
		BeanUtils.copyProperties(order, dto);
		return dto;
	}

	/**
	 * 解析Polymarket以字符串形式返回的JSON数组字段。
	 *
	 * @param json 原始JSON数组字符串
	 * @param fieldName 字段名，用于错误提示
	 * @return 解析后的数组
	 */
	private JSONArray parseJsonArray(String json, String fieldName) {
		if (StrUtil.isBlank(json)) {
			throw new ServiceException(ResponseCode.CODE_1286);
		}
		try {
			JSONArray array = JSONArray.parseArray(json);
			if (array == null || array.isEmpty()) {
				throw new ServiceException(ResponseCode.CODE_1286);
			}
			return array;
		} catch (ServiceException e) {
			throw e;
		} catch (Exception e) {
			throw new ServiceException(ResponseCode.CODE_1286);
		}
	}

	/**
	 * 从市场详情的第一个event里读取指定字符串字段。
	 *
	 * @param market 市场详情
	 * @param fieldName event字段名
	 * @return 第一个event的字段值，可能为空
	 */
	private String firstEventString(JSONObject market, String fieldName) {
		JSONArray events = market.getJSONArray("events");
		if (events == null || events.isEmpty()) {
			return null;
		}
		JSONObject event = events.getJSONObject(0);
		return event == null ? null : event.getString(fieldName);
	}

	/**
	 * 解析Gamma API返回的市场结束时间。
	 *
	 * @param market 市场详情
	 * @return 结束时间；上游无结束时间时返回null
	 */
	private Date parseEndTime(JSONObject market) {
		String endDate = StrUtil.blankToDefault(market.getString("endDate"), market.getString("endDateIso"));
		if (StrUtil.isBlank(endDate)) {
			return null;
		}
		try {
			return Date.from(Instant.parse(endDate));
		} catch (Exception ignored) {
			try {
				return java.sql.Date.valueOf(endDate);
			} catch (Exception e) {
				throw new ServiceException(ResponseCode.CODE_1294);
			}
		}
	}

	/**
	 * 报价和创建订单共用的计算快照。
	 */
	private static class MarketPriceSnapshot {
		private String marketWrapperJson;
		private JSONObject market;
		private String marketSlug;
		private Integer bizType;
		private String marketId;
		private String conditionId;
		private String marketQuestion;
		private String eventSlug;
		private String eventTitle;
		private Integer outcomeIndex;
		private String outcomeName;
		private String assetId;
		private String assetIdsJson;
		private String outcomesJson;
		private BigDecimal afiAmount;
		private BigDecimal afiPrice;
		private BigDecimal afiUsdtAmount;
		private BigDecimal outcomePrice;
		private BigDecimal shareAmount;
		private BigDecimal maxPayoutUsdt;
		private Date endTime;
	}

}
