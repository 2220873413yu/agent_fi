package com.xms.common.utils;

import cn.hutool.crypto.digest.DigestUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/**
 * @author: GT63S
 * @createDate: 2025/7/25
 */
@Slf4j
public class JuSign {
	public static boolean juSignCheck(Map<String, Object> hashMap, String sign, String appSecret) {
		String newSign = generateSing(hashMap, appSecret);
		log.debug("newSign:{} sign {}", newSign, sign);
		return newSign.equals(sign);
	}

	public static String generateSing(Map<String, Object> hashMap, String appSecret) {
		TreeMap<String, String> treeMap = hashMap.entrySet().stream().filter(x -> Objects.nonNull(x.getValue())).collect(TreeMap::new, (tm, e) -> tm.put(e.getKey(), String.valueOf(e.getValue())), Map::putAll);
		StringBuilder stringBuilder = new StringBuilder();
		treeMap.forEach((key, value) -> {
			if (!StringUtils.isBlank(value) && !"sign".equals(key)) {
				stringBuilder.append(key);
				stringBuilder.append(value);
			}
		});
		// appSecret拼接
		stringBuilder.append(appSecret);
		log.debug("拼接后的 stringBuilder:{}", stringBuilder);
		// md5加密
		String newSign = DigestUtil.md5Hex(stringBuilder.toString());
		log.debug("拼接后MD5之后的 stringBuilder:{}", stringBuilder);
		return newSign;
	}
}
