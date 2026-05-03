package com.xms.common.utils.uuid;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import com.xms.common.constant.SysConstant;
import com.xms.common.utils.SecurityUtils;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 各种id生成策略
 * <p>Title: IDUtils</p>
 * <p>Description: </p>
 * <p>Company: www.itcast.com</p>
 *
 * @version 1.0
 */
public class IDUtils {

	private SimpleDateFormat sfd = new SimpleDateFormat("yyyyMMddHHmmssSSS");
	private Set<Integer> randomSet = new HashSet<Integer>();
	private String dateFlag;
	private int sequence;

	/**
	 * 初始化  默认(默认是时间格式和序列)
	 *
	 * @return
	 * @throws
	 * @date 2021/6/15 16:45
	 */
	public IDUtils() {
		this.sfd = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		this.dateFlag = this.sfd.format(new Date());
		this.sequence = 0;
	}

	/**
	 * 初始化
	 *
	 * @param format 时间格式   yyyyMMddHHmmssSSS
	 * @return
	 * @throws
	 * @date 2021/6/15 16:45
	 */
	public IDUtils(String format) {
		this.sfd = new SimpleDateFormat(format);
		this.dateFlag = this.sfd.format(new Date());
		this.sequence = 0;
	}

	/**
	 * 初始化
	 *
	 * @param sequence 开始累加数字
	 * @return
	 * @throws
	 * @date 2021/6/15 16:45
	 */
	public IDUtils(int sequence) {
		this.sequence = sequence;
	}

	/**
	 * 万能的雪花算法
	 *
	 * @param workerId     支持的最大机器id，结果是31
	 * @param datacenterId 支持的最大数据标识id，结果是31
	 */
	public static Snowflake getSnowflake(long workerId, long datacenterId) {
		return IdUtil.getSnowflake(workerId, datacenterId);
	}

	/**
	 * 万能的雪花算法
	 */
	public static String getSnowflakeStr() {
		return getSnowflake().nextIdStr();
	}

	/**
	 * 万能的雪花算法
	 */
	public static Long getSnowflakeId() {
		return IDUtils.getSnowflake().nextId();
	}

	/**
	 * 万能的雪花算法
	 */
	public static Snowflake getSnowflake() {
		return IdUtil.getSnowflake();
	}

	/**
	 *   万能的雪花算法
	 * @param workerId 支持的最大机器id，结果是31
	 *
	 */
	public static Snowflake getSnowflake(long workerId) {
		return getSnowflake(workerId, RandomUtil.randomInt(SysConstant.THIRTY_ONE));
	}

	/**
	 * xms获取orderNo
	 */
	public static String getOrderNo() {
		return generatedNoByFormatDateAndRandom(5, String.valueOf(SecurityUtils.getFrontUserId()));
	}

	public static String getOrderNoBack() {
		return generatedNoByFormatDateAndRandom(5, String.valueOf(SecurityUtils.getUserId()));
	}

	/**
	 * 根据时间+机器码+后面随机数组成的编号
	 *
	 * @param machine      机器码
	 * @param randomDigits 随机数的位数
	 * @return java.lang.String
	 * @throws
	 * @date 2021/6/15 16:46
	 */
	public static String generatedNoByFormatDateAndRandom(int randomDigits, String machine) {
		String no = "";
		SimpleDateFormat sfd = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		Set<Integer> randomSet = new HashSet<Integer>();
		String formatDate = sfd.format(new Date());
		StringBuffer sb = new StringBuffer("9");
		for (int i = 1; i < randomDigits; i++) {
			sb.append("9");
		}
		Random random = new Random();

		int randomNum = 0;
		do {
			randomNum = random.nextInt(Integer.valueOf(sb.toString()));
		} while (randomSet.contains(randomNum));
		randomSet.add(randomNum);

		if (Objects.nonNull(machine)) {
			no = formatDate + machine + String.format("%0" + randomDigits + "d", randomNum);
		} else {
			no = formatDate + String.format("%0" + randomDigits + "d", randomNum);
		}
		return no;
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

	/**
	 * 图片名生成
	 */
	public static String genImageName() {
		//取当前时间的长整形值包含毫秒
		long millis = System.currentTimeMillis();
		//long millis = System.nanoTime();
		//加上三位随机数
		Random random = new Random();
		int end3 = random.nextInt(999);
		//如果不足三位前面补0
		String str = millis + String.format("%03d", end3);

		return str;
	}

	/**
	 * id生成
	 */
	public static long genItemId() {
		//取当前时间的长整形值包含毫秒
		long millis = System.currentTimeMillis();
		//long millis = System.nanoTime();
		//加上两位随机数
		Random random = new Random();
		int end2 = random.nextInt(99);
		//如果不足两位前面补0
		String str = millis + String.format("%02d", end2);
		long id = new Long(str);
		return id;
	}

	public String generatedNoByFormatDate() {
		return this.generatedNoByFormatDate(null);
	}

	public String generatedNoByFormatDateAndNum(int digits) {
		return this.generatedNoByFormatDateAndNum(digits, null);
	}

	public String generatedNoByFormatDateAndRandom(int randomDigits) {
		return this.generatedNoByFormatDateAndRandom(randomDigits, null);
	}

	public String generatedNoByPrefix(String prefix, int digits) {
		return this.generatedNoByPrefix(prefix, digits, null);
	}

	public String generatedNoByDateAndPrefix(String prefix) {
		return this.generatedNoByDateAndPrefix(prefix, null);
	}

	public String generatedNoBySuffix(String suffix, int digits) {
		return this.generatedNoBySuffix(suffix, digits, null);
	}

	public String generatedNoByDateAndSuffix(String suffix) {
		return this.generatedNoByDateAndSuffix(suffix, null);
	}

	/**
	 * 根据时间获取编号
	 *
	 * @param machine 机器码
	 * @return java.lang.String
	 * @throws
	 * @date 2021/6/15 16:46
	 */
	public String generatedNoByFormatDate(String machine) {
		String formatDate = this.sfd.format(new Date());
		if (Objects.nonNull(machine)) {
			return formatDate + machine;
		}
		return formatDate;
	}

	/**
	 * 根据时间+机器码+后面累加的位数组成的编号
	 *
	 * @param machine 机器码
	 * @param digits  累加值的位数
	 * @return java.lang.String
	 * @throws
	 * @date 2021/6/15 16:46
	 */
	public String generatedNoByFormatDateAndNum(int digits, String machine) {
		String no = "";
		String formatDate = this.sfd.format(new Date());

		if (this.dateFlag.equals(formatDate)) {
			this.sequence++;
		} else {
			this.sequence = 0;
		}

		if (Objects.nonNull(machine)) {
			no = formatDate + machine + String.format("%0" + digits + "d", this.sequence);
		} else {
			no = formatDate + String.format("%0" + digits + "d", this.sequence);
		}

		return no;
	}

	/**
	 * 根据前缀+累加位数
	 *
	 * @param prefix  前缀
	 * @param machine 机器码(可省略)
	 * @param digits  随机数的位数
	 * @return java.lang.String
	 * @throws
	 * @date 2021/6/15 16:46
	 */
	public String generatedNoByPrefix(String prefix, int digits, String machine) {
		String no = "";
		this.sequence++;

		if (Objects.nonNull(machine)) {
			no = prefix + machine + String.format("%0" + digits + "d", this.sequence);
		} else {
			no = prefix + String.format("%0" + digits + "d", this.sequence);
		}
		return no;
	}

	/**
	 * 根据前缀+时间
	 *
	 * @param prefix  后缀
	 * @param machine 机器码(可省略) null
	 * @return java.lang.String
	 * @throws
	 * @date 2021/6/15 16:46
	 */
	public String generatedNoByDateAndPrefix(String prefix, String machine) {
		String no = "";
		String formatDate = this.sfd.format(new Date());

		if (Objects.nonNull(machine)) {
			no = prefix + formatDate + machine;
		} else {
			no = prefix + formatDate;
		}
		return no;
	}

	/**
	 * 根据后缀+累加位数
	 *
	 * @param suffix  后缀
	 * @param machine 机器码(可省略) null
	 * @param digits  随机数的位数
	 * @return java.lang.String
	 * @throws
	 * @date 2021/6/15 16:46
	 */
	public String generatedNoBySuffix(String suffix, int digits, String machine) {
		String no = "";
		this.sequence++;

		if (Objects.nonNull(machine)) {
			no = machine + String.format("%0" + digits + "d", this.sequence) + suffix;
		} else {
			no = String.format("%0" + digits + "d", this.sequence) + suffix;
		}
		return no;
	}

	/**
	 * 根据后缀+时间
	 *
	 * @param suffix  后缀
	 * @param machine 机器码(可省略) null
	 * @return java.lang.String
	 * @throws
	 * @date 2021/6/15 16:46
	 */
	public String generatedNoByDateAndSuffix(String suffix, String machine) {
		String no = "";
		String formatDate = this.sfd.format(new Date());

		if (Objects.nonNull(machine)) {
			no = formatDate + machine + suffix;
		} else {
			no = formatDate + suffix;
		}
		return no;
	}


}
