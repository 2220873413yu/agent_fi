package com.xms.app.security.handle;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import com.alibaba.fastjson2.JSON;
import com.xms.app.service.impl.AppTokenService;
import com.xms.common.core.domain.model.xms.LoginAppUser;
import com.xms.common.result.ResponseWrap;
import com.xms.common.utils.ServletUtils;
import com.xms.common.utils.StringUtils;


/**
 * 自定义退出处理类 返回成功
 *
 *
 */
@Configuration
public class LogoutSuccessHandlerImpl implements LogoutSuccessHandler
{
    @Autowired
    private AppTokenService appTokenService;

    /**
     * 退出处理
     *
     * @return
     */
    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException
    {
        LoginAppUser loginUser = appTokenService.getLoginUser(request);
        if (StringUtils.isNotNull(loginUser))
        {
            // 删除用户缓存记录
			appTokenService.delLoginUser(loginUser.getClientId(), loginUser.getUserId().toString());
        }
        ServletUtils.renderString(response, JSON.toJSONString(ResponseWrap.successWithEmpty()));
    }
}
