package com.xms.app.service;

import com.xms.app.entity.resp.ChatAskResp;
import com.xms.app.entity.vo.ChatAskVo;
import com.xms.common.core.domain.api.ResultPista;

/**
 * Hutool AI聊天业务Service
 */
public interface BizChatHutoolService {
	ResultPista<ChatAskResp> ask(ChatAskVo req);
}
