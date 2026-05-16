package com.xms.app.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.xms.app.entity.dto.PolymarketOrderDto;
import com.xms.app.entity.dto.PolymarketOrderQuoteDto;
import com.xms.app.entity.req.PolymarketOrderReq;
import com.xms.app.service.BizCommonService;
import com.xms.app.service.PolymarketOrderAppService;
import com.xms.app.service.PolymarketService;
import com.xms.common.config.redis.lock.RedisLock;
import com.xms.common.constant.ConstantSys;
import com.xms.common.constant.ConstantType;
import com.xms.common.core.domain.api.ResultPista;
import com.xms.common.exception.ServiceException;
import com.xms.common.result.ResponseCode;
import com.xms.common.utils.uuid.IDUtils;
import com.xms.dao.domain.PolymarketOrder;
import com.xms.dao.entity.domain.UserInfo;
import com.xms.dao.entity.domain.UserMoney;
import com.xms.dao.service.IPolymarketOrderService;
import com.xms.dao.service.ISysParaService;
import com.xms.dao.service.IUserMoneyService;
import com.xms.dao.service.UserInfoService;
import com.xms.dao.service.UserWalletService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 处理App端AFI支付的Polymarket平台内部订单。
 *
 * <p>本服务不会向Polymarket真实CLOB订单簿下单。创建订单时只冻结AFI价格、Polymarket结果价格和市场JSON快照；
 * 市场结束并resolved后，再按快照订单和Polymarket最终结果做平台内部兑付。</p>
 */
@Service
public class PolymarketOrderAppServiceImpl implements PolymarketOrderAppService {

	private static final int STATUS_PENDING = 0;
	private static final int STATUS_WON = 1;
	private static final int STATUS_LOST = 2;
	private static final int STATUS_NEED_REVIEW = 3;
	private static final int MIN_SECONDS_BEFORE_END = 5;
	private static final int MONEY_SCALE = 6;
	private static final int SHARE_SCALE = 8;
	private static final int DEFAULT_SETTLE_LIMIT = 100;
	private static final int MAX_SETTLE_LIMIT = 500;
	private static final BigDecimal DEFAULT_MIN_ORDER_AFI_AMOUNT = new BigDecimal("10");

	private final PolymarketService polymarketService;
	private final BizCommonService bizCommonService;
	private final IPolymarketOrderService polymarketOrderService;
	private final IUserMoneyService userMoneyService;
	private final UserWalletService userWalletService;
	private final UserInfoService userInfoService;
	private final ISysParaService sysParaServiceImpl;

	public PolymarketOrderAppServiceImpl(PolymarketService polymarketService,
										 BizCommonService bizCommonService,
										 IPolymarketOrderService polymarketOrderService,
										 IUserMoneyService userMoneyService,
										 UserWalletService userWalletService,
										 UserInfoService userInfoService,
										 ISysParaService sysParaServiceImpl) {
		this.polymarketService = polymarketService;
		this.bizCommonService = bizCommonService;
		this.polymarketOrderService = polymarketOrderService;
		this.userMoneyService = userMoneyService;
		this.userWalletService = userWalletService;
		this.userInfoService = userInfoService;
		this.sysParaServiceImpl = sysParaServiceImpl;
	}

	/**
	 * 生成内部订单报价预览。
	 *
	 * <p>只读取当前AFI价格和Polymarket结果价格，不扣钱包、不写订单。正式下单时仍会重新拉取实时价格。</p>
	 *
	 * @param req 报价请求，afiAmount表示用户计划支付的AFI数量
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
	 * @param req 下单请求，afiAmount会从用户AFI钱包validNum2扣减
	 * @param userId 当前App用户ID
	 * @return 已创建的内部订单快照
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	@RedisLock(value = "polymarket:order:create", param = "#userId")
	public ResultPista<PolymarketOrderDto> create(PolymarketOrderReq req, Long userId) {
		// 步骤1：重新生成价格快照，保证下单使用后端实时价格，不使用前端报价结果。
		MarketPriceSnapshot snapshot = buildMarketPriceSnapshot(req);
		// 步骤2：读取用户与钱包余额，确认用户存在且AFI可用余额足够。
		UserInfo userInfo = userInfoService.lambdaQuery().eq(UserInfo::getUserId, userId).one();
		if (userInfo == null) {
			throw new ServiceException(ResponseCode.CODE_1001);
		}
		UserMoney userMoney = userMoneyService.lambdaQuery().eq(UserMoney::getId, userId).one();
		if (userMoney == null || userMoney.getValidNum2() == null || userMoney.getValidNum2().compareTo(snapshot.afiAmount) < 0) {
			throw new ServiceException(ResponseCode.CODE_1015);
		}

		// 步骤3：事务内先扣AFI并写钱包流水；后续订单入库失败会整体回滚。
		String orderNo = IDUtils.getSnowflakeStr();
		int rows = userWalletService.handerUserMoney(snapshot.afiAmount.negate(), orderNo, userId, userId,
			ConstantType.user_money_log_source_type.type_40, ConstantType.user_money_coin_type.type_2);
		if (rows != 1) {
			throw new ServiceException(ResponseCode.CODE_1015);
		}

		// 步骤4：保存内部订单，记录下单时的市场、价格、份额和最大兑付快照。
		PolymarketOrder order = buildOrder(orderNo, userId, userInfo.getAccount(), snapshot);
		if (!polymarketOrderService.save(order)) {
			throw new ServiceException(ResponseCode.CODE_1002);
		}
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
			throw new ServiceException("orderNo cannot be blank");
		}
		PolymarketOrder order = polymarketOrderService.lambdaQuery()
			.eq(PolymarketOrder::getOrderNo, orderNo.trim())
			.eq(PolymarketOrder::getUserId, userId)
			.eq(PolymarketOrder::getDeleted, 0)
			.one();
		if (order == null) {
			throw new ServiceException(ResponseCode.CODE_1001);
		}
		return toDto(order, true);
	}

	/**
	 * 批量结算已到期的待结算订单。
	 *
	 * <p>只自动处理Polymarket已经resolved且outcomePrices明确为0/1的市场；争议、50/50、字段缺失或接口异常都进入待人工复核，
	 * 避免错误兑付用户资产。</p>
	 *
	 * @param limit 本批最多处理的订单数
	 * @return 被更新为已猜中、未猜中或待复核的订单数量
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public int settlePendingOrders(Integer limit) {
		int pageSize = normalizeSettleLimit(limit);
		// 步骤1：按结束时间扫描待结算订单，每批限制数量，避免一次任务处理过多。
		List<PolymarketOrder> pendingOrders = polymarketOrderService.lambdaQuery()
			.eq(PolymarketOrder::getStatus, STATUS_PENDING)
			.eq(PolymarketOrder::getDeleted, 0)
			.le(PolymarketOrder::getEndTime, new Date())
			.orderByAsc(PolymarketOrder::getId)
			.last("limit " + pageSize)
			.list();
		if (CollectionUtil.isEmpty(pendingOrders)) {
			return 0;
		}

		int updated = 0;
		for (PolymarketOrder order : pendingOrders) {
			// 步骤2：逐笔读取Polymarket最终结果并结算；失败或不明确的订单进入人工复核。
			if (settleOneOrder(order)) {
				updated++;
			}
		}
		return updated;
	}

	/**
	 * 结算单笔待结算订单。
	 *
	 * <p>猜中时按订单份额兑付USDT到validNum1；猜错时兑付0；结果不明确时转待人工复核。</p>
	 *
	 * @param order 待结算订单
	 * @return true表示订单状态发生变化
	 */
	private boolean settleOneOrder(PolymarketOrder order) {
		JSONObject marketWrapper;
		JSONObject market;
		SettlementResult result;
		try {
			// 步骤1：按订单marketSlug实时查询Polymarket市场结果快照。
			marketWrapper = polymarketService.getMarketBySlug(order.getMarketSlug());
			market = marketWrapper.getJSONObject("market");
			// 步骤2：只接受UMA已resolved且outcomePrices明确0/1的结果。
			result = resolveWinner(market);
		} catch (Exception e) {
			return markNeedReview(order, order.getSettleSnapshotJson(), e.getMessage());
		}
		if (!result.resolved) {
			return markNeedReview(order, marketWrapper.toJSONString(), result.reason);
		}

		Date now = new Date();
		order.setResultOutcomeIndex(result.winnerIndex);
		order.setResultOutcomeName(result.winnerName);
		order.setSettleSnapshotJson(marketWrapper.toJSONString());
		order.setSettleTime(now);
		order.setUpdateTime(now);
		if (order.getOutcomeIndex().equals(result.winnerIndex)) {
			// 步骤3A：用户选中的结果是赢家，按shareAmount兑付USDT钱包validNum1。
			order.setStatus(STATUS_WON);
			order.setPayoutUsdtAmount(order.getShareAmount());
			int rows = userWalletService.handerUserMoney(order.getShareAmount(), order.getOrderNo(), order.getUserId(), order.getUserId(),
				ConstantType.user_money_log_source_type.type_41, ConstantType.user_money_coin_type.type_1);
			if (rows != 1) {
				throw new ServiceException(ResponseCode.CODE_1015);
			}
		} else {
			// 步骤3B：用户未猜中，只更新订单状态，不产生钱包入账。
			order.setStatus(STATUS_LOST);
			order.setPayoutUsdtAmount(BigDecimal.ZERO.setScale(MONEY_SCALE, RoundingMode.DOWN));
		}
		// 步骤4：最后更新订单结算结果；如果更新失败，前面的钱包入账会随事务回滚。
		if (!polymarketOrderService.updateById(order)) {
			throw new ServiceException("Polymarket order update failed");
		}
		return true;
	}

	/**
	 * 将订单标记为待人工复核。
	 *
	 * <p>当Polymarket结果不明确、接口失败或字段缺失时，不自动兑付，保存原因和快照供后台人工处理。</p>
	 *
	 * @param order 待处理订单
	 * @param snapshotJson 最近一次结算市场快照，可为空
	 * @param reason 进入复核的原因
	 * @return true表示订单更新成功
	 */
	private boolean markNeedReview(PolymarketOrder order, String snapshotJson, String reason) {
		order.setStatus(STATUS_NEED_REVIEW);
		order.setSettleSnapshotJson(snapshotJson);
		order.setRemark(StrUtil.subPre(StrUtil.blankToDefault(reason, "Polymarket settlement needs review"), 255));
		order.setUpdateTime(new Date());
		return polymarketOrderService.updateById(order);
	}

	/**
	 * 构建下单或报价使用的价格快照。
	 *
	 * <p>该方法会实时读取Polymarket市场、校验是否可下单、读取AFI价格，并计算AFI等值USDT、购买份额和最大兑付。</p>
	 *
	 * @param req 报价或下单请求
	 * @return 报价和创建订单共用的不可变业务快照
	 */
	private MarketPriceSnapshot buildMarketPriceSnapshot(PolymarketOrderReq req) {
		// 步骤1：先做本地请求参数校验，避免无效参数触发外部请求。
		validateRequest(req);
		// 步骤2：实时读取Polymarket市场详情，并校验市场仍可交易。
		JSONObject marketWrapper = polymarketService.getMarketBySlug(req.getMarketSlug().trim());
		JSONObject market = marketWrapper.getJSONObject("market");
		if (market == null) {
			throw new ServiceException("Polymarket market data is empty");
		}
		validateMarketOpen(market);

		// 步骤3：解析结果名称与结果价格，outcomeIndex必须同时落在两个数组范围内。
		JSONArray outcomes = parseJsonArray(market.getString("outcomes"), "outcomes");
		JSONArray outcomePrices = parseJsonArray(market.getString("outcomePrices"), "outcomePrices");
		if (req.getOutcomeIndex() >= outcomes.size() || req.getOutcomeIndex() >= outcomePrices.size()) {
			throw new ServiceException("outcomeIndex is out of range");
		}
		String outcomeName = outcomes.getString(req.getOutcomeIndex());
		BigDecimal outcomePrice = outcomePrices.getBigDecimal(req.getOutcomeIndex());
		if (outcomePrice == null || outcomePrice.compareTo(BigDecimal.ZERO) <= 0) {
			throw new ServiceException("Polymarket outcome price is empty or zero");
		}

		// 步骤4：读取当前AFI价格，按AFI金额折算USDT，再按结果价格计算持有份额。
		BigDecimal afiAmount = req.getAfiAmount().setScale(MONEY_SCALE, RoundingMode.DOWN);
		BigDecimal afiPrice = bizCommonService.getAfiPrice().setScale(MONEY_SCALE, RoundingMode.DOWN);
		BigDecimal afiUsdtAmount = afiAmount.multiply(afiPrice).setScale(MONEY_SCALE, RoundingMode.DOWN);
		if (afiUsdtAmount.compareTo(BigDecimal.ZERO) <= 0) {
			throw new ServiceException("AFI USDT value must be greater than zero");
		}
		BigDecimal shareAmount = afiUsdtAmount.divide(outcomePrice, SHARE_SCALE, RoundingMode.DOWN);
		if (shareAmount.compareTo(BigDecimal.ZERO) <= 0) {
			throw new ServiceException("Calculated share amount must be greater than zero");
		}

		// 步骤5：组装快照，后续报价直接返回，正式下单会把这些字段落到订单表。
		MarketPriceSnapshot snapshot = new MarketPriceSnapshot();
		snapshot.marketWrapperJson = marketWrapper.toJSONString();
		snapshot.market = market;
		snapshot.marketSlug = market.getString("slug");
		snapshot.marketId = market.getString("id");
		snapshot.conditionId = market.getString("conditionId");
		snapshot.marketQuestion = market.getString("question");
		snapshot.eventSlug = firstEventString(market, "slug");
		snapshot.eventTitle = firstEventString(market, "title");
		snapshot.outcomeIndex = req.getOutcomeIndex();
		snapshot.outcomeName = outcomeName;
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
	 * <p>最低AFI数量使用系统参数，单位是AFI；该校验在外部接口调用前执行。</p>
	 *
	 * @param req 报价或下单请求
	 */
	private void validateRequest(PolymarketOrderReq req) {
		if (req == null || StrUtil.isBlank(req.getMarketSlug())) {
			throw new ServiceException("marketSlug cannot be blank");
		}
		if (req.getOutcomeIndex() == null || req.getOutcomeIndex() < 0) {
			throw new ServiceException("outcomeIndex must be non-negative");
		}
		if (req.getAfiAmount() == null || req.getAfiAmount().compareTo(BigDecimal.ZERO) <= 0) {
			throw new ServiceException("afiAmount must be greater than zero");
		}
		BigDecimal minAfiAmount = getMinAfiOrderAmount();
		if (req.getAfiAmount().compareTo(minAfiAmount) < 0) {
			throw new ServiceException("afiAmount cannot be less than " + minAfiAmount.stripTrailingZeros().toPlainString() + " AFI");
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
			throw new ServiceException("Polymarket market is closed or inactive");
		}
		Boolean acceptingOrders = market.getBoolean("acceptingOrders");
		if (Boolean.FALSE.equals(acceptingOrders)) {
			throw new ServiceException("Polymarket market is not accepting orders");
		}
		Date endTime = parseEndTime(market);
		if (endTime != null && endTime.getTime() - System.currentTimeMillis() < MIN_SECONDS_BEFORE_END * 1000L) {
			// 短周期Up/Down市场波动很快，结束前最后5秒禁止内部下单，避免价格快照和结算结果贴得过近。
			throw new ServiceException("Polymarket market will end within " + MIN_SECONDS_BEFORE_END + " seconds");
		}
	}

	/**
	 * 从Polymarket resolved市场快照中解析赢家结果。
	 *
	 * <p>自动结算只接受唯一一个价格为1、其他价格为0的结果；其他情况都返回未明确原因，进入人工复核。</p>
	 *
	 * @param market Polymarket市场详情
	 * @return 解析后的赢家结果；无法自动判断时返回未明确原因
	 */
	private SettlementResult resolveWinner(JSONObject market) {
		if (market == null) {
			return SettlementResult.unresolved("empty market");
		}
		if (!"resolved".equalsIgnoreCase(market.getString("umaResolutionStatus"))) {
			return SettlementResult.unresolved("market is not resolved");
		}
		JSONArray outcomes = parseJsonArray(market.getString("outcomes"), "outcomes");
		JSONArray prices = parseJsonArray(market.getString("outcomePrices"), "outcomePrices");
		int winnerIndex = -1;
		for (int i = 0; i < prices.size(); i++) {
			BigDecimal price = prices.getBigDecimal(i);
			if (price != null && price.compareTo(BigDecimal.ONE) == 0) {
				if (winnerIndex >= 0) {
					return SettlementResult.unresolved("multiple winning outcomes");
				}
				winnerIndex = i;
			} else if (price == null || price.compareTo(BigDecimal.ZERO) != 0) {
				return SettlementResult.unresolved("outcome price is not clear 0/1");
			}
		}
		if (winnerIndex < 0 || winnerIndex >= outcomes.size()) {
			return SettlementResult.unresolved("winner outcome not found");
		}
		return SettlementResult.resolved(winnerIndex, outcomes.getString(winnerIndex));
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
			.marketId(snapshot.marketId)
			.conditionId(snapshot.conditionId)
			.marketQuestion(snapshot.marketQuestion)
			.outcomeIndex(snapshot.outcomeIndex)
			.outcomeName(snapshot.outcomeName)
			.afiAmount(snapshot.afiAmount)
			.afiPrice(snapshot.afiPrice)
			.afiUsdtAmount(snapshot.afiUsdtAmount)
			.outcomePrice(snapshot.outcomePrice)
			.shareAmount(snapshot.shareAmount)
			.maxPayoutUsdt(snapshot.maxPayoutUsdt)
			.endTime(snapshot.endTime)
			.status(STATUS_PENDING)
			.payoutUsdtAmount(BigDecimal.ZERO.setScale(MONEY_SCALE, RoundingMode.DOWN))
			.orderSnapshotJson(snapshot.marketWrapperJson)
			.deleted(0)
			.build();
		order.setCreateTime(now);
		order.setUpdateTime(now);
		return order;
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
		if (!includeSnapshots) {
			dto.setOrderSnapshotJson(null);
			dto.setSettleSnapshotJson(null);
		}
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
			throw new ServiceException("Polymarket " + fieldName + " is empty");
		}
		return JSONArray.parseArray(json);
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
				throw new ServiceException("Polymarket endDate format is invalid");
			}
		}
	}

	/**
	 * 归一化待结算任务批次大小。
	 *
	 * @param limit 请求处理数量
	 * @return 限制后的处理数量
	 */
	private int normalizeSettleLimit(Integer limit) {
		if (limit == null || limit <= 0) {
			return DEFAULT_SETTLE_LIMIT;
		}
		return Math.min(limit, MAX_SETTLE_LIMIT);
	}

	/**
	 * 报价和创建订单共用的计算快照。
	 */
	private static class MarketPriceSnapshot {
		private String marketWrapperJson;
		private JSONObject market;
		private String marketSlug;
		private String marketId;
		private String conditionId;
		private String marketQuestion;
		private String eventSlug;
		private String eventTitle;
		private Integer outcomeIndex;
		private String outcomeName;
		private BigDecimal afiAmount;
		private BigDecimal afiPrice;
		private BigDecimal afiUsdtAmount;
		private BigDecimal outcomePrice;
		private BigDecimal shareAmount;
		private BigDecimal maxPayoutUsdt;
		private Date endTime;
	}

	/**
	 * Polymarket结果解析后的结算判断。
	 */
	private static class SettlementResult {
		private final boolean resolved;
		private final int winnerIndex;
		private final String winnerName;
		private final String reason;

		private SettlementResult(boolean resolved, int winnerIndex, String winnerName, String reason) {
			this.resolved = resolved;
			this.winnerIndex = winnerIndex;
			this.winnerName = winnerName;
			this.reason = reason;
		}

		private static SettlementResult resolved(int winnerIndex, String winnerName) {
			return new SettlementResult(true, winnerIndex, winnerName, null);
		}

		private static SettlementResult unresolved(String reason) {
			return new SettlementResult(false, -1, null, reason);
		}
	}
}
