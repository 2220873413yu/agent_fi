package com.xms.app.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.xms.app.entity.dto.PolymarketEventDto;
import com.xms.app.entity.dto.PolymarketEventListDto;
import com.xms.app.entity.dto.PolymarketEventMarketDto;
import com.xms.app.entity.dto.PolymarketMarketDetailDto;
import com.xms.app.entity.dto.PolymarketUpDownEventDto;
import com.xms.app.entity.dto.PolymarketUpDownListDto;
import com.xms.app.service.PolymarketService;
import com.xms.common.config.redis.XmsRedis;
import com.xms.common.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 读取Polymarket Gamma公开行情数据。
 *
 * <p>前端列表和详情接口返回本地精简DTO，并用Redis做短缓存；内部报价、下单和结算复核仍可读取上游原始JSON字段。</p>
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PolymarketServiceImpl implements PolymarketService {

	private static final String GAMMA_BASE_URL = "https://gamma-api.polymarket.com";
	private static final String EVENT_LIST_CACHE_KEY_PREFIX = "polymarket:events:simple:";
	private static final String MARKET_DETAIL_CACHE_KEY_PREFIX = "polymarket:market:detail:simple:";
	private static final String SECTION_CRYPTO = "crypto";
	private static final String SECTION_SPORTS = "sports";
	private static final int CRYPTO_TAG_ID = 21;
	private static final int SPORTS_TAG_ID = 1;
	private static final int DEFAULT_LIMIT = 20;
	private static final int MAX_LIMIT = 100;
	private static final int SIMPLE_MARKET_LIMIT = 8;
	private static final long EVENT_LIST_CACHE_SECONDS = 10L;
	private static final long MARKET_DETAIL_CACHE_SECONDS = 5L;
	private static final int REQUEST_TIMEOUT_MS = 5000;
	private static final long UP_DOWN_WINDOW_SECONDS = 300L;
	private static final int DEFAULT_UP_DOWN_BEFORE = 2;
	private static final int DEFAULT_UP_DOWN_AFTER = 6;
	private static final int MAX_UP_DOWN_SIDE_WINDOWS = 12;
	private static final List<String> SUPPORTED_UP_DOWN_COINS = Arrays.asList("btc", "eth", "sol", "xrp");

	private final XmsRedis xmsRedis;

	/**
	 * 查询普通Crypto/Sports板块事件列表，并裁剪成前端展示需要的精简字段。
	 *
	 * <p>Redis缓存只用于减少Gamma API请求压力，缓存时间固定10秒；缓存失败时会回退为实时请求上游，不影响页面展示。</p>
	 *
	 * @param section 板块名称，当前支持crypto和sports
	 * @param limit 每页事件数量，服务层会限制最大值
	 * @param offset 分页偏移量，从0开始
	 * @return 精简后的事件列表，每个事件最多返回8个市场
	 */
	@Override
	public PolymarketEventListDto listEvents(String section, Integer limit, Integer offset) {
		// 步骤1：归一化板块和分页参数，保证缓存key与上游请求地址稳定。
		String normalizedSection = normalizeSection(section);
		int tagId = tagIdOf(normalizedSection);
		int pageSize = normalizeLimit(limit);
		int pageOffset = normalizeOffset(offset);
		String sourceUrl = GAMMA_BASE_URL + "/events?tag_id=" + tagId
			+ "&active=true&closed=false&limit=" + pageSize
			+ "&offset=" + pageOffset
			+ "&order=volume24hr&ascending=false";

		// 步骤2：Redis只做10秒性能缓存；Polymarket上游仍是行情数据准绳。
		String cacheKey = EVENT_LIST_CACHE_KEY_PREFIX + normalizedSection + ":" + pageSize + ":" + pageOffset;
		try {
			return xmsRedis.get(cacheKey, () -> loadSimpleEvents(normalizedSection, tagId, pageSize, pageOffset, sourceUrl),
				EVENT_LIST_CACHE_SECONDS, TimeUnit.SECONDS);
		} catch (Exception e) {
			log.warn("Polymarket事件列表缓存读取失败，改为实时请求Gamma，cacheKey={}", cacheKey, e);
			return loadSimpleEvents(normalizedSection, tagId, pageSize, pageOffset, sourceUrl);
		}
	}

	/**
	 * 查询前端展示用的单个市场精简详情。
	 *
	 * <p>Redis缓存只用于减少详情刷新时的Gamma API压力，缓存时间固定5秒；正式报价和下单不读取该缓存。</p>
	 *
	 * @param slug Polymarket市场slug
	 * @return 市场详情精简DTO，不包含完整events原始数组
	 */
	@Override
	public PolymarketMarketDetailDto getMarketBySlug(String slug) {
		String cleanSlug = normalizeMarketSlug(slug);
		String sourceUrl = marketSlugUrl(cleanSlug);
		String cacheKey = MARKET_DETAIL_CACHE_KEY_PREFIX + cleanSlug;
		try {
			return xmsRedis.get(cacheKey, () -> loadSimpleMarketDetail(cleanSlug, sourceUrl),
				MARKET_DETAIL_CACHE_SECONDS, TimeUnit.SECONDS);
		} catch (Exception e) {
			log.warn("Polymarket市场详情缓存读取失败，改为实时请求Gamma，cacheKey={}", cacheKey, e);
			return loadSimpleMarketDetail(cleanSlug, sourceUrl);
		}
	}

	/**
	 * 查询内部业务使用的Polymarket原始市场详情。
	 *
	 * <p>报价、下单和结算复核依赖原始字段，例如outcomes、outcomePrices、clobTokenIds、events等，不能使用前端精简DTO替代。</p>
	 *
	 * @param slug Polymarket市场slug
	 * @return 原始市场详情包装对象，并带本地调试字段
	 */
	@Override
	public JSONObject getRawMarketBySlug(String slug) {
		String cleanSlug = normalizeMarketSlug(slug);
		String sourceUrl = marketSlugUrl(cleanSlug);
		Object market = fetchJson(sourceUrl);
		JSONObject result = baseResponse(null, null, sourceUrl);
		result.put("slug", cleanSlug);
		result.put("market", market);
		return result;
	}

	/**
	 * 查询加密货币5分钟Up/Down短周期事件。
	 *
	 * <p>Gamma没有提供一个完全等同官网短周期玩法页的聚合接口，所以这里按Polymarket固定slug规则生成
	 * {@code {coin}-updown-5m-{timestamp}}，逐个拉取已发布的事件。未发布的未来窗口会被跳过，保证页面可用。</p>
	 *
	 * @param coins 逗号分隔的币种，支持btc、eth、sol、xrp
	 * @param before 当前时间窗口之前的5分钟窗口数量
	 * @param after 当前时间窗口之后的5分钟窗口数量
	 * @return Up/Down事件集合，包含原始事件、币种、窗口开始/结束Unix秒和来源URL
	 */
	@Override
	public PolymarketUpDownListDto listCryptoUpDownEvents(String coins, Integer before, Integer after) {
		List<String> normalizedCoins = normalizeUpDownCoins(coins);
		int previousWindows = normalizeWindowCount(before, DEFAULT_UP_DOWN_BEFORE);
		int futureWindows = normalizeWindowCount(after, DEFAULT_UP_DOWN_AFTER);
		long currentWindow = floorToFiveMinuteWindow(Instant.now().getEpochSecond());
		List<PolymarketUpDownEventDto> events = new ArrayList<>();

		for (String coin : normalizedCoins) {
			for (int i = -previousWindows; i <= futureWindows; i++) {
				// Polymarket短周期市场按5分钟Unix秒拼slug，直接用slug查询更贴近官网入口。
				long windowStart = currentWindow + i * UP_DOWN_WINDOW_SECONDS;
				String slug = coin + "-updown-5m-" + windowStart;
				String sourceUrl = GAMMA_BASE_URL + "/events/slug/" + slug;
				try {
					Object event = fetchJson(sourceUrl);
					if (event instanceof JSONObject) {
						PolymarketUpDownEventDto upDownEvent = toSimpleUpDownEvent((JSONObject) event, coin, windowStart);
						if (upDownEvent != null) {
							events.add(upDownEvent);
						}
					}
				} catch (ServiceException ignored) {
					// 部分未来窗口Polymarket可能尚未发布，跳过即可，不能因为单个slug不存在导致整个模块白屏。
				}
			}
		}

		return PolymarketUpDownListDto.builder()
			.section("crypto-updown")
			.coins(normalizedCoins.stream().map(item -> item.toUpperCase(Locale.ROOT)).collect(Collectors.toList()))
			.before(previousWindows)
			.after(futureWindows)
			.count(events.size())
			.fetchedAt(LocalDateTime.now().toString())
			.events(events)
			.build();
	}

	/**
	 * 查询加密货币5分钟Up/Down短周期事件的Polymarket原始全量数据。
	 *
	 * <p>该方法用于调试上游字段，不裁剪tags、series、markets、feeSchedule等大字段；仅在外层和每个event上补充本地调试字段，
	 * 方便定位本次请求的币种、时间窗口和Gamma来源URL。</p>
	 *
	 * @param coins 逗号分隔的币种，支持btc、eth、sol、xrp
	 * @param before 当前时间窗口之前查询几个5分钟窗口
	 * @param after 当前时间窗口之后查询几个5分钟窗口
	 * @return Polymarket原始Up/Down事件集合，并带本地调试字段
	 */
	@Override
	public JSONObject listRawCryptoUpDownEvents(String coins, Integer before, Integer after) {
		List<String> normalizedCoins = normalizeUpDownCoins(coins);
		int previousWindows = normalizeWindowCount(before, DEFAULT_UP_DOWN_BEFORE);
		int futureWindows = normalizeWindowCount(after, DEFAULT_UP_DOWN_AFTER);
		long currentWindow = floorToFiveMinuteWindow(Instant.now().getEpochSecond());
		JSONArray events = new JSONArray();

		for (String coin : normalizedCoins) {
			for (int i = -previousWindows; i <= futureWindows; i++) {
				// 按Polymarket固定slug规则逐个拉取原始event，未发布窗口跳过，不影响其它币种和窗口。
				long windowStart = currentWindow + i * UP_DOWN_WINDOW_SECONDS;
				String slug = coin + "-updown-5m-" + windowStart;
				String sourceUrl = GAMMA_BASE_URL + "/events/slug/" + slug;
				try {
					Object event = fetchJson(sourceUrl);
					if (event instanceof JSONObject) {
						JSONObject rawEvent = (JSONObject) event;
						rawEvent.put("coin", coin.toUpperCase(Locale.ROOT));
						rawEvent.put("windowStartUnix", windowStart);
						rawEvent.put("windowEndUnix", windowStart + UP_DOWN_WINDOW_SECONDS);
						rawEvent.put("sourceUrl", sourceUrl);
						events.add(rawEvent);
					}
				} catch (ServiceException ignored) {
					// 原始调试接口也允许未来窗口尚未发布，保持和精简Up/Down接口一致的容错行为。
				}
			}
		}

		JSONObject result = baseResponse("crypto-updown", null, GAMMA_BASE_URL + "/events/slug/{coin}-updown-5m-{timestamp}");
		result.put("coins", normalizedCoins.stream().map(item -> item.toUpperCase(Locale.ROOT)).collect(Collectors.toList()));
		result.put("before", previousWindows);
		result.put("after", futureWindows);
		result.put("count", events.size());
		result.put("events", events);
		return result;
	}

	/**
	 * 实时读取Gamma事件列表并裁剪成前端列表需要的精简结构。
	 *
	 * @param section 本地板块名称，当前支持crypto和sports
	 * @param tagId Gamma tag_id
	 * @param limit 分页数量
	 * @param offset 分页偏移量
	 * @param sourceUrl 本次Gamma请求地址
	 * @return 精简事件列表，不包含Polymarket原始大字段
	 */
	/**
	 * 将Polymarket原始Up/Down事件裁剪成前端可直接展示和下单选择的扁平结构。
	 *
	 * <p>Up/Down事件通常只有一个可交易market；如果上游暂未返回market或assetId数组缺失，则跳过该窗口，避免前端展示不可下单数据。</p>
	 *
	 * @param event Gamma按slug返回的原始Up/Down事件
	 * @param coin 当前查询币种，小写入参
	 * @param windowStart 5分钟窗口开始Unix秒
	 * @return 精简后的Up/Down窗口；缺少可下单market或assetId时返回null
	 */
	private PolymarketUpDownEventDto toSimpleUpDownEvent(JSONObject event, String coin, long windowStart) {
		// 步骤1：Up/Down只取第一个market作为可交易市场，列表不透传原始markets数组。
		JSONObject market = firstMarket(event);
		if (market == null) {
			return null;
		}
		List<String> assetIds = toStringList(parseJsonArrayField(market, "clobTokenIds"));
		if (assetIds.isEmpty()) {
			return null;
		}

		// 步骤2：把起始比较价从eventMetadata中提到顶层，方便前端展示Up/Down判断规则。
		JSONObject eventMetadata = event.getJSONObject("eventMetadata");
		BigDecimal priceToBeat = resolveUpDownPriceToBeat(eventMetadata, event, market);

		return PolymarketUpDownEventDto.builder()
			.coin(coin.toUpperCase(Locale.ROOT))
			.title(event.getString("title"))
			.eventSlug(event.getString("slug"))
			.marketSlug(market.getString("slug"))
			.startTime(firstNotBlank(market.getString("eventStartTime"), event.getString("startTime")))
			.endTime(firstNotBlank(market.getString("endDate"), event.getString("endDate")))
			.windowStartUnix(windowStart)
			.windowEndUnix(windowStart + UP_DOWN_WINDOW_SECONDS)
			.priceToBeat(priceToBeat)
			.outcomes(toStringList(parseJsonArrayField(market, "outcomes")))
			.outcomePrices(toStringList(parseJsonArrayField(market, "outcomePrices")))
			.assetIds(assetIds)
			.bestBid(getBigDecimal(market, "bestBid"))
			.bestAsk(getBigDecimal(market, "bestAsk"))
			.lastTradePrice(getBigDecimal(market, "lastTradePrice"))
			.volume24hr(getBigDecimal(market, "volume24hr"))
			.liquidity(getBigDecimal(market, "liquidity"))
			.active(market.getBoolean("active"))
			.closed(market.getBoolean("closed"))
			.acceptingOrders(market.getBoolean("acceptingOrders"))
			.umaResolutionStatus(market.getString("umaResolutionStatus"))
			.orderMinSize(getBigDecimal(market, "orderMinSize"))
			.conditionId(market.getString("conditionId"))
			.questionId(market.getString("questionID"))
			.build();
	}

	/**
	 * 实时读取Gamma事件列表并裁剪成前端列表需要的精简结构。
	 *
	 * @param section 本地板块名称，当前支持crypto和sports
	 * @param tagId Gamma tag_id
	 * @param limit 分页数量
	 * @param offset 分页偏移量
	 * @param sourceUrl 本次Gamma请求地址
	 * @return 精简事件列表，不包含Polymarket原始大字段
	 */
	/**
	 * 读取Up/Down窗口的起始比较价格。
	 *
	 * <p>Polymarket常见位置是eventMetadata.priceToBeat；这里兼容event顶层和market顶层的priceToBeat，
	 * 但不会用bestBid、bestAsk或outcomePrices推导，因为那些是交易价格，不是标的资产起始价格。</p>
	 *
	 * @param event Gamma返回的Up/Down事件对象
	 * @param market 事件下的第一个market对象
	 * @return 起始比较价格；上游未返回时为null
	 */
	private BigDecimal resolveUpDownPriceToBeat(JSONObject eventMetadata, JSONObject event, JSONObject market) {
		BigDecimal metadataPrice = eventMetadata == null ? null : getBigDecimal(eventMetadata, "priceToBeat");
		if (metadataPrice != null) {
			return metadataPrice;
		}
		BigDecimal eventPrice = getBigDecimal(event, "priceToBeat");
		if (eventPrice != null) {
			return eventPrice;
		}
		return getBigDecimal(market, "priceToBeat");
	}

	private PolymarketEventListDto loadSimpleEvents(String section, Integer tagId, Integer limit, Integer offset, String sourceUrl) {
		JSONArray rawEvents = fetchJsonArray(sourceUrl);
		List<PolymarketEventDto> events = new ArrayList<>();
		for (Object item : rawEvents) {
			if (item instanceof JSONObject) {
				events.add(toSimpleEvent((JSONObject) item));
			}
		}
		return PolymarketEventListDto.builder()
			.section(section)
			.tagId(tagId)
			.fetchedAt(LocalDateTime.now().toString())
			.sourceUrl(sourceUrl)
			.limit(limit)
			.offset(offset)
			.count(events.size())
			.events(events)
			.build();
	}

	/**
	 * 实时读取单个市场详情并裁剪为前端展示需要的精简结构。
	 *
	 * @param slug 查询入参里的市场slug
	 * @param sourceUrl 本次Gamma请求地址
	 * @return 市场详情精简DTO
	 */
	private PolymarketMarketDetailDto loadSimpleMarketDetail(String slug, String sourceUrl) {
		Object rawMarket = fetchJson(sourceUrl);
		if (!(rawMarket instanceof JSONObject)) {
			throw new ServiceException("Polymarket返回数据格式异常，预期为市场对象");
		}
		JSONObject market = (JSONObject) rawMarket;
		JSONObject event = firstEvent(market);
		return PolymarketMarketDetailDto.builder()
			.sourceUrl(sourceUrl)
			.fetchedAt(LocalDateTime.now().toString())
			.slug(slug)
			.eventId(event == null ? null : event.getString("id"))
			.eventSlug(event == null ? null : event.getString("slug"))
			.eventTitle(event == null ? null : event.getString("title"))
			.eventDescription(event == null ? null : event.getString("description"))
			.eventIcon(event == null ? null : event.getString("icon"))
			.eventImage(event == null ? null : event.getString("image"))
			.marketId(market.getString("id"))
			.question(market.getString("question"))
			.marketSlug(market.getString("slug"))
			.description(market.getString("description"))
			.conditionId(market.getString("conditionId"))
			.questionId(market.getString("questionID"))
			.outcomes(toStringList(parseJsonArrayField(market, "outcomes")))
			.outcomePrices(toStringList(parseJsonArrayField(market, "outcomePrices")))
			.assetIds(toStringList(parseJsonArrayField(market, "clobTokenIds")))
			.bestBid(getBigDecimal(market, "bestBid"))
			.bestAsk(getBigDecimal(market, "bestAsk"))
			.lastTradePrice(getBigDecimal(market, "lastTradePrice"))
			.spread(getBigDecimal(market, "spread"))
			.volume24hr(getBigDecimal(market, "volume24hr"))
			.volume1wk(getBigDecimal(market, "volume1wk"))
			.volume1mo(getBigDecimal(market, "volume1mo"))
			.liquidity(getBigDecimal(market, "liquidity"))
			.active(market.getBoolean("active"))
			.closed(market.getBoolean("closed"))
			.acceptingOrders(market.getBoolean("acceptingOrders"))
			.umaResolutionStatus(market.getString("umaResolutionStatus"))
			.endDate(market.getString("endDate"))
			.orderMinSize(getBigDecimal(market, "orderMinSize"))
			.build();
	}

	/**
	 * 将Polymarket原始event裁剪成列表展示需要的事件字段。
	 *
	 * @param event Gamma返回的原始事件对象
	 * @return 事件精简DTO，markets最多保留前8个
	 */
	private PolymarketEventDto toSimpleEvent(JSONObject event) {
		JSONArray rawMarkets = event.getJSONArray("markets");
		List<PolymarketEventMarketDto> markets = new ArrayList<>();
		if (rawMarkets != null) {
			for (int i = 0; i < rawMarkets.size() && markets.size() < SIMPLE_MARKET_LIMIT; i++) {
				Object rawMarket = rawMarkets.get(i);
				if (rawMarket instanceof JSONObject) {
					markets.add(toSimpleMarket((JSONObject) rawMarket));
				}
			}
		}
		return PolymarketEventDto.builder()
			.id(event.getString("id"))
			.title(event.getString("title"))
			.slug(event.getString("slug"))
			.description(event.getString("description"))
			.icon(event.getString("icon"))
			.image(event.getString("image"))
			.volume24hr(getBigDecimal(event, "volume24hr"))
			.liquidity(getBigDecimal(event, "liquidity"))
			.active(event.getBoolean("active"))
			.closed(event.getBoolean("closed"))
			.markets(markets)
			.build();
	}

	/**
	 * 将Polymarket原始market裁剪成列表、选择结果和状态判断所需字段。
	 *
	 * @param market Gamma返回的原始市场对象
	 * @return 市场精简DTO，outcomes和outcomePrices会解析成数组
	 */
	private PolymarketEventMarketDto toSimpleMarket(JSONObject market) {
		return PolymarketEventMarketDto.builder()
			.id(market.getString("id"))
			.question(market.getString("question"))
			.slug(market.getString("slug"))
			.outcomes(toStringList(parseJsonArrayField(market, "outcomes")))
			.outcomePrices(toStringList(parseJsonArrayField(market, "outcomePrices")))
			.assetIds(toStringList(parseJsonArrayField(market, "clobTokenIds")))
			.bestBid(getBigDecimal(market, "bestBid"))
			.bestAsk(getBigDecimal(market, "bestAsk"))
			.lastTradePrice(getBigDecimal(market, "lastTradePrice"))
			.volume24hr(getBigDecimal(market, "volume24hr"))
			.liquidity(getBigDecimal(market, "liquidity"))
			.endDate(market.getString("endDate"))
			.groupItemTitle(market.getString("groupItemTitle"))
			.active(market.getBoolean("active"))
			.closed(market.getBoolean("closed"))
			.acceptingOrders(market.getBoolean("acceptingOrders"))
			.umaResolutionStatus(market.getString("umaResolutionStatus"))
			.conditionId(market.getString("conditionId"))
			.orderMinSize(getBigDecimal(market, "orderMinSize"))
			.build();
	}

	/**
	 * 从Polymarket市场详情里取第一个所属事件。
	 *
	 * @param market Gamma返回的市场对象
	 * @return 第一个事件对象；不存在时返回null
	 */
	private JSONObject firstEvent(JSONObject market) {
		JSONArray events = market.getJSONArray("events");
		if (events == null || events.isEmpty()) {
			return null;
		}
		Object first = events.get(0);
		return first instanceof JSONObject ? (JSONObject) first : null;
	}

	/**
	 * 从Polymarket事件中取第一个market。
	 *
	 * <p>Up/Down短周期事件当前只需要展示和下单第一个市场；没有market时跳过该窗口。</p>
	 *
	 * @param event Gamma返回的事件对象
	 * @return 第一个market对象；不存在时返回null
	 */
	private JSONObject firstMarket(JSONObject event) {
		JSONArray markets = event.getJSONArray("markets");
		if (markets == null || markets.isEmpty()) {
			return null;
		}
		Object first = markets.get(0);
		return first instanceof JSONObject ? (JSONObject) first : null;
	}

	/**
	 * 返回第一个非空字符串。
	 *
	 * @param first 优先值
	 * @param second 兜底值
	 * @return 优先非空字符串；都为空时返回null
	 */
	private String firstNotBlank(String first, String second) {
		return StrUtil.isNotBlank(first) ? first : second;
	}

	/**
	 * 解析Gamma中以JSON字符串或数组形式返回的字段。
	 *
	 * @param object 原始JSON对象
	 * @param field 字段名，例如outcomes、outcomePrices、clobTokenIds
	 * @return 解析后的数组；字段缺失或格式异常时返回空数组
	 */
	private JSONArray parseJsonArrayField(JSONObject object, String field) {
		Object value = object.get(field);
		if (value instanceof JSONArray) {
			return (JSONArray) value;
		}
		if (value instanceof String && StrUtil.isNotBlank((String) value)) {
			try {
				Object parsed = JSON.parse((String) value);
				if (parsed instanceof JSONArray) {
					return (JSONArray) parsed;
				}
			} catch (Exception e) {
				log.debug("Polymarket数组字段解析失败，field={}, value={}", field, value);
			}
		}
		return new JSONArray();
	}

	/**
	 * 将JSON数组转换成字符串列表，保证DTO返回结构稳定。
	 *
	 * @param array JSON数组
	 * @return 字符串列表
	 */
	private List<String> toStringList(JSONArray array) {
		List<String> result = new ArrayList<>();
		if (array == null) {
			return result;
		}
		for (Object item : array) {
			result.add(item == null ? null : String.valueOf(item));
		}
		return result;
	}

	/**
	 * 从Gamma原始JSON中读取数值字段，兼容字符串和数字两种格式。
	 *
	 * @param object 原始JSON对象
	 * @param field 字段名
	 * @return 数值字段；字段缺失或不可解析时返回null
	 */
	private BigDecimal getBigDecimal(JSONObject object, String field) {
		Object value = object.get(field);
		if (value == null) {
			return null;
		}
		try {
			return new BigDecimal(String.valueOf(value));
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * 标准化本地支持的Polymarket板块名称。
	 *
	 * @param section 前端传入的板块名称
	 * @return 标准化后的板块名称
	 */
	private String normalizeSection(String section) {
		String value = StrUtil.blankToDefault(section, SECTION_CRYPTO).trim().toLowerCase();
		if (!SECTION_CRYPTO.equals(value) && !SECTION_SPORTS.equals(value)) {
			throw new ServiceException("section只支持crypto或sports");
		}
		return value;
	}

	/**
	 * 将本地板块名称映射为Gamma tag_id。
	 *
	 * @param section 标准化后的板块名称
	 * @return Polymarket Gamma的tag_id
	 */
	private int tagIdOf(String section) {
		return SECTION_SPORTS.equals(section) ? SPORTS_TAG_ID : CRYPTO_TAG_ID;
	}

	/**
	 * 限制事件列表分页大小，避免一次请求拉取过多上游数据。
	 *
	 * @param limit 前端请求的分页大小
	 * @return 限制后的分页大小
	 */
	private int normalizeLimit(Integer limit) {
		if (limit == null || limit <= 0) {
			return DEFAULT_LIMIT;
		}
		return Math.min(limit, MAX_LIMIT);
	}

	/**
	 * 标准化Polymarket列表分页偏移量。
	 *
	 * @param offset 前端请求的偏移量
	 * @return 非负偏移量
	 */
	private int normalizeOffset(Integer offset) {
		if (offset == null || offset < 0) {
			return 0;
		}
		return offset;
	}

	/**
	 * 标准化市场slug并做空值保护。
	 *
	 * @param slug 前端或内部调用传入的市场slug
	 * @return 去除首尾空格后的slug
	 */
	private String normalizeMarketSlug(String slug) {
		if (StrUtil.isBlank(slug)) {
			throw new ServiceException("Polymarket市场slug不能为空");
		}
		return slug.trim();
	}

	/**
	 * 拼接Polymarket Gamma市场详情URL。
	 *
	 * @param slug 已标准化的市场slug
	 * @return Gamma市场详情URL
	 */
	private String marketSlugUrl(String slug) {
		return GAMMA_BASE_URL + "/markets/slug/" + slug;
	}

	/**
	 * 过滤并标准化Up/Down短周期市场支持的币种。
	 *
	 * @param coins 逗号分隔的币种字符串
	 * @return 小写币种列表，只包含当前确认可按slug查询的币种
	 */
	private List<String> normalizeUpDownCoins(String coins) {
		if (StrUtil.isBlank(coins)) {
			return Arrays.asList("btc", "eth", "sol");
		}
		List<String> normalized = Arrays.stream(coins.split(","))
			.map(value -> value.trim().toLowerCase(Locale.ROOT))
			.filter(SUPPORTED_UP_DOWN_COINS::contains)
			.distinct()
			.collect(Collectors.toList());
		if (normalized.isEmpty()) {
			throw new ServiceException("coins只支持btc,eth,sol,xrp");
		}
		return normalized;
	}

	/**
	 * 限制向前/向后查询的5分钟窗口数量，避免一次页面刷新打太多Gamma API请求。
	 *
	 * @param value 前端请求的窗口数量
	 * @param defaultValue 默认窗口数量
	 * @return 归一化后的窗口数量
	 */
	private int normalizeWindowCount(Integer value, int defaultValue) {
		if (value == null || value < 0) {
			return defaultValue;
		}
		return Math.min(value, MAX_UP_DOWN_SIDE_WINDOWS);
	}

	/**
	 * 将当前Unix秒向下对齐到5分钟窗口起点。
	 *
	 * @param epochSecond 当前Unix秒
	 * @return 5分钟对齐后的Unix秒，用于拼接Up/Down市场slug
	 */
	private long floorToFiveMinuteWindow(long epochSecond) {
		return epochSecond / UP_DOWN_WINDOW_SECONDS * UP_DOWN_WINDOW_SECONDS;
	}

	/**
	 * 调用预期返回JSON数组的Gamma接口。
	 *
	 * @param sourceUrl Gamma接口URL
	 * @return 解析后的JSON数组
	 */
	private JSONArray fetchJsonArray(String sourceUrl) {
		Object data = fetchJson(sourceUrl);
		if (!(data instanceof JSONArray)) {
			throw new ServiceException("Polymarket返回数据格式异常，预期为数组");
		}
		return (JSONArray) data;
	}

	/**
	 * 调用Polymarket Gamma API并解析JSON响应。
	 *
	 * @param sourceUrl Gamma接口URL
	 * @return 解析后的JSON对象或数组
	 */
	private Object fetchJson(String sourceUrl) {
		try (HttpResponse response = HttpUtil.createGet(sourceUrl)
			.timeout(REQUEST_TIMEOUT_MS)
			.execute()) {
			if (!response.isOk()) {
				throw new ServiceException("Polymarket请求失败，HTTP状态=" + response.getStatus());
			}
			return JSON.parse(response.body());
		} catch (ServiceException e) {
			throw e;
		} catch (Exception e) {
			throw new ServiceException("Polymarket请求异常：" + e.getMessage());
		}
	}

	/**
	 * 构建独立调研页通用调试字段。
	 *
	 * @param section 本地板块名称；市场详情接口可为空
	 * @param tagId Polymarket tag_id；市场详情接口可为空
	 * @param sourceUrl 上游请求URL
	 * @return 通用响应包装对象
	 */
	private JSONObject baseResponse(String section, Integer tagId, String sourceUrl) {
		JSONObject result = new JSONObject();
		result.put("section", section);
		result.put("tagId", tagId);
		result.put("sourceUrl", sourceUrl);
		result.put("fetchedAt", LocalDateTime.now().toString());
		return result;
	}
}
