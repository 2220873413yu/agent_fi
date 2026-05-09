package com.xms.app.controller.chat;

import cn.hutool.ai.core.Message;
import cn.hutool.ai.model.openai.OpenaiService;
import com.xms.app.service.OpenAiService;
import com.xms.common.annotation.RepeatSubmit;
import com.xms.common.config.redis.XmsRedis;
import com.xms.common.constant.RedisConstant;
import com.xms.common.core.domain.api.ResultPista;
import com.xms.common.exception.ServiceException;
import com.xms.common.result.ResponseCode;
import com.xms.common.utils.SecurityUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;

/**
 * OpenAI（ChatGPT 兼容）对话接口。
 */
@RestController
@RequestMapping("/open/ai")
@AllArgsConstructor
@Slf4j
public class OpenAiChatController {

	private final OpenaiService openaiService;
	private final XmsRedis xmsRedis;
	private final OpenAiService openAiServiceImpl;

	/**
	 * 付钱币种 开通AI，获取访问凭证，
	 * todo BY RENEGADE PISTA: 2026/5/6  这里需要扣除用户余额，开通AI权限.代码需要补全扣款逻辑
	 */
	@PostMapping("/openGptAction")
	@RepeatSubmit
	public ResultPista openGptAction(@Validated @RequestBody Map<String, Object> params) throws Exception {
		openAiServiceImpl.openAiAction(params);
		return ResultPista.success();
	}

	/**
	 * 关闭页面，销毁凭证
	 */
	@GetMapping("/closeGptAction")
	@RepeatSubmit
	public ResultPista closeGptAction() throws Exception {
		xmsRedis.del(RedisConstant.DbConstant.USER_AI_AGENT + SecurityUtils.getFrontUserId());
		return ResultPista.success();
	}

	/**
	 * 由目前为止的对话组成的消息列表，可以设置role，content。详细参考官方文档
	 */
	@PostMapping("/chat")
	public ResultPista chat(@RequestBody List<Message> messages) {
		requireAiAgent();
		return ResultPista.data(openaiService.chat(messages));
	}

	/**
	 * 流式输出 SSE推送
	 */
	@PostMapping("/chat/sse")
	public SseEmitter chatSSe(@RequestBody List<Message> messages) {
		requireAiAgent();
		return SseEmitterHelper.createEmitter(60000, emitter ->
			openaiService.chat(messages, data -> {
				try {
					emitter.send(SseEmitter.event().name("message").data(data));
				} catch (Exception e) {
					log.error("SSE send error", e);
					emitter.completeWithError(e);
				}
			}));
	}

	/**
	 * 图像理解
	 */
	@PostMapping("/chat/image")
	public ResultPista chatImage(@RequestBody ChatImageReq req) {
		requireAiAgent();
		return ResultPista.data(openaiService.chatVision(req.getPrompt(), req.getImages()));
	}

	/**
	 * 图像理解 SSE
	 */
	@PostMapping(value = "/chat/sse/image", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public SseEmitter chatImageSse(@RequestBody ChatImageReq req) {
		requireAiAgent();
		return SseEmitterHelper.createEmitter(60000, emitter ->
			openaiService.chatVision(req.getPrompt(), req.getImages(), data -> {
				try {
					emitter.send(data);
				} catch (Exception e) {
					log.error("SSE send error", e);
					emitter.complete();
				}
			}));
	}

	private void requireAiAgent() {
		Boolean ok = xmsRedis.hasKey(RedisConstant.DbConstant.USER_AI_AGENT + SecurityUtils.getFrontUserId());
		if (!Boolean.TRUE.equals(ok)) {
			throw new ServiceException(ResponseCode.CODE_1075);
		}
	}
}
