package com.xms.dao.api;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONObject;
import com.xms.common.constant.SysConstant;
import com.xms.common.exception.ServiceException;
import com.xms.common.result.ResponseCode;
import com.xms.dao.service.ISysParaService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 第三方相关服务
 *
 * @author: renengadePISTA
 * @createDate: 2024/1/10
 */
@Component
@AllArgsConstructor
@Slf4j
public class PullThirdInfo {
	private final Environment environment;
	private final ISysParaService sysParaServiceImpl;

	public BigDecimal getPriceGroup() {
			try {
				String res = HttpUtil.get(environment.getProperty("contract.PRICEURL"));
				JSONObject jsonObject = JSONObject.parseObject(res);
				if (!SysConstant.TWO_HUNDRED.equals(jsonObject.getInteger("code"))) {
					log.error("结果：{}", jsonObject);
					//throw new ServiceException(ResponseCode.PRICE_GET_FAIL);
				}
				JSONObject priceGroup = jsonObject.getJSONObject("data");
				BigDecimal fsnUsd = priceGroup.getBigDecimal("fsn");
				log.debug("获取合约价格结果：{}", priceGroup.toJSONString());
				if (fsnUsd.compareTo(BigDecimal.ZERO) <= 0) {
					//取兜底价格
					BigDecimal defaultPrice = new BigDecimal("1");
					return defaultPrice;
				}
				return fsnUsd;
			} catch (Exception e) {
				e.printStackTrace();
				log.error("groupDto  结果", e);
				return new BigDecimal("1");
			}

	}

/*
	public boolean pushAmountData(Withdrawal withdrawal) {
		//  BY RENEGADE PISTA: 2024/1/9  拉取合约API，申请提现
		Map<String, Object> map = handlerContractSign(withdrawal, environment);
		String url = environment.getProperty("contract.WITHDRAWURL");
		String jsonStr = JSONUtil.toJsonStr(map);
		log.error("错误日志请求参数:{},withdrawal:{}", map, withdrawal);
		String result = HttpUtil.post(url, jsonStr);
		log.info("提现日志请求 req:{},resp:{}", map, result);
		JSONObject reJonson = JSONObject.parseObject(result);
		if (!SysConstant.TWO_HUNDRED.toString().equals(reJonson.getString("code"))) {
			log.error("错误日志：{},请求参数:{}", reJonson, map);
			throw new ServiceException(ResponseCode.PUSH_AMOUNT_FAIL);
		}
		return true;
	}

	private Map<String, Object> handlerContractSign(Withdrawal withdrawal, Environment environment) {
		Map<String, Object> map = new HashMap<>(5);
		//订单id
		map.put("orderId", withdrawal.getCode());
		//提现地址
		map.put("userAddress", withdrawal.getAccountNo());
		//提现数量
		map.put("amount", withdrawal.getActualAmount());
		//对应的合约地址
		if (withdrawal.getCoinType().equals(ConstantType.user_money_coin_type.type_1)) {
			map.put("token", "USDT");
		} else if (withdrawal.getCoinType().equals(ConstantType.user_money_coin_type.type_3)) {
			map.put("token", "FSN");
		}


		// // //时间戳
		// map.put("deadline", System.currentTimeMillis());
		// String string = MapUtil.sortJoin(map, StrUtil.EMPTY, StrUtil.EMPTY, true, environment.getProperty("contract.SIGN"));
		// log.debug("签名之前的串：{}", string);
		// String sign = SecureUtil.signParamsMd5(map, environment.getProperty("contract.SIGN"));
		// log.debug("签名之后的串：{}", sign);
		// map.put("signature", sign);
		return map;
	}*/


}
