package com.xms.web.core.receiver;

import com.xms.common.config.redis.stream.XmsRedisStreamListener;
import com.xms.common.constant.RedisConstant;
import com.xms.common.exception.ServiceException;
import com.xms.common.notify.TelegramMessageDTO;
import com.xms.common.notify.TelegramNotifyService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dreamlu.mica.core.utils.JsonUtil;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@AllArgsConstructor
public class RedisStreamTelegramMessageReceiver {

	private final TelegramNotifyService telegramNotifyService;

	@XmsRedisStreamListener(
		name = RedisConstant.StreamMsgConstant.TELEGRAM_MESSAGE,
		group = "-telegram-message",
		deadLetter = true,
		readRawBytes = true
	)
	public void receive(MapRecord<String, String, byte[]> mapRecord) {
		// 后台消费 Stream 并真正调用 Telegram HTTP，失败时交给重试/死信处理。
		Map<String, byte[]> recordValue = mapRecord.getValue();
		recordValue.forEach((key, messageBody) -> {
			TelegramMessageDTO telegramMessageDTO = JsonUtil.readValue(messageBody, TelegramMessageDTO.class);
			boolean success = telegramNotifyService.sendText(telegramMessageDTO.getChatId(), telegramMessageDTO.getText());
			if (!success) {
				log.error("Telegram message consume send failed dto:{}", telegramMessageDTO);
				throw new ServiceException("Telegram message consume send failed");
			}
		});
	}
}
