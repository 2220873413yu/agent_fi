package com.xms.app.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import com.xms.common.config.redis.XmsRedis;
import com.xms.common.constant.CacheConstants;
import com.xms.common.constant.Constants;
import com.xms.common.core.domain.model.xms.LoginAppUser;
import com.xms.common.exception.ServiceException;
import com.xms.common.result.ResponseCode;
import com.xms.common.utils.Func;
import com.xms.common.utils.ServletUtils;
import com.xms.common.utils.StringUtils;
import com.xms.common.utils.ip.AddressUtils;
import com.xms.common.utils.ip.IpUtils;
import eu.bitwalker.useragentutils.UserAgent;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * token验证处理
 *
 * @author MIER
 */
@Component
@Slf4j
public class AppTokenService {
	/**
	 * 1000微妙=一秒
	 */
	protected static final long MILLIS_SECOND = 1000;
	/**
	 * 60s *1000微妙= 一分钟
	 */
	protected static final long MILLIS_MINUTE = 60 * MILLIS_SECOND;
	protected static final String AUTHORIZE_URL = "/api/oauth/authorize";
	/**
	 * 可以让旧token处理的情况
	 */
	protected static final String[] AUTHORIZE_OLD_URL = {"/api/open/tradeOrderCallback", "/api/open/tradeOrderSettleAction",
		"/api/open/tradeOrderBallSettleAction", "/api/open/tradeOrderRefund", "/api/open/tradeOrderBattleKillSettleAction"};
	/**
	 * 三天
	 */
	private static final Long MILLIS_MINUTE_TEN = 9 * 24 * 60 * 60 * 1000L;
	// 令牌自定义标识
	@Value("${token.header}")
	private String header;
	// 令牌秘钥
	@Value("${token.secret}")
	private String secret;
	/**
	 * 令牌有效期（默认30分钟）
	 */
	@Value("${token.expireTime}")
	private int expireTime;
	@Autowired
	private XmsRedis xmsRedis;

	/**
	 * 获取用户身份信息
	 *
	 * @return 用户信息
	 */
	public LoginAppUser getLoginUser(HttpServletRequest request) {
		// 获取请求携带的令牌
		String token = getToken(request);
		log.debug("request 拿到的token:{}", token);
		if (StringUtils.isNotEmpty(token)) {
			try {
				//判断字符串为null情况
				if ("null".equalsIgnoreCase(token)) {
					return null;
				}
				Claims claims = parseToken(token);
				// 解析对应的权限以及用户信息
				String uuid = (String) claims.get(Constants.LOGIN_APP_USER_KEY);
				String client_id = (String) claims.get(Constants.CLIENT_ID);
				String userKey = getTokenKey(client_id, uuid);
				if (!client_id.equals(Constants.TOKEN_APP_PREFIX)) {
					if (request.getRequestURI().equals(AUTHORIZE_URL)) {
						return null;
					}
					if (!client_id.equals(request.getHeader(Constants.CLIENT_ID_TO))) {
						throw new ServiceException(ResponseCode.CODE_1053);
					}
				}
				//不需要认证的
				if (ObjectUtil.contains(AUTHORIZE_OLD_URL, request.getRequestURI())) {
					return null;
				}
				LoginAppUser user = xmsRedis.get(userKey);
				//判定token是否存在
				if (user == null) {
					return null;
				}
				//如果token，代表重复登陆，会提示强踢
				if (!user.getToken().equals(claims.get(Constants.TOKEN_CODE))) {
					throw new ServiceException(ResponseCode.CODE_1005);
				}
				return user;
			} catch (Exception e) {
				if (e instanceof ServiceException) {
					throw e;
				}
				e.printStackTrace();
			}
		}
		return null;
	}

	public LoginAppUser verifyUser(HttpServletRequest request) {
		// 获取请求携带的令牌
		String token = getToken(request);
		if (StringUtils.isNotEmpty(token)) {
			//判断字符串为null情况
			if ("null".equalsIgnoreCase(token)) {
				return null;
			}
			Claims claims = parseToken(token);
			// 解析对应的权限以及用户信息
			String uuid = (String) claims.get(Constants.LOGIN_APP_USER_KEY);
			String client_id = (String) claims.get(Constants.CLIENT_ID);
			String userKey = getTokenKey(client_id, uuid);
			if (!client_id.equals(Constants.TOKEN_APP_PREFIX) && !client_id.equals(request.getHeader(Constants.CLIENT_ID_TO))) {
				throw new ServiceException(ResponseCode.CODE_1053);
			}
			LoginAppUser user = xmsRedis.get(userKey);
			//判定token是否存在
			if (user == null) {
				return null;
			}
			//如果token，不相等，代表被强踢了，旧token临时通过， 返回null
			if (!user.getToken().equals(claims.get(Constants.TOKEN_CODE))) {
				return null;
			}
			user.setToken(token);
			return user;
		}
		throw new ServiceException(ResponseCode.CODE_1005);
	}

	/**
	 * 设置用户身份信息
	 */
	public void setLoginUser(LoginAppUser loginUser) {
		if (StringUtils.isNotNull(loginUser) && StringUtils.isNotEmpty(loginUser.getToken())) {
			refreshToken(loginUser);
		}
	}

	/**
	 * 删除用户身份信息
	 */
	public void delLoginUser(String clientId, String userUniqueKey) {
		String userKey = getTokenKey(clientId, userUniqueKey);
		xmsRedis.del(userKey);
	}

	/**
	 * 创建令牌
	 *
	 * @param loginUser 用户信息
	 * @return 令牌  token
	 */
	public String createToken(LoginAppUser loginUser) {
		// 删除用户记录
		delLoginUser(loginUser.getClientId(), loginUser.getUserId().toString());
		String token = IdUtil.fastUUID();
		loginUser.setToken(token);
		setUserAgent(loginUser);
		refreshToken(loginUser);

		Map<String, Object> claims = new HashMap<>(4);
		claims.put(Constants.LOGIN_APP_USER_KEY, loginUser.getUserId().toString());
		claims.put(Constants.CLIENT_ID, loginUser.getClientId());
		claims.put(Constants.TOKEN_CODE, token);
		return createToken(claims);
	}

	/**
	 * 验证令牌有效期，相差不足20分钟，自动刷新缓存
	 *
	 * @param loginUser
	 * @return 令牌
	 */
	public void verifyToken(LoginAppUser loginUser) {
		long expireTime = loginUser.getExpireTime();
		long currentTime = System.currentTimeMillis();
		if (expireTime - currentTime <= MILLIS_MINUTE_TEN) {
			refreshToken(loginUser);
		}
	}

	/**
	 * 刷新令牌有效期
	 *
	 * @param loginUser 登录信息
	 */
	public void refreshToken(LoginAppUser loginUser) {
		loginUser.setLoginTime(System.currentTimeMillis());
		// 根据uuid将loginUser缓存
		String userKey = getTokenKey(loginUser.getClientId(), loginUser.getUserId().toString());
		loginUser.setExpireTime(loginUser.getLoginTime() + expireTime * MILLIS_MINUTE);
		xmsRedis.set(userKey, loginUser, Func.toLong(expireTime), TimeUnit.MINUTES);
	}

	/**
	 * 设置用户代理信息
	 *
	 * @param loginUser 登录信息
	 */
	public void setUserAgent(LoginAppUser loginUser) {
		UserAgent userAgent = UserAgent.parseUserAgentString(ServletUtils.getRequest().getHeader("User-Agent"));
		String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
		loginUser.setIpaddr(ip);
		loginUser.setLoginLocation(AddressUtils.getRealAddressByIP(ip));
		loginUser.setBrowser(userAgent.getBrowser().getName());
		loginUser.setOs(userAgent.getOperatingSystem().getName());
	}

	/**
	 * 从数据声明生成令牌
	 *
	 * @param claims 数据声明
	 * @return 令牌
	 */
	private String createToken(Map<String, Object> claims) {
		SecretKey key = new SecretKeySpec(Decoders.BASE64.decode(secret), SignatureAlgorithm.HS512.getJcaName());
		return Jwts.builder().setClaims(claims).signWith(key).compact();
	}

	/**
	 * 从令牌中获取数据声明
	 *
	 * @param token 令牌
	 * @return 数据声明
	 */
	private Claims parseToken(String token) {
		return Jwts.parserBuilder().setSigningKey(secret).build().parseClaimsJws(token).getBody();
	}

	/**
	 * 从令牌中获取用户名
	 *
	 * @param token 令牌
	 * @return 用户名
	 */
	public String getUsernameFromToken(String token) {
		Claims claims = parseToken(token);
		return claims.getSubject();
	}

	/**
	 * 获取请求token
	 *
	 * @param request
	 * @return token
	 */
	public String getToken(HttpServletRequest request) {
		String token = request.getHeader(header);
		if (StringUtils.isNotEmpty(token) && token.startsWith(Constants.TOKEN_APP_PREFIX)) {
			token = token.replace(Constants.TOKEN_APP_PREFIX, "");
		}
		return token;
	}

	/**
	 * 组装用户token的 key 名
	 *
	 * @param clientId
	 * @param userUniqueKey
	 * @return
	 */
	private String getTokenKey(String clientId, String userUniqueKey) {
		return CacheConstants.LOGIN_APP_TOKEN_KEY + clientId + userUniqueKey;
	}


}
