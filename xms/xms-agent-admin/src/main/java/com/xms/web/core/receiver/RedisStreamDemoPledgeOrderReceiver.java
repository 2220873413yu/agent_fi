package com.xms.web.core.receiver;

import com.xms.common.config.redis.stream.XmsRedisStreamListener;
import com.xms.common.constant.RedisConstant;
import com.xms.common.exception.ServiceException;
import com.xms.dao.service.IDemoPledgeOrderConsumerService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dreamlu.mica.core.utils.JsonUtil;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * 示例质押订单后置处理 Redis Stream 消费者。
 */
@Slf4j
@Component
@AllArgsConstructor
public class RedisStreamDemoPledgeOrderReceiver {
	private final IDemoPledgeOrderConsumerService demoPledgeOrderConsumerService;

	/**
	 * 消费示例质押订单ID，并交给业务服务按数据库状态抢占处理。
	 *
	 * @param mapRecord Redis Stream 原始消息
	 */
	@XmsRedisStreamListener(
		name = RedisConstant.StreamMsgConstant.DEMO_PLEDGE_ORDER,
		group = "-demo-pledge-order",
		deadLetter = true,
		readRawBytes = true
	)
	public void msgFlowRecordReceiver(MapRecord<String, String, byte[]> mapRecord) {
		Map<String, byte[]> recordValue = mapRecord.getValue();
		recordValue.forEach((key, messageBody) -> {
			try {
				Long orderId = JsonUtil.readValue(messageBody, Long.class);
				demoPledgeOrderConsumerService.processPaidOrder(orderId);
			} catch (Exception e) {
				log.error("示例质押订单后置处理失败，content={}", new String(messageBody, StandardCharsets.UTF_8), e);
				throw new ServiceException("示例质押订单后置处理失败");
			}
		});
	}
}
