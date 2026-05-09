package com.xms.app.security.filter;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.system.SystemUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.xms.app.handler.CustomException;
import com.xms.common.config.redis.XmsRedis;
import com.xms.common.utils.ServletUtils;
import com.xms.common.utils.ip.IpUtils;
import com.xms.dao.service.UserInfoService;
import com.xms.app.service.impl.AppTokenService;
import com.xms.common.constant.ConstantStatic;
import com.xms.common.constant.ConstantType;
import com.xms.common.constant.SysConstant;
import com.xms.common.core.domain.api.ResultPista;
import com.xms.common.core.domain.model.xms.LoginAppUser;
import com.xms.common.exception.ServiceException;
import com.xms.common.result.ResponseCode;
import com.xms.common.result.ResponseWrap;
import com.xms.common.utils.SecurityUtils;
import com.xms.common.utils.StringUtil;
import com.xms.common.utils.StringUtils;
import com.xms.common.utils.rsa.AESUtil;
import com.xms.common.utils.rsa.RSAUtilApp;
import com.xms.common.utils.rsa.SysSecurityKeyConstant;
import com.xms.common.utils.rsa.WrapperedRequest;
import com.xms.common.utils.sign.Base64;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * token过滤器 验证token有效性
 *
 * @author MIER
 */
@Component
@Slf4j
public class JwtAuthenticationAppTokenFilter extends OncePerRequestFilter {

	private static final String URL_PREFIX_UPLOAD = "/api/common/upload";
	/**
	 * 用户在链上购买了节点回调事件
	 */
	private static final String URL_NOTIFY_NODE_PACKAGE_CALLBACK = "/api/notify/nodePackage";
	private static final String ERROR_PARAM = "参数处理异常";

	/**
	 * 提现回调
	 */
	private static final String URL_WITHDRAWAL_CALLBACK = "/api/withdrawal/callback";

	/**
	 * 充值回调
	 */
	private static final String URL_STAKE_ORDER_CALL_BACK = "/api/nodeOrder/callback";

	/**
	 * df资产划转
	 */
	private static final String URL_NOTIFY_DF_TRANSFER = "/api/notify/dfTransfer";

	//黑名单拦截的ip
	public static List<String> ipAddressList = new ArrayList<>(50);

//	/**
//	 * 用户在链上进行swap
//	 */
//	private static final String URL_NOTIFY_SWAP_ORDER_CALLBACK = "/api/notify/swapOrder";
//
//	/**
//	 * 用户支付成功 创建激活码订单，回调接口(支付激活币)
//	 */
//	private static final String URL_NOTIFY_ACTIVE_ORDER_CALLBACK = "/api/activeOrder/callback";
//
//	/**
//	 * 领取空投回调
//	 */
//	private static final String URL_NOTIFY_CLAIM_AIRDROP_CALLBACK = "/api/claimAirdrop/callback";
	@Autowired
	private AppTokenService appTokenService;
	@Autowired
	private UserInfoService userInfoService;
	@Autowired
	private XmsRedis xmsredis;
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
		throws ServletException, IOException {
		ResponseCode.setLang(resolveLanguage(request.getHeader("Accept-Language")));
		try {
			MDC.put(SysConstant.CORRELATION_ID, IdUtil.fastUUID());
			String requestURI = request.getRequestURI();
			String resIp = IpUtils.getIpAddr(ServletUtils.getRequest());
			log.info("请求url:{},resIp:{}", request.getRequestURI(), resIp);
			if (SystemUtil.getOsInfo().getName().toUpperCase().contains(ConstantStatic.OS_NAME_WINDOWS)
				|| requestURI.contains(URL_PREFIX_UPLOAD)
				//质押订单回调
				|| requestURI.contains(URL_STAKE_ORDER_CALL_BACK)
				|| requestURI.contains("/sysnotice/getBannerList")
				//提现回调
				|| requestURI.contains(URL_WITHDRAWAL_CALLBACK)
				|| requestURI.contains("/api/notify/recharge")
			) {
				LoginAppUser loginAppUser = appTokenService.getLoginUser(request);
				if (StringUtils.isNotNull(loginAppUser) && StringUtils.isNull(SecurityUtils.getAuthentication())) {
					UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(loginAppUser, null, loginAppUser.getAuthorities());
					authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
					SecurityContextHolder.getContext().setAuthentication(authenticationToken);
				}
				chain.doFilter(request, response);
			} else {
				try {
					// 接收到请求，记录请求内容
					ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
					HttpServletRequest req = attributes.getRequest();
					this.requestDataDecrypt(req, response, chain,request);
				} catch (Exception e) {
					e.printStackTrace();
					log.error(ERROR_PARAM);
					if (e instanceof ServiceException || e instanceof CustomException) {
						throw new ServiceException(e.getMessage(),((ServiceException) e).getCode());
						//writeResponse(response, JSON.toJSONString(ResultPista.fail(((ServiceException) e).getCode(), e.getMessage())));
					} else {
						//text/html;charset=utf-8
						response.setContentType(String.format("%s;charset=utf-8", MediaType.TEXT_HTML_VALUE));
						writeResponse(response, JSONObject.toJSONString(ResponseWrap.dataError(ResponseCode.CODE_1000, ERROR_PARAM)));
					}
				}
			}
		} finally {
			MDC.remove(SysConstant.CORRELATION_ID);
			ResponseCode.clearLang();
		}
	}

	/**
	 * 数据解密
	 *
	 * @param request
	 * @param response
	 * @param chain
	 * @throws Exception
	 */
	private void requestDataDecrypt(ServletRequest request, ServletResponse response, FilterChain chain,HttpServletRequest request1)
		throws Exception {
		JSONObject param = new JSONObject();
		WrapperedRequest wrapRequest;
		String context;
		String contentType = request.getContentType();
		String requestBody = getRequestBody((HttpServletRequest) request, contentType);
		log.debug("加密密文：{}", requestBody);
		String key = ((HttpServletRequest) request).getHeader("key");
		//Base64.encode(encryptByPublicKey(AESUtil.KEY.getBytes(),SysSecurityKeyConstant.publicKey_app));
		log.debug("加密KEY值为：{}", key);
		//校验参数Token
		if (!StringUtil.isEmpty(requestBody)) {
			//解密数据
			byte[] hexStringToBytes = Base64.decode(key);
			byte[] decryptByPrivateKey = RSAUtilApp.decryptByPrivateKey(hexStringToBytes, SysSecurityKeyConstant.privateKey_app);
			log.debug("解密KEY值为：{}", new String(decryptByPrivateKey, StandardCharsets.UTF_8));
			context = AESUtil.aesDecrypt(requestBody, new String(decryptByPrivateKey, StandardCharsets.UTF_8));
			log.info("解密请求参数：{}", context);
			param = JSONObject.parseObject(context);
			//将参数转化成JSON对象
		}
		String resIp = IpUtils.getIpAddr(ServletUtils.getRequest());
		log.info("请求url:{},resIp:{}", request1.getRequestURI(), resIp);
		if(ipAddressList.contains(resIp)){
			log.error("IP黑名单拦截,resIp:{},请求url:{}", resIp, request1.getRequestURI());
//			writeResponse(response, JSONObject.toJSONString(ResultPista.fail(ResponseCode.CODE_107.getCode(), ResponseCode.CODE_107.getMsg())));
//			return;
		}
		//将校验后的参数写入Request
		wrapRequest = new WrapperedRequest((HttpServletRequest) request, param.toJSONString());
		//校验token有效性
		LoginAppUser appUser = appTokenService.getLoginUser(wrapRequest);
		if (StringUtils.isNotNull(appUser) && StringUtils.isNull(SecurityUtils.getAuthentication())) {
			//查询用户状态
			int status = userInfoService.getUserStatus(appUser.getUserId());
			if (status != ConstantType.open_or_close.type_1) {
				writeResponse(response, JSON.toJSONString(ResponseWrap.loginAuth(ResponseCode.CODE_401)));
				//throw new ServiceException(ResponseCode.CODE_401);
				return;
			}
			UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(appUser, null, appUser.getAuthorities());
			authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(wrapRequest));
			SecurityContextHolder.getContext().setAuthentication(authenticationToken);
		}
		chain.doFilter(wrapRequest, response);
	}

	private String getRequestBody(HttpServletRequest req, String contentType) {
		String json = "";
		try {
			log.debug("接口请求类型:{}", contentType);
			if (contentType == null || contentType.contains(MediaType.APPLICATION_JSON_VALUE)) {
				BufferedReader reader = req.getReader();
				StringBuilder sb = new StringBuilder();
				String line;
				while ((line = reader.readLine()) != null) {
					sb.append(line);
				}
				json = sb.toString();
			} else if (contentType.contains(MediaType.APPLICATION_FORM_URLENCODED_VALUE)) {
				json = req.getParameter("data");
			} else {
				if (req.getQueryString() != null && !"".equals(req.getQueryString())) {
					json = req.getQueryString();
				}
			}
			log.info("获取到的请求数据:{}", json);
		} catch (IOException e) {
			log.error("请求体读取失败", e);
		}
		return json;
	}

	private void writeResponse(ServletResponse response, String responseString)
		throws IOException {
		response.setContentType(String.format("%s;charset=utf-8", MediaType.TEXT_HTML_VALUE));
		PrintWriter out = response.getWriter();
		out.print(responseString);
		out.flush();
		out.close();
	}

	private String resolveLanguage(String header) {
		if (StrUtil.isBlank(header)) {
			return "zh";
		}
		String lang = header.trim();
		if (StrUtil.isBlank(lang)) {
			return "zh";
		}
		String normalized = normalizeLang(lang);
		return normalized == null ? "zh" : normalized;
	}

	private String normalizeLang(String lang) {
		String lower = lang.toLowerCase();
		if (lower.startsWith("zh-tw")) {
			return "zh-TW";
		}
		if (lower.startsWith("zh")) {
			return "zh";
		}
		if (lower.startsWith("en")) {
			return "en";
		}
		if (lower.startsWith("ja")) {
			return "ja";
		}
		if (lower.startsWith("ko")) {
			return "ko";
		}
		if (lower.startsWith("my") || lower.startsWith("ms")) {
			return "my";
		}
		if (lower.startsWith("vi")) {
			return "vi";
		}
		return null;
	}

}
