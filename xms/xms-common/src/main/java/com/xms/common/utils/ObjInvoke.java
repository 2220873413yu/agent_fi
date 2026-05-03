package com.xms.common.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * yangzhiwei
 * 使用反射给实体类k赋值（默认值）
 * insert update会报null异常，为空时不能插入和更新
 */
public class ObjInvoke {
	public static Object getObjDefault(Object obj) {
		// 得到类对象
		Class objCla = obj.getClass();
		Field[] fs = objCla.getDeclaredFields();
		for (int i = 0; i < fs.length; i++) {
			Field f = fs[i];
			// 设置些属性是可以访问的
			boolean isStatic = Modifier.isStatic(f.getModifiers());
			if (isStatic) {
				continue;
			}
			// 设置些属性是可以访问的
			f.setAccessible(true);
			try {
				// 得到此属性的值
				Object val = f.get(obj);
				// 得到此属性的类型
				String type = f.getType().toString();
				if (type.endsWith("String") && val == null) {
					// 给属性设值
					f.set(obj, "");
				} else if ((type.endsWith("int") || type.endsWith("Integer") || type.endsWith("double")) && val == null) {
					f.set(obj, 0);
				} else if ((type.endsWith("long") || type.endsWith("Long")) && val == null) {
					f.set(obj, 0L);
				} else if (type.endsWith("Date") && val == null) {
					f.set(obj, Date.valueOf("1970-01-01"));
				} else if (type.endsWith("Timestamp") && val == null) {
					f.set(obj, Timestamp.valueOf("1970-01-01 00:00:00"));
				} else if (type.endsWith("BigDecimal") && val == null) {
					f.set(obj, new BigDecimal(0));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return obj;
	}

	public static List getObjDefaultList(List objList) {
		List list = new ArrayList();
		for (Object t : objList) {
			list.add(getObjDefault(t));
		}
		return list;
	}
}
