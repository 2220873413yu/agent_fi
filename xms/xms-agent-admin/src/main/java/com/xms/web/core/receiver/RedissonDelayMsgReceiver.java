package com.xms.web.core.receiver;

import com.alibaba.fastjson2.JSONObject;
import com.xms.common.config.redis.XmsRedis;
import com.xms.common.config.redis.delayqueue.MultiDelayMsgDO;
import com.xms.common.config.redis.delayqueue.RedissonDelayOrder;
import com.xms.common.config.redis.delayqueue.annotation.RedissonListener;
import com.xms.common.config.redis.delayqueue.config.RedissonTemplate;
import com.xms.common.config.redis.delayqueue.message.MessageConverter;
import com.xms.common.config.redis.delayqueue.message.RedissonHeaders;
import com.xms.common.constant.RedisConstant;
import com.xms.common.constant.SysConstant;
import com.xms.common.utils.Func;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 延迟队列消息坚挺
 *
 * @author: GT63S
 * @createDate: 2024/7/6
 */
@Slf4j
@Component
@AllArgsConstructor
public class RedissonDelayMsgReceiver {

	@Autowired
	@Qualifier("renegadeMessageConverter")
	private MessageConverter renegadeMessageConverter;

	@Autowired
	private RedissonTemplate redissonTemplate;
	@Autowired
	private XmsRedis xmsRedis;

	@RedissonListener(queues = RedisConstant.StreamMsgConstant.DELAY_DEL_CACHE, messageConverter = "renegadeMessageConverter")
	public void handler(@Header(value = RedissonHeaders.MESSAGE_ID, required = false) String messageId,
						@Header(RedissonHeaders.DELIVERY_QUEUE_NAME) String queue,
						@Header(RedissonHeaders.SEND_TIMESTAMP) long sendTimestamp,
						@Header(RedissonHeaders.EXPECTED_DELAY_MILLIS) long expectedDelayMillis,
						@Header(value = "my_header", required = false, defaultValue = RedisConstant.StreamMsgConstant.DELAY_DEL_CACHE) String myHeader,
						@Payload RedissonDelayOrder<MultiDelayMsgDO<String>> message) {
		long actualDelay = System.currentTimeMillis() - (sendTimestamp + expectedDelayMillis);
		log.info("缓存延迟 receive {}, delayed {} millis", message, actualDelay);
		List<String> list = Func.toStrList(message.getOrderId());
		list.parallelStream().forEach(xmsRedis::del);
		//查询未支付的订单，判断是否存在延迟时间
		if (message.getData() != null) {
			MultiDelayMsgDO delayMsg = JSONObject.parseObject(JSONObject.toJSONString(message.getData()), MultiDelayMsgDO.class);
			log.info("剩下的延迟时间集合：{}", delayMsg.getDelayMills());
			if (delayMsg.hasNextDelay()) {
				//继续入队延迟队列
				Long aLong = delayMsg.removeNextDelay();
				RedissonDelayOrder<MultiDelayMsgDO<String>> redissonDelayOrder = new RedissonDelayOrder<>(message.getOrderId(), aLong, SysConstant.ONE, delayMsg, queue);
				redissonTemplate.sendWithDelay(queue, redissonDelayOrder, aLong);
			}
		}
	}

}
