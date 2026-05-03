package com.xms.common.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static java.util.regex.Pattern.*;

/**
 * 正则表达式相关类
 *
 *
 *
 */
public class RegexUtil {

	/**
	 * 正则表达式，交易金额校验（不包含0）
	 *
	 * @param str
	 * @return
	 */
	public static boolean isVaildTradeNum(String tardeNum) {
		if ("".equals(tardeNum) || null == tardeNum) {
			return false;
		} else {
			Pattern pattern = compile("^(([1-9][0-9]*)|(([0]\\.\\d{1,2}|[1-9][0-9]*\\.\\d{1,2})))$");
			return pattern.matcher(tardeNum).matches();
		}
	}

	/**
	 * 邮箱校验
	 *
	 * @param email
	 * @return
	 */
	public static boolean isValidEmail(String email) {
		if ("".equals(email) || null == email) {
			return false;
		} else {
			Pattern pattern = compile("^\\w+@[a-zA-Z0-9]{2,10}(?:\\.[a-z]{2,4}){1,3}$");
			return pattern.matcher(email).matches();
		}
	}

	/**
	 * 手机号码校验
	 *
	 * @param tel
	 * @return
	 */
	public static boolean isValidTel(String tel) {
		if ("".equals(tel) || null == tel) {
			return false;
		} else {
			Pattern pattern = compile("^1(3|4|5|6|7|8|9)\\d{9}");
			return pattern.matcher(tel).matches();
		}
	}

	/**
	 * 6位整数校验
	 *
	 * @param num
	 * @return
	 */
	public static boolean isValidSixNum(String num) {
		if ("".equals(num) || null == num) {
			return false;
		} else {
			Pattern pattern = compile("^\\d{6}$");
			return pattern.matcher(num).matches();
		}
	}

	/**
	 * 香港手机号码8位数，3|4|5|6|8|9开头+7位任意数
	 */
	public static boolean isHKPhoneLegal(String str) throws PatternSyntaxException {
		if ("".equals(str) || null == str) {
			return false;
		} else {
			String regExp = "^(3|4|5|6|8|9)\\d{7}$";
			Pattern p = compile(regExp);
			Matcher m = p.matcher(str);
			return m.matches();
		}
	}

	/**
	 * 台湾：09开头后面跟8位数字
	 */
	public static boolean isTWPhoneLegal(String str) throws PatternSyntaxException {
		if ("".equals(str) || null == str) {
			return false;
		} else {
			String regExp = "^[0][9]\\d{8}$";
			Pattern p = compile(regExp);
			Matcher m = p.matcher(str);
			return m.matches();
		}
	}

	/**
	 * 澳门：66或68开头后面跟5位数字
	 */
	public static boolean isMCPhoneLegal(String str) throws PatternSyntaxException {
		if ("".equals(str) || null == str) {
			return false;
		} else {
			String regExp = "^[6]([8|6])\\d{5}$";
			Pattern p = compile(regExp);
			Matcher m = p.matcher(str);
			return m.matches();
		}
	}

	public static boolean isNotNumEmail(String num) {
		if ("".equals(num) || null == num) {
			return false;
		} else {
			Pattern pattern1 = compile("^[0-9]*$");
			Pattern pattern2 = compile("^\\w+@[a-zA-Z0-9]{2,10}(?:\\.[a-z]{2,4}){1,3}$");
			return (!pattern1.matcher(num).matches()) && (!pattern2.matcher(num).matches());
		}
	}

	/**
	 * 要求：可以包含数字、字母、下划线，并且要同时含有数字和字母，且长度要在4-8位之间
	 *
	 * @param str
	 * @return
	 */
	public static boolean isValidName(String str) {
		if ("".equals(str) || null == str) {
			return false;
		} else {
			Pattern pattern = compile("^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z_]{4,8}$");
			return pattern.matcher(str).matches();
		}
	}

	/**
	 * 要求：可以包含数字、字母、下划线，并且要同时含有数字和字母，且长度要在6-20位之间
	 *
	 * @param str
	 * @return
	 */
	public static boolean isValidQQ(String str) {
		if ("".equals(str) || null == str) {
			return false;
		} else {
			Pattern pattern = compile("^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z_]{6,20}$");
			return pattern.matcher(str).matches();
		}
	}

	/**
	 * 姓名校验：要求（两到四位中文）
	 *
	 * @param str
	 * @return
	 */
	public static boolean isValidTrueName(String str) {
		if ("".equals(str) || null == str) {
			return false;
		} else {
			Pattern pattern = compile("^[\\u4e00-\\u9fa5]{2,4}$");
			return pattern.matcher(str).matches();
		}
	}

	/**
	 * 大陆身份证号校验
	 *
	 * @param str
	 * @return
	 */
	public static boolean isValidIdCard(String str) {
		if ("".equals(str) || null == str) {
			return false;
		} else {
			Pattern pattern = compile("^(\\d{15}|\\d{18}|\\d{17}[x|X])$");
			return pattern.matcher(str).matches();
		}
	}

	/**
	 * 香港身份证号校验
	 *
	 * @param str
	 * @return
	 */
	public static boolean isHKValidIdCard(String str) {
		if ("".equals(str) || null == str) {
			return false;
		} else {
			Pattern pattern = compile("^((\\s?[A-Za-z])|([A-Za-z]{2}))\\d{6}(\\([0−9aA]\\)|[0-9aA])$");
			return pattern.matcher(str).matches();
		}
	}

	/**
	 * 台湾身份证号校验
	 *
	 * @param str
	 * @return
	 */
	public static boolean isTWValidIdCard(String str) {
		if ("".equals(str) || null == str) {
			return false;
		} else {
			Pattern pattern = compile("^[a-zA-Z][0-9]{9}$");
			return pattern.matcher(str).matches();
		}

	}

	/**
	 * 澳门身份证号校验
	 *
	 * @param str
	 * @return
	 */
	public static boolean isMCValidIdCard(String str) {
		if ("".equals(str) || null == str) {
			return false;
		} else {
			Pattern pattern = compile("^[1|5|7][0-9]{6}\\(?[0-9A-Z]\\)?$");
			return pattern.matcher(str).matches();
		}
	}

	/**
	 * 银行账号校验
	 *
	 * @param str
	 * @return
	 */
	public static boolean isValidBankCard(String str) {
		if ("".equals(str) || null == str) {
			return false;
		} else {
			Pattern pattern = compile("^(\\d{16}|\\d{19}|\\d{18})$");
			return pattern.matcher(str).matches();
		}
	}

	/**
	 * 正整数校验
	 *
	 * @param str
	 * @return
	 */
	public static boolean isValidNum(String str) {
		if ("".equals(str) || null == str) {
			return false;
		} else {
			Pattern pattern = compile("^[1-9]\\d*$");
			return pattern.matcher(str).matches();
		}
	}

	/**
	 * 非负数校验(包含0)
	 *
	 * @param str
	 * @return
	 */
	public static boolean isValidNumFloat(String str) {
		if ("".equals(str) || null == str) {
			return false;
		} else {
			Pattern pattern = compile("^[+]{0,1}(\\d+)$|^[+]{0,1}(\\d+\\.\\d+)$");
			return pattern.matcher(str).matches();
		}
	}

	/**
	 * 非负数校验(bu包含0)(包含六位小数)
	 *
	 * @param str
	 * @return
	 */
	public static boolean isValidNumFloat4S(String str) {
		if ("".equals(str) || null == str) {
			return false;
		} else {
			Pattern pattern = compile("^(([1-9][0-9]*)|(([0]\\.\\d{1,2}|[1-9][0-9]*\\.\\d{1,6})))$");
			return pattern.matcher(str).matches();
		}
	}

	/**
	 * 密码长度为6位的校验
	 *
	 * @return
	 */
	public static boolean verifyPassWordSix(String str) {
		if ("".equals(str) || null == str) {
			return false;
		} else {
			Pattern pattern = compile("^(?=.*[0-9])(?=.*[a-zA-Z])(.{6})$");
			return pattern.matcher(str).matches();
		}

	}

	/**
	 * 判断ETH地址是否规范
	 * @param address
	 * @return
	 */
	public static boolean isValidETHAddress(String address){
		try{
			if("0x".equals(address.substring(0,2))&&address.length()==42){
				return true;
			}
			return false;
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
	}

	public static void main(String[] args) {
		Pattern Password_Pattern = compile("^(?=.*[0-9])(?=.*[a-zA-Z])(.{6})$");
		Matcher matcher = Password_Pattern.matcher("1457849");
		if (matcher.matches()) {
			System.out.println("1");
		} else {
			System.out.println("122");
		}
	}

}
