package com.xms.common.notify;

import lombok.Data;

@Data
public class TelegramMessageDTO {
	private String chatId;
	private String text;
}
