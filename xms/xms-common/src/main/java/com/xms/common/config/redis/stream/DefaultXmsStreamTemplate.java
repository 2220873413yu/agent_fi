/*
 * Copyright (c) 2019-2029, Dreamlu 卢春梦 (596392912@qq.com & www.dreamlu.net).
 * <p>
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE 3.0;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.gnu.org/licenses/lgpl.html
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.xms.common.config.redis.stream;

import com.xms.common.constant.RedisConstant;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.connection.RedisStreamCommands;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.connection.stream.Record;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.data.redis.core.convert.RedisCustomConversions;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.*;

/**
 * 默认的 RStreamTemplate 消息发送模板，根据实际业务场景，可以重写自定义。
 *
 * @author renegade
 */
public class DefaultXmsStreamTemplate implements RenegadeStreamTemplate {
	private static final RedisCustomConversions CUSTOM_CONVERSIONS = new RedisCustomConversions();
	private final RedisTemplate<String, Object> redisTemplate;
	private final StreamOperations<String, String, Object> streamOperations;

	public DefaultXmsStreamTemplate(RedisTemplate<String, Object> redisTemplate) {
		this.redisTemplate = redisTemplate;
		this.streamOperations = redisTemplate.opsForStream();
	}

	@Override
	public RecordId send(Record<String, ?> record) {
		// 1. MapRecord
		if (record instanceof MapRecord) {
			return streamOperations.add(record);
		}
		String stream = Objects.requireNonNull(record.getStream(), "RStreamTemplate send stream name is null.");
		Object recordValue = Objects.requireNonNull(record.getValue(), () -> "RStreamTemplate send stream: " + stream + " value is null.");
		Class<?> valueClass = recordValue.getClass();
		// 2. 普通类型的 ObjectRecord
		if (CUSTOM_CONVERSIONS.isSimpleType(valueClass)) {
			return streamOperations.add(record);
		}
		// 3. 自定义类型处理
		Map<String, Object> payload = new HashMap<>();
		payload.put(RenegadeStreamTemplate.OBJECT_PAYLOAD_KEY, recordValue);
		MapRecord<String, String, Object> mapRecord = MapRecord.create(stream, payload);
		return streamOperations.add(mapRecord);
	}

	@Override
	public RecordId send(String name, String key, byte[] data, RedisStreamCommands.XAddOptions options) {
		RedisSerializer<String> stringSerializer = StringRedisSerializer.UTF_8;
		byte[] nameBytes = Objects.requireNonNull(stringSerializer.serialize(name), "redis stream name is null.");
		byte[] keyBytes = Objects.requireNonNull(stringSerializer.serialize(key), "redis stream key is null.");
		Map<byte[], byte[]> mapDate = Collections.singletonMap(keyBytes, data);
		return redisTemplate.execute((RedisCallback<RecordId>) redis -> {
			RedisStreamCommands streamCommands = redis.streamCommands();
			return streamCommands.xAdd(MapRecord.create(nameBytes, mapDate), options);
		});
	}

	@Override
	public Long delete(String name, String... recordIds) {
		return streamOperations.delete(name, recordIds);
	}

	@Override
	public Long delete(String name, RecordId... recordIds) {
		return streamOperations.delete(name, recordIds);
	}

	@Override
	public Long trim(String name, long count, boolean approximateTrimming) {
		return streamOperations.trim(name, count, approximateTrimming);
	}

	@Override
	public Long acknowledge(String name, String group, String... recordIds) {
		return streamOperations.acknowledge(name, group, recordIds);
	}

	@Override
	public Long acknowledge(String name, String group, RecordId... recordIds) {
		return streamOperations.acknowledge(name, group, recordIds);
	}

	@Override
	public Long acknowledge(String group, Record<String, ?> record) {
		return streamOperations.acknowledge(group, record);
	}

	/**
	 * 读取消息，强制带消费组、消费者
	 * XREADGROUP GROUP group consumer [COUNT count] [BLOCK milliseconds] [NOACK] STREAMS key[key ...] ID[ID ...]
	 * 特殊符号 0-0：表示从pending列表重新读取消息，不支持阻塞，无法读取的过程自动ack
	 * 特殊符号 > ：表示只接收比消费者晚创建的消息，之前的消息不管
	 * 特殊符号 $ ：在xReadGroup中使用是无意义的，报错提示：ERR The $ ID is meaningless in the context of XREADGROUP
	 *
	 * @param consumer
	 * @param options
	 * @param offsets
	 */
	@Override
	public List<MapRecord<String, Object, Object>> xReadGroup(Consumer consumer, StreamReadOptions options, StreamOffset<String>... offsets) {
		//一次拉取5000条，并自动确认
		options = options.autoAcknowledge().count(5000);
		StreamOffset<String> offset = StreamOffset.create(RedisConstant.StreamMsgConstant.XMS_DEAD_MSG, ReadOffset.lastConsumed());
		StreamOperations<String, Object, Object> opsForStream = redisTemplate.opsForStream();
		try {
			StreamInfo.XInfoGroups groups = opsForStream.groups(RedisConstant.StreamMsgConstant.XMS_DEAD_MSG);
			if (groups.stream().noneMatch((x) -> RedisConstant.StreamMsgConstant.XMS_DEAD_MSG.equals(x.groupName()))) {
				opsForStream.createGroup(RedisConstant.StreamMsgConstant.XMS_DEAD_MSG, RedisConstant.StreamMsgConstant.XMS_DEAD_MSG);
			}
		} catch (RedisSystemException e) {
			// RedisCommandExecutionException: ERR no such key
			opsForStream.createGroup(RedisConstant.StreamMsgConstant.XMS_DEAD_MSG, RedisConstant.StreamMsgConstant.XMS_DEAD_MSG);
		}
		return opsForStream.read(Consumer.from(RedisConstant.StreamMsgConstant.XMS_DEAD_MSG, RedisConstant.StreamMsgConstant.XMS_DEAD_MSG), options, offset);
	}

	@Override
	public List<MapRecord<String, String, byte[]>> xRead(String name, Integer count,String lastId ) {
		StreamOperations<String, String, byte[]> opsForStream = redisTemplate.opsForStream();
		// 读取count条消息, 阻塞1s 方式
		StreamReadOptions readOptions = StreamReadOptions.empty().count(count).block(Duration.ofSeconds(1));;
		// 从Stream的开始读取消息
		StreamOffset<String> streamOffset = StreamOffset.create(name, ReadOffset.from(lastId));
		return opsForStream.read(readOptions, streamOffset);

	}
}
