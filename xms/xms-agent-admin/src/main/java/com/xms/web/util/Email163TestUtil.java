package com.xms.web.util;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;

/**
 * 163邮箱发送测试工具
 * 163邮箱在国内网络环境下更稳定
 */
public class Email163TestUtil {
    
    public static void main(String[] args) {
        // 163邮箱配置（需要你注册163邮箱并开启SMTP服务）
        final String username = "your_163_email@163.com"; // 替换为你的163邮箱
        final String password = "your_163_auth_code"; // 替换为你的163邮箱授权码
        final String toEmail = "2220873413@qq.com";
        
        System.out.println("开始163邮箱发送测试...");
        System.out.println("发件人: " + username);
        System.out.println("收件人: " + toEmail);
        System.out.println("\n请先确保：");
        System.out.println("1. 已注册163邮箱");
        System.out.println("2. 已在163邮箱设置中开启SMTP服务");
        System.out.println("3. 已获取授权码（不是登录密码）");
        
        // 163邮箱SMTP配置
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.163.com");
        props.put("mail.smtp.port", "25"); // 163默认端口25
        props.put("mail.debug", "true");
        
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
            message.setSubject("163邮箱测试邮件");
            message.setText("这是通过163邮箱发送的测试邮件，发送时间：" + new java.util.Date());
            
            System.out.println("\n正在发送邮件...");
            Transport.send(message);
            System.out.println("✅ 163邮箱发送成功！");
            
        } catch (Exception e) {
            System.out.println("❌ 163邮箱发送失败: " + e.getMessage());
            System.out.println("\n解决步骤：");
            System.out.println("1. 访问 mail.163.com 注册邮箱");
            System.out.println("2. 登录后进入设置 -> POP3/SMTP/IMAP");
            System.out.println("3. 开启SMTP服务");
            System.out.println("4. 获取授权码（会通过短信发送）");
            System.out.println("5. 将上面的username和password替换为你的实际信息");
        }
    }
} 