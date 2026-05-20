package com.xms.app;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

/**
 * CoinGlass API测试
 * @author xms
 */
public class CoinGlassApiTest {

	//public static void main(String[] args) {
		//testFundingRateApi();
	//}

	public static void main(String[] args) {

	}

	/**
	 *  获取polymarket 事件 closed知道结束事件
	 */
	public void test1(){
		HttpResponse response = HttpUtil.createGet("https://gamma-api.polymarket.com/markets/slug/nba-cle-nyk-2026-05-19-spread-home-6pt5")
			.timeout(5000)
			.execute();
		System.out.println(response.body());
	}
	/**
	 * 测试资金费率API
	 */
	public static void testFundingRateApi() {
		try {
			// 使用 Hutool 发送请求
			String url = "https://open-api-v4.coinglass.com/api/futures/funding-rate/exchange-list";

			String response = HttpUtil.createGet(url)
				.header("accept", "application/json")
				.header("CG-API-KEY", "your_api_key_here") // 替换为你的API Key
				.timeout(30000) // 30秒超时
				.execute()
				.body();

			System.out.println("CoinGlass资金费率数据响应: " + response);

			if (StrUtil.isNotBlank(response)) {
				JSONObject responseJson = JSONUtil.parseObj(response);

				// 检查响应状态
				if ("0".equals(responseJson.getStr("code"))) {
					JSONArray dataArray = responseJson.getJSONArray("data");
					System.out.println("获取到 " + dataArray.size() + " 个币种的资金费率数据");

					// 打印前3条数据
					for (int i = 0; i < Math.min(3, dataArray.size()); i++) {
						JSONObject item = dataArray.getJSONObject(i);
						System.out.println("=== " + item.getStr("symbol") + " 资金费率数据 ===");

						// USDT/USD保证金模式
						JSONArray stablecoinArray = item.getJSONArray("stablecoin_margin_list");
						if (stablecoinArray != null && !stablecoinArray.isEmpty()) {
							System.out.println("USDT/USD保证金模式:");
							for (int j = 0; j < stablecoinArray.size(); j++) {
								JSONObject exchangeData = stablecoinArray.getJSONObject(j);
								System.out.println("  " + exchangeData.getStr("exchange") +
									": " + exchangeData.getBigDecimal("funding_rate") + "%" +
									" (间隔: " + exchangeData.getInt("funding_rate_interval") + "小时)");
							}
						}

						// 币本位保证金模式
						JSONArray tokenArray = item.getJSONArray("token_margin_list");
						if (tokenArray != null && !tokenArray.isEmpty()) {
							System.out.println("币本位保证金模式:");
							for (int j = 0; j < tokenArray.size(); j++) {
								JSONObject exchangeData = tokenArray.getJSONObject(j);
								System.out.println("  " + exchangeData.getStr("exchange") +
									": " + exchangeData.getBigDecimal("funding_rate") + "%" +
									" (间隔: " + exchangeData.getInt("funding_rate_interval") + "小时)");
							}
						}
						System.out.println();
					}
				} else {
					System.err.println("API错误: " + responseJson.getStr("msg"));
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
