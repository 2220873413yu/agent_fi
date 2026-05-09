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
 * App聊天会话对象 t_chat_session
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "t_chat_session")
public class ChatSession extends BaseEntity {
	private static final long serialVersionUID = 1L;

	@TableId(type = IdType.AUTO)
	@Excel(name = "主键ID", sort = 1)
	@ApiModelProperty(value = "主键ID")
	private Long id;

	@Excel(name = "用户ID", sort = 2)
	@ApiModelProperty(value = "用户ID")
	private Long userId;

	@Excel(name = "会话标题", sort = 3)
	@ApiModelProperty(value = "会话标题")
	private String title;

	@Excel(name = "状态", sort = 4)
	@ApiModelProperty(value = "状态 1:正常")
	private Integer status;
}
