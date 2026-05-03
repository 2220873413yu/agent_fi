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

/**
 * 消息类型
 *
 * @author renegade
 */
public enum MessageModel {

	/**
	 * 广播。独立消费，每条消息都能呗消费，流中的数据在读取后并不会被删除，还是存在的。如果多个实例同时使用xread读取，都是可以读取到消息的
	 *  注意点：由于没有确认机制，因为确认以后会直接删除，这里广播所有，那么只有在消费端，确认所有都广播ok以后，再手动确认删除
	 */
	BROADCASTING,

	/**
	 * 集群消息， 可通过创建分组，保证一条信息只被一个实例消费,推荐
	 */
	CLUSTERING;

}
