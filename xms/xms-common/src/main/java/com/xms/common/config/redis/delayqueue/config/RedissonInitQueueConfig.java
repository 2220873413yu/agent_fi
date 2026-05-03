package com.xms.common.config.redis.delayqueue.config;

import com.xms.common.config.redis.delayqueue.message.DefaultMulRedissonMsgConverter;
import com.xms.common.config.redis.delayqueue.message.DefaultRedissonMessageConverter;
import com.xms.common.config.redis.delayqueue.message.MessageConverter;
import com.xms.common.constant.RedisConstant;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author: GT63S
 * @createDate: 2024/7/6
 */
@Configuration
public class RedissonInitQueueConfig {

	/**
	 * 延迟队列多级转换器
	 *
	 * @return
	 */
	@Bean("renegadeMessageConverter")
	public MessageConverter messageConverter() {

		return new DefaultMulRedissonMsgConverter();
	}

	/**
	 * 延迟队列多级转换器
	 *
	 * @return
	 */
	@Bean("messageGeneralConverter")
	public MessageConverter messageGeneralConverter() {
		return new DefaultRedissonMessageConverter();
	}

	/**
	 * 声名队列 延迟删除缓存
	 *
	 * @return
	 */
	@Bean
	public RedissonQueue redissonQueue() {
		return new RedissonQueue(RedisConstant.StreamMsgConstant.DELAY_DEL_CACHE, true, null, messageConverter());
	}


}
