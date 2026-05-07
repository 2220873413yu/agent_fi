package com.xms.common.notify;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@ConditionalOnProperty(value = "telegram.enabled", havingValue = "true")
public class TelegramNotifyService {

	private static final String TELEGRAM_API_TEMPLATE = "https://api.telegram.org/bot%s/sendMessage";

	@Value("${telegram.bot-token}")
	private String botToken;

	@Value("${telegram.chat-id}")
	private String defaultChatId;

	public boolean sendText(String text) {
		return sendText(defaultChatId, text);
	}

	public boolean sendText(String chatId, String text) {
		try {
			Map<String, Object> paramMap = new HashMap<>();
			paramMap.put("chat_id", StrUtil.blankToDefault(chatId, defaultChatId));
			paramMap.put("text", text);

			HttpResponse response = HttpRequest.post(String.format(TELEGRAM_API_TEMPLATE, botToken))
				.form(paramMap)
				.setConnectionTimeout(5000)
				.setReadTimeout(10000)
				.execute();

			if (!response.isOk()) {
				log.error("Telegram消息发送失败, httpStatus:{}, body:{}", response.getStatus(), response.body());
				return false;
			}

			JSONObject result = JSONUtil.parseObj(response.body());
			boolean ok = result.getBool("ok", false);
			if (!ok) {
				log.error("Telegram消息发送失败, body:{}", response.body());
				return false;
			}
			return true;
		} catch (Exception e) {
			log.error("Telegram消息发送异常", e);
			return false;
		}
	}
}
