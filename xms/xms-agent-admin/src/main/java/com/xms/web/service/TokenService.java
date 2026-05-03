package com.xms.web.service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import cn.hutool.core.util.IdUtil;
import com.xms.common.config.redis.XmsRedis;
import jakarta.servlet.http.HttpServletRequest;

import com.xms.common.core.domain.entity.SysUser;
import com.xms.common.exception.ServiceException;
import com.xms.system.mapper.SysUserMapper;
import io.jsonwebtoken.io.Decoders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.xms.common.constant.CacheConstants;
import com.xms.common.constant.Constants;
import com.xms.common.core.domain.model.LoginUser;
import com.xms.common.utils.ServletUtils;
import com.xms.common.utils.StringUtils;
import com.xms.common.utils.ip.AddressUtils;
import com.xms.common.utils.ip.IpUtils;
import eu.bitwalker.useragentutils.UserAgent;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

/**
 * token验证处理
 */
@Component
public class TokenService {
	protected static final long MILLIS_SECOND = 1000;
	protected static final long MILLIS_MINUTE = 60 * MILLIS_SECOND;
	private static final Long MILLIS_MINUTE_TEN = 20 * 60 * 1000L;
	// 令牌自定义标识
	@Value("${token.header}")
	private String header;
	// 令牌秘钥
	@Value("${token.secret}")
	private String secret;
	// 令牌有效期（默认30分钟）
	@Value("${token.expireTime}")
	private int expireTime;
	@Autowired
	private XmsRedis redisCache;

	@Autowired
	private SysUserMapper sysUserMapper;

	/**
	 * 获取用户身份信息
	 *
	 * @return 用户信息
	 */
	public LoginUser getLoginUser(HttpServletRequest request) {
		// 获取请求携带的令牌
		String token = getToken(request);
		if (StringUtils.isNotEmpty(token)) {
			try {
				Claims claims = parseToken(token);
				// 解析对应的权限以及用户信息
				String uuid = (String) claims.get(Constants.LOGIN_USER_KEY);
				String userKey = getTokenKey(uuid);
				LoginUser user = redisCache.get(userKey);
				return user;
			} catch (Exception e) {
			}
		}
		return null;
	}

	/**
	 * @return String
	 * @Title: getLoginUserUUID
	 * @param:
	 * @Description: 根据登录令牌获取tokenId
	 */
	public String getLoginUserUUID(String token) {
		Claims claims = parseToken(token);
		// 解析对应的权限以及用户信息
		String uuid = (String) claims.get(Constants.LOGIN_USER_KEY);
		return uuid;
	}

	/**
	 * 设置用户身份信息
	 */
	public void setLoginUser(LoginUser loginUser) {
		if (StringUtils.isNotNull(loginUser) && StringUtils.isNotEmpty(loginUser.getToken())) {
			refreshToken(loginUser);
		}
	}

	/**
	 * 删除用户身份信息
	 */
	public void delLoginUser(String token) {
		if (StringUtils.isNotEmpty(token)) {
			String userKey = getTokenKey(token);
			redisCache.del(userKey);
		}
	}

	/**
	 * 创建令牌
	 *
	 * @param loginUser 用户信息
	 * @return 令牌
	 */
	public String createToken(LoginUser loginUser) {
		String token = IdUtil.fastUUID();
		loginUser.setToken(token);
		setUserAgent(loginUser);
		refreshToken(loginUser);

		Map<String, Object> claims = new HashMap<>();
		claims.put(Constants.LOGIN_USER_KEY, token);
		return createToken(claims);
	}

	/**
	 * 验证令牌有效期，相差不足20分钟，自动刷新缓存
	 *
	 * @param loginUser
	 * @return 令牌
	 */
	public void verifyToken(LoginUser loginUser) {
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
	public void refreshToken(LoginUser loginUser) {
		int expire = expireTime;
		//判断该用户是否有登陆时长
		if (loginUser.getUser().getLoginDuration() != null && loginUser.getUser().getLoginDuration() != -1) {
			Integer duration = loginUser.getUser().getLoginDuration();//小时
			expire = duration * 60;
			if (duration == 0) {
				throw new ServiceException("对不起，您的账号：" + loginUser.getUsername() + " 已到使用时间，请联系管理员续登");
			}
			SysUser user = new SysUser();
			user.setUserId(loginUser.getUserId());
			user.setLoginDuration(0);
			sysUserMapper.updateUser(user);
		}

		loginUser.setLoginTime(System.currentTimeMillis());
		loginUser.setExpireTime(loginUser.getLoginTime() + expire * MILLIS_MINUTE);
		// 根据uuid将loginUser缓存
		String userKey = getTokenKey(loginUser.getToken());
		redisCache.set(userKey, loginUser,(long) expire, TimeUnit.MINUTES);
	}

	/**
	 * 设置用户代理信息
	 *
	 * @param loginUser 登录信息
	 */
	public void setUserAgent(LoginUser loginUser) {
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
	private String getToken(HttpServletRequest request) {
		String token = request.getHeader(header);
		if (StringUtils.isNotEmpty(token) && token.startsWith(Constants.TOKEN_PREFIX)) {
			token = token.replace(Constants.TOKEN_PREFIX, "");
		}
		return token;
	}

	private String getTokenKey(String uuid) {
		return CacheConstants.LOGIN_TOKEN_KEY + uuid;
	}
}
