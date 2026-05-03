package com.xms.web.core.config.canal;


import com.alibaba.otter.canal.protocol.CanalEntry;
import com.xms.web.core.serializer.IMessageSerializer;
import com.xms.web.core.serializer.JacksonMessageSerializer;
import com.xms.web.service.impl.AsyncDiyMessageHandlerImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import top.javatool.canal.client.factory.EntryColumnModelFactory;
import top.javatool.canal.client.handler.EntryHandler;
import top.javatool.canal.client.handler.MessageHandler;
import top.javatool.canal.client.handler.RowDataHandler;
import top.javatool.canal.client.handler.impl.RowDataHandlerImpl;
import top.javatool.canal.client.handler.impl.SyncMessageHandlerImpl;
import top.javatool.canal.client.spring.boot.autoconfigure.ThreadPoolAutoConfiguration;
import top.javatool.canal.client.spring.boot.properties.CanalProperties;
import top.javatool.canal.client.spring.boot.properties.CanalSimpleProperties;

import java.util.List;

/**
 * 重写配置类
 *
 * @author MIER
 */
@Configuration
@EnableConfigurationProperties(CanalSimpleProperties.class)
@ConditionalOnBean(value = {EntryHandler.class})
@ConditionalOnProperty(value = CanalProperties.CANAL_MODE, havingValue = "simpleDiy", matchIfMissing = true)
@Import(ThreadPoolAutoConfiguration.class)
public class SimpleDiyClientAutoConfiguration {


	private final CanalSimpleProperties canalSimpleProperties;


	public SimpleDiyClientAutoConfiguration(CanalSimpleProperties canalSimpleProperties) {
		this.canalSimpleProperties = canalSimpleProperties;
	}


	/**
	 * binlog解析器
	 *
	 * @return
	 */
	@Bean
	public RowDataHandler<CanalEntry.RowData> rowDataHandler() {
		return new RowDataHandlerImpl(new EntryColumnModelFactory());
	}

	/**
	 * 异步，执行保证数据一致性
	 * 消息处理器
	 *
	 * @param rowDataHandler binlog解析器
	 * @param entryHandlers  监听器。需要重写的
	 */
	@Bean
	@ConditionalOnProperty(value = CanalProperties.CANAL_ASYNC, havingValue = "true")
	public MessageHandler messageHandler(RowDataHandler<CanalEntry.RowData> rowDataHandler, List<EntryHandler> entryHandlers) {
		return new AsyncDiyMessageHandlerImpl(entryHandlers, rowDataHandler);
	}

	/**
	 * 同步 确认机制，强一致性
	 * 消息处理器
	 *
	 * @param rowDataHandler binlog解析器
	 * @param entryHandlers  监听器。需要重写的@return
	 */
	@Bean
	@ConditionalOnProperty(value = CanalProperties.CANAL_ASYNC, havingValue = "false")
	public MessageHandler messageHandler1(RowDataHandler<CanalEntry.RowData> rowDataHandler, List<EntryHandler> entryHandlers) {
		return new SyncMessageHandlerImpl(entryHandlers, rowDataHandler);
	}

	/**
	 * 消息序列化
	 */
	@Bean
	public IMessageSerializer messageSerializer() {
		return new JacksonMessageSerializer();
	}
	/**
	 * 启动客户端
	 *
	 * @param messageHandler 消息处理器
	 * @return
	 */
	@Bean(initMethod = "start", destroyMethod = "stop")
	public SimpleDiyCanalClient simpleCanalClient(MessageHandler messageHandler) {
		String server = canalSimpleProperties.getServer();
		String[] array = server.split(":");
		return SimpleDiyCanalClient.builder()
			.hostname(array[0])
			.port(Integer.parseInt(array[1]))
			.destination(canalSimpleProperties.getDestination())
			.userName(canalSimpleProperties.getUserName())
			.password(canalSimpleProperties.getPassword())
			.messageHandler(messageHandler)
			.retryTimes(10)
			.retryInterval(3000)
			.batchSize(canalSimpleProperties.getBatchSize())
			.filter(canalSimpleProperties.getFilter())
			.timeout(canalSimpleProperties.getTimeout())
			.unit(canalSimpleProperties.getUnit())
			.build();
	}
}
