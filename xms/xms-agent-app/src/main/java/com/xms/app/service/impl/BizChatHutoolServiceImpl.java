package com.xms.app.service.impl;

import cn.hutool.ai.AIUtil;
import cn.hutool.ai.core.Message;
import cn.hutool.ai.model.openai.OpenaiConfig;
import cn.hutool.ai.model.openai.OpenaiService;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.xms.app.entity.resp.ChatAskResp;
import com.xms.app.entity.vo.ChatAskVo;
import com.xms.app.service.BizChatHutoolService;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Hutool AI聊天业务Service实现
 */
@Service
@Slf4j
public class BizChatHutoolServiceImpl implements BizChatHutoolService {
	private static final int HISTORY_LIMIT = 10;
	private static final String ROLE_USER = "user";
	private static final String ROLE_ASSISTANT = "assistant";
	private static final String ROLE_SYSTEM = "system";

	private final IChatSessionService chatSessionService;
	private final IChatMessageService chatMessageService;

	@Value("${openai.api-key:}")
	private String openaiApiKey;

	@Value("${openai.base-url:https://api.openai.com/v1}")
	private String openaiBaseUrl;

	@Value("${openai.model:gpt-4o-mini}")
	private String openaiModel;

	public BizChatHutoolServiceImpl(IChatSessionService chatSessionService, IChatMessageService chatMessageService) {
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
		OpenAiAnswer openAiAnswer = requestByHutool(history, req);
		ChatMessage assistantMessage = saveAssistantMessage(userId, session.getId(), openAiAnswer);
		updateSession(session, req.getQuestion());

		ChatAskResp resp = new ChatAskResp();
		resp.setSessionId(session.getId());
		resp.setUserMessageId(userMessage.getId());
		resp.setAssistantMessageId(assistantMessage.getId());
		resp.setAnswer(openAiAnswer.answer);
		return ResultPista.data(resp);
	}

	private OpenAiAnswer requestByHutool(List<ChatMessage> history, ChatAskVo req) {
		if (StrUtil.isBlank(openaiApiKey)) {
			throw new ServiceException("OpenAI API Key未配置");
		}
		try {
			OpenaiService openaiService = buildOpenaiService();
			String rawResponse;
			if (CollectionUtil.isNotEmpty(req.getImageUrls())) {
				rawResponse = openaiService.chatVision(buildVisionPrompt(history, req), req.getImageUrls(), openaiModel);
			} else {
				rawResponse = openaiService.chat(buildMessages(history, req));
			}
			return parseOpenAiAnswer(rawResponse);
		} catch (ServiceException e) {
			throw e;
		} catch (Exception e) {
			log.error("Hutool AI聊天请求异常", e);
			throw new ServiceException("Hutool AI聊天请求异常：" + e.getClass().getSimpleName() + " " + StrUtil.blankToDefault(e.getMessage(), ""));
		}
	}

	private OpenaiService buildOpenaiService() {
		OpenaiConfig config = new OpenaiConfig(openaiApiKey);
		config.setApiUrl(openaiBaseUrl.replaceAll("/+$", ""));
		config.setModel(openaiModel);
		return AIUtil.getOpenAIService(config);
	}

	private List<Message> buildMessages(List<ChatMessage> history, ChatAskVo req) {
		List<Message> messages = new ArrayList<>();
		messages.add(new Message(ROLE_SYSTEM, "你是系统内置聊天助手。请结合用户最近历史消息回答，保持简洁准确。"));
		for (ChatMessage item : history) {
			messages.add(new Message(item.getRole(), buildHistoryContent(item)));
		}
		messages.add(new Message(ROLE_USER, req.getQuestion()));
		return messages;
	}

	private String buildVisionPrompt(List<ChatMessage> history, ChatAskVo req) {
		StringBuilder builder = new StringBuilder("你是系统内置聊天助手。请结合用户最近历史消息和本次图片回答，保持简洁准确。\n");
		if (CollectionUtil.isNotEmpty(history)) {
			builder.append("最近最多10条历史消息：\n");
			for (ChatMessage item : history) {
				builder.append(item.getRole()).append(": ").append(buildHistoryContent(item)).append('\n');
			}
		}
		builder.append("本次问题：").append(req.getQuestion());
		return builder.toString();
	}

	private String buildHistoryContent(ChatMessage item) {
		String content = StrUtil.nullToEmpty(item.getContent());
		if (StrUtil.isNotBlank(item.getImageUrls())) {
			content = content + "\n历史图片URL：" + item.getImageUrls();
		}
		return content;
	}

	private OpenAiAnswer parseOpenAiAnswer(String rawResponse) {
		JSONObject json = JSONUtil.parseObj(rawResponse);
		JSONObject error = json.getJSONObject("error");
		if (error != null) {
			throw new ServiceException("Hutool AI聊天请求失败：" + error.getStr("message"));
		}
		String answer = parseChoiceContent(json);
		if (StrUtil.isBlank(answer)) {
			throw new ServiceException("Hutool AI未返回有效回答");
		}

		OpenAiAnswer openAiAnswer = new OpenAiAnswer();
		openAiAnswer.answer = answer;
		JSONObject usage = json.getJSONObject("usage");
		if (usage != null) {
			openAiAnswer.inputTokens = usage.getInt("prompt_tokens");
			openAiAnswer.outputTokens = usage.getInt("completion_tokens");
			openAiAnswer.totalTokens = usage.getInt("total_tokens");
		}
		return openAiAnswer;
	}

	private String parseChoiceContent(JSONObject json) {
		JSONArray choices = json.getJSONArray("choices");
		if (choices == null || choices.isEmpty()) {
			return null;
		}
		JSONObject choice = JSONUtil.parseObj(choices.get(0));
		JSONObject message = choice.getJSONObject("message");
		if (message == null) {
			return null;
		}
		Object content = message.get("content");
		return content == null ? null : String.valueOf(content);
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
