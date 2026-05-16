package com.xms.app.service.impl;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.esotericsoftware.minlog.Log;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.xms.app.service.BizCommonService;
import com.xms.common.config.redis.XmsRedis;
import com.xms.common.constant.RedisConstant;
import com.xms.common.exception.ServiceException;
import com.xms.common.result.ResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;


@Slf4j
@Service
public class BizCommonServiceImpl implements BizCommonService {

	private final Object lock = new Object();
	/**
	 * 交易对符号
	 */
	private  final String DFC_USDT_PAIR = "DFCUSDT_SPBL";
	private  final String AFI_USDT_PAIR = "AFIUSDT";
	private  final Cache<String, BigDecimal> dfcPriceCache = Caffeine.newBuilder()
		.expireAfterWrite(10, TimeUnit.SECONDS)
		.maximumSize(100)
		.build();
	private  final Cache<String, BigDecimal> afiPriceCache = Caffeine.newBuilder()
		.expireAfterWrite(10, TimeUnit.SECONDS)
		.maximumSize(100)
		.build();
	@Value("${df.callDfcUrl}")
	private String callDfcUrl;

	@Value("${lq.baseUrl}")
	private String baseUrl;

	@Autowired
	private XmsRedis xmsRedis;



	/**
	 * 获取Aleo币的价格
	 * 通过发送HTTP GET请求到Gate.io API获取ALEO对USDT的最新交易价格
	 *
	 * @return ALEO对USDT的最新交易价格如果请求失败或解析无数据，则返回null
	 */
	public static BigDecimal getCoinPrice(String coinPair) {
		try {
			// 发送 GET 请求
			//10s超时
			String response = HttpUtil.get("https://api.gateio.ws/api/v4/spot/tickers?currency_pair=" + coinPair, 5000);
			Log.info("请求gate获取aleo 返回值 response:{}", response);
			JSONArray jsonArray = JSONUtil.parseArray(response);
			//格式如下
			//[{"currency_pair":"ALEO_USDT","last":"1.538","lowest_ask":"1.538","lowest_size":"97.41","highest_bid":"1.537","highest_size":"68.25","change_percentage":"-1.28","base_volume":"2575536.82","quote_volume":"3970271.31244","high_24h":"1.585","low_24h":"1.503"}]
			if (jsonArray != null && jsonArray.size() > 0) {
				BigDecimal lastPrice = jsonArray.getJSONObject(0).getBigDecimal("last");
				return lastPrice;
			} else {
				log.warn("请求gate获取 coinPair:{} 返回值为空或格式不正确", coinPair);
				throw new ServiceException(ResponseCode.CODE_1284);
			}
		} catch (Exception e) {
			log.error("请求gate获取 coinPair:{}, 错误: {}", coinPair, e.getMessage());
			throw new ServiceException(ResponseCode.CODE_1284);
		}
	}

	/**
	 * 从缓存获取aleo币价格
	 *
	 * @return
	 */
	@Override
	public BigDecimal getOortPrice() {
		try {
			BigDecimal oortCoinPrice = xmsRedis.get(RedisConstant.CAPTCHA_OORT_PRICE, () -> {
				synchronized (lock) {
					// 再次检查缓存，避免重复请求
					BigDecimal cachedPrice = xmsRedis.get(RedisConstant.CAPTCHA_OORT_PRICE);
					if (cachedPrice != null) {
						return cachedPrice;
					}
					return getCoinPrice("OORT_USDT");
				}
			}, RedisConstant.FIFTEEN_SECONDS_EXPIRE_TIME, TimeUnit.SECONDS);
			return oortCoinPrice;
		} catch (Exception e) {
			// 处理 Redis 获取或设置缓存时的异常
			// 记录日志或采取其他措施
			throw new RuntimeException("Failed to retrieve Aleo coin price from cache", e);
		}
	}

	/**
	 * 例如 获取dfc价格
	 */
	@Override
	public BigDecimal getDFcPrice(){
		BigDecimal cachedPrice = dfcPriceCache.getIfPresent(DFC_USDT_PAIR);
		if (cachedPrice != null) {
			return cachedPrice;
		}
		synchronized (lock) {
			cachedPrice = dfcPriceCache.getIfPresent(DFC_USDT_PAIR);
			if (cachedPrice != null) {
				return cachedPrice;
			}
			try {
				//HttpRequest request = HttpUtil.createGet("https://api-spot.weex.com/api/v2/market/ticker?symbol=DFCUSDT_SPBL")
				//HttpRequest request = HttpUtil.createGet("http://39.109.116.226:40800/api/v2/market/ticker?symbol=DFCUSDT_SPBL")
				HttpRequest request = HttpUtil.createGet(callDfcUrl)
					//.setProxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 7897)))
					.timeout(5000);
				String response = request.execute().body();
				Log.info("请求weex获取dfc 返回值 response:{}", response);
				JSONObject jsonObject = JSONUtil.parseObj(response);
				if (!"00000".equals(jsonObject.getStr("code"))) {
					log.warn("请求weex获取dfc异常 code:{}", jsonObject.getStr("code"));
					throw new ServiceException("get dfc price error");
				}
				JSONObject data = jsonObject.getJSONObject("data");
				if (data == null) {
					log.warn("请求weex获取dfc data为空");
					throw new ServiceException("get dfc price error");
				}
				String lastPrice = data.getStr("lastPrice");
				if (lastPrice == null) {
					lastPrice = data.getStr("close");
				}
				if (lastPrice == null) {
					log.warn("请求weex获取dfc lastPrice为空");
					throw new ServiceException("get dfc price error");
				}
				BigDecimal price = new BigDecimal(lastPrice);
				dfcPriceCache.put(DFC_USDT_PAIR, price);
				return price;
			} catch (Exception e) {
				log.error("请求weex获取dfc错误: {}", e.getMessage());
				if (e instanceof ServiceException) {
					throw (ServiceException)e;
				}
				throw new ServiceException("get dfc price error");
			}
		}
	}

	/**
	 * 获取AFI价格
	 */
	@Override
	public BigDecimal getAfiPrice() {
		return new BigDecimal("0.5");
		/*BigDecimal cachedPrice = afiPriceCache.getIfPresent(AFI_USDT_PAIR);
		if (cachedPrice != null) {
			return cachedPrice;
		}
		synchronized (lock) {
			cachedPrice = afiPriceCache.getIfPresent(AFI_USDT_PAIR);
			if (cachedPrice != null) {
				return cachedPrice;
			}
			try {
				String url = baseUrl + "/chain/price";
				HttpRequest request = HttpUtil.createGet(url).timeout(5000);
				String response = request.execute().body();
				Log.info("请求获取afi价格 返回值 response:{}", response);
				JSONObject jsonObject = JSONUtil.parseObj(response);
				if (!"200".equals(jsonObject.getStr("code"))) {
					log.warn("请求获取afi价格异常 code:{}", jsonObject.getStr("code"));
					throw new ServiceException("get afi price error");
				}
				JSONObject data = jsonObject.getJSONObject("data");
				if (data == null) {
					log.warn("请求获取afi价格 data为空");
					throw new ServiceException("get afi price error");
				}
				BigDecimal price = data.getBigDecimal("afi");
				if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
					log.warn("请求获取afi价格为空或小于等于0, price:{}", price);
					throw new ServiceException("get afi price error");
				}
				afiPriceCache.put(AFI_USDT_PAIR, price);
				return price;
			} catch (Exception e) {
				log.error("请求获取afi价格错误: {}", e.getMessage());
				if (e instanceof ServiceException) {
					throw (ServiceException)e;
				}
				throw new ServiceException("get afi price error");
			}
		}*/
	}
}
