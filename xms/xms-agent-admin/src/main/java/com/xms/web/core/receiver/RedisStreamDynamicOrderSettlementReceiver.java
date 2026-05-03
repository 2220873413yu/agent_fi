package com.xms.web.core.receiver;

import com.xms.common.config.redis.stream.XmsRedisStreamListener;
import com.xms.common.constant.RedisConstant;
import com.xms.common.exception.ServiceException;
import com.xms.web.service.IRedisStreamBizJobService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dreamlu.mica.core.utils.JsonUtil;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 购买矿机之后的等级处理
 *
 * @author MIER
 */
@Slf4j
@Component
@AllArgsConstructor
public class RedisStreamDynamicOrderSettlementReceiver {
	private final IRedisStreamBizJobService redisStreamBizJobServiceImpl;
	@XmsRedisStreamListener(
		name = RedisConstant.StreamMsgConstant.ORDER_DYNAMIC_SETTLEMENT,
		group = "-order-dynamic-settlement",
		deadLetter = true,
		readRawBytes = true
	)
	public void msgFlowRecordReceiver(MapRecord<String, String, byte[]> mapRecord) {
		Map<String, byte[]> recordValue = mapRecord.getValue();
		recordValue.forEach((key, messageBody) -> {
			// 手动序列化和反序列化，避免 redis 序列化不一致问题
			List list = JsonUtil.readValue(messageBody, List.class);

			try {
				Integer b = redisStreamBizJobServiceImpl.handlerDynamicOrderSettlement(list);
				if (1 != b) {
					throw new ServiceException("购买矿机之后的任务处理失败");
				}
			} catch (Exception e) {
				e.printStackTrace();
				throw new ServiceException("购买矿机之后的任务处理失败");
			}
		});
	}
}
