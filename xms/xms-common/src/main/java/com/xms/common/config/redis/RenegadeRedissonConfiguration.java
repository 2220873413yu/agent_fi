
package com.xms.common.config.redis;


import com.xms.common.config.redis.delayqueue.config.RedissonConfigUtils;
import com.xms.common.config.redis.delayqueue.config.RedissonQueueBeanPostProcessor;
import com.xms.common.config.redis.delayqueue.config.RedissonQueueRegistry;
import com.xms.common.config.redis.delayqueue.config.RedissonTemplate;
import com.xms.common.config.redis.lock.RedisLockAspect;
import com.xms.common.config.redis.lock.RedisLockClient;
import com.xms.common.config.redis.lock.RedisLockClientImpl;
import jodd.util.StringUtil;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.*;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

/**
 * 分布式锁自动化配置
 */

@Configuration
@ConditionalOnClass(RedissonClient.class)
@EnableConfigurationProperties(RenegadeRdissonProperties.class)
@ConditionalOnProperty(value = "renegade.redisson.enabled", havingValue = "true")
public class RenegadeRedissonConfiguration {

	private static Config singleConfig(RenegadeRdissonProperties properties) {
		Config config = new Config();
		config.setThreads(properties.getThreads());
		config.setNettyThreads(properties.getNettyThreads());
		config.setLockWatchdogTimeout(properties.getLockWatchdogTimeoutMillis());
		SingleServerConfig serversConfig = config.useSingleServer();
		serversConfig.setAddress(properties.getAddress());
		String password = properties.getPassword();
		if (StringUtil.isNotBlank(password)) {
			serversConfig.setPassword(password);
		}
		serversConfig.setDatabase(properties.getDatabase());
		serversConfig.setConnectionPoolSize(properties.getPoolSize());
		serversConfig.setConnectionMinimumIdleSize(properties.getIdleSize());
		serversConfig.setIdleConnectionTimeout(properties.getConnectionTimeout());
		serversConfig.setConnectTimeout(properties.getConnectionTimeout());
		serversConfig.setTimeout(properties.getTimeout());

		serversConfig.setRetryAttempts(properties.getRetryAttempts());
		serversConfig.setRetryInterval(properties.getRetryIntervalMillis());
		return config;
	}

	private static Config masterSlaveConfig(RenegadeRdissonProperties properties) {
		Config config = new Config();
		config.setThreads(properties.getThreads());
		config.setNettyThreads(properties.getNettyThreads());
		config.setLockWatchdogTimeout(properties.getLockWatchdogTimeoutMillis());
		config.setThreads(properties.getThreads());
		config.setNettyThreads(properties.getNettyThreads());
		config.setLockWatchdogTimeout(properties.getLockWatchdogTimeoutMillis());
		MasterSlaveServersConfig serversConfig = config.useMasterSlaveServers();
		serversConfig.setMasterAddress(properties.getMasterAddress());
		serversConfig.addSlaveAddress(properties.getSlaveAddress());
		String password = properties.getPassword();
		if (StringUtil.isNotBlank(password)) {
			serversConfig.setPassword(password);
		}
		serversConfig.setDatabase(properties.getDatabase());
		serversConfig.setMasterConnectionPoolSize(properties.getPoolSize());
		serversConfig.setMasterConnectionMinimumIdleSize(properties.getIdleSize());
		serversConfig.setSlaveConnectionPoolSize(properties.getPoolSize());
		serversConfig.setSlaveConnectionMinimumIdleSize(properties.getIdleSize());
		serversConfig.setIdleConnectionTimeout(properties.getConnectionTimeout());
		serversConfig.setConnectTimeout(properties.getConnectionTimeout());
		serversConfig.setTimeout(properties.getTimeout());

		serversConfig.setRetryAttempts(properties.getRetryAttempts());
		serversConfig.setRetryInterval(properties.getRetryIntervalMillis());
		return config;
	}

	private static Config sentinelConfig(RenegadeRdissonProperties properties) {
		Config config = new Config();
		config.setThreads(properties.getThreads());
		config.setNettyThreads(properties.getNettyThreads());
		config.setLockWatchdogTimeout(properties.getLockWatchdogTimeoutMillis());
		SentinelServersConfig serversConfig = config.useSentinelServers();
		serversConfig.setMasterName(properties.getMasterName());
		serversConfig.addSentinelAddress(properties.getSentinelAddress());
		String password = properties.getPassword();
		if (StringUtil.isNotBlank(password)) {
			serversConfig.setPassword(password);
		}
		serversConfig.setDatabase(properties.getDatabase());
		serversConfig.setMasterConnectionPoolSize(properties.getPoolSize());
		serversConfig.setMasterConnectionMinimumIdleSize(properties.getIdleSize());
		serversConfig.setSlaveConnectionPoolSize(properties.getPoolSize());
		serversConfig.setSlaveConnectionMinimumIdleSize(properties.getIdleSize());
		serversConfig.setIdleConnectionTimeout(properties.getConnectionTimeout());
		serversConfig.setConnectTimeout(properties.getConnectionTimeout());
		serversConfig.setTimeout(properties.getTimeout());

		serversConfig.setRetryAttempts(properties.getRetryAttempts());
		serversConfig.setRetryInterval(properties.getRetryIntervalMillis());
		return config;
	}

	private static Config clusterConfig(RenegadeRdissonProperties properties) {
		Config config = new Config();
		config.setThreads(properties.getThreads());
		config.setNettyThreads(properties.getNettyThreads());
		config.setLockWatchdogTimeout(properties.getLockWatchdogTimeoutMillis());
		ClusterServersConfig serversConfig = config.useClusterServers();
		serversConfig.addNodeAddress(properties.getNodeAddress());
		String password = properties.getPassword();
		if (StringUtil.isNotBlank(password)) {
			serversConfig.setPassword(password);
		}
		serversConfig.setMasterConnectionPoolSize(properties.getPoolSize());
		serversConfig.setMasterConnectionMinimumIdleSize(properties.getIdleSize());
		serversConfig.setSlaveConnectionPoolSize(properties.getPoolSize());
		serversConfig.setSlaveConnectionMinimumIdleSize(properties.getIdleSize());
		serversConfig.setIdleConnectionTimeout(properties.getConnectionTimeout());
		serversConfig.setConnectTimeout(properties.getConnectionTimeout());
		serversConfig.setTimeout(properties.getTimeout());

		serversConfig.setRetryAttempts(properties.getRetryAttempts());
		serversConfig.setRetryInterval(properties.getRetryIntervalMillis());
		return config;
	}

	private static RedissonClient redissonClient(RenegadeRdissonProperties properties) {
		RenegadeRdissonProperties.Mode mode = properties.getMode();
		Config config;
		switch (mode) {
			case sentinel:
				config = sentinelConfig(properties);
				break;
			case cluster:
				config = clusterConfig(properties);
				break;
			case master:
				config = masterSlaveConfig(properties);
				break;
			case single:
				config = singleConfig(properties);
				break;
			default:
				config = new Config();
				break;
		}
		return Redisson.create(config);
	}

	@Bean
	@ConditionalOnMissingBean
	public RedissonClient redissonBeanClient(RenegadeRdissonProperties properties) {
		return redissonClient(properties);
	}

	@Bean
	@ConditionalOnMissingBean
	public RedisLockClient redisLockClient(RenegadeRdissonProperties properties) {
		return new RedisLockClientImpl(redissonBeanClient(properties));
	}

	@Bean
	@ConditionalOnMissingBean
	public RedisLockAspect redisLockAspect(RedisLockClient redisLockClient) {
		return new RedisLockAspect(redisLockClient);
	}

	@Scope(BeanDefinition.SCOPE_SINGLETON)
	@Bean(name = RedissonConfigUtils.REDISSON_QUEUE_BEAN_PROCESSOR_BEAN_NAME)
	public RedissonQueueBeanPostProcessor redissonQueueBeanPostProcessor() {
		return new RedissonQueueBeanPostProcessor();
	}

	@Scope(BeanDefinition.SCOPE_SINGLETON)
	@Bean(name = RedissonConfigUtils.REDISSON_QUEUE_REGISTRY_BEAN_NAME)
	public RedissonQueueRegistry redissonQueueRegistry() {
		return new RedissonQueueRegistry();
	}

	@Scope(BeanDefinition.SCOPE_SINGLETON)
	@Bean
	@ConditionalOnMissingBean
	public RedissonTemplate redissonTemplate() {
		return new RedissonTemplate();
	}

}
