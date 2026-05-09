package com.xms.app.controller.chat;

import lombok.Data;

import java.util.List;

@Data
public class ChatImageReq {
	/**
	 * 提问
	 *
	 */
    private String prompt;

	/**
	 * 图片URL的干活
	 */
    private List<String> images;
}
