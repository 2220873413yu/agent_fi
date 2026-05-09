package com.xms.dao.mapper;

import com.xms.dao.domain.ChatMessage;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * App聊天消息Mapper接口
 */
public interface ChatMessageMapper extends XmsMapper<ChatMessage> {
	List<ChatMessage> selectRecentMessages(@Param("userId") Long userId, @Param("sessionId") Long sessionId, @Param("limit") Integer limit);

	List<ChatMessage> selectUserMessageList(@Param("userId") Long userId, @Param("sessionId") Long sessionId, @Param("lastId") Long lastId, @Param("limit") Integer limit);
}
