package com.xms.common.utils.rsa;

import java.util.*;

public class StringUtil {

	/**
	 * 鍒ゆ柇瀛楃绌挎槸鍚︿负绌�
	 * 褰撳瓧绗︿覆涓虹┖鎴栫┖瀛楃涓叉椂杩斿洖true 鍚﹀垯杩斿洖false
	 */
	public static final boolean isEmpty(String str)
	{
		if (str == null || str.trim().length() == 0)
		{
			return true;
		}
		return false;

	}

	/**
	 * 鍒ゆ柇瀛楃鏁扮粍鏄惁涓虹┖
	 */
	public static boolean areNotEmpty(String... values) {
		boolean result = true;
		if (values == null || values.length == 0) {
			result = false;
		} else {
			for (String value : values) {
				result &= !isEmpty(value);
			}
		}
		return result;
	}


	/**
	 * 鎶婃暟缁勬墍鏈夊厓绱犳帓搴�
	 */
	public static String createLinkString(Map<String, String> params) {
		List<String> keys = new ArrayList<String>(params.keySet());
		Collections.sort(keys);
		String prestr = "";
		for (int i = 0; i < keys.size(); i++) {
			String key = keys.get(i);
			String value = params.get(key);
			if (i == keys.size() - 1) {
				prestr = prestr + key + "=" + value;
			} else {
				prestr = prestr + key + "=" + value + "&";
			}
		}
		return prestr;
	}

	/**
	 * map 杞垚 string
	 */
	public static String mapToString(Map<String, String> map) {
		SortedMap<String, String> sortedMap = new TreeMap<String, String>(map);

		StringBuilder sb = new StringBuilder();

		for (Map.Entry<String, String> entry : sortedMap.entrySet()) {
			if (StringUtil.isEmpty(entry.getValue())) {
				continue;
			}
			sb.append(entry.getKey()).append('=').append(entry.getValue()).append('&');
		}
		sb.deleteCharAt(sb.length() - 1);

		return sb.length() == 0 ? "" : sb.toString();
	}
}
