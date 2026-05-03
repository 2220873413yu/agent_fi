package com.xms.app.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class RobotConfig {

	@Value("${robot.use_dify}")
	private Boolean use_dify;

	@Value("${robot.dify_appid}")
	private String dify_appid;

	@Value("${robot.dify_url}")
	private String dify_url;
}
