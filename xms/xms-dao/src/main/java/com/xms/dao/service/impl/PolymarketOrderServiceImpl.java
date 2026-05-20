package com.xms.dao.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.xms.common.constant.Constants;
import com.xms.common.constant.ConstantType;
import com.xms.common.exception.ServiceException;
import com.xms.common.notify.AsyncPolymarketMarketSettleService;
import com.xms.common.result.ResponseCode;
import com.xms.common.utils.uuid.IDUtils;
import com.xms.dao.domain.PolymarketMarket;
import com.xms.dao.domain.PolymarketOrder;
import com.xms.dao.entity.domain.UserMoney;
import com.xms.dao.mapper.PolymarketOrderMapper;
import com.xms.dao.service.IPolymarketMarketService;
import com.xms.dao.service.IPolymarketOrderService;
import com.xms.dao.service.UserWalletService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Polymarket平台内部订单DAO服务实现。
 *
 * <p>这里承接后台订单列表、后台手动结算和Quartz兜底结算。结算以市场为单位抢占状态，同一个marketSlug只查询一次Polymarket，
 * 然后批量处理该市场下所有待结算订单。</p>
 */
@Service
public class PolymarketOrderServiceImpl extends XmsDataServiceImpl<PolymarketOrderMapper, PolymarketOrder>
	implements IPolymarketOrderService {

	private static final int ORDER_STATUS_PENDING = 0;
	private static final int ORDER_STATUS_SETTLED = 1;
	private static final int RESOLVED_STATUS_WON = 1;
	private static final int RESOLVED_STATUS_LOST = 2;
	private static final int MARKET_STATUS_PENDING = 0;
	private static final int MARKET_STATUS_SETTLING = 1;
	private static final int MARKET_STATUS_COMPLETED = 2;
	private static final int MARKET_STATUS_NEED_REVIEW = 3;
	private static final int DEFAULT_SETTLE_LIMIT = 100;
	private static final int MAX_SETTLE_LIMIT = 500;
	private static final int REQUEST_TIMEOUT_MS = 5000;
	private static final int MONEY_SCALE = 6;
	private static final int SHARE_SCALE = 8;
	private static final int STUCK_SETTLING_MINUTES = 10;
	private static final int WALLET_BATCH_SIZE = 1000;
	private static final String GAMMA_MARKET_SLUG_URL = "https://gamma-api.polymarket.com/markets/slug/";

	private final UserWalletService userWalletService;
	private final IPolymarketMarketService polymarketMarketService;
	private final AsyncPolymarketMarketSettleService asyncPolymarketMarketSettleService;
	private final Environment environment;
	private final String lqBaseUrl;

	public PolymarketOrderServiceImpl(UserWalletService userWalletService,
									  IPolymarketMarketService polymarketMarketService,
									  AsyncPolymarketMarketSettleService asyncPolymarketMarketSettleService,
									  Environment environment,
									  @Value("${lq.baseUrl}") String lqBaseUrl) {
		this.userWalletService = userWalletService;
		this.polymarketMarketService = polymarketMarketService;
		this.asyncPolymarketMarketSettleService = asyncPolymarketMarketSettleService;
		this.environment = environment;
		this.lqBaseUrl = lqBaseUrl;
	}

	/**
	 * 通过XML Mapper查询Polymarket内部订单。
	 *
	 * <p>后台列表和导出共用同一套SQL，保证筛选条件一致。</p>
	 *
	 * @param polymarketOrder 查询条件
	 * @return 订单列表
	 */
	@Override
	public List<PolymarketOrder> selectPolymarketOrderList(PolymarketOrder polymarketOrder) {
		return baseMapper.selectPolymarketOrderList(polymarketOrder);
	}

	/**
	 * 批量派发已到期的待结算市场。
	 *
	 * <p>Quartz兜底任务调用该方法。它只把市场从待结算抢占为结算中，并预留延迟队列发送位置；
	 * 当前不查询Polymarket、不处理订单、不写钱包。</p>
	 *
	 * @param limit 本批最多派发的市场数
	 * @return 成功改为结算中的市场数量
	 */
	@Override
	public int settlePendingOrders(Integer limit) {
		// 步骤1：扫描市场表中已到结束时间且仍待结算的市场，Quartz只负责派发，不直接结算订单。
		List<PolymarketMarket> pendingMarkets = polymarketMarketService.lambdaQuery()
			.eq(PolymarketMarket::getStatus, MARKET_STATUS_PENDING)
			.eq(PolymarketMarket::getDeleted, 0)
			.isNotNull(PolymarketMarket::getEndTime)
			.le(PolymarketMarket::getEndTime, new Date())
			.orderByAsc(PolymarketMarket::getEndTime)
			.last("limit " + normalizeLimit(limit))
			.list();
		if (CollectionUtil.isEmpty(pendingMarkets)) {
			return 0;
		}

		int updated = 0;
		for (PolymarketMarket market : pendingMarkets) {
			// 步骤2：只抢占状态为结算中；后续延迟队列消费者再真正查询Polymarket和批量结算订单。
			if (dispatchMarketForSettlement(market.getMarketSlug())) {
				updated++;
			}
		}
		return updated;
	}

	/**
	 * 重投递长时间处于结算中的Polymarket市场。
	 *
	 * <p>该方法用于兜底修复“市场已经被WebSocket或Quartz抢占为结算中，但队列消息未投递或消费者未执行”的数据。
	 * 只处理update_time距离当前时间超过10分钟的市场，避免上一轮消费者仍在处理时重复投递。</p>
	 *
	 * @param limit 本批最多重投递的市场数量
	 * @return 实际重投递的市场数量
	 */
	@Override
	public int redispatchStuckSettlingMarkets(Integer limit) {
		Date thresholdTime = buildStuckSettlingThresholdTime();
		// 步骤1：只扫描结算中且更新时间超过10分钟的市场，数据库状态仍是兜底准绳。
		List<PolymarketMarket> stuckMarkets = polymarketMarketService.lambdaQuery()
			.eq(PolymarketMarket::getStatus, MARKET_STATUS_SETTLING)
			.eq(PolymarketMarket::getDeleted, 0)
			.le(PolymarketMarket::getUpdateTime, thresholdTime)
			.orderByAsc(PolymarketMarket::getUpdateTime)
			.last("limit " + normalizeLimit(limit))
			.list();
		if (CollectionUtil.isEmpty(stuckMarkets)) {
			return 0;
		}

		int redispatched = 0;
		for (PolymarketMarket market : stuckMarkets) {
			if (StrUtil.isBlank(market.getMarketSlug())) {
				continue;
			}
			// 步骤2：不改市场状态、不直接发奖，只把marketSlug重新投递到统一延迟队列。
			asyncPolymarketMarketSettleService.sendMarketSettleMessage(market.getMarketSlug().trim());
			redispatched++;
		}
		return redispatched;
	}

	/**
	 * 派发单个市场进入结算中。
	 *
	 * <p>后台按钮可调用该方法。待结算市场会先改为结算中，然后立即调用真正结算方法；已经处于结算中的市场会直接尝试处理。</p>
	 *
	 * @param marketSlug Polymarket市场slug
	 * @return true表示市场状态被本次调用更新
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean settleMarketBySlug(String marketSlug) {
		if (StrUtil.isBlank(marketSlug)) {
			return false;
		}
		String slug = marketSlug.trim();
		PolymarketMarket market = polymarketMarketService.lambdaQuery()
			.eq(PolymarketMarket::getMarketSlug, slug)
			.eq(PolymarketMarket::getDeleted, 0)
			.one();
		if (market == null) {
			return false;
		}
		if (MARKET_STATUS_PENDING == market.getStatus()) {
			if (!dispatchMarketForSettlement(slug)) {
				return false;
			}
			return processSettlingMarket(slug);
		}
		if (MARKET_STATUS_SETTLING == market.getStatus()) {
			return processSettlingMarket(slug);
		}
		return false;
	}

	/**
	 * 处理已经处于结算中的市场。
	 *
	 * <p>该方法是后续延迟队列消费者真正应调用的结算入口。它只处理status=1的市场，查询Polymarket并批量结算该市场下订单。</p>
	 *
	 * @param marketSlug Polymarket市场slug
	 * @return true表示市场状态被本次调用更新
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean processSettlingMarket(String marketSlug) {
		if (StrUtil.isBlank(marketSlug)) {
			return false;
		}
		String slug = marketSlug.trim();
		PolymarketMarket market = polymarketMarketService.lambdaQuery()
			.eq(PolymarketMarket::getMarketSlug, slug)
			.eq(PolymarketMarket::getStatus, MARKET_STATUS_SETTLING)
			.eq(PolymarketMarket::getDeleted, 0)
			.one();
		if (market == null) {
			return false;
		}

		JSONObject marketSnapshot;
		SettlementResult result;
		Date now = new Date();
		try {
			// 步骤1：一个市场只请求一次Polymarket，后续所有用户订单复用同一个最终结果。
			marketSnapshot = fetchMarketBySlug(slug);
			result = resolveWinner(marketSnapshot);
		} catch (Exception e) {
			return markMarketNeedReview(market, null, e.getMessage(), now);
		}

		if (!result.resolved) {
			// 步骤2：未resolved说明还没正式开奖，回到待结算，等待Quartz后续重新派发。
			if ("market is not resolved".equals(result.reason)) {
				return returnMarketPending(market, marketSnapshot, result.reason, now);
			}
			// 步骤3：结果字段异常或价格不是明确0/1时转人工复核，避免错误兑付。
			return markMarketNeedReview(market, marketSnapshot, result.reason, now);
		}

		// 步骤4：市场结果明确后，批量结算该市场下全部待结算订单。
		List<PolymarketOrder> pendingOrders = lambdaQuery()
			.eq(PolymarketOrder::getMarketSlug, slug)
			.eq(PolymarketOrder::getStatus, ORDER_STATUS_PENDING)
			.orderByAsc(PolymarketOrder::getId)
			.list();

		BigDecimal totalPayout = BigDecimal.ZERO.setScale(MONEY_SCALE, RoundingMode.DOWN);
		BigDecimal totalPayoutAfi = BigDecimal.ZERO.setScale(SHARE_SCALE, RoundingMode.DOWN);
		List<UserMoney> walletIncrements = new ArrayList<>(WALLET_BATCH_SIZE);
		Date settleTime = new Date();
		BigDecimal payoutAfiPrice;
		try {
			// 步骤5：结算发奖改为等值AFI，先锁定本次结算使用的AFI/USDT实时价格快照。
			payoutAfiPrice = loadSettlementAfiPrice();
		} catch (Exception e) {
			return markMarketNeedReview(market, marketSnapshot, e.getMessage(), now);
		}
		for (PolymarketOrder order : pendingOrders) {
			order.setResultOutcomeIndex(result.winnerIndex);
			order.setResultOutcomeName(result.winnerName);
			order.setSettleTime(settleTime);
			order.setUpdateTime(settleTime);
			order.setPayoutAfiPrice(payoutAfiPrice);
			if (order.getOutcomeIndex().equals(result.winnerIndex)) {
				// 猜中订单保留USDT等值，并按结算AFI价格换算成AFI发到validNum2。
				BigDecimal payoutAfiAmount = calculatePayoutAfiAmount(order.getShareAmount(), payoutAfiPrice);
				order.setStatus(ORDER_STATUS_SETTLED);
				order.setResolvedStatus(RESOLVED_STATUS_WON);
				order.setPayoutUsdtAmount(order.getShareAmount());
				order.setPayoutAfiAmount(payoutAfiAmount);
				totalPayout = totalPayout.add(order.getShareAmount());
				totalPayoutAfi = totalPayoutAfi.add(payoutAfiAmount);
				walletIncrements.add(buildPayoutWalletIncrement(order));
				if (walletIncrements.size() >= WALLET_BATCH_SIZE) {
					batchUpdateMoneyValid2(walletIncrements);
					walletIncrements.clear();
				}
			} else {
				// 猜错订单只更新订单状态，不产生钱包入账。
				order.setStatus(ORDER_STATUS_SETTLED);
				order.setResolvedStatus(RESOLVED_STATUS_LOST);
				order.setPayoutUsdtAmount(BigDecimal.ZERO.setScale(MONEY_SCALE, RoundingMode.DOWN));
				order.setPayoutAfiAmount(BigDecimal.ZERO.setScale(SHARE_SCALE, RoundingMode.DOWN));
			}
		}

		batchUpdateMoneyValid2(walletIncrements);
		walletIncrements.clear();
		if (CollectionUtil.isNotEmpty(pendingOrders) && !updateBatchById(pendingOrders)) {
			throw new ServiceException("Polymarket order batch update failed");
		}

		// 步骤5：最后落市场完成状态和总兑付，事务失败会回滚钱包和订单状态。
		market.setStatus(MARKET_STATUS_COMPLETED);
		market.setUmaResolutionStatus(marketSnapshot.getString("umaResolutionStatus"));
		market.setResultOutcomeIndex(result.winnerIndex);
		market.setResultOutcomeName(result.winnerName);
		market.setWinningAssetId(result.winnerAssetId);
		market.setTotalPayoutUsdtAmount(totalPayout.setScale(MONEY_SCALE, RoundingMode.DOWN));
		market.setTotalPayoutAfiAmount(totalPayoutAfi.setScale(SHARE_SCALE, RoundingMode.DOWN));
		market.setLastCheckTime(now);
		market.setSettleTime(settleTime);
		market.setMarketSnapshotJson(marketSnapshot.toJSONString());
		market.setRemark(null);
		market.setUpdateTime(settleTime);
		return polymarketMarketService.updateById(market);
	}

	/**
	 * 将待结算市场派发为结算中。
	 *
	 * <p>状态抢占成功后投递marketSlug到延迟队列，由消费者统一调用processSettlingMarket完成复核和发奖。</p>
	 *
	 * @param marketSlug Polymarket市场slug
	 * @return true表示成功从待结算抢占为结算中
	 */
	private boolean dispatchMarketForSettlement(String marketSlug) {
		if (StrUtil.isBlank(marketSlug)) {
			return false;
		}
		boolean dispatched = polymarketMarketService.markSettling(marketSlug.trim());
		if (dispatched) {
			// 市场已经从待结算抢占为结算中，后续由延迟队列消费者统一处理订单结算和钱包入账。
			asyncPolymarketMarketSettleService.sendMarketSettleMessage(marketSlug.trim());
		}
		return dispatched;
	}

	/**
	 * 组装猜中订单的AFI钱包入账增量。
	 *
	 * <p>这里只收集UserMoney增量，后续由batchUpdateMoneyValid2按1000条分批落库。
	 * 入账金额使用订单已计算好的payoutAfiAmount；sourceId使用Polymarket订单ID，便于流水追踪到具体订单。</p>
	 *
	 * @param order 已确认猜中的Polymarket订单
	 * @return 可用于批量更新钱包validNum2的AFI增量
	 */
	private UserMoney buildPayoutWalletIncrement(PolymarketOrder order) {
		UserMoney userMoney = new UserMoney();
		userMoney.setId(order.getUserId());
		userMoney.setValidNum2(order.getPayoutAfiAmount());
		userMoney.setGtId(IDUtils.getSnowflake(ConstantType.user_money_coin_type.type_2).nextIdStr());
		userMoney.setSourceCode(order.getOrderNo());
		userMoney.setSourceType(ConstantType.user_money_log_source_type.type_41);
		userMoney.setSourceId(order.getId());
		userMoney.setUpdateTime(order.getSettleTime());
		return userMoney;
	}

	/**
	 * 批量更新中奖订单的AFI钱包validNum2。
	 *
	 * <p>该方法只处理已确认中奖订单的AFI入账增量，调用方按1000条分批传入；
	 * 如果实际更新行数与本批数量不一致，抛出异常让外层事务回滚钱包、订单和市场状态。</p>
	 *
	 * @param userMoneyList AFI钱包validNum2增量列表
	 */
	private void batchUpdateMoneyValid2(List<UserMoney> userMoneyList) {
		if (CollectionUtil.isEmpty(userMoneyList)) {
			return;
		}
		int rows = userWalletService.batchUpdateUserMoney(userMoneyList);
		if (rows != userMoneyList.size()) {
			throw new ServiceException(ResponseCode.CODE_1015);
		}
	}

	/**
	 * 获取Polymarket结算发奖使用的AFI/USDT实时价格。
	 *
	 * <p>发奖入口位于DAO模块，不能依赖App层BizCommonService，因此这里按同一个 `/chain/price` 上游口径读取AFI价格；
	 * dev-home环境沿用本地固定价0.5，避免本地调试依赖外部行情。</p>
	 *
	 * @return AFI/USDT价格，单位USDT
	 */
	private BigDecimal loadSettlementAfiPrice() {
		String profile = environment.getProperty(Constants.ACTIVE_PROFILES_PROPERTY);
		if (Constants.ACTIVE_PROPERTY_DEV_HOME.equalsIgnoreCase(profile)||
			Constants.ACTIVE_PROPERTY_DEV.equalsIgnoreCase(profile)) {
			return new BigDecimal("0.5").setScale(MONEY_SCALE, RoundingMode.DOWN);
		}
		try {
			HttpResponse response = HttpUtil.createGet(lqBaseUrl + "/chain/price").timeout(REQUEST_TIMEOUT_MS).execute();
			JSONObject jsonObject = JSON.parseObject(response.body());
			if (!"200".equals(jsonObject.getString("code"))) {
				throw new ServiceException(ResponseCode.CODE_1288);
			}
			JSONObject data = jsonObject.getJSONObject("data");
			BigDecimal price = data == null ? null : data.getBigDecimal("afi");
			if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
				throw new ServiceException(ResponseCode.CODE_1288);
			}
			return price.setScale(MONEY_SCALE, RoundingMode.DOWN);
		} catch (ServiceException e) {
			throw e;
		} catch (Exception e) {
			throw new ServiceException(ResponseCode.CODE_1288);
		}
	}

	/**
	 * 将中奖USDT等值按结算AFI价格换算成实际发放AFI数量。
	 *
	 * <p>AFI发放保留8位并向下取整，避免精度舍入导致超发；USDT等值仍保存在订单payoutUsdtAmount用于对账。</p>
	 *
	 * @param payoutUsdtAmount 中奖应兑付USDT等值
	 * @param payoutAfiPrice 结算时AFI/USDT价格
	 * @return 实际发放AFI数量
	 */
	private BigDecimal calculatePayoutAfiAmount(BigDecimal payoutUsdtAmount, BigDecimal payoutAfiPrice) {
		if (payoutUsdtAmount == null || payoutUsdtAmount.compareTo(BigDecimal.ZERO) <= 0
			|| payoutAfiPrice == null || payoutAfiPrice.compareTo(BigDecimal.ZERO) <= 0) {
			throw new ServiceException(ResponseCode.CODE_1288);
		}
		return payoutUsdtAmount.divide(payoutAfiPrice, SHARE_SCALE, RoundingMode.DOWN);
	}

	/**
	 * 将已抢占的市场退回待结算，等待后续重试。
	 *
	 * @param market 市场记录
	 * @param snapshot Polymarket最新快照
	 * @param reason 本次未能开奖的原因
	 * @param now 本次检查时间
	 * @return 更新结果
	 */
	private boolean returnMarketPending(PolymarketMarket market, JSONObject snapshot, String reason, Date now) {
		market.setStatus(MARKET_STATUS_PENDING);
		market.setUmaResolutionStatus(snapshot == null ? null : snapshot.getString("umaResolutionStatus"));
		market.setMarketSnapshotJson(snapshot == null ? null : snapshot.toJSONString());
		market.setLastCheckTime(now);
		market.setUpdateTime(now);
		market.setRemark(StrUtil.subPre(StrUtil.blankToDefault(reason, "Polymarket market is not resolved"), 255));
		return polymarketMarketService.updateById(market);
	}

	/**
	 * 将市场转为待人工复核。
	 *
	 * <p>结果不明确或接口异常时不自动结算订单，避免错误兑付用户资产。</p>
	 *
	 * @param market 市场记录
	 * @param snapshot Polymarket最新快照，可为空
	 * @param reason 复核原因
	 * @param now 本次检查时间
	 * @return 更新结果
	 */
	private boolean markMarketNeedReview(PolymarketMarket market, JSONObject snapshot, String reason, Date now) {
		market.setStatus(MARKET_STATUS_NEED_REVIEW);
		market.setUmaResolutionStatus(snapshot == null ? null : snapshot.getString("umaResolutionStatus"));
		market.setMarketSnapshotJson(snapshot == null ? null : snapshot.toJSONString());
		market.setLastCheckTime(now);
		market.setUpdateTime(now);
		market.setRemark(StrUtil.subPre(StrUtil.blankToDefault(reason, "Polymarket settlement needs review"), 255));
		return polymarketMarketService.updateById(market);
	}

	/**
	 * 按slug从Polymarket Gamma API查询市场详情。
	 *
	 * @param slug 市场slug
	 * @return 市场详情对象
	 */
	private JSONObject fetchMarketBySlug(String slug) {
		try (HttpResponse response = HttpUtil.createGet(GAMMA_MARKET_SLUG_URL + slug).timeout(REQUEST_TIMEOUT_MS).execute()) {
			if (!response.isOk()) {
				throw new ServiceException("Polymarket request failed, HTTP " + response.getStatus());
			}
			return JSON.parseObject(response.body());
		} catch (ServiceException e) {
			throw e;
		} catch (Exception e) {
			throw new ServiceException("Polymarket request error: " + e.getMessage());
		}
	}

	/**
	 * 从已resolved的Polymarket市场中解析赢家结果。
	 *
	 * @param market Polymarket市场原始对象
	 * @return 结算结果；无法自动判断时包含原因
	 */
	private SettlementResult resolveWinner(JSONObject market) {
		if (market == null || !"resolved".equalsIgnoreCase(market.getString("umaResolutionStatus"))) {
			return SettlementResult.unresolved("market is not resolved");
		}
		JSONArray outcomes = JSONArray.parseArray(market.getString("outcomes"));
		JSONArray prices = JSONArray.parseArray(market.getString("outcomePrices"));
		JSONArray clobTokenIds = StrUtil.isBlank(market.getString("clobTokenIds")) ? null : JSONArray.parseArray(market.getString("clobTokenIds"));
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
		String winnerAssetId = clobTokenIds == null || winnerIndex >= clobTokenIds.size() ? null : clobTokenIds.getString(winnerIndex);
		return SettlementResult.resolved(winnerIndex, outcomes.getString(winnerIndex), winnerAssetId);
	}

	/**
	 * 限制单次结算任务处理市场数量。
	 *
	 * @param limit 请求数量
	 * @return 归一化后的数量
	 */
	private int normalizeLimit(Integer limit) {
		if (limit == null || limit <= 0) {
			return DEFAULT_SETTLE_LIMIT;
		}
		return Math.min(limit, MAX_SETTLE_LIMIT);
	}

	/**
	 * 计算结算中市场重投递的时间阈值。
	 *
	 * <p>固定使用当前时间往前10分钟，避免刚进入结算中的市场还在消费者处理时被重复投递。</p>
	 *
	 * @return 当前时间减10分钟后的阈值时间
	 */
	private Date buildStuckSettlingThresholdTime() {
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.MINUTE, -STUCK_SETTLING_MINUTES);
		return calendar.getTime();
	}

	/**
	 * Polymarket结果解析后的结算判断。
	 */
	private static class SettlementResult {
		private final boolean resolved;
		private final int winnerIndex;
		private final String winnerName;
		private final String winnerAssetId;
		private final String reason;

		private SettlementResult(boolean resolved, int winnerIndex, String winnerName, String winnerAssetId, String reason) {
			this.resolved = resolved;
			this.winnerIndex = winnerIndex;
			this.winnerName = winnerName;
			this.winnerAssetId = winnerAssetId;
			this.reason = reason;
		}

		private static SettlementResult resolved(int winnerIndex, String winnerName, String winnerAssetId) {
			return new SettlementResult(true, winnerIndex, winnerName, winnerAssetId, null);
		}

		private static SettlementResult unresolved(String reason) {
			return new SettlementResult(false, -1, null, null, reason);
		}
	}
}
