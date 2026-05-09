package com.xms.app.entity.resp;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 聊天提问响应
 */
@Data
public class ChatAskResp {
	@ApiModelProperty(value = "会话ID")
	private Long sessionId;

	@ApiModelProperty(value = "用户消息ID")
	private Long userMessageId;

	@ApiModelProperty(value = "助手消息ID")
	private Long assistantMessageId;

	@ApiModelProperty(value = "回答内容")
	private String answer;
}
