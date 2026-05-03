

package com.xms.common.config.redis.stream;


import cn.hutool.core.thread.ThreadUtil;
import com.xms.common.constant.Constants;
import com.xms.common.utils.CharPool;
import com.xms.common.utils.StringUtil;
import com.xms.common.utils.ip.IpUtils;
import com.xms.common.utils.spring.SpringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.stream.StreamMessageListenerContainer.StreamMessageListenerContainerOptions;
import org.springframework.util.ErrorHandler;

import java.time.Duration;
import java.util.Map;

/**
 * redis Stream  配置
 *
 * @author renegade
 */
@Configuration
@EnableConfigurationProperties(XmsStreamRedisProperties.class)
@ConditionalOnProperty(prefix = XmsStreamRedisProperties.PREFIX, name = "enable", havingValue = "true")
@Slf4j
public class RedisStreamConfiguration {
	private StreamMessageListenerContainer<String, MapRecord<String, String, byte[]>> streamMessageListenerContainer;
	@Bean
	public RenegadeStreamTemplate streamTemplate(RedisTemplate redisTemplate) {
		return new DefaultXmsStreamTemplate(redisTemplate);
	}
	@Bean
	@ConditionalOnMissingBean
	public StreamMessageListenerContainerOptions<String, MapRecord<String, String, byte[]>> streamMessageListenerContainerOptions(XmsStreamRedisProperties streamProperties,
                                                                                                                                  ObjectProvider<ErrorHandler> errorHandlerObjectProvider,
                                                                                                                                  RedisTemplate redisTemplate,
                                                                                                                                  ObjectProvider<ServerProperties> serverPropertiesObjectProvider,
                                                                                                                                  Environment environment) {
		StreamMessageListenerContainer.StreamMessageListenerContainerOptionsBuilder<String, MapRecord<String, String, byte[]>> builder = StreamMessageListenerContainerOptions
			.builder()
			.keySerializer(RedisSerializer.string())
			.hashKeySerializer(RedisSerializer.string())
			.hashValueSerializer(RedisSerializer.byteArray());
		// 批量大小
		Integer pollBatchSize = streamProperties.getPollBatchSize();
		if (pollBatchSize != null && pollBatchSize > 0) {
			builder.batchSize(pollBatchSize);
		}
		// poll 超时时间
		Duration pollTimeout = streamProperties.getPollTimeout();
		if (pollTimeout != null && !pollTimeout.isNegative()) {
			builder.pollTimeout(pollTimeout);
		}
		// errorHandler
		errorHandlerObjectProvider.ifAvailable((builder::errorHandler));
		//  L.cm executor
		builder.executor(SpringUtils.getBean("asyncVirtualExecutor"));
		builder.errorHandler(t -> {
			log.error("RedisStreamConfiguration 是否能不做", t);
			//出现错误以后，重启监听器，以及重新订阅
			// if (t instanceof RedisConnectionFailureException || t instanceof RedisException) {
			// }
			boolean running = false;
			while (!running) {
				log.warn("redis stream try to auto connect");
				streamMessageListenerContainer.start();
				manualInitSubicrie(streamMessageListenerContainer, redisTemplate, serverPropertiesObjectProvider, streamProperties, environment);
				running = streamMessageListenerContainer.isRunning();
				ThreadUtil.sleep(5000L);
			}

		});
		return builder.build();
	}

	@Bean
	@ConditionalOnMissingBean
	public StreamMessageListenerContainer<String, MapRecord<String, String, byte[]>> streamMessageListenerContainer(RedisConnectionFactory redisConnectionFactory,
																													StreamMessageListenerContainerOptions<String, MapRecord<String, String, byte[]>> streamMessageListenerContainerOptions) {
		this.streamMessageListenerContainer =
			StreamMessageListenerContainer.create(redisConnectionFactory, streamMessageListenerContainerOptions);
		return this.streamMessageListenerContainer;
	}


	/**
     * 手动订阅的干活
	 * @param streamMessageListenerContainer
     * @param redisTemplate
     * @param serverPropertiesObjectProvider
     * @param streamProperties
     * @param environment
	 */
	public void manualInitSubicrie(StreamMessageListenerContainer<String, MapRecord<String, String, byte[]>> streamMessageListenerContainer,
								   RedisTemplate redisTemplate,
								   ObjectProvider<ServerProperties> serverPropertiesObjectProvider,
								   XmsStreamRedisProperties streamProperties,
								   Environment environment) {    // 消费组名称
		String consumerGroup = getConsumerGroup(streamProperties, environment);
		// 消费者名称
		String consumerName = getConsumerName(serverPropertiesObjectProvider, streamProperties);
		//异常了，重新订阅，需要清空，核查是否需要额外补偿的。
		XmsStreamListenerDetector.queueIds.clear();
		for (Map.Entry<String, Object> entry : XmsStreamListenerDetector.beans.entrySet()) {
			new XmsStreamListenerDetector(streamMessageListenerContainer, redisTemplate, consumerGroup, consumerName,streamTemplate(redisTemplate))
				.manualInitialization(entry.getValue(), entry.getKey());
		}


	}

	private String getConsumerGroup(XmsStreamRedisProperties streamProperties, Environment environment) {
		String consumerGroup = streamProperties.getConsumerGroup();
		if (StringUtil.isBlank(consumerGroup)) {
			String appName = environment.getRequiredProperty(Constants.SPRING_APP_NAME_KEY);
			String profile = environment.getProperty(Constants.ACTIVE_PROFILES_PROPERTY);
			consumerGroup = StringUtil.isBlank(profile) ? appName : appName + CharPool.COLON + profile;
		}
		return consumerGroup;
	}

	@Bean
	@ConditionalOnMissingBean
	public XmsStreamListenerDetector streamListenerDetector(StreamMessageListenerContainer<String, MapRecord<String, String, byte[]>> streamMessageListenerContainer,
                                                            RedisTemplate redisTemplate,
                                                            ObjectProvider<ServerProperties> serverPropertiesObjectProvider,
                                                            XmsStreamRedisProperties streamProperties,
                                                            Environment environment) {
		// 消费组名称
		String consumerGroup = getConsumerGroup(streamProperties, environment);
		// 消费者名称
		String consumerName = getConsumerName(serverPropertiesObjectProvider, streamProperties);
		return new XmsStreamListenerDetector(streamMessageListenerContainer, redisTemplate, consumerGroup, consumerName,streamTemplate(redisTemplate));
	}

	private String getConsumerName(ObjectProvider<ServerProperties> serverPropertiesObjectProvider, XmsStreamRedisProperties streamProperties) {
		String consumerName = streamProperties.getConsumerName();
		if (StringUtil.isBlank(consumerName)) {
			final StringBuilder consumerNameBuilder = new StringBuilder(IpUtils.getHostIp());
			serverPropertiesObjectProvider.ifAvailable(serverProperties -> {
				consumerNameBuilder.append(CharPool.COLON).append(serverProperties.getPort());
			});
			consumerName = consumerNameBuilder.toString();
		}
		return consumerName;
	}



}
