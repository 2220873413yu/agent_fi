package com.xms.app.util;

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
	public static void sendMail(MailInfo mailInfo) throws Exception {

		// 设置邮件服务器的属性
		Properties properties = new Properties();
//		properties.put("mail.smtp.host", "smtp.gmail.com");
//		properties.put("mail.smtp.port", "587");
//		properties.put("mail.smtp.auth", "true");
//		properties.put("mail.smtp.ssl.enable", "true");

		properties.put("mail.smtp.auth", "true");
//	properties.put("mail.smtp.starttls.enable", "true");
		properties.put("mail.smtp.host", "smtp.gmail.com");
		properties.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
		properties.setProperty("mail.smtp.socketFactory.fallback", "false");
		properties.setProperty("mail.smtp.port", "465");
		properties.setProperty("mail.smtp.socketFactory.port", "465");

		// 获取会话对象
		Session session = Session.getInstance(properties, new Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(mailInfo.getUsername(), mailInfo.getPassword());
			}
		});

		// 创建邮件消息
		MimeMessage message = new MimeMessage(session);
		message.setFrom(new InternetAddress(mailInfo.getUsername()));
		message.addRecipient(Message.RecipientType.TO, new InternetAddress(mailInfo.getToUser()));
		message.setSubject(mailInfo.getSubject());
		message.setText(mailInfo.getContent());

		// 发送邮件
		Transport.send(message);
		log.debug("收件者 {} ,邮件发送成功！", mailInfo.getToUser());

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

}
