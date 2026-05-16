package com.xms.app.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.xms.app.service.PolymarketService;
import com.xms.common.exception.ServiceException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * 读取Polymarket Gamma公开行情数据。
 *
 * <p>服务独立调研页和内部下单报价。当前实现尽量保留上游原始字段，方便排查Polymarket字段变化，
 * 暂不把行情数据落库或强制建成本地完整模型。</p>
 */
@Service
public class PolymarketServiceImpl implements PolymarketService {

	private static final String GAMMA_BASE_URL = "https://gamma-api.polymarket.com";
	private static final String SECTION_CRYPTO = "crypto";
	private static final String SECTION_SPORTS = "sports";
	private static final int CRYPTO_TAG_ID = 21;
	private static final int SPORTS_TAG_ID = 1;
	private static final int DEFAULT_LIMIT = 20;
	private static final int MAX_LIMIT = 100;
	private static final int REQUEST_TIMEOUT_MS = 5000;
	private static final long UP_DOWN_WINDOW_SECONDS = 300L;
	private static final int DEFAULT_UP_DOWN_BEFORE = 2;
	private static final int DEFAULT_UP_DOWN_AFTER = 6;
	private static final int MAX_UP_DOWN_SIDE_WINDOWS = 12;
	private static final List<String> SUPPORTED_UP_DOWN_COINS = Arrays.asList("btc", "eth", "sol", "xrp");

	@Override
	public JSONObject listEvents(String section, Integer limit, Integer offset) {
		// 步骤1：把本地板块名映射成Gamma的tag_id，并限制分页参数。
		String normalizedSection = normalizeSection(section);
		int tagId = tagIdOf(normalizedSection);
		int pageSize = normalizeLimit(limit);
		int pageOffset = normalizeOffset(offset);
		String sourceUrl = GAMMA_BASE_URL + "/events?tag_id=" + tagId
			+ "&active=true&closed=false&limit=" + pageSize
			+ "&offset=" + pageOffset
			+ "&order=volume24hr&ascending=false";

		// 步骤2：请求Gamma事件列表，返回时补充本地调试字段，便于页面展示和接口排查。
		JSONArray events = fetchJsonArray(sourceUrl);
		JSONObject result = baseResponse(normalizedSection, tagId, sourceUrl);
		result.put("limit", pageSize);
		result.put("offset", pageOffset);
		result.put("count", events.size());
		result.put("events", events);
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
	public JSONObject listCryptoUpDownEvents(String coins, Integer before, Integer after) {
		List<String> normalizedCoins = normalizeUpDownCoins(coins);
		int previousWindows = normalizeWindowCount(before, DEFAULT_UP_DOWN_BEFORE);
		int futureWindows = normalizeWindowCount(after, DEFAULT_UP_DOWN_AFTER);
		long currentWindow = floorToFiveMinuteWindow(Instant.now().getEpochSecond());
		JSONArray events = new JSONArray();

		for (String coin : normalizedCoins) {
			for (int i = -previousWindows; i <= futureWindows; i++) {
				// Polymarket短周期市场按5分钟Unix秒拼slug，直接用slug查询比通用events列表更贴近官网入口。
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
			throw new ServiceException("Polymarket返回数据格式异常，预期数组");
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
