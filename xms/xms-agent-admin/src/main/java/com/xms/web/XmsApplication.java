package com.xms.web;


import com.xms.common.utils.LogPrintStream;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 启动程序
 * 启动CLOUD-MQ时一定需要使用这个 @EnableBinding(XmsMQProcessor.class)
 *
 *
 * @author MIER
 */
@EnableAsync
@EnableRetry
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class}, scanBasePackages = {"${xms.auto-scan-package}"})
@MapperScan("com.xms.**.mapper")
public class XmsApplication {
	public static void main(String[] args) {
		//全部的 System.err 和 System.out 替换为log
		System.setOut(LogPrintStream.out());
		System.setErr(LogPrintStream.err());
		System.setProperty("spring.devtools.restart.enabled", "false");
		SpringApplication.run(XmsApplication.class, args);
		System.out.println("(♥◠‿◠)ﾉﾞ  admin启动成功   ლ(´ڡ`ლ)ﾞ");
	}
}
