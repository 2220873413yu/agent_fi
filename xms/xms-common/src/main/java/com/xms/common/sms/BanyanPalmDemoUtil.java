package com.xms.common.sms;

import java.net.URLEncoder;

import com.xms.common.core.domain.AjaxResult;
import com.xms.common.core.domain.api.ResultPista;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;

import com.alibaba.fastjson.JSONObject;
import com.xms.common.result.ResponseCode;
import com.xms.common.result.ResponseWrap;

public class BanyanPalmDemoUtil {

	public static final String url = "http://api.1cloudsp.com/api/v2/single_send";
	public static final String accesskey = "gJ4ZzfY0t02aL7He";
    public static final String accessSecret = "eVbpeC2E4RfYpQs0rJ6vXjEgkEoajydf";
    public static final String sign = "289592";
    public static final String templateId = "254825";

	public static ResultPista<String> sendMesg(String account, String code) {
		try {
			HttpClient httpClient = new HttpClient();
	        PostMethod postMethod = new PostMethod(url);
	        postMethod.getParams().setContentCharset("UTF-8");
	        postMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,new DefaultHttpMethodRetryHandler());


	        NameValuePair[] data = {
	                new NameValuePair("accesskey", accesskey),
	                new NameValuePair("secret", accessSecret),
	                new NameValuePair("sign", sign), //签名ID
	                new NameValuePair("templateId", templateId), //模版ID
	                new NameValuePair("mobile", account), //手机号
	                new NameValuePair("content", URLEncoder.encode(code, "utf-8"))//（验证码）
	        };
	        postMethod.setRequestBody(data);
	        postMethod.setRequestHeader("Connection", "close");

	        int statusCode = httpClient.executeMethod(postMethod);
	        System.out.println("statusCode: " + statusCode + ", body: "
	                    + postMethod.getResponseBodyAsString());
	        if(statusCode != 200){
				return ResultPista.fail(ResponseCode.CODE_1000.getCode(), "短信发送异常");
	        }
	        JSONObject body = JSONObject.parseObject(postMethod.getResponseBodyAsString());
	        if("0".equals(body.getString("code"))) {
				return ResultPista.success();
	        }else {
				return ResultPista.fail(ResponseCode.CODE_1000.getCode(), body.getString("msg"));
			}
		}catch (Exception e) {
			e.printStackTrace();
			return ResultPista.fail(ResponseCode.CODE_1000.getCode(), "短信发送异常");
		}
	 }

	public static AjaxResult sendMessage(String account, String code) {
		try {
			HttpClient httpClient = new HttpClient();
			PostMethod postMethod = new PostMethod(url);
			postMethod.getParams().setContentCharset("UTF-8");
			postMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,new DefaultHttpMethodRetryHandler());


			NameValuePair[] data = {
				new NameValuePair("accesskey", accesskey),
				new NameValuePair("secret", accessSecret),
				new NameValuePair("sign", sign), //签名ID
				new NameValuePair("templateId", templateId), //模版ID
				new NameValuePair("mobile", account), //手机号
				new NameValuePair("content", URLEncoder.encode(code, "utf-8"))//（验证码）
			};
			postMethod.setRequestBody(data);
			postMethod.setRequestHeader("Connection", "close");

			int statusCode = httpClient.executeMethod(postMethod);
			System.out.println("statusCode: " + statusCode + ", body: "
				+ postMethod.getResponseBodyAsString());
			if(statusCode != 200){
				return AjaxResult.error(ResponseCode.CODE_1000.getCode(),"短信发送异常");
			}
			JSONObject body = JSONObject.parseObject(postMethod.getResponseBodyAsString());
			if("0".equals(body.getString("code"))) {
				return AjaxResult.success();
			}else {
				return AjaxResult.error(ResponseCode.CODE_1000.getCode(),body.getString("msg"));
			}
		}catch (Exception e) {
			e.printStackTrace();
			return AjaxResult.error(ResponseCode.CODE_1000.getCode(),"短信发送异常");
		}
	}
}
