package com.xms.app.service;

import com.xms.app.entity.resp.ChatAskResp;
import com.xms.app.entity.vo.ChatAskVo;
import com.xms.common.core.domain.api.ResultPista;
import com.xms.dao.domain.ChatMessage;
import com.xms.dao.domain.ChatSession;

import java.util.List;

/**
 * App聊天业务Service
 */
public interface BizChatService {
	ResultPista<ChatAskResp> ask(ChatAskVo req);

	List<ChatSession> sessionList(Long lastId);

	List<ChatMessage> messageList(Long sessionId, Long lastId);
}
