

package com.xms.common.config;

import com.xms.common.annotation.impl.LogTraceAspect;
import com.xms.common.props.XmsProperties;
import com.xms.common.props.XmsPropertySourcePostProcessor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

/**
 * 自定义配置
 * xms property config
 *
 * @author MIER
 */
@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE)
@EnableConfigurationProperties(XmsProperties.class)
public class XmsPropertyConfiguration {

	@Bean
	public XmsPropertySourcePostProcessor xmsPropertySourcePostProcessor() {
		return new XmsPropertySourcePostProcessor();
	}
	@Bean
	public LogTraceAspect logTraceAspect() {
		return new LogTraceAspect();
	}
}
