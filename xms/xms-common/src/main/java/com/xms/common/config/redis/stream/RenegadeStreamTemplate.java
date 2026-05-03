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

import org.springframework.data.redis.connection.RedisStreamCommands;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.connection.stream.Record;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.lang.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * 基于 redis Stream 的消息发布器
 *
 * @author renegade
 */
public interface RenegadeStreamTemplate {

	/**
	 * 自定义 pojo 类型 key
	 */
	String OBJECT_PAYLOAD_KEY = "@payload";
	/**
	 * 最大长度限制 9W
	 */
	Long MAXLEN = 99999L;
	/**
	 * 方便多 redis 数据源使用
	 *
	 * @param redisTemplate RedisTemplate
	 * @return MicaRedisCache
	 */
	static RenegadeStreamTemplate use(RedisTemplate<String, Object> redisTemplate) {
		return new DefaultXmsStreamTemplate(redisTemplate);
	}

	/**
	 * 发布消息
	 *
	 * @param name  队列名
	 * @param value 消息
	 * @return 消息id
	 */
	default RecordId send(String name, Object value) {
		return send(ObjectRecord.create(name, value));
	}

	/**
	 * 发布消息
	 *
	 * @param name  队列名
	 * @param key   消息key
	 * @param value 消息
	 * @return 消息id
	 */
	default RecordId send(String name, String key, Object value) {
		return send(name, Collections.singletonMap(key, value));
	}

	/**
	 * 发布消息
	 *
	 * @param name 队列名
	 * @param key  消息key
	 * @param data 消息
	 * @return 消息id
	 */
	default RecordId send(String name, String key, byte[] data) {
		//XAddOptions.none()
		return send(name, key, data, RedisStreamCommands.XAddOptions.maxlen(MAXLEN));
	}

	/**
	 * 发布消息
	 *
	 * @param name   队列名
	 * @param key    消息key
	 * @param data   消息
	 * @param maxLen 限制 stream 最大长度
	 * @return 消息id
	 */
	default RecordId send(String name, String key, byte[] data, long maxLen) {
		return send(name, key, data, RedisStreamCommands.XAddOptions.maxlen(maxLen));
	}

	/**
	 * 发布消息
	 *
	 * @param name    队列名
	 * @param key     消息key
	 * @param data    消息
	 * @param options XAddOptions 参数的附加选项，比如限制长度呀
	 * @return 消息id
	 */
	RecordId send(String name, String key, byte[] data, RedisStreamCommands.XAddOptions options);

	/**
	 * 发布消息
	 *
	 * @param name   队列名
	 * @param key    消息key
	 * @param data   消息
	 * @param mapper mapper
	 * @param <T>    泛型
	 * @return 消息id
	 */
	default <T> RecordId send(String name, String key, T data, Function<T, byte[]> mapper, long maxLen) {
		return send(name, key, mapper.apply(data), maxLen);
	}

	/**
	 * 发布消息
	 *
	 * @param name    队列名
	 * @param key     消息key
	 * @param data    消息
	 * @param mapper  mapper
	 * @param options XAddOptions
	 * @param <T>     泛型
	 * @return 消息id
	 */
	default <T> RecordId send(String name, String key, T data, Function<T, byte[]> mapper, RedisStreamCommands.XAddOptions options) {
		return send(name, key, mapper.apply(data), options);
	}

	/**
	 * 发布消息
	 *
	 * @param name   队列名
	 * @param key    消息key
	 * @param data   消息
	 * @param mapper 消息转换
	 * @param <T>    泛型
	 * @return 消息id
	 */
	default <T> RecordId send(String name, String key, T data, Function<T, byte[]> mapper) {
		return send(name, key, mapper.apply(data));
	}

	/**
	 * 批量发布
	 *
	 * @param name     队列名
	 * @param messages 消息
	 * @return 消息id
	 */
	default RecordId send(String name, Map<String, Object> messages) {
		return send(MapRecord.create(name, messages));
	}

	/**
	 * 发送消息
	 *
	 * @param record Record
	 * @return 消息id
	 */
	RecordId send(Record<String, ?> record);

	/**
	 * 删除消息
	 *
	 * @param name      stream name
	 * @param recordIds recordIds
	 * @return Long
	 */
	@Nullable
	Long delete(String name, String... recordIds);

	/**
	 * 删除消息
	 *
	 * @param name      stream name
	 * @param recordIds recordIds
	 * @return Long
	 */
	@Nullable
	Long delete(String name, RecordId... recordIds);

	/**
	 * 删除消息
	 *
	 * @param record Record
	 * @return Long
	 */
	@Nullable
	default Long delete(Record<String, ?> record) {
		return delete(record.getStream(), record.getId());
	}

	/**
	 * 对流进行修剪，限制长度
	 *
	 * @param name  name
	 * @param count count
	 * @return Long
	 */
	@Nullable
	default Long trim(String name, long count) {
		return trim(name, count, false);
	}

	/**
	 * 对流进行修剪，限制长度
	 *
	 * @param name                name
	 * @param count               count
	 * @param approximateTrimming approximateTrimming
	 * @return Long
	 */
	@Nullable
	Long trim(String name, long count, boolean approximateTrimming);

	/**
	 * 手动 ack
	 *
	 * @param name      name
	 * @param group     group
	 * @param recordIds recordIds
	 * @return Long
	 */
	@Nullable
	Long acknowledge(String name, String group, String... recordIds);

	/**
	 * 手动 ack
	 *
	 * @param name      name
	 * @param group     group
	 * @param recordIds recordIds
	 * @return Long
	 */
	@Nullable
	Long acknowledge(String name, String group, RecordId... recordIds);

	/**
	 * 手动 ack
	 *
	 * @param group  group
	 * @param record record
	 * @return Long
	 */
	@Nullable
	Long acknowledge(String group, Record<String, ?> record);

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
	List<MapRecord<String, Object, Object>> xReadGroup(Consumer consumer, StreamReadOptions options, StreamOffset<String>... offsets);

    List<MapRecord<String, String, byte[]>> xRead(String name, Integer count,String lastId );
}
