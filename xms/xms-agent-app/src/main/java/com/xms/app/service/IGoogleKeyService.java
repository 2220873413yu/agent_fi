package com.xms.app.service;

import java.util.Map;

import com.xms.common.result.ResponseWrap;

public interface IGoogleKeyService {
	
	ResponseWrap<Map<String,Object>> getGoogleAuthKey(Long userId);

    ResponseWrap<String> checkGoogleAuthKey(Long userId, String autoCode);

    ResponseWrap<String> getAuthKey();
}
