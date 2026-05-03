package com.xms.web.util;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

/**
 * @author: GT63S
 * @createDate: 2025/2/20
 */
@Slf4j
public class AliyunSenMailUtil {
	public static void main(String[] args) {
		// 发件人邮箱和授权码（注意：不是Gmail密码，而是应用专用密码）
		final String username = "sigmaprono1@gmail.com";
		final String password = "yjmcryfrjaencgvz";

		// 收件人邮箱
		String toEmail = "17606020554@163.com";

		// 设置邮件服务器属性
		Properties props = new Properties();
//		props.put("mail.smtp.auth", "true");
//		props.put("mail.smtp.starttls.enable", "true");
//		props.put("mail.smtp.host", "smtp.gmail.com");
		//props.put("mail.smtp.port", "587");

		props.put("mail.smtp.auth", "true");
//	props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", "smtp.gmail.com");
		props.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
		props.setProperty("mail.smtp.socketFactory.fallback", "false");
		props.setProperty("mail.smtp.port", "465");
		props.setProperty("mail.smtp.socketFactory.port", "465");

		// 创建会话
		Session session = Session.getInstance(props, new Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(username, password);
			}
		});

		try {
			// 创建邮件消息
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(username));
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
			message.setSubject("测试邮件");
			message.setText("这是一封测试邮件，通过Java程序发送。");

			// 发送邮件
			Transport.send(message);

			System.out.println("邮件发送成功！");

		} catch (MessagingException e) {
			System.out.println("发送邮件时出错：" + e.getMessage());
			e.printStackTrace();
		}
	}

	//来个内部bean类
	@Data
	public static class MailInfo {
		private String username;
		private String password;
		private String host;
		private String toUser;
		/**
		 * 主题
		 */
		private String subject;
		/**
		 * 文本
		 */
		private String content;

	}
	//	public static void main(String[] args) throws Exception{
//		// 发件人邮箱和密码
//		 final String username = "sigmaprono2@gmail.com";
//		final String password = "qabpwphrgzmlowlo";
//		//
//		// // 收件人邮箱
//		// String toUser = "291312408@qq.com";
//		//
//		// // SMTP 服务器地址和端口
//		// String host = "smtpdm-ap-southeast-1.aliyun.com";
//		int port = 465; // 使用 SSL 加密端口
//
//		// 设置邮件服务器的属性
//		Properties properties = new Properties();
//		properties.put("mail.smtp.host", "smtpdm-ap-southeast-1.aliyun.com");
//		properties.put("mail.smtp.port", port);
//		properties.put("mail.smtp.auth", "true");
//		properties.put("mail.smtp.ssl.enable", "true");
//
//		// 获取会话对象
//		Session session = Session.getInstance(properties, new Authenticator() {
//			@Override
//			protected PasswordAuthentication getPasswordAuthentication() {
//				return new PasswordAuthentication(username, password);
//			}
//		});
//
//		// 创建邮件消息
//		MimeMessage message = new MimeMessage(session);
//		message.setFrom(new InternetAddress(username));
//		message.addRecipient(Message.RecipientType.TO, new InternetAddress("17606020554@163.com"));
//		message.setSubject("subject");
//		message.setText("text");
//
//		// 发送邮件
//		Transport.send(message);
//		System.out.println("收件者 {} ,邮件发送成功！17606020554@163.com");
//	}

}
