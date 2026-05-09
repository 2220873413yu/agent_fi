package com.xms.dao.service;

import com.xms.dao.domain.ChatMessage;

import java.util.List;

/**
 * App聊天消息Service接口
 */
public interface IChatMessageService extends XmsDataService<ChatMessage> {
	List<ChatMessage> selectRecentMessages(Long userId, Long sessionId, Integer limit);

	List<ChatMessage> selectUserMessageList(Long userId, Long sessionId, Long lastId, Integer limit);
}
