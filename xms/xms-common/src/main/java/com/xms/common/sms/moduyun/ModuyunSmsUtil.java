package com.xms.common.sms.moduyun;

import com.xms.common.core.domain.api.ResultPista;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author MIER
 */
@Component
@Slf4j
@ConditionalOnProperty(value = "sms.enabled", havingValue = "true")
public class ModuyunSmsUtil {
	@Value("${sms.url}")
	@Deprecated
	private String url;
	@Value("${sms.accesskey}")
	private String accesskey;
	@Value("${sms.accessSecret}")
	private String accessSecret;
	@Value("${sms.sign}")
	private String sign;
	@Value("${sms.templateId}")
	private String templateId;

	private String password="4f703fd22f6a2c77af97fc36eeed4bca";
	private String spId="593392";

	/**
	 * 发送短信测试案例
	 * @param args
	 */
	public static void main1(String[] args) {
		String content="005232";
		String spId="593392";
		String password="4f703fd22f6a2c77af97fc36eeed4bca";
		String mobile="17606020554";
			// 短信接口地址
			String url = "http://120.79.113.147:9511/api/send-sms-single";
			String msgContent=String.format("【易达】您的验证码为：%s,如非本人操作,请忽略.",content);
			// 构建请求参数
			Map<String, Object> paramMap = new HashMap<>();
			paramMap.put("sp_id", spId);
			paramMap.put("mobile", mobile);
			paramMap.put("content", msgContent);
			// 对密码进行md5处理
			paramMap.put("password",password);

			// 使用HttpRequest设置超时时间
			cn.hutool.http.HttpRequest request = cn.hutool.http.HttpUtil.createPost(url)
				.form(paramMap)
				.setConnectionTimeout(5000)
				.setReadTimeout(10000);

			// 发送请求
			cn.hutool.http.HttpResponse response = request.execute();

			// 检查HTTP状态码
			if (!response.isOk()) {
				log.error("短信发送失败，HTTP状态码：{}", response.getStatus());

			}

			// 获取响应内容
			String result = response.body();
			log.info("短信发送结果：{}", result);

			// 解析JSON响应
			cn.hutool.json.JSONObject jsonResult = cn.hutool.json.JSONUtil.parseObj(result);
			int code = jsonResult.getInt("code");

			// 判断是否发送成功
			if (code == 0) {
				log.info("短信发送成功，消息ID：{}", jsonResult.getStr("msg_id"));

			} else {
				log.error("短信发送失败，错误码：{}，错误信息：{}", code, jsonResult.getStr("msg"));

			}
	}

	public ResultPista sendMesg(String account, String code) {
		try {
			//请根据实际 accesskey 和 secretkey 进行开发，以下只作为演示 sdk 使用
			//type:0普通短信 1营销短信
			int type = 0;
			//国家区号
			String nationcode = "86";
			//短信模板的变量值 ，将短信模板中的变量{1},{2}替换为参数中的值
			List<String> params = new ArrayList<>();
			//模板中存在多个可变参数，可以添加对应的值。
			params.add(code);
			//自定义字段，用户可以根据自己的需要来使用
			String ext = "";
			//初始化单发
			SmsSingleSender singleSender = new SmsSingleSender(accesskey, accessSecret);
			//普通单发,注意前面必须为【】符号包含，置于头或者尾部。
			SmsSingleSenderResult result = singleSender.send(type, nationcode, account, sign, templateId, params, ext);
			if (result.result == 0) {
				return ResultPista.success();
			}
			return ResultPista.fail(result.getErrMsg());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ResultPista.fail();
	}

	/**
	 * 发送短信
	 * @param mobile 手机号
	 * @param content 短信内容
	 * @return 是否发送成功
	 */
	public ResultPista sendSms(String mobile, String content) {
		try {
			// 短信接口地址
			String url = "http://120.79.113.147:9511/api/send-sms-single";
			String msgContent=String.format("【易达】您的验证码为：%s,如非本人操作,请忽略.",content);
			// 构建请求参数
			Map<String, Object> paramMap = new HashMap<>();
			paramMap.put("sp_id", spId);
			paramMap.put("mobile", mobile);
			paramMap.put("content", msgContent);
			// 对密码进行md5处理
			paramMap.put("password",password);

			// 使用HttpRequest设置超时时间
			cn.hutool.http.HttpRequest request = cn.hutool.http.HttpUtil.createPost(url)
				.form(paramMap)
				.setConnectionTimeout(5000)
				.setReadTimeout(10000);

			// 发送请求
			cn.hutool.http.HttpResponse response = request.execute();

			// 检查HTTP状态码
			if (!response.isOk()) {
				log.error("短信发送失败，HTTP状态码：{}", response.getStatus());
				return ResultPista.fail();
			}

			// 获取响应内容
			String result = response.body();
			log.info("短信发送结果：{}", result);

			// 解析JSON响应
			cn.hutool.json.JSONObject jsonResult = cn.hutool.json.JSONUtil.parseObj(result);
			int code = jsonResult.getInt("code");

			// 判断是否发送成功
			if (code == 0) {
				log.info("短信发送成功，消息ID：{}", jsonResult.getStr("msg_id"));
				return ResultPista.success();
			} else {
				log.error("短信发送失败，错误码：{}，错误信息：{}", code, jsonResult.getStr("msg"));
				return ResultPista.fail();
			}

		} catch (Exception e) {
			log.error("短信发送异常", e);
			return ResultPista.fail();
		}
	}
}
