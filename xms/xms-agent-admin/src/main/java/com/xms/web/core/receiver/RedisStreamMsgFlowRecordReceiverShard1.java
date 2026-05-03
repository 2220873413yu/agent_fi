/*
 * Copyright (c) 2019-2029, Dreamlu 卢春梦 (596392912@qq.com & dreamlu.net).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.xms.web.core.receiver;

import com.xms.common.config.redis.stream.XmsRedisStreamListener;
import com.xms.common.constant.RedisConstant;
import com.xms.common.exception.ServiceException;
import com.xms.dao.entity.domain.UserMoneyCanalWrapper;
import com.xms.dao.service.UserMoneyLogService;
import com.xms.web.core.serializer.IMessageSerializer;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.stereotype.Component;
import top.javatool.canal.client.spring.boot.properties.CanalProperties;

import java.util.Map;

/**
 * 账户binlog日志监听下行消息，具体落库
 *
 * @author MIER
 */
@Slf4j
@Component
@AllArgsConstructor
@ConditionalOnProperty(value = CanalProperties.CANAL_MODE, havingValue = "simpleDiy", matchIfMissing = true)
public class RedisStreamMsgFlowRecordReceiverShard1 {
	private final IMessageSerializer messageSerializer;
	private final UserMoneyLogService userMoneyLogServiceImpl;

	@XmsRedisStreamListener(
		name = RedisConstant.StreamMsgConstant.CANAL_MSG,
		readRawBytes = true,deadLetter=true
	)
	public void msgFlowRecordReceiver(MapRecord<String, String, byte[]> mapRecord) {
		Map<String, byte[]> recordValue = mapRecord.getValue();
		recordValue.forEach((key, messageBody) -> {
			// 手动序列化和反序列化，避免 redis 序列化不一致问题
			UserMoneyCanalWrapper userMoneyCanalWrapper = messageSerializer.deserialize(messageBody);
			if (userMoneyCanalWrapper == null) {
				return;
			}
			log.info("=========>>>Shard-1  key 反序列化：{}   || MapRecord 的 userMoneyCanalWrapper, {}", key, userMoneyCanalWrapper.getAfter().getId());
			boolean b = userMoneyLogServiceImpl.handerMsg(userMoneyCanalWrapper.getBefore(), userMoneyCanalWrapper.getAfter());
			if (!b) {
				throw new ServiceException("异常了，需要重新消费的那边重发的干活");
			}
		});
	}

}
