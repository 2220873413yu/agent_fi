package com.xms.web.util;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;

/**
 * Gmail发送测试工具
 */
public class GmailTestUtil {
    
    public static void main(String[] args) {
        // Gmail配置
        final String username = "sigmaprono1@gmail.com";
        final String password = "tcgrtuduozlvakbx";
        final String toEmail = "2220873413@qq.com";
        
        System.out.println("开始Gmail发送测试...");
        System.out.println("发件人: " + username);
        System.out.println("收件人: " + toEmail);
        
        // 方法1：尝试587端口 + STARTTLS
        System.out.println("\n=== 方法1: 587端口 + STARTTLS ===");
        testMethod1(username, password, toEmail);
        
        // 方法2：尝试465端口 + SSL
        System.out.println("\n=== 方法2: 465端口 + SSL ===");
        testMethod2(username, password, toEmail);
        
        // 方法3：尝试简化配置
        System.out.println("\n=== 方法3: 简化配置 ===");
        testMethod3(username, password, toEmail);
    }
    
    // 方法1：587端口 + STARTTLS
    private static void testMethod1(String username, String password, String toEmail) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");
        props.put("mail.debug", "true");
        
        sendMail(props, username, password, toEmail, "测试邮件 - 方法1");
    }
    
    // 方法2：465端口 + SSL
    private static void testMethod2(String username, String password, String toEmail) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.ssl.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "465");
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");
        props.put("mail.debug", "true");
        
        sendMail(props, username, password, toEmail, "测试邮件 - 方法2");
    }
    
    // 方法3：简化配置
    private static void testMethod3(String username, String password, String toEmail) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.starttls.enable", "true");
        
        sendMail(props, username, password, toEmail, "测试邮件 - 方法3");
    }
    
    // 通用发送方法
    private static void sendMail(Properties props, String username, String password, String toEmail, String subject) {
        try {
            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });
            
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject(subject);
            message.setText("这是一封测试邮件，发送时间：" + new java.util.Date());
            
            System.out.println("正在发送邮件...");
            Transport.send(message);
            System.out.println("✅ 邮件发送成功！");
            
        } catch (Exception e) {
            System.out.println("❌ 发送失败: " + e.getMessage());
            if (e.getCause() != null) {
                System.out.println("原因: " + e.getCause().getMessage());
            }
        }
    }
} 