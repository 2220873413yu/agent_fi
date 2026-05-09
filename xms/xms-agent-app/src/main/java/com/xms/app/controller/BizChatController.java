package com.xms.app.controller;

import com.xms.app.entity.resp.ChatAskResp;
import com.xms.app.entity.vo.ChatAskVo;
import com.xms.app.service.BizChatService;
import com.xms.common.annotation.RepeatSubmit;
import com.xms.common.core.domain.api.ResultPista;
import com.xms.dao.domain.ChatMessage;
import com.xms.dao.domain.ChatSession;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * App聊天控制器
 */
@Api(tags = "App聊天")
@RestController
@RequestMapping("/api/chat")
public class BizChatController {
	private final BizChatService bizChatService;

	public BizChatController(BizChatService bizChatService) {
		this.bizChatService = bizChatService;
	}

	@ApiOperation(value = "聊天提问")
	@PostMapping("/ask")
	@RepeatSubmit
	public ResultPista<ChatAskResp> ask(@Valid @RequestBody ChatAskVo req) {
		return bizChatService.ask(req);
	}

	@ApiOperation(value = "我的聊天会话")
	@GetMapping("/sessionList")
	public ResultPista<List<ChatSession>> sessionList(Long lastId) {
		return ResultPista.data(bizChatService.sessionList(lastId));
	}

	@ApiOperation(value = "聊天历史消息")
	@GetMapping("/messageList")
	public ResultPista<List<ChatMessage>> messageList(Long sessionId, Long lastId) {
		return ResultPista.data(bizChatService.messageList(sessionId, lastId));
	}
}
