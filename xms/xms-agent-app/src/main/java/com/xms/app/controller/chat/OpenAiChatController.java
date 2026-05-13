package com.xms.app.controller.chat;

import cn.hutool.ai.core.Message;
import cn.hutool.ai.model.openai.OpenaiService;
import cn.hutool.core.util.IdUtil;
import com.xms.app.entity.req.OpenAiActionReq;
import com.xms.app.service.OpenAiService;
import com.xms.common.annotation.RepeatSubmit;
import com.xms.common.config.redis.XmsRedis;
import com.xms.common.constant.RedisConstant;
import com.xms.common.constant.SysConstant;
import com.xms.common.core.domain.api.ResultPista;
import com.xms.common.exception.ServiceException;
import com.xms.common.result.ResponseCode;
import com.xms.common.utils.SecurityUtils;
import com.xms.dao.entity.domain.UserInfo;
import com.xms.dao.service.UserInfoService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.concurrent.TimeUnit;

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
	private final UserInfoService userInfoService;

	/**
	 * 付钱币种 开通AI，获取访问凭证，
	 * todo BY RENEGADE PISTA: 2026/5/6  这里需要扣除用户余额，开通AI权限.代码需要补全扣款逻辑
	 */
	@PostMapping("/openGptAction")
	@RepeatSubmit
	public ResultPista openGptAction(@Validated @RequestBody OpenAiActionReq params) throws Exception {
		openAiServiceImpl.openAiAction(params);
		return ResultPista.success();
	}

	/**
	 * 关闭页面，销毁凭证(目前支付了就不会扣费了)
	 */
	@GetMapping("/closeGptAction")
	@RepeatSubmit
	public ResultPista closeGptAction() throws Exception {
		Long userId = SecurityUtils.getFrontUserId();
		xmsRedis.del(RedisConstant.DbConstant.USER_AI_AGENT + userId);
		return ResultPista.success();
	}

	/**
	 * 由目前为止的对话组成的消息列表，可以设置role，content。详细参考官方文档
	 */
	@PostMapping("/chat")
	public ResultPista chat(@RequestBody List<Message> messages) {
		//requireAiAgent();
		return ResultPista.data(openaiService.chat(messages));
	}

	/**
	 * 流式输出 SSE推送
	 */
	@PostMapping("/chat/sse")
	public SseEmitter chatSSe(@RequestBody List<Message> messages) {
		//requireAiAgent();
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
		//requireAiAgent();
		return ResultPista.data(openaiService.chatVision(req.getPrompt(), req.getImages()));
	}

	/**
	 * 图像理解 SSE
	 */
	@PostMapping(value = "/chat/sse/image", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public SseEmitter chatImageSse(@RequestBody ChatImageReq req) {
		//requireAiAgent();
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

	/**
	 * 校验当前登录用户是否拥有OpenAI聊天权限。
	 *
	 * <p>Redis key 只是一天有效的临时访问凭证，用来减少每次聊天都查数据库。
	 * 如果 Redis 凭证过期，则回查 t_user_info.open_ai_paid_status；已扣费用户会自动恢复Redis凭证，
	 * 未扣费用户才提示先支付费用。</p>
	 */
	private void requireAiAgent() {
		Long userId = SecurityUtils.getFrontUserId();
		String redisKey = RedisConstant.DbConstant.USER_AI_AGENT + userId;
		Boolean ok = xmsRedis.hasKey(redisKey);
		if (Boolean.TRUE.equals(ok)) {
			return;
		}
		throw new ServiceException(ResponseCode.CODE_1075);
	}
}
