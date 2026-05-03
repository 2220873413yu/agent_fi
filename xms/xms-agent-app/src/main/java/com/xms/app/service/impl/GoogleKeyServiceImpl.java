package com.xms.app.service.impl;

import com.xms.app.service.IGoogleKeyService;
import com.xms.common.config.redis.XmsRedis;
import com.xms.common.constant.CacheConstants;
import com.xms.common.result.ResponseCode;
import com.xms.common.result.ResponseWrap;
import com.xms.common.utils.googleUtil.GoogleAuthenticator;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class GoogleKeyServiceImpl implements IGoogleKeyService {

    @Autowired
    private XmsRedis redisCache;

    @Override
    public ResponseWrap<Map<String,Object>> getGoogleAuthKey(Long userId) {
        Map<String,Object> respondMap = new HashMap<>();
      //获取用户信息
//      SysUser account = userService.selectUserById(userId);
//      if(StringUtils.isEmpty(account.getGoogleKey())) {
          log.info("开始google获取key:" + LocalDateTime.now());
          //获取谷歌KEY
          String auth_key = GoogleAuthenticator.generateSecretKey();
          log.info("结束google获取key:" + LocalDateTime.now());
          String key = CacheConstants.user_google_auth_key + userId;
          redisCache.set(key, auth_key, 3600L, TimeUnit.SECONDS);
          log.info("redis存储key成功:" + LocalDateTime.now());
          respondMap.put("auth_key", auth_key);
          respondMap.put("is_bind", "0");
//      } else {
//          respondMap.put("is_bind", "1");
//      }
      return ResponseWrap.success(respondMap);
    }

    @Override
    public ResponseWrap<String> checkGoogleAuthKey(Long userId, String autoCode) {
    	//获取谷歌KEY
        log.info("开始redis获取key:" + LocalDateTime.now());
        String key = CacheConstants.user_google_auth_key + userId;
        Object auth_key = redisCache.get(key);
        log.info("结束redis获取key:" + LocalDateTime.now());
        if(auth_key == null || StringUtils.isEmpty(auth_key.toString())) {
            return ResponseWrap.dataError(ResponseCode.CODE_1011);
        }
        //验证谷歌验证码
        if(!GoogleAuthenticator.authcode(autoCode, auth_key.toString())) {
        	return ResponseWrap.dataError(ResponseCode.CODE_1011);
        }
        log.info("验证google验证码结束:" + LocalDateTime.now());
        //验证成功
//        SysUser user = new SysUser();
//        user.setUserId(userId);
//        user.setGoogleKey(String.valueOf(auth_key));
//        int num = userMapper.updateUser(user);
        log.info("写入数据库:" + LocalDateTime.now());
//        if(num != 1) {
//            return AjaxResult.error("谷歌验证码绑定失败，请重试");
//        }
        return ResponseWrap.successWithEmpty();
    }

    @Override
    public ResponseWrap<String> getAuthKey() {
        //获取谷歌KEY
        String auth_key = GoogleAuthenticator.generateSecretKey();
        return ResponseWrap.success(auth_key);
    }
}
