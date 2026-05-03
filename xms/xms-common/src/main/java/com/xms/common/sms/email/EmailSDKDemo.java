package com.xms.common.sms.email;


public class EmailSDKDemo {
    public static void main(String[] args) {

		//EmailSDKDemoUtil.sendMesg("1370584959@qq.com","123456");
//    	try {
//    		//请根据实际 accesskey 和 secretkey 进行开发，以下只作为演示 sdk 使用
//    		String accesskey = "6452108b199a6253d9b3c032";
//    		String secretkey = "66db3836907c4ff8bee9a4f76aa4579a";
//
//    		//44,73
//
//    		// 邮件类型，0 事务投递，其他的为商业投递量
//    		int type=0;
//    		// 拓展字段
//    		String ext="";
//    		// 必须 管理控制台中配置的发信地址(登陆后台查看发信地址)
//    		String fromEmail="email@microchains.info";
//    		//// 必须 ,如果为true是的时候，replyEmail必填，false的时候replyEmail可以为空
//    		Boolean needToReply=false;
//    		 // 如果needToReply为true是的时候,则为必填,false的时候replyEmail可以为空
//    		String replyEmail="";
//
//    		// 必须 目标邮件地址
//    		String toEmail="xxx@qq.com";
//
//    		//// 可选 发信人昵称,
//    		String fromAlias="摩杜云";
//    		// 必须 邮件主题。
//    		String subject="邮件验证码";
//    		// 必须 邮件 html 正文。
//    		String htmlBody="<h1>尊敬的客户您好。您本次的验证码为：xxx</h1>";
//    		// 可选 取值范围 0~1: 1 为打开数据跟踪功能; 0 为关闭数据跟踪功能。该参数默认值为
//    		String clickTrace="0";
//
//	    	EmailSingleSender singleSender = new EmailSingleSender(accesskey, secretkey);
//	    	EmailSingleSenderResult singleSenderResult=singleSender.send(type, fromEmail, toEmail, fromAlias, needToReply, replyEmail, subject, htmlBody, clickTrace, ext);
//	    	System.out.println(singleSenderResult);
//    	} catch (Exception e) {
//			e.printStackTrace();
//		}
    }
}
