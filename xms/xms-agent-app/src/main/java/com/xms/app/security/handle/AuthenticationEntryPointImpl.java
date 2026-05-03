package com.xms.app.security.handle;

import java.io.IOException;
import java.io.Serializable;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.alibaba.fastjson.JSON;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.xms.common.result.ResponseCode;
import com.xms.common.result.ResponseWrap;
import com.xms.common.utils.ServletUtils;

/**
 * 认证失败处理类 返回未授权
 *
 *
 */
@Component
public class AuthenticationEntryPointImpl implements AuthenticationEntryPoint, Serializable
{
    private static final long serialVersionUID = -8970718410437077606L;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException e)
            throws IOException
    {
        ServletUtils.renderString(response, JSON.toJSONString(ResponseWrap.loginAuth(ResponseCode.CODE_1012)));
    }
}
