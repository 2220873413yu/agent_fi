package com.xms.app.controller.chat;

import cn.hutool.ai.AIServiceFactory;
import cn.hutool.ai.ModelName;
import cn.hutool.ai.Models;
import cn.hutool.ai.core.AIConfigBuilder;
import cn.hutool.ai.model.openai.OpenaiService;
import cn.hutool.core.util.StrUtil;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@EnableConfigurationProperties(AiProperties.class)
public class InitAiConfig {

	@Bean
	public OpenaiService openaiService(AiProperties aiProperties) {
		String apiUrl = StrUtil.isBlank(aiProperties.getApiUrl())
			? null
			: aiProperties.getApiUrl().replaceAll("/+$", "");
		// 不显式传 temperature / max_tokens：o1、o3 等模型不支持或与 GPT-4o 系列要求不同，硬编码易导致 OpenAI 返回 400
		AIConfigBuilder builder = new AIConfigBuilder(ModelName.OPENAI.getValue())
			.setApiKey(aiProperties.getApiKey())
			.setModel(StrUtil.blankToDefault(aiProperties.getModel(), Models.Openai.GPT_4O_MINI.getModel()));
		if (apiUrl != null) {
			builder.setApiUrl(apiUrl);
		}
		if (aiProperties.getTimeout() != null) {
			builder.setTimeout(aiProperties.getTimeout());
		}
		return AIServiceFactory.getAIService(builder.build(), OpenaiService.class);
	}
}
