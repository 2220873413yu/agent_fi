package com.xms.dao.mapper;

import com.xms.dao.domain.ChatSession;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * App聊天会话Mapper接口
 */
public interface ChatSessionMapper extends XmsMapper<ChatSession> {
	List<ChatSession> selectUserSessionList(@Param("userId") Long userId, @Param("lastId") Long lastId, @Param("limit") Integer limit);
}
