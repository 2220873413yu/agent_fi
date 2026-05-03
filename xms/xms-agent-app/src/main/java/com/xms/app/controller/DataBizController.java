//package com.xms.app.controller;
//
//import cn.hutool.core.util.StrUtil;
//import cn.hutool.http.HttpUtil;
//import cn.hutool.json.JSONArray;
//import cn.hutool.json.JSONObject;
//import cn.hutool.json.JSONUtil;
//import com.google.common.cache.Cache;
//import com.google.common.cache.CacheBuilder;
//import com.xms.app.config.CoinGlassConfig;
//import com.xms.app.entity.MarketTradeBo;
//import com.xms.app.entity.dto.ArbitrageData;
//import com.xms.app.entity.dto.CoinMarketData;
//import com.xms.app.entity.dto.FundingRateData;
//
//import com.xms.common.config.redis.XmsRedis;
//import com.xms.common.constant.ConstantStatic;
//import com.xms.common.constant.RedisConstant;
//import com.xms.common.constant.SysConstant;
//import com.xms.common.core.domain.api.ResultPista;
//
//import com.xms.common.utils.CollectionUtil;
//import com.xms.dao.domain.MarketTradeConfig;
//import com.xms.dao.domain.MarketsCoin;
//import com.xms.dao.service.IMarketTradeConfigService;
//import com.xms.dao.service.IMarketsCoinService;
//import io.swagger.annotations.Api;
//import io.swagger.annotations.ApiOperation;
//
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.math.BigDecimal;
//import java.math.RoundingMode;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//import java.util.concurrent.TimeUnit;
//import java.util.stream.Collectors;
//
///**
// *
// * 行情数据接口
// *
// * @since 2023-06-12
// */
//@Api(tags = "行情数据接口")
//@RestController
//@RequestMapping("/api/data")
//@Slf4j
//public class DataBizController {
//
//	private final BigDecimal principal = new BigDecimal("10000");
//	private final BigDecimal feeRatio = new BigDecimal("0.0011");
//	@Autowired
//	private CoinGlassConfig coinGlassConfig;
//
//	@Autowired
//	private IMarketTradeConfigService marketTradeConfigService;
//
//	@Autowired
//	private XmsRedis xmsRedis;
//
//	@Autowired
//	private IMarketsCoinService marketsCoinService;
//
//	// 资金费率数据缓存，15秒过期
//	private final Cache<String, List<ArbitrageData>> fundingRateCache = CacheBuilder.newBuilder()
//		.expireAfterWrite(15, TimeUnit.SECONDS)
//		.maximumSize(10)
//		.build();
//
//	// 币种市场数据缓存，15秒过期
//	private final Cache<String, List<CoinMarketData>> coinMarketCache = CacheBuilder.newBuilder()
//		.expireAfterWrite(15, TimeUnit.SECONDS)
//		.maximumSize(10)
//		.build();
//
//	//158d1b4326164c9081d8bfa1b24d4988
//	/**
//	 * 从CoinGlass API获取套利数据
//	 */
//	private List<ArbitrageData> fetchArbitrageDataFromApi(BigDecimal usd,BigDecimal fee) {
//		// 使用 Hutool 发送请求
//		String url = "https://open-api-v4.coinglass.com/api/futures/funding-rate/arbitrage";
//
//		String response = HttpUtil.createGet(url)
//			.form("usd", usd.toString())
//			.header("accept", "application/json")
//			.header("CG-API-KEY", coinGlassConfig.getSecret())
//			.timeout(30000) // 30秒超时
//			.execute()
//			.body();
//
//
//		if (StrUtil.isNotBlank(response)) {
//			JSONObject responseJson = JSONUtil.parseObj(response);
//
//			// 检查响应状态
//			if ("0".equals(responseJson.getStr("code"))) {
//								JSONArray dataArray = responseJson.getJSONArray("data");
//
//				// 先转换所有数据
//				List<ArbitrageData> arbitrageList = dataArray.stream()
//					.map(obj -> {
//						JSONObject item = (JSONObject) obj;
//						ArbitrageData arbitrage = new ArbitrageData();
//
//						// 基础信息
//						arbitrage.setSymbol(item.getStr("symbol"));
//						arbitrage.setApr(item.getBigDecimal("apr"));
//						arbitrage.setFunding(item.getBigDecimal("funding"));
//						arbitrage.setFee(item.getBigDecimal("fee"));
//						arbitrage.setSpread(item.getBigDecimal("spread"));
//						arbitrage.setNextFundingTime(item.getLong("next_funding_time"));
//
//						// 做多交易所信息
//						JSONObject buyInfo = item.getJSONObject("buy");
//							ArbitrageData.ExchangeInfo buy = JSONUtil.toBean(buyInfo, ArbitrageData.ExchangeInfo.class);
//							arbitrage.setBuy(buy);
//
//						// 做空交易所信息
//						JSONObject sellInfo = item.getJSONObject("sell");
//							ArbitrageData.ExchangeInfo sell = JSONUtil.toBean(sellInfo, ArbitrageData.ExchangeInfo.class);
//							arbitrage.setSell(sell);
//
//						BigDecimal buyProfit = arbitrage.getBuy().getFundingRate()
//							.multiply(usd).setScale(SysConstant.TWO, RoundingMode.DOWN)
//							.divide(SysConstant.BAIFENBI, ConstantStatic.newScale, ConstantStatic.roundingModeNew).abs();
//
//						BigDecimal sellProfit = arbitrage.getSell().getFundingRate()
//							.multiply(usd).setScale(SysConstant.TWO, RoundingMode.DOWN)
//							.divide(SysConstant.BAIFENBI, ConstantStatic.newScale, ConstantStatic.roundingModeNew).abs();
//						arbitrage.setProfit(sellProfit.subtract(buyProfit).abs());
//						//手续费
//						arbitrage.setTransactionFee(fee);
//						arbitrage.setFunding(buy.getFundingRate().subtract(sell.getFundingRate()).abs());
//						return arbitrage;
//					})
//					.sorted((a, b) -> {
//						// funding 为 null 时默认为 0
//						BigDecimal profitA = a.getProfit() != null ? a.getProfit() : BigDecimal.ZERO;
//						BigDecimal profitB = b.getProfit() != null ? b.getProfit() : BigDecimal.ZERO;
//						// 降序排序（大的在前）
//						return profitB.compareTo(profitA);
//					})
//					// 取前15条
//					.limit(15)
//					.collect(Collectors.toList());
//				return arbitrageList;
//			} else {
//				String errorMsg = "CoinGlass API错误: " + responseJson.getStr("msg");
//				log.error(errorMsg);
//				throw new RuntimeException(errorMsg);
//			}
//		} else {
//			String errorMsg = "CoinGlass API响应为空";
//			log.error(errorMsg);
//			throw new RuntimeException(errorMsg);
//		}
//	}
//
//	/**
//	 * 套利->获取币种资金费率-交易所列表
//	 * @return
//	 */
//	@ApiOperation(value = "获取币种资金费率-交易所列表")
//	@GetMapping(value = "/getFundingRateList")
//	public ResultPista<List<ArbitrageData>> getFundingRateList(BigDecimal amount){
//		try {
//			if(amount == null || amount.compareTo(BigDecimal.ZERO) <= 0){
//				amount = principal;
//			}else{
//				amount = amount.setScale(0, RoundingMode.DOWN);
//			}
//			// 尝试从缓存获取数据
//			String cacheKey = "funding_rate_list:"+amount.toString();
//			List<ArbitrageData> cachedData = fundingRateCache.getIfPresent(cacheKey);
//			if (cachedData != null) {
//				log.info("从缓存获取资金费率数据，缓存键: {}", cacheKey);
//				return ResultPista.data(cachedData);
//			}
//
//			log.info("缓存未命中，从CoinGlass API获取资金费率数据");
//
//			// 缓存未命中，从API获取数据
//			List<ArbitrageData> freshData = fetchArbitrageDataFromApi(amount,
//				amount.multiply(feeRatio).setScale(ConstantStatic.newScale, RoundingMode.DOWN));
//
//			// 存入缓存
//			fundingRateCache.put(cacheKey, freshData);
//			log.info("资金费率数据已缓存，缓存键: {}", cacheKey);
//
//			return ResultPista.data(freshData);
//		} catch (Exception e) {
//			log.error("获取资金费率数据失败", e);
//			return ResultPista.fail("获取资金费率数据失败: " + e.getMessage());
//		}
//	}
//
//	/**
//	 * 从CoinGlass API获取资金费率数据
//	 */
//	private List<FundingRateData> fetchFundingRateDataFromApi() {
//		try {
//			// 使用 Hutool 发送请求
//			String url = "https://open-api-v4.coinglass.com/api/futures/funding-rate/exchange-list";
//
//			String response = HttpUtil.createGet(url)
//				.header("accept", "application/json")
//				.header("CG-API-KEY", coinGlassConfig.getSecret())
//				.timeout(30000) // 30秒超时
//				.execute()
//				.body();
//
//			log.info("CoinGlass资金费率数据响应: {}", response);
//
//			if (StrUtil.isNotBlank(response)) {
//				JSONObject responseJson = JSONUtil.parseObj(response);
//
//				// 检查响应状态
//				if ("0".equals(responseJson.getStr("code"))) {
//					JSONArray dataArray = responseJson.getJSONArray("data");
//					List<FundingRateData> fundingRateList = new ArrayList<>();
//
//					// 限制只取前15条数据
//					int maxItems = Math.min(15, dataArray.size());
//					for (int i = 0; i < maxItems; i++) {
//						JSONObject item = dataArray.getJSONObject(i);
//						FundingRateData fundingRate = new FundingRateData();
//
//						// 币种名称
//						fundingRate.setSymbol(item.getStr("symbol"));
//
//						// 处理USDT/USD保证金模式数据
//						JSONArray stablecoinArray = item.getJSONArray("stablecoin_margin_list");
//						if (stablecoinArray != null && !stablecoinArray.isEmpty()) {
//							List<FundingRateData.ExchangeFundingRate> stablecoinList = new ArrayList<>();
//							for (int j = 0; j < stablecoinArray.size(); j++) {
//								JSONObject exchangeData = stablecoinArray.getJSONObject(j);
//								FundingRateData.ExchangeFundingRate rate = new FundingRateData.ExchangeFundingRate();
//								rate.setExchange(exchangeData.getStr("exchange"));
//								rate.setFundingRateInterval(exchangeData.getInt("funding_rate_interval"));
//								rate.setFundingRate(exchangeData.getBigDecimal("funding_rate"));
//								rate.setNextFundingTime(exchangeData.getLong("next_funding_time"));
//								stablecoinList.add(rate);
//							}
//							fundingRate.setStablecoinMarginList(stablecoinList);
//						}
//
//						// 处理币本位保证金模式数据
//						JSONArray tokenArray = item.getJSONArray("token_margin_list");
//						if (tokenArray != null && !tokenArray.isEmpty()) {
//							List<FundingRateData.ExchangeFundingRate> tokenList = new ArrayList<>();
//							for (int j = 0; j < tokenArray.size(); j++) {
//								JSONObject exchangeData = tokenArray.getJSONObject(j);
//								FundingRateData.ExchangeFundingRate rate = new FundingRateData.ExchangeFundingRate();
//								rate.setExchange(exchangeData.getStr("exchange"));
//								rate.setFundingRateInterval(exchangeData.getInt("funding_rate_interval"));
//								rate.setFundingRate(exchangeData.getBigDecimal("funding_rate"));
//								rate.setNextFundingTime(exchangeData.getLong("next_funding_time"));
//								tokenList.add(rate);
//							}
//							fundingRate.setTokenMarginList(tokenList);
//						}
//
//						fundingRateList.add(fundingRate);
//					}
//
//					return fundingRateList;
//				} else {
//					String errorMsg = "CoinGlass API错误: " + responseJson.getStr("msg");
//					log.error(errorMsg);
//					throw new RuntimeException(errorMsg);
//				}
//			} else {
//				String errorMsg = "CoinGlass API响应为空";
//				log.error(errorMsg);
//				throw new RuntimeException(errorMsg);
//			}
//
//		} catch (Exception e) {
//			log.error("获取资金费率数据异常: {}", e.getMessage(), e);
//			throw new RuntimeException("获取资金费率数据异常: " + e.getMessage());
//		}
//	}
//
//	/**
//	 * 加密货币->获取币种市场列表
//	 * @return
//	 */
//	@ApiOperation(value = "获取币种市场列表")
//	@GetMapping(value = "/getCoinMarkets")
//	public ResultPista<List<CoinMarketData>> getCoinMarkets() {
//		try {
//			// 尝试从缓存获取数据
//			String cacheKey = String.format("coin_markets_%d_%d", 20, 1);
//			List<CoinMarketData> cachedData = coinMarketCache.getIfPresent(cacheKey);
//			if (cachedData != null) {
//				//log.info("从缓存获取币种市场数据，缓存键: {}", cacheKey);
//				return ResultPista.data(cachedData);
//			}
//
//			log.info("缓存未命中，从CoinGlass API获取币种市场数据");
//
//			// 缓存未命中，从API获取数据
//			List<CoinMarketData> freshData = fetchCoinMarketsFromApi(20, 1);
//
//			// 存入缓存
//			coinMarketCache.put(cacheKey, freshData);
//			log.info("币种市场数据已缓存，缓存键: {}", cacheKey);
//			if(CollectionUtil.isNotEmpty(freshData)){
//				Map<String,String> iconMap = xmsRedis.get(ConstantStatic.market_icon, () ->
//					marketsCoinService.lambdaQuery()
//						.select(MarketsCoin::getSymbol,MarketsCoin::getIcon)
//						.list().stream().collect(Collectors.toMap(MarketsCoin::getSymbol,MarketsCoin::getIcon,(k1,k2)->k2)));
//				for (CoinMarketData freshDatum : freshData) {
//					freshDatum.setIcon(iconMap.get(freshDatum.getSymbol()));
//				}
//			}
//			return ResultPista.data(freshData);
//		} catch (Exception e) {
//			log.error("获取币种市场数据失败", e);
//			return ResultPista.fail("获取币种市场数据失败: " + e.getMessage());
//		}
//	}
//
//	public static void main(String[] args) {
//		List<CoinMarketData> coinMarketList = new ArrayList<>();
//		// 使用 Hutool 发送请求
//		String url = "https://open-api-v4.coinglass.com/api/spot/coins-markets";
//
//		String response = HttpUtil.createGet(url)
//			.form("per_page", String.valueOf(20))
//			.form("page", String.valueOf(1))
//			.header("accept", "application/json")
//			.header("CG-API-KEY", "158d1b4326164c9081d8bfa1b24d4988")
//			.timeout(30000) // 30秒超时
//			.execute()
//			.body();
//
//		//log.info("CoinGlass币种市场数据响应: {}", response);
//
//		if (StrUtil.isNotBlank(response)) {
//			JSONObject responseJson = JSONUtil.parseObj(response);
//
//			// 检查响应状态
//			if ("0".equals(responseJson.getStr("code"))) {
//				JSONArray dataArray = responseJson.getJSONArray("data");
//
//
//				for (int i = 0; i < dataArray.size(); i++) {
//					JSONObject item = dataArray.getJSONObject(i);
//					CoinMarketData coinMarket = new CoinMarketData();
//
//					// 基础信息
//					coinMarket.setSymbol(item.getStr("symbol"));
//					coinMarket.setCurrentPrice(item.getBigDecimal("current_price"));
//					coinMarket.setMarketCap(item.getBigDecimal("market_cap"));
//
//					// 价格变化（美元）- 只取15分钟、1小时、4小时、24小时
//					coinMarket.setPriceChange15m(item.getBigDecimal("price_change_15m"));
//					coinMarket.setPriceChange1h(item.getBigDecimal("price_change_1h"));
//					coinMarket.setPriceChange4h(item.getBigDecimal("price_change_4h"));
//					coinMarket.setPriceChange24h(item.getBigDecimal("price_change_24h"));
//
//					// 价格变化百分比（%）
//					coinMarket.setPriceChangePercent15m(item.getBigDecimal("price_change_percent_15m"));
//					coinMarket.setPriceChangePercent1h(item.getBigDecimal("price_change_percent_1h"));
//					coinMarket.setPriceChangePercent4h(item.getBigDecimal("price_change_percent_4h"));
//					coinMarket.setPriceChangePercent24h(item.getBigDecimal("price_change_percent_24h"));
//
//					// 成交量（美元）
//					coinMarket.setVolumeUsd15m(item.getBigDecimal("volume_usd_15m"));
//					coinMarket.setVolumeUsd1h(item.getBigDecimal("volume_usd_1h"));
//					coinMarket.setVolumeUsd4h(item.getBigDecimal("volume_usd_4h"));
//					coinMarket.setVolumeUsd24h(item.getBigDecimal("volume_usd_24h"));
//
//					// 成交量变化百分比（%）
//					coinMarket.setVolumeChangePercent15m(item.getBigDecimal("volume_change_percent_15m"));
//					coinMarket.setVolumeChangePercent1h(item.getBigDecimal("volume_change_percent_1h"));
//					coinMarket.setVolumeChangePercent4h(item.getBigDecimal("volume_change_percent_4h"));
//					coinMarket.setVolumeChangePercent24h(item.getBigDecimal("volume_change_percent_24h"));
//
//					// 主动买入成交量（美元）
//					coinMarket.setBuyVolumeUsd15m(item.getBigDecimal("buy_volume_usd_15m"));
//					coinMarket.setBuyVolumeUsd1h(item.getBigDecimal("buy_volume_usd_1h"));
//					coinMarket.setBuyVolumeUsd4h(item.getBigDecimal("buy_volume_usd_4h"));
//					coinMarket.setBuyVolumeUsd24h(item.getBigDecimal("buy_volume_usd_24h"));
//
//					// 主动卖出成交量（美元）
//					coinMarket.setSellVolumeUsd15m(item.getBigDecimal("sell_volume_usd_15m"));
//					coinMarket.setSellVolumeUsd1h(item.getBigDecimal("sell_volume_usd_1h"));
//					coinMarket.setSellVolumeUsd4h(item.getBigDecimal("sell_volume_usd_4h"));
//					coinMarket.setSellVolumeUsd24h(item.getBigDecimal("sell_volume_usd_24h"));
//
//					// 主动成交量净流入（= 买入 - 卖出）
//					coinMarket.setVolumeFlowUsd15m(item.getBigDecimal("volume_flow_usd_15m"));
//					coinMarket.setVolumeFlowUsd1h(item.getBigDecimal("volume_flow_usd_1h"));
//					coinMarket.setVolumeFlowUsd4h(item.getBigDecimal("volume_flow_usd_4h"));
//					coinMarket.setVolumeFlowUsd24h(item.getBigDecimal("volume_flow_usd_24h"));
//
//					coinMarketList.add(coinMarket);
//				}
//			}
//		}
//		System.out.println(coinMarketList);
//	}
//	/**
//	 * 从CoinGlass API获取币种市场数据
//	 */
//	private List<CoinMarketData> fetchCoinMarketsFromApi(int perPage, int page) {
//		// 使用 Hutool 发送请求
//		String url = coinGlassConfig.getBaseUrl() + "/api/spot/coins-markets";
//
//		String response = HttpUtil.createGet(url)
//			.form("per_page", String.valueOf(perPage))
//			.form("page", String.valueOf(page))
//			.header("accept", "application/json")
//			.header("CG-API-KEY", coinGlassConfig.getSecret())
//			.timeout(30000) // 30秒超时
//			.execute()
//			.body();
//
//		//log.info("CoinGlass币种市场数据响应: {}", response);
//
//		if (StrUtil.isNotBlank(response)) {
//			JSONObject responseJson = JSONUtil.parseObj(response);
//
//			// 检查响应状态
//			if ("0".equals(responseJson.getStr("code"))) {
//				JSONArray dataArray = responseJson.getJSONArray("data");
//				List<CoinMarketData> coinMarketList = new ArrayList<>();
//
//				for (int i = 0; i < dataArray.size(); i++) {
//					JSONObject item = dataArray.getJSONObject(i);
//					CoinMarketData coinMarket = new CoinMarketData();
//
//					// 基础信息
//					coinMarket.setSymbol(item.getStr("symbol"));
//					coinMarket.setCurrentPrice(item.getBigDecimal("current_price"));
//					coinMarket.setMarketCap(item.getBigDecimal("market_cap"));
//
//					// 价格变化（美元）- 只取15分钟、1小时、4小时、24小时
//					coinMarket.setPriceChange15m(item.getBigDecimal("price_change_15m"));
//					coinMarket.setPriceChange1h(item.getBigDecimal("price_change_1h"));
//					coinMarket.setPriceChange4h(item.getBigDecimal("price_change_4h"));
//					coinMarket.setPriceChange24h(item.getBigDecimal("price_change_24h"));
//
//					// 价格变化百分比（%）
//					coinMarket.setPriceChangePercent15m(item.getBigDecimal("price_change_percent_15m"));
//					coinMarket.setPriceChangePercent1h(item.getBigDecimal("price_change_percent_1h"));
//					coinMarket.setPriceChangePercent4h(item.getBigDecimal("price_change_percent_4h"));
//					coinMarket.setPriceChangePercent24h(item.getBigDecimal("price_change_percent_24h"));
//
//					// 成交量（美元）
//					coinMarket.setVolumeUsd15m(item.getBigDecimal("volume_usd_15m"));
//					coinMarket.setVolumeUsd1h(item.getBigDecimal("volume_usd_1h"));
//					coinMarket.setVolumeUsd4h(item.getBigDecimal("volume_usd_4h"));
//					coinMarket.setVolumeUsd24h(item.getBigDecimal("volume_usd_24h"));
//
//					// 成交量变化百分比（%）
//					coinMarket.setVolumeChangePercent15m(item.getBigDecimal("volume_change_percent_15m"));
//					coinMarket.setVolumeChangePercent1h(item.getBigDecimal("volume_change_percent_1h"));
//					coinMarket.setVolumeChangePercent4h(item.getBigDecimal("volume_change_percent_4h"));
//					coinMarket.setVolumeChangePercent24h(item.getBigDecimal("volume_change_percent_24h"));
//
//					// 主动买入成交量（美元）
//					coinMarket.setBuyVolumeUsd15m(item.getBigDecimal("buy_volume_usd_15m"));
//					coinMarket.setBuyVolumeUsd1h(item.getBigDecimal("buy_volume_usd_1h"));
//					coinMarket.setBuyVolumeUsd4h(item.getBigDecimal("buy_volume_usd_4h"));
//					coinMarket.setBuyVolumeUsd24h(item.getBigDecimal("buy_volume_usd_24h"));
//
//					// 主动卖出成交量（美元）
//					coinMarket.setSellVolumeUsd15m(item.getBigDecimal("sell_volume_usd_15m"));
//					coinMarket.setSellVolumeUsd1h(item.getBigDecimal("sell_volume_usd_1h"));
//					coinMarket.setSellVolumeUsd4h(item.getBigDecimal("sell_volume_usd_4h"));
//					coinMarket.setSellVolumeUsd24h(item.getBigDecimal("sell_volume_usd_24h"));
//
//					// 主动成交量净流入（= 买入 - 卖出）
//					coinMarket.setVolumeFlowUsd15m(item.getBigDecimal("volume_flow_usd_15m"));
//					coinMarket.setVolumeFlowUsd1h(item.getBigDecimal("volume_flow_usd_1h"));
//					coinMarket.setVolumeFlowUsd4h(item.getBigDecimal("volume_flow_usd_4h"));
//					coinMarket.setVolumeFlowUsd24h(item.getBigDecimal("volume_flow_usd_24h"));
//
//					coinMarketList.add(coinMarket);
//				}
//
//				return coinMarketList;
//			} else {
//				String errorMsg = "CoinGlass API错误: " + responseJson.getStr("msg");
//				log.error(errorMsg);
//				throw new RuntimeException(errorMsg);
//			}
//		} else {
//			String errorMsg = "CoinGlass API响应为空";
//			log.error(errorMsg);
//			throw new RuntimeException(errorMsg);
//		}
//	}
//
//
//	/**
//	 * 获取要订阅的期货行情数据
//	 * @param type 市场类型 WI:美国外汇期货,WX:欧美股指期货,WA:外盘贵金属期货
//	 * @param locale 语言 1:简体中文,2:繁体,3:英文,4:日文,5:韩文
//	 * @return
//	 */
//	@ApiOperation(value = "获取期货行情数据")
//	@GetMapping(value = "/getFuturesMarketData")
//	public ResultPista<List<MarketTradeBo>> getFuturesMarketData(String type,Integer locale) {
//		if(locale == null){
//			locale =1;
//		}
//		Integer localeInt = locale;
//		if("WI".equalsIgnoreCase(type) ||  "WX".equalsIgnoreCase(type)|| "WA".equalsIgnoreCase(type)){
//			type = type.toUpperCase();
//			String finalType = type;
//			List<MarketTradeBo> codeSet = xmsRedis.get(ConstantStatic.market_trade + finalType + RedisConstant.SEPARATOR + localeInt, () ->
//			marketTradeConfigService
//				.lambdaQuery()
//				.eq(MarketTradeConfig::getType, finalType)
//				.eq(MarketTradeConfig::getDataPankou,1)
//				.select(MarketTradeConfig::getDataCode,MarketTradeConfig::getDataLabel,
//					MarketTradeConfig::getDataLabelHk,MarketTradeConfig::getDataLabelEn,
//					MarketTradeConfig::getDataLabelJa,MarketTradeConfig::getDataLabelKr)
//				.list().stream().map(record->{
//					MarketTradeBo entity = new MarketTradeBo();
//					entity.setCode(record.getDataCode());
//					if(localeInt == 1){
//						entity.setName(record.getDataLabel());
//					}else if(localeInt == 2){
//						entity.setName(record.getDataLabelHk());
//					}else if(localeInt == 3){
//						entity.setName(record.getDataLabelEn());
//					}else if(localeInt == 4){
//						entity.setName(record.getDataLabelJa());
//					}else if(localeInt == 5){
//						entity.setName(record.getDataLabelKr());
//					}
//					return entity;
//				}).collect(Collectors.toList()));
//			return ResultPista.data(codeSet);
//		}
//		return ResultPista.success();
//	}
//}
