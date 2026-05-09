package com.xms.app.service;

import com.xms.app.entity.req.OpenAiActionReq;

import java.util.Map;

public interface OpenAiService {
	int openAiAction(OpenAiActionReq params);
}
