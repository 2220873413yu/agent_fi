package com.xms.app.client;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson2.JSONArray;
import com.xms.common.notify.AsyncPolymarketMarketSettleService;
import com.xms.dao.domain.PolymarketMarket;
import com.xms.dao.service.IPolymarketMarketService;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

/**
 * Polymarket WebSocket客户端启动和订阅服务。
 *
 * <p>该组件替代旧数海行情启动逻辑。启动时从市场聚合表加载待结算市场的asset_id，
 * 连接Polymarket Market Channel，并在握手成功或重连后发送订阅消息。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LongLiveClientStart {

	private static final int MARKET_STATUS_PENDING = 0;
	private static final int SUBSCRIBE_LIMIT = 500;

	private final IPolymarketMarketService polymarketMarketService;
	private final AsyncPolymarketMarketSettleService asyncPolymarketMarketSettleService;
	private volatile Channel activeChannel;

	/**
	 * 异步启动Polymarket Market Channel客户端。
	 *
	 * <p>启动后只订阅待结算市场的asset_id；没有待结算市场时仍保持连接，后续可通过重连或扩展刷新订阅。</p>
	 */
	@Async("asyncVirtualExecutor")
	public void handerWsMsg() throws Exception {
		createWsMsg();
	}

	/**
	 * 创建Polymarket WebSocket连接。
	 *
	 * <p>连接建立后由handler在握手成功事件中调用{@link #sendSubscribeMessage(Channel)}发送订阅。</p>
	 */
	public void createWsMsg() throws Exception {
		CountDownLatch countDownLatch = new CountDownLatch(1);
		TcpClient webSocketClient = new TcpClient(GlobalConstant.POLYMARKET_MARKET_WS_URL, countDownLatch, this);
		webSocketClient.connect();
		countDownLatch.await();
		log.info("Polymarket WebSocket客户端启动完成");
	}

	/**
	 * 向Polymarket发送Market Channel订阅消息。
	 *
	 * <p>订阅资产来自当前待结算市场的asset_ids_json。该方法可在首次握手成功和断线重连后重复调用。</p>
	 *
	 * @param channel 已完成WebSocket握手的通道
	 */
	public void sendSubscribeMessage(Channel channel) {
		// 采用全量重订阅：每次从数据库重新加载所有待结算市场asset_id，避免本地订阅状态和数据库不一致。
		// 同一个市场多次下单会重复触发刷新，但loadPendingAssetIds会去重；重复开奖事件也由市场状态兜底。
		if (channel == null || !channel.isActive()) {
			log.info("Polymarket WebSocket通道不可用，暂不刷新订阅，等待重连或Quartz兜底");
			return;
		}
		this.activeChannel = channel;
		List<String> assetIds = loadPendingAssetIds();
		if (CollectionUtil.isEmpty(assetIds)) {
			log.info("当前没有可订阅的Polymarket待结算asset_id");
			return;
		}
		com.alibaba.fastjson2.JSONObject subscribe = new com.alibaba.fastjson2.JSONObject();
		ArrayList as = new ArrayList<>();
		as.add(assetIds.getFirst());
		subscribe.put("assets_ids", as);
		subscribe.put("type", GlobalConstant.POLYMARKET_MARKET_TYPE);
		subscribe.put("custom_feature_enabled", true);
		channel.writeAndFlush(new TextWebSocketFrame(subscribe.toJSONString()));
		log.info("Polymarket Market Channel订阅已发送，asset{}", as);
	}

	/**
	 * 下单成功后刷新Polymarket订阅。
	 *
	 * <p>这是实时监听增强逻辑：只重新发送当前所有待结算市场的asset_id订阅，不发奖、不改市场状态。
	 * 如果WebSocket当前不可用，则只记录日志，等待重连重新订阅或Quartz兜底派发。</p>
	 */
	public void refreshSubscribeAfterOrderCreated() {
		try {
			sendSubscribeMessage(activeChannel);
		} catch (Exception e) {
			log.warn("Polymarket下单后刷新WebSocket订阅失败，等待重连或Quartz兜底，error={}", e.getMessage());
		}
	}

	/**
	 * 从市场表加载待结算市场的所有asset_id。
	 *
	 * <p>只查询status=0且未删除、asset_ids_json不为空的市场；使用LinkedHashSet去重并保持稳定顺序。</p>
	 *
	 * @return 待订阅的asset_id列表
	 */
	private List<String> loadPendingAssetIds() {
		// 只订阅待结算市场；已结算、人工复核或已派发中的市场不再加入订阅集合。
		// LinkedHashSet用于去重并保持稳定顺序，同市场先买Yes再买No时不会重复发送相同asset_id。
		List<PolymarketMarket> markets = polymarketMarketService.lambdaQuery()
			.eq(PolymarketMarket::getStatus, MARKET_STATUS_PENDING)
			.eq(PolymarketMarket::getDeleted, 0)
			.isNotNull(PolymarketMarket::getAssetIdsJson)
			.orderByAsc(PolymarketMarket::getEndTime)
			.last("limit " + SUBSCRIBE_LIMIT)
			.list();
		Set<String> assetIds = new LinkedHashSet<>();
		for (PolymarketMarket market : markets) {
			try {
				JSONArray array = JSONArray.parseArray(market.getAssetIdsJson());
				if (array == null) {
					continue;
				}
				for (int i = 0; i < array.size(); i++) {
					String assetId = array.getString(i);
					if (assetId != null && !assetId.trim().isEmpty()) {
						assetIds.add(assetId.trim());
					}
				}
			} catch (Exception e) {
				log.warn("Polymarket市场asset_ids_json解析失败，marketSlug={}, error={}", market.getMarketSlug(), e.getMessage());
			}
		}
		return new ArrayList<>(assetIds);
	}

	/**
	 * 派发Polymarket已开奖市场进入结算中。
	 *
	 * <p>WebSocket只负责触发状态抢占和投递结算队列，不直接发奖、不写钱包。真正结算由消费者调用processSettlingMarket完成。</p>
	 *
	 * @param slug Polymarket市场slug，可为空
	 * @param winningAssetId 赢家asset_id/token_id，可为空
	 * @param winningOutcome 赢家名称，仅用于日志
	 */
	public void dispatchResolvedMarket(String slug, String winningAssetId, String winningOutcome) {
		// WebSocket只是发现市场已开奖：这里只把市场从0待结算抢占为1结算中。
		// 不在这里结算订单、不写钱包；后续延迟队列消费者统一调用processSettlingMarket发奖。
		PolymarketMarket market = findPendingMarket(slug, winningAssetId);
		if (market == null) {
			log.info("Polymarket已开奖事件未匹配到待结算市场，slug={}, winningAssetId={}, winningOutcome={}", slug, winningAssetId, winningOutcome);
			return;
		}
		boolean dispatched = polymarketMarketService.markSettling(market.getMarketSlug());
		if (dispatched) {
			log.info("Polymarket市场已由WebSocket派发为结算中，marketSlug={}, winningAssetId={}, winningOutcome={}",
				market.getMarketSlug(), winningAssetId, winningOutcome);
			// WebSocket路径和Quartz路径共用同一个派发服务，消费者统一复核Gamma API后再发奖。
			asyncPolymarketMarketSettleService.sendMarketSettleMessage(market.getMarketSlug());
		}
	}

	/**
	 * 按slug或赢家asset_id查找仍待结算的市场。
	 *
	 * <p>优先使用slug精确匹配；如果事件缺少slug，则使用winning_asset_id在asset_ids_json中模糊匹配。</p>
	 *
	 * @param slug Polymarket市场slug
	 * @param winningAssetId 赢家asset_id/token_id
	 * @return 待结算市场，找不到时返回null
	 */
	private PolymarketMarket findPendingMarket(String slug, String winningAssetId) {
		if (slug != null && !slug.trim().isEmpty()) {
			return polymarketMarketService.lambdaQuery()
				.eq(PolymarketMarket::getMarketSlug, slug.trim())
				.eq(PolymarketMarket::getStatus, MARKET_STATUS_PENDING)
				.eq(PolymarketMarket::getDeleted, 0)
				.one();
		}
		if (winningAssetId == null || winningAssetId.trim().isEmpty()) {
			return null;
		}
		return polymarketMarketService.lambdaQuery()
			.eq(PolymarketMarket::getStatus, MARKET_STATUS_PENDING)
			.eq(PolymarketMarket::getDeleted, 0)
			.like(PolymarketMarket::getAssetIdsJson, winningAssetId.trim())
			.orderByAsc(PolymarketMarket::getEndTime)
			.last("limit 1")
			.one();
	}
}
