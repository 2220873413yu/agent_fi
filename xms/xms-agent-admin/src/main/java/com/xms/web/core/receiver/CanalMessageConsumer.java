package com.xm.web.core.receiver;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.core.RocketMQPushConsumerLifecycleListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * canal 的rocketmq版本，demo，并且支持消息批量消费， 默认是一条条消费
 * 该版本采用传统的tcp消费，暂不使用mq
 *
 * @author: renengadePISTA
 * @createDate: 2024/4/14
 * consumerGroup 消费者组， topic 主题 分组保证一条消息只能一个消费者消费
 */
@Slf4j
// @Component
// @RocketMQMessageListener(consumerGroup = "canal-msg-group-01", topic = "canal-msg-topic-01")
public class CanalMessageConsumer implements
	RocketMQListener<MessageExt>,
	RocketMQPushConsumerLifecycleListener {
	private String charset = "UTF-8";
	/**
	 * Timeout for sending reply messages.
	 */
	private int replyTimeout = 3000;

	@Override
	public void onMessage(MessageExt message) {
		String body = new String(message.getBody(), StandardCharsets.UTF_8);
		log.debug("重写目的，注入进去，但是如果重写DiyDefaultMessageListenerConcurrently 这个就不会进去，canal收到消息: {} body: {}", message.getQueueOffset(), body);
		// 业务执行完毕没有抛出异常则由代理类自动提交ACK
	}
	// @Autowired
	// private  DefaultRocketMQListenerContainer defaultRocketMQListenerContainer;

	@Override
	public void prepareStart(DefaultMQPushConsumer consumer) {
		//拉取间隔呀 毫秒
		consumer.setPullInterval(3);
		//消费者一次处理的消息的最大批次
		consumer.setConsumeMessageBatchMaxSize(1024);
		//费者一次从消息队列中拉取的消息批次大小
		consumer.setPullBatchSize(1024);
		// 为了看到效果，暂时只让一个线程拉取消息（mq默认20）
		consumer.setConsumeThreadMax(20);
		consumer.setMessageListener(new DiyDefaultMessageListenerConcurrently());
		consumer.setConsumeThreadMin(8);
		log.debug("myDefaultMQPushConsumer init");
	}

	public class DiyDefaultMessageListenerConcurrently implements MessageListenerConcurrently {

		@SneakyThrows
		@SuppressWarnings("unchecked")
		@Override
		public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
			log.debug("received msg: {}", msgs.size());
			// handleMessage(msgs.get(0));
			// return ConsumeConcurrentlyStatus.RECONSUME_LATER;
			return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
		}
	}

	// private void handleMessage(
	// 	MessageExt messageExt) throws MQClientException, RemotingException, InterruptedException {
	// 	if (SpringUtils.getBean(DefaultRocketMQListenerContainer.class).getRocketMQListener() != null) {
	// 		SpringUtils.getBean(DefaultRocketMQListenerContainer.class).getRocketMQListener().onMessage(doConvertMessage(messageExt));
	// 	} else if (SpringUtils.getBean(DefaultRocketMQListenerContainer.class).getRocketMQReplyListener() != null) {
	// 		Object replyContent = SpringUtils.getBean(DefaultRocketMQListenerContainer.class).getRocketMQReplyListener().onMessage(doConvertMessage(messageExt));
	// 		Message<?> message = MessageBuilder.withPayload(replyContent).build();
	//
	// 		org.apache.rocketmq.common.message.Message replyMessage = MessageUtil.createReplyMessage(messageExt, convertToBytes(message));
	// 		DefaultMQProducer producer = SpringUtils.getBean(DefaultRocketMQListenerContainer.class).getConsumer().getDefaultMQPushConsumerImpl()
	// 			.getmQClientFactory().getDefaultMQProducer();
	// 		producer.setSendMsgTimeout(replyTimeout);
	// 		producer.send(replyMessage, new SendCallback() {
	// 			@Override
	// 			public void onSuccess(SendResult sendResult) {
	// 				if (sendResult.getSendStatus() != SendStatus.SEND_OK) {
	// 					log.error("Consumer replies message failed. SendStatus: {}", sendResult.getSendStatus());
	// 				} else {
	// 					log.debug("Consumer replies message success.");
	// 				}
	// 			}
	//
	// 			@Override
	// 			public void onException(Throwable e) {
	// 				log.error("Consumer replies message failed. error: {}", e.getLocalizedMessage());
	// 			}
	// 		});
	// 	}
	// }
	// private MethodParameter getMethodParameter() {
	// 	Class<?> targetClass;
	// 	if (SpringUtils.getBean(DefaultRocketMQListenerContainer.class).getRocketMQListener() != null) {
	// 		targetClass = AopProxyUtils.ultimateTargetClass(SpringUtils.getBean(DefaultRocketMQListenerContainer.class).getRocketMQListener());
	// 	} else {
	// 		targetClass = AopProxyUtils.ultimateTargetClass(SpringUtils.getBean(DefaultRocketMQListenerContainer.class).getRocketMQReplyListener());
	// 	}
	// 	Type messageType = this.getMessageType();
	// 	Class clazz = null;
	// 	if (messageType instanceof ParameterizedType && SpringUtils.getBean(DefaultRocketMQListenerContainer.class).getMessageConverter() instanceof SmartMessageConverter) {
	// 		clazz = (Class) ((ParameterizedType) messageType).getRawType();
	// 	} else if (messageType instanceof Class) {
	// 		clazz = (Class) messageType;
	// 	} else {
	// 		throw new RuntimeException("parameterType:" + messageType + " of onMessage method is not supported");
	// 	}
	// 	try {
	// 		final Method method = targetClass.getMethod("onMessage", clazz);
	// 		return new MethodParameter(method, 0);
	// 	} catch (NoSuchMethodException e) {
	// 		e.printStackTrace();
	// 		throw new RuntimeException("parameterType:" + messageType + " of onMessage method is not supported");
	// 	}
	// }
	// private Object doConvertMessage(MessageExt messageExt) {
	// 	Type messageType = getMessageType();
	// 	if (Objects.equals(messageType, MessageExt.class) || Objects.equals(messageType, org.apache.rocketmq.common.message.Message.class)) {
	// 		return messageExt;
	// 	} else {
	// 		String str = new String(messageExt.getBody(), Charset.forName(charset));
	// 		if (Objects.equals(messageType, String.class)) {
	// 			return str;
	// 		} else {
	// 			// If msgType not string, use objectMapper change it.
	// 			try {
	// 				if (messageType instanceof Class) {
	// 					//if the messageType has not Generic Parameter
	// 					return SpringUtils.getBean(DefaultRocketMQListenerContainer.class).getMessageConverter().fromMessage(MessageBuilder.withPayload(str).build(), (Class<?>) messageType);
	// 				} else {
	// 					//if the messageType has Generic Parameter, then use SmartMessageConverter#fromMessage with third parameter "conversionHint".
	// 					//we have validate the MessageConverter is SmartMessageConverter in this#getMethodParameter.
	// 					return ((SmartMessageConverter) SpringUtils.getBean(DefaultRocketMQListenerContainer.class).getMessageConverter()).fromMessage(MessageBuilder.withPayload(str).build(), (Class<?>) ((ParameterizedType) messageType).getRawType()
	// 						,getMethodParameter());
	// 				}
	// 			} catch (Exception e) {
	// 				log.info("convert failed. str:{}, msgType:{}", str, messageType);
	// 				throw new RuntimeException("cannot convert message to " + messageType, e);
	// 			}
	// 		}
	// 	}
	// }
	//
	// private Type getMessageType() {
	// 	Class<?> targetClass;
	// 	if (SpringUtils.getBean(DefaultRocketMQListenerContainer.class).getRocketMQListener() != null) {
	// 		targetClass = AopProxyUtils.ultimateTargetClass(SpringUtils.getBean(DefaultRocketMQListenerContainer.class).getRocketMQListener());
	// 	} else {
	// 		targetClass = AopProxyUtils.ultimateTargetClass(SpringUtils.getBean(DefaultRocketMQListenerContainer.class).getRocketMQReplyListener());
	// 	}
	// 	Type matchedGenericInterface = null;
	// 	while (Objects.nonNull(targetClass)) {
	// 		Type[] interfaces = targetClass.getGenericInterfaces();
	// 		if (Objects.nonNull(interfaces)) {
	// 			for (Type type : interfaces) {
	// 				if (type instanceof ParameterizedType &&
	// 					(Objects.equals(((ParameterizedType) type).getRawType(), RocketMQListener.class) || Objects.equals(((ParameterizedType) type).getRawType(), RocketMQReplyListener.class))) {
	// 					matchedGenericInterface = type;
	// 					break;
	// 				}
	// 			}
	// 		}
	// 		targetClass = targetClass.getSuperclass();
	// 	}
	// 	if (Objects.isNull(matchedGenericInterface)) {
	// 		return Object.class;
	// 	}
	//
	// 	Type[] actualTypeArguments = ((ParameterizedType) matchedGenericInterface).getActualTypeArguments();
	// 	if (Objects.nonNull(actualTypeArguments) && actualTypeArguments.length > 0) {
	// 		return actualTypeArguments[0];
	// 	}
	// 	return Object.class;
	// }
	//
	// private byte[] convertToBytes(Message<?> message) {
	// 	Message<?> messageWithSerializedPayload = doConvert(message.getPayload(), message.getHeaders());
	// 	Object payloadObj = messageWithSerializedPayload.getPayload();
	// 	byte[] payloads;
	// 	try {
	// 		if (null == payloadObj) {
	// 			throw new RuntimeException("the message cannot be empty");
	// 		}
	// 		if (payloadObj instanceof String) {
	// 			payloads = ((String) payloadObj).getBytes(Charset.forName(charset));
	// 		} else if (payloadObj instanceof byte[]) {
	// 			payloads = (byte[]) messageWithSerializedPayload.getPayload();
	// 		} else {
	// 			String jsonObj = (String) SpringUtils.getBean(DefaultRocketMQListenerContainer.class).getMessageConverter().fromMessage(messageWithSerializedPayload, payloadObj.getClass());
	// 			if (null == jsonObj) {
	// 				throw new RuntimeException(String.format(
	// 					"empty after conversion [messageConverter:%s,payloadClass:%s,payloadObj:%s]",
	// 					SpringUtils.getBean(DefaultRocketMQListenerContainer.class).getMessageConverter().getClass(), payloadObj.getClass(), payloadObj));
	// 			}
	// 			payloads = jsonObj.getBytes(Charset.forName(charset));
	// 		}
	// 	} catch (Exception e) {
	// 		throw new RuntimeException("convert to bytes failed.", e);
	// 	}
	// 	return payloads;
	// }
	//
	// private Message<?> doConvert(Object payload, MessageHeaders headers) {
	// 	Message<?> message = SpringUtils.getBean(DefaultRocketMQListenerContainer.class).getMessageConverter() instanceof SmartMessageConverter ?
	// 		((SmartMessageConverter) SpringUtils.getBean(DefaultRocketMQListenerContainer.class).getMessageConverter()).toMessage(payload, headers, null) :
	// 		SpringUtils.getBean(DefaultRocketMQListenerContainer.class).getMessageConverter().toMessage(payload, headers);
	// 	if (message == null) {
	// 		String payloadType = payload.getClass().getName();
	// 		Object contentType = headers != null ? headers.get(MessageHeaders.CONTENT_TYPE) : null;
	// 		throw new MessageConversionException("Unable to convert payload with type='" + payloadType +
	// 			"', contentType='" + contentType + "', converter=[" + SpringUtils.getBean(DefaultRocketMQListenerContainer.class).getMessageConverter() + "]");
	// 	}
	// 	MessageBuilder<?> builder = MessageBuilder.fromMessage(message);
	// 	builder.setHeaderIfAbsent(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.TEXT_PLAIN);
	// 	return builder.build();
	// }
}

