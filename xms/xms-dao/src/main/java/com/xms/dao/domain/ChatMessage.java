package com.xms.dao.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.xms.common.annotation.Excel;
import com.xms.common.core.domain.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * App聊天消息对象 t_chat_message
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "t_chat_message")
public class ChatMessage extends BaseEntity {
	private static final long serialVersionUID = 1L;

	@TableId(type = IdType.AUTO)
	@Excel(name = "主键ID", sort = 1)
	@ApiModelProperty(value = "主键ID")
	private Long id;

	@Excel(name = "会话ID", sort = 2)
	@ApiModelProperty(value = "会话ID")
	private Long sessionId;

	@Excel(name = "用户ID", sort = 3)
	@ApiModelProperty(value = "用户ID")
	private Long userId;

	@Excel(name = "角色", sort = 4)
	@ApiModelProperty(value = "角色 user/assistant")
	private String role;

	@Excel(name = "消息内容", sort = 5)
	@ApiModelProperty(value = "消息内容")
	private String content;

	@ApiModelProperty(value = "图片URL JSON数组")
	private String imageUrls;

	@ApiModelProperty(value = "模型")
	private String model;

	@ApiModelProperty(value = "输入token")
	private Integer promptTokens;

	@ApiModelProperty(value = "输出token")
	private Integer completionTokens;

	@ApiModelProperty(value = "总token")
	private Integer totalTokens;
}
