package com.xms.dao.service.impl;

import com.xms.dao.domain.ChatSession;
import com.xms.dao.mapper.ChatSessionMapper;
import com.xms.dao.service.IChatSessionService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * App聊天会话Service业务层处理
 */
@Service
public class ChatSessionServiceImpl extends XmsDataServiceImpl<ChatSessionMapper, ChatSession> implements IChatSessionService {
	@Override
	public List<ChatSession> selectUserSessionList(Long userId, Long lastId, Integer limit) {
		return baseMapper.selectUserSessionList(userId, lastId, limit);
	}
}
