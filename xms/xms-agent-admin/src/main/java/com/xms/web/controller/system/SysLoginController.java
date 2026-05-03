package com.xms.web.controller.system;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;

import com.alibaba.fastjson2.JSONObject;
import com.xms.common.config.redis.XmsRedis;
import com.xms.common.constant.*;
import com.xms.common.core.domain.api.ResultPista;
import com.xms.common.core.domain.entity.SysDictData;
import com.xms.common.exception.ServiceException;
import com.xms.common.result.ResponseCode;
import com.xms.common.utils.DictUtils;
import com.xms.system.service.ISysUserService;
import com.xms.web.service.SysLoginService;
import com.xms.web.service.SysPermissionService;
import com.xms.web.service.TokenService;

import com.xms.web.util.AliyunSenMailUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.xms.common.core.domain.AjaxResult;
import com.xms.common.core.domain.entity.SysMenu;
import com.xms.common.core.domain.entity.SysUser;
import com.xms.common.core.domain.model.LoginBody;
import com.xms.common.core.domain.model.LoginUser;
import com.xms.common.utils.SecurityUtils;
import com.xms.system.service.ISysMenuService;

/**
 * 登录验证
 */
@RestController
public class SysLoginController {
	@Autowired
	private SysLoginService loginService;

	@Autowired
	private ISysMenuService menuService;

	@Autowired
	private SysPermissionService permissionService;

	@Autowired
	private TokenService tokenService;

	@Autowired
	private XmsRedis xmsRedis;

	@Autowired
	private ISysUserService userService;

	/**
	 * 登录方法
	 *
	 * @param loginBody 登录信息
	 * @return 结果
	 */
	@PostMapping("/login")
	public AjaxResult login(@RequestBody LoginBody loginBody) {
		if(StrUtil.isBlankIfStr(loginBody)){
			throw new ServiceException("谷歌验证码不能为空");
		}
		AjaxResult ajax = AjaxResult.success();
		// 生成令牌
		String token = loginService.login(loginBody.getUsername(), loginBody.getPassword(), loginBody.getCode(),
			loginBody.getUuid(),loginBody.getGoogleCode());
		ajax.put(Constants.TOKEN, token);
/*		//下线之前登录的账号,只允许一个账号登录
		String loginToken = tokenService.getLoginUserUUID(token);
		Set<Object> keys = xmsRedis.keys(CacheConstants.LOGIN_TOKEN_KEY + "*");

		for (Object o : keys) {
			String key = (String) o;
			LoginUser user = JSONUtil.toBean(JSONObject.toJSONString((Object) xmsRedis.get(key)), LoginUser.class);
			if (user.getUsername().equals(loginBody.getUsername()) && !user.getToken().equals(loginToken)) {
				xmsRedis.del(CacheConstants.LOGIN_TOKEN_KEY + user.getToken());
			}
		}*/
		return ajax;
	}

	/**
	 * 获取用户信息
	 *
	 * @return 用户信息
	 */
	@GetMapping("getInfo")
	public AjaxResult getInfo() {
		SysUser user = SecurityUtils.getLoginUser().getUser();
		// 角色集合
		Set<String> roles = permissionService.getRolePermission(user);
		// 权限集合
		Set<String> permissions = permissionService.getMenuPermission(user);
		AjaxResult ajax = AjaxResult.success();
		ajax.put("user", user);
		ajax.put("roles", roles);
		ajax.put("permissions", permissions);
		return ajax;
	}

	/**
	 * 获取路由信息
	 *
	 * @return 路由信息
	 */
	@GetMapping("getRouters")
	public AjaxResult getRouters() {
		Long userId = SecurityUtils.getUserId();
		List<SysMenu> menus = menuService.selectMenuTreeByUserId(userId);
		return AjaxResult.success(menuService.buildMenus(menus));
	}
}
