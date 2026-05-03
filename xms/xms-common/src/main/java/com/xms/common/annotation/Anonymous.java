package com.xms.common.annotation;

import java.lang.annotation.*;

/**
 *  标记不需要登录认证的接口
 *
 *
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Anonymous
{
}
