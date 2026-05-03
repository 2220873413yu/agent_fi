package com.xms.common.utils;


import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.xms.common.constant.HttpStatus;
import com.xms.common.core.domain.model.LoginUser;
import com.xms.common.core.domain.model.xms.LoginAppUser;
import com.xms.common.exception.ServiceException;

/**
 * 安全服务工具类
 */
public class SecurityUtils {
	/**
	 * 用户ID
	 **/
	public static Long getUserId() {
		try {
			return getLoginUser().getUserId();
		} catch (Exception e) {
			throw new ServiceException("获取用户ID异常", HttpStatus.UNAUTHORIZED);
		}
	}

	/**
	 * 获取部门ID
	 **/
	public static Long getDeptId() {
		try {
			return getLoginUser().getDeptId();
		} catch (Exception e) {
			throw new ServiceException("获取部门ID异常", HttpStatus.UNAUTHORIZED);
		}
	}

	/**
	 * 获取用户账户
	 **/
	public static String getUsername() {
		try {
			return getLoginUser().getUsername();
		} catch (Exception e) {
			throw new ServiceException("获取用户账户异常", HttpStatus.UNAUTHORIZED);
		}
	}

	/**
	 * 获取用户
	 **/
	public static LoginUser getLoginUser() {
		try {
			return (LoginUser) getAuthentication().getPrincipal();
		} catch (Exception e) {
			throw new ServiceException("获取用户信息异常", HttpStatus.UNAUTHORIZED);
		}
	}

	/**
	 * 获取app用户
	 **/
	public static LoginAppUser getLoginAppUser() {
		try {
			return (LoginAppUser) getAuthentication().getPrincipal();
		} catch (Exception e) {
			throw new ServiceException("获取用户信息异常", HttpStatus.UNAUTHORIZED);
		}
	}
	public static Long getFrontUserId() {
		try {
			return getLoginAppUser().getUserId();
		} catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException("获取app用户ID异常", HttpStatus.UNAUTHORIZED);
		}
	}

	/**
	 * 获取Authentication
	 */
	public static Authentication getAuthentication() {
		return SecurityContextHolder.getContext().getAuthentication();
	}

	/**
	 * 生成BCryptPasswordEncoder密码
	 *
	 * @param password 密码
	 * @return 加密字符串
	 */
	public static String encryptPassword(String password) {
		BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		return passwordEncoder.encode(password);
	}

	/**
	 * list转string 用，分割
	 *
	 * @param mnemonicList
	 * @return
	 */
	public static String listToString(List<String> mnemonicList) {
		String join = String.join(",", mnemonicList);
		return join;
	}

	/**
	 * 判断密码是否相同
	 *
	 * @param rawPassword     真实密码
	 * @param encodedPassword 加密后字符
	 * @return 结果
	 */
	public static boolean matchesPassword(String rawPassword, String encodedPassword) {
		BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		return passwordEncoder.matches(rawPassword, encodedPassword);
	}

	/**
	 * 是否为管理员
	 *
	 * @param userId 用户ID
	 * @return 结果
	 */
	public static boolean isAdmin(Long userId) {
		return userId != null && 1L == userId;
	}




}
