package com.xms.web.service;

import cn.hutool.system.SystemUtil;
import com.xms.common.config.redis.XmsRedis;
import com.xms.common.constant.CacheConstants;
import com.xms.common.constant.ConstantStatic;
import com.xms.common.utils.ThreadLocalUtil;
import com.xms.common.utils.googleUtil.GoogleAuthenticator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import com.xms.common.core.domain.entity.SysUser;
import com.xms.common.core.domain.model.LoginUser;
import com.xms.common.enums.UserStatus;
import com.xms.common.exception.ServiceException;
import com.xms.common.utils.StringUtils;
import com.xms.system.service.ISysUserService;

/**
 * 用户验证处理
 *
 *
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService
{
    private static final Logger log = LoggerFactory.getLogger(UserDetailsServiceImpl.class);

    @Autowired
    private ISysUserService userService;

    @Autowired
    private SysPermissionService permissionService;

	@Autowired
	private SysPasswordService sysPasswordService;

	@Autowired
	private XmsRedis xmsRedis;

	@Autowired
	private Environment environment;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException
    {
        SysUser user = userService.selectUserByUserName(username);
        if (StringUtils.isNull(user))
        {
            log.info("登录用户：{} 不存在.", username);
            throw new ServiceException("登录用户：" + username + " 不存在");
        }
        else if (UserStatus.DELETED.getCode().equals(user.getDelFlag()))
        {
            log.info("登录用户：{} 已被删除.", username);
            throw new ServiceException("对不起，您的账号：" + username + " 已被删除");
        }
        else if (UserStatus.DISABLE.getCode().equals(user.getStatus()))
        {
            log.info("登录用户：{} 已被停用.", username);
            throw new ServiceException("对不起，您的账号：" + username + " 已停用");
        }

		String googleCode = ThreadLocalUtil.get(username);
		if (!SystemUtil.getOsInfo().getName().toUpperCase().contains(ConstantStatic.OS_NAME_WINDOWS)) {
			if (!GoogleAuthenticator.authcode(googleCode, user.getGoogleKey())) {
				xmsRedis.incr(CacheConstants.PWD_ERR_CNT_KEY + username);
				xmsRedis.expire(CacheConstants.PWD_ERR_CNT_KEY, Long.parseLong(environment.getProperty("user.password.lockTime")));
				throw new ServiceException("谷歌验证码错误");
			}
		}
        sysPasswordService.validate(user);

        return createLoginUser(user);
    }

    public UserDetails createLoginUser(SysUser user)
    {
        return new LoginUser(user.getUserId(), user.getDeptId(), user, permissionService.getMenuPermission(user));
    }
}
