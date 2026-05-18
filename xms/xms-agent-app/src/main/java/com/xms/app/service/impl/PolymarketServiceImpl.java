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
 * <p>普通Crypto/Sports事件列表返回本地精简DTO，并用Redis做10秒短缓存；市场详情和Up/Down调研接口仍保留上游原始结构，方便排查字段变化。</p>
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PolymarketServiceImpl implements PolymarketService {

	private static final String GAMMA_BASE_URL = "https://gamma-api.polymarket.com";
	private static final String EVENT_LIST_CACHE_KEY_PREFIX = "polymarket:events:simple:";
	private static final String SECTION_CRYPTO = "crypto";
	private static final String SECTION_SPORTS = "sports";
	private static final int CRYPTO_TAG_ID = 21;
	private static final int SPORTS_TAG_ID = 1;
	private static final int DEFAULT_LIMIT = 20;
	private static final int MAX_LIMIT = 100;
	private static final int SIMPLE_MARKET_LIMIT = 8;
	private static final long EVENT_LIST_CACHE_SECONDS = 10L;
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
	 * 实时读取Gamma事件列表并裁剪成前端列表需要的精简结构。
	 *
	 * @param section 本地板块名称，当前支持crypto和sports
	 * @param tagId Gamma tag_id
	 * @param limit 分页数量
	 * @param offset 分页偏移量
	 * @param sourceUrl 本次Gamma请求地址
	 * @return 精简事件列表，不包含Polymarket原始大字段
	 */
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
			.bestBid(getBigDecimal(market, "bestBid"))
			.bestAsk(getBigDecimal(market, "bestAsk"))
			.lastTradePrice(getBigDecimal(market, "lastTradePrice"))
			.volume24hr(getBigDecimal(market, "volume24hr"))
			.liquidity(getBigDecimal(market, "liquidity"))
			.endDate(market.getString("endDate"))
			.active(market.getBoolean("active"))
			.closed(market.getBoolean("closed"))
			.acceptingOrders(market.getBoolean("acceptingOrders"))
			.umaResolutionStatus(market.getString("umaResolutionStatus"))
			.conditionId(market.getString("conditionId"))
			.orderMinSize(getBigDecimal(market, "orderMinSize"))
			.build();
	}

	/**
	 * 解析Gamma中以JSON字符串或数组形式返回的字段。
	 *
	 * @param object 原始JSON对象
	 * @param field 字段名，例如outcomes、outcomePrices
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
	public JSONObject listCryptoUpDownEvents(String coins, Integer before, Integer after) {
		List<String> normalizedCoins = normalizeUpDownCoins(coins);
		int previousWindows = normalizeWindowCount(before, DEFAULT_UP_DOWN_BEFORE);
		int futureWindows = normalizeWindowCount(after, DEFAULT_UP_DOWN_AFTER);
		long currentWindow = floorToFiveMinuteWindow(Instant.now().getEpochSecond());
		JSONArray events = new JSONArray();

		for (String coin : normalizedCoins) {
			for (int i = -previousWindows; i <= futureWindows; i++) {
				// Polymarket短周期市场按5分钟Unix秒拼slug，直接用slug查询更贴近官网入口。
				long windowStart = currentWindow + i * UP_DOWN_WINDOW_SECONDS;
				String slug = coin + "-updown-5m-" + windowStart;
				String sourceUrl = GAMMA_BASE_URL + "/events/slug/" + slug;
				try {
					Object event = fetchJson(sourceUrl);
					if (event instanceof JSONObject) {
						JSONObject eventObject = (JSONObject) event;
						eventObject.put("coin", coin.toUpperCase(Locale.ROOT));
						eventObject.put("windowStartUnix", windowStart);
						eventObject.put("windowEndUnix", windowStart + UP_DOWN_WINDOW_SECONDS);
						eventObject.put("sourceUrl", sourceUrl);
						events.add(eventObject);
					}
				} catch (ServiceException ignored) {
					// 部分未来窗口Polymarket可能尚未发布，跳过即可，不能因为单个slug不存在导致整个模块白屏。
				}
			}
		}

		JSONObject result = baseResponse("crypto-updown", null, GAMMA_BASE_URL + "/events/slug/{coin}-updown-5m-{timestamp}");
		result.put("coins", normalizedCoins);
		result.put("before", previousWindows);
		result.put("after", futureWindows);
		result.put("count", events.size());
		result.put("events", events);
		return result;
	}

	/**
	 * 按公开slug查询一个Polymarket市场详情。
	 *
	 * @param slug Polymarket市场slug
	 * @return 原始市场详情，并带本地调试字段
	 */
	@Override
	public JSONObject getMarketBySlug(String slug) {
		if (StrUtil.isBlank(slug)) {
			throw new ServiceException("Polymarket市场slug不能为空");
		}
		String cleanSlug = slug.trim();
		String sourceUrl = GAMMA_BASE_URL + "/markets/slug/" + cleanSlug;
		Object market = fetchJson(sourceUrl);
		JSONObject result = baseResponse(null, null, sourceUrl);
		result.put("slug", cleanSlug);
		result.put("market", market);
		return result;
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
