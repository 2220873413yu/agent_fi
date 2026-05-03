package com.xms.web.core.receiver;

import com.xms.common.constant.SysConstant;
import com.xms.common.exception.ServiceException;
import com.xms.common.thread.ExecutorRegionKit;
import com.xms.dao.domain.RewardPublisherEvent;
import com.xms.dao.service.MsgBizService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dreamlu.mica.core.utils.JsonUtil;
import org.springframework.data.redis.connection.stream.MapRecord;

import java.util.Map;

/**
 * 用户等级处理
 *
 * @author MIER
 */
@Slf4j
// @Component
@AllArgsConstructor
public class RewardDynamicRedisStreamReceiver {
	private final MsgBizService msgBizServiceImpl;

	static void receiveMsg(String key, RewardPublisherEvent body, MsgBizService msgBizServiceImpl) {
		try {
			if (body.getBizType().equals(SysConstant.ONE)) {
				//分享奖励
				Long targetUserId=body.getTargetUserIds()[0];
				ExecutorRegionKit.getExecutorRegion().getUserThreadExecutor(targetUserId).executeTry(() ->
					msgBizServiceImpl.msgRewardShareReceiver(body, key));

			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException("处理订单奖励异常");
		}
	}

//	 @XmsRedisStreamListener(
//	 	name = RedisConstant.StreamMsgConstant.XMS_ASYNC_REWARD,
//	 	group = "-XMS_ASYNC_REWARD",
//	 	readRawBytes = true
//	 )
	public void msgRewardDynamicReceiver(MapRecord<String, String, byte[]> mapRecord) {
		Map<String, byte[]> recordValue = mapRecord.getValue();
		recordValue.forEach((key, messageBody) -> {
			// 手动序列化和反序列化，避免 redis 序列化不一致问题
			RewardPublisherEvent body = JsonUtil.readValue(messageBody, RewardPublisherEvent.class);
			if (body == null) {
				return;
			}
			log.info("=========>>>leader key receive：{} targetUserId {}", key,body.getTargetUserIds());
			receiveMsg(key, body, msgBizServiceImpl);
		});
	}


}
