package com.xms.dao.service.impl;

import com.xms.common.config.redis.XmsRedis;
import com.xms.common.config.redis.delayqueue.config.RedissonTemplate;
import com.xms.common.config.redis.stream.RenegadeStreamTemplate;
import com.xms.common.constant.RedisConstant;
import com.xms.common.exception.ServiceException;
import com.xms.common.result.ResponseCode;
import com.xms.common.utils.Func;
import com.xms.dao.domain.MqMsgDO;
import com.xms.dao.domain.MqTransactionLog;
import com.xms.dao.service.IMqTransactionLogService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dreamlu.mica.core.utils.JsonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Instant;
import java.util.List;

/**
 * @author: GT63S
 * @createDate: 2024/7/23
 */
@Component
@Slf4j
@AllArgsConstructor
public class MqSendTemplate {
	private final RenegadeStreamTemplate renegadeStreamTemplate;
	private final IMqTransactionLogService mqTransactionLogServiceImpl;
	private final XmsRedis xmsRedis;
	private final RedissonTemplate redissonTemplate;

	/**
	 * 事务同步器提交以后发布消息
	 *
	 * @param queueName 队列名
	 * @param data      消息
	 */
	public <T> void syncSendSync(String queueName, T data) {
		MqMsgDO<T> sendMqDo = new MqMsgDO<>();
		sendMqDo.setTopic(queueName);
		sendMqDo.setBody(data);
		MqTransactionLog tMqTransactionLog = MqTransactionLog.builder()
			.log(JsonUtil.toJsonAsBytes(sendMqDo))
			.createTime(Instant.now().toEpochMilli())
			.build();
		boolean b = mqTransactionLogServiceImpl.save(tMqTransactionLog);
		if (!b) {
			throw new ServiceException(ResponseCode.CODE_1003);
		}
		sendMqDo.setTransactionId(tMqTransactionLog.getId().toString());
		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
			@Override
			public void afterCommit() {
				renegadeStreamTemplate.send(queueName, sendMqDo.getTransactionId(), JsonUtil.toJsonAsBytes(data));
				log.info("event.getTopic() 的 {}  发送订单ok  {}", sendMqDo.getTopic(), sendMqDo.getBody());
			}
		});
	}

	/**
	 * 缓存键值对，延迟双删，多个key，以英文逗号拼接
	 *
	 * @param key
	 */
	public void delayDoubleDelCache(String key) {
		List<String> list = Func.toStrList(key);
		list.parallelStream().forEach(xmsRedis::del);
		redissonTemplate.sendCleanCacheWithDelay(key);
	}
}
