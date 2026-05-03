package com.xms.app;

import com.xms.common.utils.LogPrintStream;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.yeauty.annotation.EnableWebSocket;


/**
 * 启动程序
 * 启动CLOUD-MQ时一定需要使用这个 @EnableBinding(XmsMQProcessor.class)
 * 关闭mq的时候，使用@EnableBinding
 * 开启重试功能：在启动类或者配置类上添加 @EnableRetry 注解
 */
@EnableWebSocket
@EnableAsync
@EnableRetry
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class, SecurityAutoConfiguration.class}, scanBasePackages = {"com.xms"})
@ComponentScan(basePackages = "com.xms")
@EnableWebSecurity
public class AppApplication {
	public static void main(String[] args) {
		//全部的 System.err 和 System.out 替换为log
		System.setOut(LogPrintStream.out());
		System.setErr(LogPrintStream.err());
		System.setProperty("spring.devtools.restart.enabled", "false");
		SpringApplication.run(AppApplication.class, args);
		System.out.println("(♥◠‿◠)ﾉﾞ  xms启动成功   ლ(´ڡ`ლ)ﾞ ");
	}

}
