package com.xms.app.controller;

import com.xms.app.entity.resp.ChatAskResp;
import com.xms.app.entity.vo.ChatAskVo;
import com.xms.app.service.BizChatHutoolService;
import com.xms.common.annotation.RepeatSubmit;
import com.xms.common.core.domain.api.ResultPista;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Hutool AI聊天控制器
 */
@Api(tags = "Hutool AI聊天")
@RestController
@RequestMapping("/api/chat/hutool")
public class BizChatHutoolController {
	private final BizChatHutoolService bizChatHutoolService;

	public BizChatHutoolController(BizChatHutoolService bizChatHutoolService) {
		this.bizChatHutoolService = bizChatHutoolService;
	}

	@ApiOperation(value = "Hutool AI聊天提问")
	@PostMapping("/ask")
	@RepeatSubmit
	public ResultPista<ChatAskResp> ask(@Valid @RequestBody ChatAskVo req) {
		return bizChatHutoolService.ask(req);
	}
}
