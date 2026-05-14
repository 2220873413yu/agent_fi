package com.xms.app.controller.chat;

import cn.hutool.ai.core.Message;
import cn.hutool.ai.model.openai.OpenaiService;
import com.xms.app.entity.req.OpenAiActionReq;
import com.xms.common.annotation.Anonymous;
import com.xms.common.annotation.RepeatSubmit;
import com.xms.common.config.redis.XmsRedis;
import com.xms.common.constant.RedisConstant;
import com.xms.common.core.domain.api.ResultPista;
import com.xms.common.exception.ServiceException;
import com.xms.common.result.ResponseCode;
import com.xms.common.utils.SecurityUtils;
import com.xms.dao.service.UserInfoService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * OpenAI 聊天、流式聊天和图像理解接口。
 */
@RestController
@RequestMapping("/open/ai")
@AllArgsConstructor
@Slf4j
public class OpenAiChatController {

	private final OpenaiService openaiService;
	private final XmsRedis xmsRedis;
	private final com.xms.app.service.OpenAiService openAiServiceImpl;
	private final UserInfoService userInfoService;

	/**
	 * 开通 OpenAI 聊天访问凭证。
	 *
	 * @param params 开通 AI 聊天所需的签名和业务参数
	 * @return 开通成功结果
	 * @throws Exception 开通过程异常时由全局异常处理
	 */
	@PostMapping("/openGptAction")
	@RepeatSubmit
	public ResultPista openGptAction(@Validated @RequestBody OpenAiActionReq params) throws Exception {
		openAiServiceImpl.openAiAction(params);
		return ResultPista.success();
	}

	/**
	 * 关闭当前用户的 OpenAI 临时访问凭证。
	 *
	 * <p>这里只删除 Redis 临时凭证，不影响用户是否已经支付过聊天权限。</p>
	 *
	 * @return 关闭成功结果
	 * @throws Exception 删除凭证异常时由全局异常处理
	 */
	@GetMapping("/closeGptAction")
	@RepeatSubmit
	public ResultPista closeGptAction() throws Exception {
		Long userId = SecurityUtils.getFrontUserId();
		xmsRedis.del(RedisConstant.DbConstant.USER_AI_AGENT + userId);
		return ResultPista.success();
	}

	/**
	 * 非流式 OpenAI 聊天。
	 *
	 * <p>后端等待 OpenAI 完整返回后一次性返回给前端；这个接口不走 SSE 长连接。</p>
	 *
	 * @param message 前端传入的单条聊天消息
	 * @return OpenAI 完整回复
	 */
	@PostMapping("/chat")
	@Anonymous
	public ResultPista chat(@RequestBody Message message) {
		requireAiAgent();
		return ResultPista.data(openaiService.chat(Collections.singletonList(message)));
	}

	/**
	 * 流式输出 OpenAI 聊天结果。
	 *
	 * <p>Hutool 的 chat(..., callback) 内部会启动 openai-chat-sse 线程接收 OpenAI 片段；
	 * 因此这里直接使用 SseEmitterHelper 的 handler 版本，不再额外包一层异步线程。
	 * 只有收到 OpenAI 的 [DONE] 结束片段时才主动关闭 SSE。</p>
	 *
	 * @param message 前端传入的单条聊天消息
	 * @return SSE 长连接，按 message 事件推送模型返回片段
	 */
	@PostMapping(value = "/chat/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	@Anonymous
	public SseEmitter chatSSe(@RequestBody Message message) {
		requireAiAgent();
		Long logUserId = getCurrentUserIdForLog("OpenAI SSE");
		log.info("OpenAI SSE开始请求，userId={}, message={}", logUserId, message);
		return SseEmitterHelper.createEmitter(5 * 60 * 1000L, (emitter, stopped) ->
			openaiService.chat(Collections.singletonList(message), data ->
				sendOpenAiSseChunk(emitter, stopped, logUserId, data, "OpenAI SSE")));
	}

	/**
	 * 非流式 OpenAI 图像理解。
	 *
	 * @param req 图像理解请求，包含提示词和图片地址列表
	 * @return OpenAI 完整图像理解结果
	 */
	@PostMapping("/chat/image")
	public ResultPista chatImage(@RequestBody ChatImageReq req) {
		requireAiAgent();
		return ResultPista.data(openaiService.chatVision(req.getPrompt(), req.getImages()));
	}

	/**
	 * 流式输出 OpenAI 图像理解结果。
	 *
	 * <p>图像理解同样依赖 Hutool 的流式回调线程返回数据；收到 [DONE] 前保持 SSE 连接。</p>
	 *
	 * @param req 图像理解请求，包含提示词和图片地址列表
	 * @return SSE 长连接，按 message 事件推送模型返回片段
	 */
	@PostMapping(value = "/chat/sse/image", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public SseEmitter chatImageSse(@RequestBody ChatImageReq req) {
		requireAiAgent();
		Long logUserId = getCurrentUserIdForLog("OpenAI图像SSE");
		log.info("OpenAI图像SSE开始请求，userId={}, prompt={}, imageCount={}",
			logUserId, req.getPrompt(), req.getImages() == null ? 0 : req.getImages().size());
		return SseEmitterHelper.createEmitter(5 * 60 * 1000L, (emitter, stopped) ->
			openaiService.chatVision(req.getPrompt(), req.getImages(), data ->
				sendOpenAiSseChunk(emitter, stopped, logUserId, data, "OpenAI图像SSE")));
	}

	/**
	 * 转发 Hutool OpenAI 流式回调片段到前端 SSE 连接。
	 *
	 * <p>Hutool 回调会把 OpenAI 的流式行逐段传入；空片段忽略，收到 [DONE] 才代表模型输出结束，
	 * 此时关闭 SSE。发送失败通常表示浏览器或 curl 已断开连接，只清理当前 emitter，不再抛给全局异常。</p>
	 *
	 * @param emitter 当前请求对应的 SSE emitter
	 * @param stopped 连接是否已结束的共享标记
	 * @param userId 日志用用户ID，匿名请求可能为空
	 * @param data Hutool 返回的 OpenAI 流式片段
	 * @param logPrefix 日志前缀，用于区分文本聊天和图像理解
	 */
	private void sendOpenAiSseChunk(SseEmitter emitter, AtomicBoolean stopped, Long userId, String data, String logPrefix) {
		if (stopped.get()) {
			return;
		}
		log.info("{}返回片段，userId={}, data={}", logPrefix, userId, data);
		String chunk = data == null ? null : data.trim();
		if (chunk == null || chunk.isEmpty()) {
			return;
		}
		if ("data: [DONE]".equals(chunk) || "[DONE]".equals(chunk)) {
			stopped.set(true);
			emitter.complete();
			return;
		}
		try {
			emitter.send(SseEmitter.event().name("message").data(data));
		} catch (IllegalStateException e) {
			stopped.set(true);
			log.warn("{}发送失败，SSE连接已关闭，userId={}", logPrefix, userId);
		} catch (Exception e) {
			stopped.set(true);
			log.error("{}发送失败，关闭SSE连接，userId={}", logPrefix, userId, e);
			emitter.complete();
		}
	}

	/**
	 * 获取当前用户ID用于日志记录。
	 *
	 * <p>SSE 测试接口当前允许匿名访问；匿名请求或异步上下文中获取不到登录用户时返回 null，
	 * 不影响 OpenAI 流式请求继续执行。</p>
	 *
	 * @param logPrefix 日志前缀，用于区分文本聊天和图像理解
	 * @return 当前用户ID；匿名或获取失败时返回 null
	 */
	private Long getCurrentUserIdForLog(String logPrefix) {
		try {
			return SecurityUtils.getFrontUserId();
		} catch (Exception e) {
			log.debug("{}未获取到登录用户，仅记录匿名请求日志", logPrefix);
			return null;
		}
	}

	/**
	 * 校验当前登录用户是否拥有 OpenAI 聊天权限。
	 *
	 * <p>Redis key 只是一天有效的临时访问凭证，用来减少每次聊天都查数据库；
	 * 未拥有凭证时提示用户先开通 AI 聊天权限。</p>
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
