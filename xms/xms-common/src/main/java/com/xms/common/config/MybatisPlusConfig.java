package com.xms.common.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.injector.ISqlInjector;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.xms.common.config.mybatis.handler.CustomerSqlInjector;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Mybatis-plus 配置类
 *
 * @author MIER
 * @since 2020-10-10
 */
@Configuration
@MapperScan("com.xms.**.mapper")
public class MybatisPlusConfig {

	@Bean
	public MybatisPlusInterceptor mybatisPlusInterceptor() {
		MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
		interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
		return interceptor;
	}

	/**
	 * 自定义sql注入器
	 * 关键部位重要的事情说三遍，不注入不生效，不注入不生效，不注入不生效
	 */
	@Bean
	public ISqlInjector iSqlInjector() {
		return new CustomerSqlInjector();
	}

}
