package com.xms.web.core.config.canal;


import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import top.javatool.canal.client.handler.EntryHandler;
import top.javatool.canal.client.spring.boot.properties.CanalProperties;
import top.javatool.canal.client.spring.boot.properties.CanalSimpleProperties;

/**
 * 重写配置类 关闭canal
 *
 * @author MIER
 */
@Configuration
@EnableConfigurationProperties(CanalSimpleProperties.class)
@ConditionalOnBean(value = {EntryHandler.class})
@ConditionalOnProperty(value = CanalProperties.CANAL_MODE, havingValue = "simpleDiyOff", matchIfMissing = true)
public class SimpleDiyClientAutoOffConfiguration {


}
