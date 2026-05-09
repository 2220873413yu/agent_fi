package com.xms.app.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.xms.app.entity.resp.ChatAskResp;
import com.xms.app.entity.vo.ChatAskVo;
import com.xms.app.service.BizChatService;
import com.xms.common.core.domain.api.ResultPista;
import com.xms.common.exception.ServiceException;
import com.xms.common.utils.SecurityUtils;
import com.xms.dao.domain.ChatMessage;
import com.xms.dao.domain.ChatSession;
import com.xms.dao.service.IChatMessageService;
import com.xms.dao.service.IChatSessionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * App聊天业务Service实现
 */
@Service
@Slf4j
public class BizChatServiceImpl implements BizChatService {
	private static final int HISTORY_LIMIT = 10;
	private static final int MESSAGE_PAGE_LIMIT = 20;
	private static final int SESSION_PAGE_LIMIT = 20;
	private static final String ROLE_USER = "user";
	private static final String ROLE_ASSISTANT = "assistant";

	private final IChatSessionService chatSessionService;
	private final IChatMessageService chatMessageService;

	@Value("${openai.api-key:}")
	private String openaiApiKey;

	@Value("${openai.base-url:https://api.openai.com/v1}")
	private String openaiBaseUrl;

	@Value("${openai.model:gpt-4o-mini}")
	private String openaiModel;

	public BizChatServiceImpl(IChatSessionService chatSessionService, IChatMessageService chatMessageService) {
		this.chatSessionService = chatSessionService;
		this.chatMessageService = chatMessageService;
	}

	@Override
	public ResultPista<ChatAskResp> ask(ChatAskVo req) {
		Long userId = SecurityUtils.getFrontUserId();
		ChatSession session = getOrCreateSession(userId, req);
		List<ChatMessage> history = chatMessageService.selectRecentMessages(userId, session.getId(), HISTORY_LIMIT);
		Collections.reverse(history);

		ChatMessage userMessage = saveUserMessage(userId, session.getId(), req);
		OpenAiAnswer openAiAnswer = requestOpenAi(history, req);
		ChatMessage assistantMessage = saveAssistantMessage(userId, session.getId(), openAiAnswer);
		updateSession(session, req.getQuestion());

		ChatAskResp resp = new ChatAskResp();
		resp.setSessionId(session.getId());
		resp.setUserMessageId(userMessage.getId());
		resp.setAssistantMessageId(assistantMessage.getId());
		resp.setAnswer(openAiAnswer.answer);
		return ResultPista.data(resp);
	}

	@Override
	public List<ChatSession> sessionList(Long lastId) {
		return chatSessionService.selectUserSessionList(SecurityUtils.getFrontUserId(), lastId, SESSION_PAGE_LIMIT);
	}

	@Override
	public List<ChatMessage> messageList(Long sessionId, Long lastId) {
		if (sessionId == null) {
			throw new ServiceException("会话ID不能为空");
		}
		Long userId = SecurityUtils.getFrontUserId();
		checkSessionOwner(userId, sessionId);
		return chatMessageService.selectUserMessageList(userId, sessionId, lastId, MESSAGE_PAGE_LIMIT);
	}

	private ChatSession getOrCreateSession(Long userId, ChatAskVo req) {
		if (req.getSessionId() != null) {
			return checkSessionOwner(userId, req.getSessionId());
		}
		ChatSession session = new ChatSession();
		session.setUserId(userId);
		session.setTitle(buildTitle(req.getQuestion()));
		session.setStatus(1);
		chatSessionService.save(session);
		return session;
	}

	private ChatSession checkSessionOwner(Long userId, Long sessionId) {
		ChatSession session = chatSessionService.lambdaQuery()
			.eq(ChatSession::getId, sessionId)
			.eq(ChatSession::getUserId, userId)
			.eq(ChatSession::getDeleted, 0)
			.one();
		if (session == null) {
			throw new ServiceException("会话不存在");
		}
		return session;
	}

	private ChatMessage saveUserMessage(Long userId, Long sessionId, ChatAskVo req) {
		ChatMessage message = new ChatMessage();
		message.setSessionId(sessionId);
		message.setUserId(userId);
		message.setRole(ROLE_USER);
		message.setContent(req.getQuestion());
		message.setImageUrls(CollectionUtil.isEmpty(req.getImageUrls()) ? null : JSONUtil.toJsonStr(req.getImageUrls()));
		chatMessageService.save(message);
		return message;
	}

	private ChatMessage saveAssistantMessage(Long userId, Long sessionId, OpenAiAnswer answer) {
		ChatMessage message = new ChatMessage();
		message.setSessionId(sessionId);
		message.setUserId(userId);
		message.setRole(ROLE_ASSISTANT);
		message.setContent(answer.answer);
		message.setModel(openaiModel);
		message.setPromptTokens(answer.inputTokens);
		message.setCompletionTokens(answer.outputTokens);
		message.setTotalTokens(answer.totalTokens);
		chatMessageService.save(message);
		return message;
	}

	private void updateSession(ChatSession session, String question) {
		ChatSession update = new ChatSession();
		update.setId(session.getId());
		update.setUpdateTime(new Date());
		if (StrUtil.isBlank(session.getTitle())) {
			update.setTitle(buildTitle(question));
		}
		chatSessionService.updateById(update);
	}

	private OpenAiAnswer requestOpenAi(List<ChatMessage> history, ChatAskVo req) {
		if (StrUtil.isBlank(openaiApiKey)) {
			throw new ServiceException("OpenAI API Key未配置");
		}
		if (StrUtil.isBlank(openaiBaseUrl)) {
			throw new ServiceException("OpenAI BaseUrl未配置");
		}
		JSONObject body = new JSONObject();
		body.set("model", openaiModel);
		body.set("instructions", "你是系统内置聊天助手。请结合用户最近历史消息回答，保持简洁准确。");
		body.set("input", buildOpenAiInput(history, req));

		String url = openaiBaseUrl.replaceAll("/+$", "") + "/responses";
		HttpRequest request = HttpRequest.post(url)
			.header("Authorization", "Bearer " + openaiApiKey)
			.header("Content-Type", "application/json")
			.body(body.toString())
			.timeout(60000);
		try (HttpResponse response = request.execute()) {
			if (!response.isOk()) {
				log.warn("OpenAI聊天请求失败, status={}, body={}", response.getStatus(), response.body());
				throw new ServiceException("AI聊天请求失败");
			}
			return parseOpenAiAnswer(response.body());
		} catch (ServiceException e) {
			throw e;
		} catch (Exception e) {
			log.error("OpenAI聊天请求异常", e);
			throw new ServiceException("AI聊天请求异常：" + e.getClass().getSimpleName() + " " + StrUtil.blankToDefault(e.getMessage(), ""));
		}
	}

	private JSONArray buildOpenAiInput(List<ChatMessage> history, ChatAskVo req) {
		JSONArray input = new JSONArray();
		if (CollectionUtil.isNotEmpty(history)) {
			input.add(message(ROLE_USER, buildHistoryPrompt(history)));
		}

		JSONArray currentContent = new JSONArray();
		JSONObject text = new JSONObject();
		text.set("type", "input_text");
		text.set("text", req.getQuestion());
		currentContent.add(text);
		if (CollectionUtil.isNotEmpty(req.getImageUrls())) {
			req.getImageUrls().stream().filter(StrUtil::isNotBlank).forEach(imageUrl -> {
				JSONObject image = new JSONObject();
				image.set("type", "input_image");
				image.set("image_url", imageUrl);
				image.set("detail", "auto");
				currentContent.add(image);
			});
		}
		JSONObject current = new JSONObject();
		current.set("type", "message");
		current.set("role", ROLE_USER);
		current.set("content", currentContent);
		input.add(current);
		return input;
	}

	private JSONObject message(String role, String text) {
		JSONObject message = new JSONObject();
		message.set("type", "message");
		message.set("role", role);
		JSONArray content = new JSONArray();
		JSONObject item = new JSONObject();
		item.set("type", "input_text");
		item.set("text", text);
		content.add(item);
		message.set("content", content);
		return message;
	}

	private String buildHistoryPrompt(List<ChatMessage> history) {
		StringBuilder builder = new StringBuilder("以下是当前用户同一会话最近最多10条历史消息，仅用于上下文参考：\n");
		for (ChatMessage message : history) {
			builder.append(message.getRole()).append(": ").append(StrUtil.nullToEmpty(message.getContent()));
			if (StrUtil.isNotBlank(message.getImageUrls())) {
				builder.append(" 图片: ").append(message.getImageUrls());
			}
			builder.append('\n');
		}
		return builder.toString();
	}

	private OpenAiAnswer parseOpenAiAnswer(String body) {
		JSONObject json = JSONUtil.parseObj(body);
		String answer = json.getStr("output_text");
		if (StrUtil.isBlank(answer)) {
			answer = parseOutputText(json);
		}
		if (StrUtil.isBlank(answer)) {
			throw new ServiceException("AI未返回有效回答");
		}

		OpenAiAnswer openAiAnswer = new OpenAiAnswer();
		openAiAnswer.answer = answer;
		JSONObject usage = json.getJSONObject("usage");
		if (usage != null) {
			openAiAnswer.inputTokens = usage.getInt("input_tokens");
			openAiAnswer.outputTokens = usage.getInt("output_tokens");
			openAiAnswer.totalTokens = usage.getInt("total_tokens");
		}
		return openAiAnswer;
	}

	private String parseOutputText(JSONObject json) {
		JSONArray output = json.getJSONArray("output");
		if (output == null) {
			return null;
		}
		for (Object outputItem : output) {
			JSONObject outputObject = JSONUtil.parseObj(outputItem);
			JSONArray content = outputObject.getJSONArray("content");
			if (content == null) {
				continue;
			}
			for (Object contentItem : content) {
				JSONObject contentObject = JSONUtil.parseObj(contentItem);
				if ("output_text".equals(contentObject.getStr("type"))) {
					return contentObject.getStr("text");
				}
			}
		}
		return null;
	}

	private String buildTitle(String question) {
		String title = StrUtil.blankToDefault(question, "新会话").trim();
		return title.length() > 30 ? title.substring(0, 30) : title;
	}

	private static class OpenAiAnswer {
		private String answer;
		private Integer inputTokens;
		private Integer outputTokens;
		private Integer totalTokens;
	}
}
