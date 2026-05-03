

package com.xms.common.config.redis.stream;

import cn.hutool.core.thread.ThreadUtil;
import com.xms.common.constant.RedisConstant;
import com.xms.common.constant.SysConstant;
import com.xms.common.thread.ExecutorRegionKit;
import com.xms.common.utils.Func;
import com.xms.common.utils.ReflectUtil;
import com.xms.common.utils.StringUtil;
import com.xms.common.utils.spring.SpringUtils;
import com.xms.common.utils.uuid.IDUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Redisson 监听器 消息监听处理器
 *
 * @author renegade
 */
@Slf4j
public class XmsStreamListenerDetector implements BeanPostProcessor, InitializingBean {
	public static Map<String, Object> beans = new HashMap<>();
	public static Map<String, Object> queueIds = new ConcurrentHashMap<>();
	private final StreamMessageListenerContainer<String, MapRecord<String, String, byte[]>> streamMessageListenerContainer;
	private final RedisTemplate<String, Object> redisTemplate;
	private final String consumerGroup;
	private final String consumerName;
	private final RenegadeStreamTemplate defaultXmsStreamTemplate;


	public XmsStreamListenerDetector(StreamMessageListenerContainer<String, MapRecord<String, String, byte[]>> streamMessageListenerContainer, RedisTemplate<String, Object> redisTemplate, String consumerGroup, String consumerName, RenegadeStreamTemplate defaultXmsStreamTemplate) {
		this.streamMessageListenerContainer = streamMessageListenerContainer;
		this.redisTemplate = redisTemplate;
		this.consumerGroup = consumerGroup;
		this.consumerName = consumerName;
		this.defaultXmsStreamTemplate = defaultXmsStreamTemplate;
	}

	private static void createGroupIfNeed(RedisTemplate<String, Object> redisTemplate, String streamKey, ReadOffset readOffset, String group) {
		StreamOperations<String, Object, Object> opsForStream = redisTemplate.opsForStream();
		try {
			StreamInfo.XInfoGroups groups = opsForStream.groups(streamKey);
			if (groups.stream().noneMatch((x) -> group.equals(x.groupName()))) {
				opsForStream.createGroup(streamKey, readOffset, group);
			}
		} catch (RedisSystemException e) {
			// RedisCommandExecutionException: ERR no such key
			opsForStream.createGroup(streamKey, group);
		}
	}

	public Object manualInitialization(Object bean, String beanName) throws BeansException {
		Class<?> userClass = ClassUtils.getUserClass(bean);
		ReflectionUtils.doWithMethods(userClass, method -> {
			XmsRedisStreamListener listener = AnnotationUtils.findAnnotation(method, XmsRedisStreamListener.class);
			if (listener != null) {
				handlerSubMsg(bean, beanName, method, listener);
			}
		}, ReflectionUtils.USER_DECLARED_METHODS);
		return bean;
	}

	private void handlerSubMsg(Object bean, String beanName, Method method, XmsRedisStreamListener listener) {
		String streamKey = listener.name();
		Assert.hasText(streamKey, "@XmsRedisStreamListener name must not be empty.");
		log.info("Found @XmsRedisStreamListener on bean:{} method:{}", beanName, method);
		// 校验 method，method 入参数大于等于1
		int paramCount = method.getParameterCount();
		if (paramCount > 1) {
			throw new IllegalArgumentException("@XmsRedisStreamListener on method " + method + " parameter count must less or equal to 1.");
		}
		// streamOffset
		ReadOffset readOffset = listener.offsetModel().getReadOffset();
		StreamOffset<String> streamOffset = StreamOffset.create(streamKey, readOffset);
		// 消费模式
		MessageModel messageModel = listener.messageModel();
		if (MessageModel.BROADCASTING == messageModel) {
			//广播模式,就是推送模式，无需确认，推完做处理，这里后续可根据需要广播的人数，确认完成
			broadCast(streamOffset, bean, method, listener.readRawBytes(), listener.deadLetter());
		} else {
			String groupId = StringUtil.isNotBlank(listener.group()) ? listener.group() : consumerGroup;
			Consumer consumer = Consumer.from(groupId, consumerName);
			// 如果需要，创建 group
			createGroupIfNeed(redisTemplate, streamKey, readOffset, groupId);
			cluster(consumer, streamOffset, listener, bean, method);
		}
		extractedPendingMsg(streamKey);
	}

	private void extractedPendingMsg(String streamKey) {
		// 处理阻塞消息
		ExecutorRegionKit.getExecutorRegion().getBlockUserVirtualThreadExecutor(IDUtils.getSnowflakeId(), false).execute(() ->
			queueIds.compute(streamKey, (k, v) -> {
				if (v == null) {
					//因为此刻以前消费者组已经启动起来了,设置此刻时间戳-1 确保跟重新发送的消息的时间戳不一致。
					long stopTimestamp = Instant.now().toEpochMilli() - 1;
					// 设置读取选项：默认从 "0-0" 开始，按批次读取
					String lastId = ReadOffsetModel.START.getReadOffset().getOffset();
					boolean flag = true;
					while (flag) {
						// 从指定 ID 开始读取
						List<MapRecord<String, String, byte[]>> messages = defaultXmsStreamTemplate.xRead(streamKey, 100, lastId);
						if (messages.isEmpty()) {
							break;
						}
						for (MapRecord<String, String, byte[]> message : messages) {
							// 获取消息ID (例: 1608463984345-0)
							String messageId = message.getId().getValue();
							// 获取消息的时间戳
							long messageTimestamp = message.getId().getTimestamp();
							log.info("Message ID: {}", messageId);
							// 判断是否达到停止时间点
							if (messageTimestamp >= stopTimestamp) {
								log.debug("判断是否达到停止时间点 messageTimestamp {} ,{}", messageTimestamp, stopTimestamp);
								flag = false;
								break;
							}
							Map<String, byte[]> recordValue = message.getValue();
							for (Map.Entry<String, byte[]> entry : recordValue.entrySet()) {
								backSendMsg(streamKey, entry.getKey(), entry.getValue());
							}
							ackStartMsg(streamKey, message);
							// 更新最后的ID，以便在下一次循环时从这里继续读取
							lastId = messageId;
						}
					}
					v = streamKey;
				}
				return v;
			}));
	}

	private void ackStartMsg(String streamKey, MapRecord<String, String, byte[]> message) {
		defaultXmsStreamTemplate.acknowledge(streamKey, message);
		defaultXmsStreamTemplate.delete(message);
	}

	private void backSendMsg(String streamQueue, String key, Object messageBody) {
		while (true) {
			RecordId result = defaultXmsStreamTemplate.send(streamQueue, key, messageBody);
			log.info("result 结果：{}", result.getTimestamp());
			if (Func.isNotEmpty(result.getTimestamp())) {
				return;
			}
			ThreadUtil.sleep(100L);
		}
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		Class<?> userClass = ClassUtils.getUserClass(bean);
		ReflectionUtils.doWithMethods(userClass, method -> {
			XmsRedisStreamListener listener = AnnotationUtils.findAnnotation(method, XmsRedisStreamListener.class);
			if (listener != null) {
				beans.put(beanName, bean);
				handlerSubMsg(bean, beanName, method, listener);
			}
		}, ReflectionUtils.USER_DECLARED_METHODS);
		return bean;
	}

	private void broadCast(StreamOffset<String> streamOffset, Object bean, Method method, boolean isReadRawBytes, boolean isDeadLetter) {
		streamMessageListenerContainer.receive(streamOffset, (message) -> {
			//  回调处理MapBackedRecord
			invokeMethod(bean, method, message, isReadRawBytes, isDeadLetter);
		});
	}

	private void cluster(Consumer consumer, StreamOffset<String> streamOffset, XmsRedisStreamListener listener, Object bean, Method method) {
		boolean autoAcknowledge = listener.autoAcknowledge();
		StreamMessageListenerContainer.ConsumerStreamReadRequest<String> readRequest = StreamMessageListenerContainer.StreamReadRequest.builder(streamOffset).consumer(consumer).autoAcknowledge(autoAcknowledge).build();
		StreamOperations<String, Object, Object> opsForStream = redisTemplate.opsForStream();
		streamMessageListenerContainer.register(readRequest, (message) -> {
			SpringUtils.getBean(XmsStreamListenerDetector.class).invokeMethod(bean, method, message, listener.readRawBytes(), listener.deadLetter());
			// ack 确认，自动确认关闭false，就需要手动确认
			if (!autoAcknowledge) {
				opsForStream.acknowledge(consumer.getGroup(), message);
				opsForStream.delete(message);
			}
		});
	}

	/**
	 * 在需要重试的方法上添加 @Retryable 注解
	 * 其里面属性说明
	 * // 设置重试拦截器的 bean 名称
	 * String interceptor() default "";
	 * <p>
	 * // 只对特定类型的异常进行重试。默认：所有异常
	 * Class<? extends Throwable>[] value() default {};
	 * <p>
	 * // 包含或者排除哪些异常进行重试
	 * Class<? extends Throwable>[] include() default {};
	 * Class<? extends Throwable>[] exclude() default {};
	 * <p>
	 * // l设置该重试的唯一标志，用于统计输出
	 * String label() default "";
	 * <p>
	 * boolean stateful() default false;
	 * <p>
	 * // 最大重试次数，默认为 3 次
	 * int maxAttempts() default 3;
	 * <p>
	 * String maxAttemptsExpression() default "";
	 * <p>
	 * // 设置重试补偿机制，可以设置重试间隔，并且支持设置重试延迟倍数
	 * Backoff backoff() default @Backoff;
	 * <p>
	 * // 异常表达式，在抛出异常后执行，以判断后续是否进行重试
	 * String exceptionExpression() default "";
	 * <p>
	 * String[] listeners() default {};
	 * maxAttempts 最大重试次数 默认3
	 * -- @Backoff 重试策略   delay 重试间隔时间，单位毫秒， multiplier 重试间隔倍数
	 * 最大重试次数为 3，第一次重试间隔为 2s，之后以 2 倍大小进行递增，第二次重试间隔为 4 s，第三次为 8s
	 * 注意：避免类class调用本类方法
	 */
	@Retryable(maxAttemptsExpression = "${xms.stream.maxAttempts}", backoff = @Backoff(delayExpression = "${xms.stream.backOffInitialInterval}",
		multiplierExpression = "${xms.stream.backOffMultiplier}"))
	public void invokeMethod(Object bean, Method method, MapRecord<String, String, byte[]> mapRecord, boolean isReadRawBytes, boolean isDeadLetter) {
		// 支持没有参数的方法
		if (method.getParameterCount() == 0) {
			ReflectUtil.invokeMethod(method, bean);
			return;
		}
		if (isReadRawBytes) {
			ReflectUtil.invokeMethod(method, bean, mapRecord);
		} else {
			ReflectUtil.invokeMethod(method, bean, getRecordValue(mapRecord));
		}
	}

	/**
	 * -- @Recover 注解： 进行善后工作：当重试达到指定次数之后，会调用指定的方法来进行日志记录等操作
	 * 注意：
	 * - -@Recover 注解标记的方法必须和被 @Retryable 标记的方法在同一个类中
	 * <p>
	 * 重试方法抛出的异常类型需要与 recover() 方法参数类型保持一致
	 * <p>
	 * recover() 方法返回值需要与重试方法返回值保证一致
	 * <p>
	 * recover() 方法中不能再抛出 Exception，否则会报无法识别该异常的错误
	 */
	@Recover
	public void recover(Exception e, Object bean, Method method, MapRecord<String, String, byte[]> mapRecord, boolean isReadRawBytes, boolean isDeadLetter) {
		log.error("isReadRawBytes  {}, mapRecord result:{}", isReadRawBytes, mapRecord.getValue());
		e.printStackTrace();
		log.error("达到最大重试次数", e);
		//不需要进死信队列
		if (!isDeadLetter) {
			return;
		}
		sendDeadLetter(mapRecord);
	}

	private void sendDeadLetter(MapRecord<String, String, byte[]> mapRecord) {
		Map<String, byte[]> recordValue = mapRecord.getValue();
		//发送到私信队列
		recordValue.forEach((key, messageBody) -> {
			while (true) {
				RecordId res = defaultXmsStreamTemplate.send(RedisConstant.StreamMsgConstant.XMS_DEAD_MSG, mapRecord.getStream() + "@" + key, messageBody);
				if (res != null) {
					return;
				}
			}
		});
	}

	private Object getRecordValue(MapRecord<String, String, byte[]> mapRecord) {
		Map<String, byte[]> messageValue = mapRecord.getValue();
		if (messageValue.containsKey(RenegadeStreamTemplate.OBJECT_PAYLOAD_KEY)) {
			byte[] payloads = messageValue.get(RenegadeStreamTemplate.OBJECT_PAYLOAD_KEY);
			Object deserialize = redisTemplate.getValueSerializer().deserialize(payloads);
			return ObjectRecord.create(mapRecord.getStream(), deserialize).withId(mapRecord.getId());
		} else {
			return mapRecord.mapEntries(entry -> {
				String key = entry.getKey();
				Object value = redisTemplate.getValueSerializer().deserialize(entry.getValue());
				return Collections.singletonMap(key, value).entrySet().iterator().next();
			});
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		try {
			streamMessageListenerContainer.start();
		} catch (Exception e) {
			e.printStackTrace();
			while (!streamMessageListenerContainer.isRunning()) {
				log.warn("redis stream try to auto connect");
				streamMessageListenerContainer.start();
				ThreadUtil.sleep(5000L);
			}
			throw e;
		}
	}


}
