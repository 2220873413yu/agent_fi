package com.xms.common.result;

import cn.hutool.core.util.StrUtil;
import com.xms.common.core.domain.api.ResultPista;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * 异常代码
 * http 协议规范为主
 */
public enum ResponseCode {

	/**
	 * 请求成功
	 */
	CODE_200(200, "成功","success"),
	/**
	 * 参数异常
	 */
	CODE_300(300, "请求错误,请稍后重试", "Request error, please try again later"),
	/**
	 * token校验不通过
	 */
	CODE_400(400, "请登录后访问", "Please login to access"),
	/**
	 * 账号禁用
	 */
	CODE_401(401, "当前账号被禁用,请联系管理员", "Account disabled, please contact administrator"),
	/**
	 * 系统异常
	 */
	CODE_500(500, "请求错误,请稍后重试", "Request error, please try again later"),

	CODE_1000(1000, "{0}", "{0}"),
	CODE_1001(1001, "请求过于频繁,请稍后再试", "Request too frequent, please try again later"),
	CODE_1002(1002, "当前访问人数过多,请稍后重试", "Too many visitors, please try again later"),
	CODE_1003(1003, "操作失败，请稍后重试", "Operation failed, please try again later"),
	CODE_1004(1004, "登录信息已过期,请重新登录", "Login information expired, please login again"),
	CODE_1005(1005, "您已在其他设备登录,请重新登录后访问", "You have logged in on another device, please login again to access"),
	CODE_1007(1007, "用户不存在", "User does not exist"),
	CODE_1010(1010, "邀请用户不存在", "Invited user does not exist"),
	CODE_1011(1011, "验证码已过期，请重新获取", "Verification code expired, please obtain a new one"),
	CODE_1012(1012, "请登录后访问", "Please login to access"),
	CODE_1015(1015, "余额不足", "Insufficient balance"),

	//PRICE_GET_FAIL(1022, "获取价格失败", "Failed to get price"),
	CODE_1024(1024, "平台处于结算时间：{0},为了保证广大用户权益，不允许交易,请耐心等待!", "Platform is in settlement time: {0}, trading is not allowed to ensure user rights, please wait patiently!"),

	CODE_1053(1053, "客户端ID有误", "Incorrect client ID"),
	RANDOM_NOT_EXIT(1054, "随机数不存在", "Random number does not exist"),
	SIGN_VALIDATE_ERROR(1055, "签名错误", "Signature error"),
	CODE_1064(1064, "该推荐人的层级过深，请换其他推荐人", "This referrer's level is too deep, please choose another referrer"),
	CODE_1066(1066, "账号已被冻结", "Account has been frozen"),

	CODE_1070(1070, "手续费异常", "Abnormal fee"),
	//CODE_1073(1073, "接收用户不存在", "Receiving user does not exist"),
	//CODE_1074(1074, "转账失败:不能转给自己", "Transfer failed: cannot transfer to yourself"),


	//CODE_1084(1084, "转账通道暂时关闭", ""),
	//CODE_1089(1089, "最少%s起转", ""),
	//CODE_1101(1101, "仅支持同线互转", ""),



	//CODE_1102(1102, "账号不合法", ""),
	CODE_1103(1103, "账号已存在", ""),
	CODE_1104(1104, "邀请用户不存在", "Invited user does not exist"),
	CODE_1106(1106, "账号已被冻结", "Account has been frozen"),
	CODE_1108(1108, "提现通道暂时关闭", ""),
	CODE_1109(1109, "最少%s起提", ""),
	CODE_1116(1116, "注册即使通讯IM失败", ""),

	CODE_1117(1117, "暂未开通", ""),





	//CODE_1207(1207, "活期基金需满1天后才能赎回", ""),
	//CODE_1209(1209, "可领取金额为0", ""),

	//新增提示语
	//CODE_1213(1213, "邮箱发送异常", ""),
	//CODE_1214(1214, "邮箱已被绑定", ""),
	CODE_1215(1215, "邮箱格式不正确", ""),


	//新加得
	//CODE_1222(1222, "请求参数异常", ""),
	//CODE_1223(1223, "小于最低质押金额", ""),
	//CODE_1224(1224, "订单不存在", ""),
	//CODE_1225(1225, "订单已被支付", ""),
	//CODE_1226(1226, "目标释放天数必须小于当前订单剩余释放天数", ""),


	//新加的
//	CODE_1238(1237, "用户已经激活", ""),
//	CODE_1239(1239, "活动未开启", ""),
//	CODE_1240(1240, "用户未激活", ""),
//	CODE_1241(1241, "今日已经领取过了", ""),
//	CODE_1242(1242, "获取价格异常", ""),
//	CODE_1243(1243, "可提现额度不足", ""),
//	CODE_1244(1244, "激活订单等待支付", ""),
//	CODE_1245(1245, "空投订单等待支付", ""),
//	CODE_1246(1246, "本轮空投已领完", ""),



	//新增业务
//	CODE_1247(1247, "节点不存在", ""),
//	CODE_1248(1248, "节点已经购买了", ""),
//	CODE_1249(1249, "节点库存不足", ""),
//	CODE_1250(1250, "节点订单等待支付", ""),


	CODE_1251(1251, "转账通道暂时关闭", ""),
	CODE_1252(1252, "少于最低转账金额", ""),
	CODE_1253(1253, "钱包地址不存在", ""),
	CODE_1254(1254, "不能转账给自己", ""),

	CODE_1255(1255, "划转地址不存在", ""),
	CODE_1256(1256, "手机号有误", ""),
	CODE_1257(1257, "输入数据过长", ""),
	CODE_1258(1258, "获取价格异常", ""),
	CODE_1259(1259, "库存不足", ""),

	CODE_1260(1260, "质押金额不能低于最低限制", ""),
	CODE_1261(1261, "质押金额不能高于最高限制", ""),
	CODE_1262(1262, "质押金额必须为 500 的整数倍", ""),
	CODE_1263(1263, "超过最大单日创建下单数", ""),

	//新增的
	CODE_1264(1264, "商品不存在", ""),
	CODE_1265(1265, "请选择收货地址", ""),
	CODE_1266(1266, "商品已下架", ""),
	CODE_1267(1267, "最少一个起购", ""),
	CODE_1075(1075, "请先支付费用"),

	CODE_1268(1268, "该用户已拥有节点", ""),
	CODE_1269(1269, "节点已下架", ""),
	CODE_1270(1270, "请先完成上一笔订单或者等待过期", ""),
	CODE_1271(1271, "生成AFI质押记录失败", ""),
	CODE_1272(1272, "托管订单已绑定AFI加速", ""),
	CODE_1273(1273, "AFI质押比例配置错误", ""),
	CODE_1274(1274, "AFI加速倍率配置错误", ""),
	CODE_1275(1275, "AFI质押加速套餐不存在或未启用", ""),

	CODE_1276(1276, "AFI质押加速套餐不能为空", ""),

	CODE_1277(1277, "托管订单不能为空", ""),
	CODE_1278(1278, "只有产出中托管订单可以质押AFI", ""),
	CODE_1279(1279, "只有1天及以上托管套餐可以质押AFI", ""),
	CODE_1280(1280, "托管订单已绑定AFI加速", ""),
	CODE_1281(1281, "AFI价格必须大于0", ""),
	CODE_1282(1282, "AFI价格未配置", ""),
	CODE_1283(1283, "托管订单金额错误", ""),
	CODE_1284(1284, "get coin price error", ""),


	CODE_107(107, "IP黑名单拦截", ""),
	CODE_108(108, "数据格式异常", "Abnormal data format"),
	CODE_109(109, "业务数据不允许重复", "Business data duplication not allowed"),
	CODE_110(110, "底层数据格式异常", "Abnormal underlying data format"),
	CODE_111(111, "底层数据NPE异常", "Underlying data NPE exception"),
	CODE_112(112, "底层数据转换异常", "Underlying data conversion exception"),
	CODE_113(113, "第三方配置信息有误/网络异常", "Third-party configuration error/Network exception"),
	CODE_114(114, "资源读取有误，请稍候再试", "Resource reading error, please try again later"),
	CODE_115(115, "数据格式有误，请重新输入", "Incorrect data format, please re-enter"),
	CODE_116(116, "账号已经存在", "Account already exists"),
	VALIDATE_CODE_ERROR(117, "验证码错误", "Verification code error");

	private static final ThreadLocal<String> LANG_CONTEXT = new ThreadLocal<>();
	private static final String DEFAULT_LANG = "zh";
	private static final String LANG_ZH = "zh";
	private static final String LANG_ZH_TW = "zh-TW";
	private static final String LANG_EN = "en";
	private static final String LANG_JA = "ja";
	private static final String LANG_KO = "ko";
	private static final String LANG_MY = "my";
	private static final String LANG_VI = "vi";
	private final Map<String, String> langMsgMap = new HashMap<>();
	private int code;

	ResponseCode(int code, String msg) {
		this(code, msg, msg);
	}

	ResponseCode(int code, String zhMsg, String enMsg, String... extraLangMsgs) {
		this.code = code;
		initLangMessages(zhMsg, enMsg);
		applyAdditionalLangs(extraLangMsgs);
	}

	public static void setLang(String lang) {
		if (StrUtil.isBlank(lang)) {
			LANG_CONTEXT.set(DEFAULT_LANG);
		} else {
			LANG_CONTEXT.set(lang);
		}
	}

	public static String getCurrentLang() {
		return LANG_CONTEXT.get();
	}

	public static void clearLang() {
		LANG_CONTEXT.remove();
	}

	public static ResponseCode changeMethod(int code) {
		return Enum.valueOf(ResponseCode.class, "CODE_" + code);
	}

	public static ResultPista<?> getR(ResponseCode respCode) {
		return ResultPista.fail(respCode.getCode(), respCode.getMsg());
	}

	private void initLangMessages(String zhMsg, String enMessage) {
		langMsgMap.put(LANG_ZH, zhMsg);
		langMsgMap.put(LANG_ZH_TW, zhMsg);
		langMsgMap.put(LANG_EN, StrUtil.blankToDefault(enMessage, zhMsg));
		langMsgMap.put(LANG_JA, zhMsg);
		langMsgMap.put(LANG_KO, zhMsg);
		langMsgMap.put(LANG_MY, zhMsg);
		langMsgMap.put(LANG_VI, zhMsg);
	}

	private void applyAdditionalLangs(String... extraLangMsgs) {
		if (extraLangMsgs == null || extraLangMsgs.length == 0) {
			return;
		}
		setLangIfPresent(LANG_ZH_TW, extraLangMsgs, 0);
		setLangIfPresent(LANG_JA, extraLangMsgs, 1);
		setLangIfPresent(LANG_KO, extraLangMsgs, 2);
		setLangIfPresent(LANG_MY, extraLangMsgs, 3);
		setLangIfPresent(LANG_VI, extraLangMsgs, 4);
	}

	private void setLangIfPresent(String langKey, String[] values, int index) {
		if (values.length > index && StrUtil.isNotBlank(values[index])) {
			langMsgMap.put(langKey, values[index]);
		}
	}

	private String normalizeLang(String lang) {
		if (StrUtil.isBlank(lang)) {
			return DEFAULT_LANG;
		}
		String lower = lang.toLowerCase(Locale.ROOT);
		if (lower.startsWith("zh-tw")) {
			return LANG_ZH_TW;
		}
		if (lower.startsWith("zh")) {
			return LANG_ZH;
		}
		if (lower.startsWith("en")) {
			return LANG_EN;
		}
		if (lower.startsWith("ja")) {
			return LANG_JA;
		}
		if (lower.startsWith("ko")) {
			return LANG_KO;
		}
		if (lower.startsWith("my") || lower.startsWith("ms")) {
			return LANG_MY;
		}
		if (lower.startsWith("vi")) {
			return LANG_VI;
	}
		return DEFAULT_LANG;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getMsg() {
		String lang = normalizeLang(LANG_CONTEXT.get());
		String value = langMsgMap.get(lang);
		if (StrUtil.isBlank(value)) {
			value = langMsgMap.get(DEFAULT_LANG);
		}
		return value;
	}

	public void setMsg(String msg) {
		langMsgMap.put(LANG_ZH, msg);
	}

	public String getMsg(String lang) {
		String normalized = normalizeLang(lang);
		return StrUtil.blankToDefault(langMsgMap.get(normalized), langMsgMap.get(DEFAULT_LANG));
	}

	public String getEnMsg() {
		return langMsgMap.get(LANG_EN);
	}

	public void setEnMsg(String enMsg) {
		langMsgMap.put(LANG_EN, enMsg);
	}

	public ResponseCode setZhTwMsg(String msg) {
		langMsgMap.put(LANG_ZH_TW, msg);
		return this;
	}

	public ResponseCode setJaMsg(String msg) {
		langMsgMap.put(LANG_JA, msg);
		return this;
	}

	public ResponseCode setKoMsg(String msg) {
		langMsgMap.put(LANG_KO, msg);
		return this;
	}

	public ResponseCode setMyMsg(String msg) {
		langMsgMap.put(LANG_MY, msg);
		return this;
	}

	public ResponseCode setViMsg(String msg) {
		langMsgMap.put(LANG_VI, msg);
		return this;
	}
}
