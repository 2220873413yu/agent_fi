package com.xms.web.security.filter;

import cn.hutool.core.util.IdUtil;
import com.xms.common.constant.SysConstant;
import com.xms.common.core.domain.model.LoginUser;
import com.xms.common.utils.SecurityUtils;
import com.xms.common.utils.StringUtils;
import com.xms.web.service.TokenService;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * token过滤器 验证token有效性
 */
@Component
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {

	@Autowired
	private TokenService tokenService;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
		throws ServletException, IOException {
		try {
			MDC.put(SysConstant.CORRELATION_ID, IdUtil.fastUUID());
			String requestURI = request.getRequestURI();
			logger.info("请求url:" + requestURI);
			LoginUser loginUser = tokenService.getLoginUser(request);
			if (StringUtils.isNotNull(loginUser) && StringUtils.isNull(SecurityUtils.getAuthentication())) {
				tokenService.verifyToken(loginUser);
				UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(loginUser, null, loginUser.getAuthorities());
				authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
				SecurityContextHolder.getContext().setAuthentication(authenticationToken);
			}
			chain.doFilter(request, response);
		} finally {
			MDC.remove(SysConstant.CORRELATION_ID);
		}
	}

}
