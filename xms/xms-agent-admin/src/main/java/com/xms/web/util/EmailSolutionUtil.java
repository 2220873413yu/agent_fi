package com.xms.web.util;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;

/**
 * 邮箱发送完整解决方案
 * 解决Gmail在中国大陆无法使用的问题
 */
public class EmailSolutionUtil {
    
    public static void main(String[] args) {
        System.out.println("=================================");
        System.out.println("      邮箱发送完整解决方案");
        System.out.println("=================================");
        
        // 收件人邮箱
        final String toEmail = "2220873413@qq.com";
        
        System.out.println("\n【问题分析】");
        System.out.println("1. Gmail在中国大陆完全被封锁（2014年12月起）");
        System.out.println("2. 包括POP3、SMTP、IMAP协议都无法使用");
        System.out.println("3. 这不是配置问题，而是网络环境问题");
        
        System.out.println("\n【解决方案】");
        System.out.println("方案1: 使用VPN + Gmail");
        System.out.println("方案2: 使用国内邮箱服务（推荐）");
        
        // 测试Gmail（预期失败）
        System.out.println("\n=== 测试1: Gmail（预期在中国大陆失败）===");
        testGmail(toEmail);
        
        // 测试163邮箱
        System.out.println("\n=== 测试2: 163邮箱（推荐）===");
        test163Email(toEmail);
        
        // 测试QQ邮箱
        System.out.println("\n=== 测试3: QQ邮箱 ===");
        testQQEmail(toEmail);
        
        // 显示配置指南
        showConfigurationGuide();
    }
    
    // Gmail测试（在中国大陆预期失败）
    private static void testGmail(String toEmail) {
        final String username = "sigmaprono1@gmail.com";
        final String password = "tcgrtuduozlvakbx";
        
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");
        props.put("mail.smtp.connectiontimeout", "10000");
        props.put("mail.smtp.timeout", "10000");
        
        sendTestEmail(props, username, password, toEmail, "Gmail测试", "Gmail");
    }
    
    // 163邮箱测试
    private static void test163Email(String toEmail) {
        // 这里需要你注册163邮箱并配置
        final String username = "your_email@163.com"; // 需要替换
        final String password = "your_auth_code"; // 需要替换为授权码
        
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.163.com");
        props.put("mail.smtp.port", "25");
        props.put("mail.debug", "false");
        
        if (username.equals("your_email@163.com")) {
            System.out.println("❌ 163邮箱未配置");
            System.out.println("请按照下面的指南配置163邮箱");
            return;
        }
        
        sendTestEmail(props, username, password, toEmail, "163邮箱测试", "163邮箱");
    }
    
    // QQ邮箱测试
    private static void testQQEmail(String toEmail) {
        // 这里需要你配置QQ邮箱
        final String username = "your_qq@qq.com"; // 需要替换
        final String password = "your_qq_auth_code"; // 需要替换为授权码
        
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.qq.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.debug", "false");
        
        if (username.equals("your_qq@qq.com")) {
            System.out.println("❌ QQ邮箱未配置");
            System.out.println("请按照下面的指南配置QQ邮箱");
            return;
        }
        
        sendTestEmail(props, username, password, toEmail, "QQ邮箱测试", "QQ邮箱");
    }
    
    // 通用发送方法
    private static void sendTestEmail(Properties props, String username, String password, 
                                    String toEmail, String subject, String provider) {
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
            message.setText("这是通过" + provider + "发送的测试邮件\n发送时间：" + new java.util.Date());
            
            System.out.println("正在发送邮件...");
            Transport.send(message);
            System.out.println("✅ " + provider + "发送成功！");
            
        } catch (Exception e) {
            System.out.println("❌ " + provider + "发送失败: " + e.getMessage());
            
            // 特殊错误处理
            if (e.getMessage().contains("Connection timed out") || 
                e.getMessage().contains("EOF") ||
                e.getMessage().contains("Network is unreachable")) {
                System.out.println("   → 网络连接问题，可能被防火墙阻断");
            } else if (e.getMessage().contains("Authentication failed")) {
                System.out.println("   → 认证失败，请检查用户名和密码");
            }
        }
    }
    
    // 显示配置指南
    private static void showConfigurationGuide() {
        System.out.println("\n=================================");
        System.out.println("        配置指南");
        System.out.println("=================================");
        
        System.out.println("\n【方案1: 使用VPN + Gmail】");
        System.out.println("1. 购买可靠的VPN服务（如ExpressVPN、NordVPN）");
        System.out.println("2. 连接到海外服务器");
        System.out.println("3. 然后使用Gmail配置");
        System.out.println("   - SMTP: smtp.gmail.com:587");
        System.out.println("   - 需要应用专用密码，不是登录密码");
        
        System.out.println("\n【方案2: 163邮箱（推荐）】");
        System.out.println("1. 访问 mail.163.com 注册邮箱");
        System.out.println("2. 登录后进入 设置 → POP3/SMTP/IMAP");
        System.out.println("3. 开启SMTP服务");
        System.out.println("4. 获取授权码（通过短信）");
        System.out.println("5. 配置:");
        System.out.println("   - SMTP: smtp.163.com:25");
        System.out.println("   - 用户名: 完整邮箱地址");
        System.out.println("   - 密码: 授权码（不是登录密码）");
        
        System.out.println("\n【方案3: QQ邮箱】");
        System.out.println("1. 登录 mail.qq.com");
        System.out.println("2. 进入 设置 → 账户");
        System.out.println("3. 开启SMTP服务");
        System.out.println("4. 获取授权码");
        System.out.println("5. 配置:");
        System.out.println("   - SMTP: smtp.qq.com:587");
        System.out.println("   - 用户名: 完整邮箱地址");
        System.out.println("   - 密码: 授权码（不是登录密码）");
        
        System.out.println("\n【为什么Gmail不工作？】");
        System.out.println("• Gmail在中国大陆被完全封锁");
        System.out.println("• 包括所有协议：SMTP、POP3、IMAP");
        System.out.println("• 始于2014年12月，至今未解除");
        System.out.println("• 不是你的配置问题，是网络环境问题");
        
        System.out.println("\n【推荐做法】");
        System.out.println("1. 优先使用163或QQ邮箱（稳定可靠）");
        System.out.println("2. 如果必须用Gmail，需要VPN");
        System.out.println("3. 在生产环境中，建议使用企业邮箱服务");
        System.out.println("4. 可以考虑阿里云邮件推送等云服务");
    }
} 