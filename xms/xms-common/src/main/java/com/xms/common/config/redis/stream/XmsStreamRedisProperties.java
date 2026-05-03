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

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * redis 配置
 *
 * @author renegade
 */
@Getter
@Setter
@ConfigurationProperties(XmsStreamRedisProperties.PREFIX)
public class XmsStreamRedisProperties {
	public static final String PREFIX = "xms.stream";

	/**
	 * 是否开启 stream
	 */
	boolean enable = false;
	/**
	 * consumer group，默认：服务名 + 环境
	 */
	String consumerGroup;
	/**
	 * 消费者名称，默认：ip + 端口
	 */
	String consumerName;
	/**
	 * poll 批量大小
	 */
	Integer pollBatchSize;
	/**
	 * poll 超时时间
	 */
	Duration pollTimeout;
	/**
	 * 最大重试次数
	 */
	Integer maxAttempts;
	/**
	 *  #重试间隔时间,单位毫秒
	 */
	Integer backOffInitialInterval;
	/**
	 * 重试间隔时间的倍数  相邻两次重试之间的间隔时间的倍数。默认2，即第二次是第一次间隔时间的2倍，第三次是第二次的2倍
	 */
	Integer backOffMultiplier;
}

