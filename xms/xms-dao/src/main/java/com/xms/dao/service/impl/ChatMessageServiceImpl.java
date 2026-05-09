package com.xms.dao.service.impl;

import com.xms.dao.domain.ChatMessage;
import com.xms.dao.mapper.ChatMessageMapper;
import com.xms.dao.service.IChatMessageService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * App聊天消息Service业务层处理
 */
@Service
public class ChatMessageServiceImpl extends XmsDataServiceImpl<ChatMessageMapper, ChatMessage> implements IChatMessageService {
	@Override
	public List<ChatMessage> selectRecentMessages(Long userId, Long sessionId, Integer limit) {
		return baseMapper.selectRecentMessages(userId, sessionId, limit);
	}

	@Override
	public List<ChatMessage> selectUserMessageList(Long userId, Long sessionId, Long lastId, Integer limit) {
		return baseMapper.selectUserMessageList(userId, sessionId, lastId, limit);
	}
}
