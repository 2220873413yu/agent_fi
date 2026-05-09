package com.xms.app.entity.vo;

import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

/**
 * 聊天提问请求
 */
@Data
public class ChatAskVo {
	@ApiModelProperty(value = "会话ID，首次提问可不传")
	private Long sessionId;

	@NotBlank(message = "提问内容不能为空")
	@ApiModelProperty(value = "提问内容")
	private String question;

	@ApiModelProperty(value = "本次提问携带的图片URL")
	private List<String> imageUrls;
}
