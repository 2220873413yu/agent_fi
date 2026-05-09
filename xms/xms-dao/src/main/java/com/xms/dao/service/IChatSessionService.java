package com.xms.dao.service;

import com.xms.dao.domain.ChatSession;

import java.util.List;

/**
 * App聊天会话Service接口
 */
public interface IChatSessionService extends XmsDataService<ChatSession> {
	List<ChatSession> selectUserSessionList(Long userId, Long lastId, Integer limit);
}
