package com.xms.common.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.reflect.FieldUtils;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.TreeMap;

@Slf4j
public class Obj2Sign {

    /**
     * 生成签名
     * 通过obj转成key&value，并且以为asc从小到大排序
     *
     * @param obj
     * @return
     */
    public static String getSign(Object obj) {
        log.info("com.ruoyi.common.utils.Obj2Sign.getSign|生成签名|obj:{}",obj);
        Map<String, String> params = getObjectValue(obj);
        String result = "";
        StringBuilder sb = new StringBuilder();
        try {
            // 构造签名键值对的格式
            for (String key : params.keySet()) {
                String value = StringUtils.isBlank(params.get(key)) ? "" : params.get(key);
                sb.append(key).append("=").append(value).append("&");
            }
            result = sb.toString();
            //删除最后一个&
            result = result.substring(0, result.length() - 1);
        } catch (Exception e) {
            log.error("解析异常:e:{}", e);
            return null;
        }
        log.info("签名参数：{}", result);
        return result;
    }

    /**
     * 把对象变成TreeMap
     *
     * @param object
     * @return
     */
    private static Map<String, String> getObjectValue(Object object) {
        Map<String, String> map = new TreeMap<>();
        Class<?> clz = object.getClass();
        Field[] fields = clz.getDeclaredFields();

        try {
            for (Field field : fields) {
                String val = "";
                if (FieldUtils.readField(field, object, true) != null) {
                    val = FieldUtils.readField(field, object, true).toString();
                }
                map.put(field.getName(), val);
            }
        } catch (Exception e) {
            log.error("获取属性异常：{}", e);
        }
        return map;
    }
}
