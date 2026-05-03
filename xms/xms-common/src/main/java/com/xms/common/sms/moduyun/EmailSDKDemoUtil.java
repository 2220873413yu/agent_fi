package com.xms.common.sms.moduyun;

import com.xms.common.core.domain.api.ResultPista;
import com.xms.common.sms.email.EmailSingleSender;
import com.xms.common.sms.email.EmailSingleSenderResult;



public class EmailSDKDemoUtil {
	private static  final String accesskey ="67d9173c98154c41e4e371d7";
	private static  final String secretkey ="8e3298096dac4a08a31f3694c33e7a1c";
	private static  final String fromEmail ="email@rwa.gylk.xyz";

	//	public static void main(String[] args) {
//		System.out.println(sendMesg("17606020554@163.com","6666"));
//	}
	public static ResultPista sendMesg(String email, String code) {
		try {

			//44,73

			// 邮件类型，0 事务投递，其他的为商业投递量
			int type = 0;
			// 拓展字段
			String ext = "";
			// 必须 管理控制台中配置的发信地址(登陆后台查看发信地址)
			//// 必须 ,如果为true是的时候，replyEmail必填，false的时候replyEmail可以为空
			Boolean needToReply = false;
			// 如果needToReply为true是的时候,则为必填,false的时候replyEmail可以为空
			String replyEmail = "";

			//// 可选 发信人昵称,
			String fromAlias = "FSNDAO验证码";
			// 必须 邮件主题。
			String subject = "邮件验证码";
			// 必须 邮件 html 正文。
			String htmlBody = "<h1>尊敬的客户您好。您本次的验证码为：" + code + "</h1>";
			// 可选 取值范围 0~1: 1 为打开数据跟踪功能; 0 为关闭数据跟踪功能。该参数默认值为
			String clickTrace = "0";

			EmailSingleSender singleSender = new EmailSingleSender(accesskey, secretkey);
			EmailSingleSenderResult singleSenderResult = singleSender.send(type, fromEmail, email, fromAlias, needToReply, replyEmail, subject, htmlBody, clickTrace, ext);
			System.out.println(singleSenderResult);
			return ResultPista.success("发送邮件成功!");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ResultPista.fail("发送失败");
	}
}
