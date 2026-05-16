package com.xms.dao.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.xms.common.constant.ConstantType;
import com.xms.common.exception.ServiceException;
import com.xms.common.result.ResponseCode;
import com.xms.dao.domain.PolymarketOrder;
import com.xms.dao.mapper.PolymarketOrderMapper;
import com.xms.dao.service.IPolymarketOrderService;
import com.xms.dao.service.UserWalletService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;

/**
 * Polymarket平台内部订单DAO服务实现。
 *
 * <p>这里承接后台订单列表、后台手动结算和Quartz定时结算。结算成功时会写订单结果，并在猜中时兑付USDT到用户validNum1。</p>
 */
@Service
public class PolymarketOrderServiceImpl extends XmsDataServiceImpl<PolymarketOrderMapper, PolymarketOrder>
	implements IPolymarketOrderService {

	private static final int STATUS_PENDING = 0;
	private static final int STATUS_WON = 1;
	private static final int STATUS_LOST = 2;
	private static final int STATUS_NEED_REVIEW = 3;
	private static final int DEFAULT_SETTLE_LIMIT = 100;
	private static final int MAX_SETTLE_LIMIT = 500;
	private static final int REQUEST_TIMEOUT_MS = 5000;
	private static final String GAMMA_MARKET_SLUG_URL = "https://gamma-api.polymarket.com/markets/slug/";

	private final UserWalletService userWalletService;

	public PolymarketOrderServiceImpl(UserWalletService userWalletService) {
		this.userWalletService = userWalletService;
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
	 * 批量结算已到期的待结算订单。
	 *
	 * <p>只自动处理UMA已resolved且最终outcomePrices明确为0/1的市场；其他结果进入待人工复核，避免错误兑付。</p>
	 *
	 * @param limit 本批最多处理的订单数
	 * @return 被更新为终态或待复核的订单数量
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public int settlePendingOrders(Integer limit) {
		// 步骤1：扫描已到结束时间的待结算订单，限制批次大小，避免一次任务处理过多。
		List<PolymarketOrder> pendingOrders = lambdaQuery()
			.eq(PolymarketOrder::getStatus, STATUS_PENDING)
			.eq(PolymarketOrder::getDeleted, 0)
			.le(PolymarketOrder::getEndTime, new Date())
			.orderByAsc(PolymarketOrder::getId)
			.last("limit " + normalizeLimit(limit))
			.list();
		int updated = 0;
		for (PolymarketOrder order : pendingOrders) {
			// 步骤2：逐笔查询Polymarket最终结果并写订单状态。
			if (settleOneOrder(order)) {
				updated++;
			}
		}
		return updated;
	}

	/**
	 * 结算单笔内部订单。
	 *
	 * <p>猜中时按shareAmount兑付USDT；猜错时兑付0；无法安全判断结果时转待人工复核。</p>
	 *
	 * @param order 待结算订单
	 * @return true表示订单状态已更新
	 */
	private boolean settleOneOrder(PolymarketOrder order) {
		JSONObject market;
		SettlementResult result;
		try {
			// 步骤1：按订单保存的marketSlug查询Polymarket当前市场详情。
			market = fetchMarketBySlug(order.getMarketSlug());
			// 步骤2：解析赢家，只接受已resolved且价格明确为0/1的结果。
			result = resolveWinner(market);
		} catch (Exception e) {
			return markNeedReview(order, order.getSettleSnapshotJson(), e.getMessage());
		}
		if (!result.resolved) {
			return markNeedReview(order, market.toJSONString(), result.reason);
		}

		Date now = new Date();
		order.setResultOutcomeIndex(result.winnerIndex);
		order.setResultOutcomeName(result.winnerName);
		order.setSettleSnapshotJson(market.toJSONString());
		order.setSettleTime(now);
		order.setUpdateTime(now);
		if (order.getOutcomeIndex().equals(result.winnerIndex)) {
			// 步骤3A：用户猜中，兑付USDT到validNum1并写钱包流水。
			order.setStatus(STATUS_WON);
			order.setPayoutUsdtAmount(order.getShareAmount());
			int rows = userWalletService.handerUserMoney(order.getShareAmount(), order.getOrderNo(), order.getUserId(), order.getUserId(),
				ConstantType.user_money_log_source_type.type_41, ConstantType.user_money_coin_type.type_1);
			if (rows != 1) {
				throw new ServiceException(ResponseCode.CODE_1015);
			}
		} else {
			// 步骤3B：用户猜错，只落订单状态和0兑付金额，不写钱包入账。
			order.setStatus(STATUS_LOST);
			order.setPayoutUsdtAmount(BigDecimal.ZERO.setScale(6, RoundingMode.DOWN));
		}
		// 步骤4：更新订单结算字段；失败时事务回滚，避免钱包和订单状态不一致。
		if (!updateById(order)) {
			throw new ServiceException("Polymarket order update failed");
		}
		return true;
	}

	/**
	 * 将订单转为待人工复核。
	 *
	 * @param order 需要更新的订单
	 * @param snapshotJson 市场快照，可为空
	 * @param reason 复核原因
	 * @return 更新结果
	 */
	private boolean markNeedReview(PolymarketOrder order, String snapshotJson, String reason) {
		order.setStatus(STATUS_NEED_REVIEW);
		order.setSettleSnapshotJson(snapshotJson);
		order.setRemark(StrUtil.subPre(StrUtil.blankToDefault(reason, "Polymarket settlement needs review"), 255));
		order.setUpdateTime(new Date());
		return updateById(order);
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
	 * 限制单次结算任务处理数量。
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
