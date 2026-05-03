package com.xms.common.utils;

import java.util.Random;
import java.util.UUID;

public class GenUUID {

    /**
     * 生成uuid
     * @return uuid
     * */
    public static String uuid(){
        /**获取UUID并转化为String对象*/
        String uuid = UUID.randomUUID().toString();
        /**因为UUID本身为32位只是生成时多了“-”，所以将它们去点就可*/
        uuid = uuid.replace("-", "");
        return uuid;
    }

    /**
     * 生成16位的uuid
     * @return
     */
    public static String uuid_16(){
        /**获取UUID并转化为String对象*/
        String uuid = UUID.randomUUID().toString();
        /**因为UUID本身为32位只是生成时多了“-”，所以将它们去点就可*/
        uuid = uuid.replace("-", "").substring(16);
        return uuid;
    }

    /**
	 * 生成纯数字
     * @param num
	 * @return
	 */
	public static String getCode(int num) {
		Long time = System.currentTimeMillis();
		String strTime = String.valueOf(time);
		String code = strTime.substring(strTime.length() - num);
		return code;
	}

	/**
	 * @return String
	 * @Title: generateSecretKey
	 * @param:
	 * @Description: 生成指定位数的secretkey
	 */
	public static String generateSecretKey(int length) {
		String[] array = {"y", "B", "w", "0", "v", "F", "s", "H", "h", "2",
			"o", "L", "9", "N", "l", "P", "7", "R", "E", "T", "5", "V",
			"b", "X", "m", "u", "H", "W", "c", "d", "e", "C", "g", "3",
			"i", "j", "k", "O", "Y", "n", "K", "p", "S", "r", "G", "t",
			"Z", "q", "f", "6", "A", "z", "D", "1", "J", "a", "4", "U",
			"x", "Q", "8", "M"};
		int max = array.length;
		String str = "";
		Random random = new Random();
		for (int i = 0; i < length; i++) {
			int indexOf = random.nextInt(max) % (max - 0 + 1) + 0;
			str += array[indexOf];
		}
		return str;
	}

}
