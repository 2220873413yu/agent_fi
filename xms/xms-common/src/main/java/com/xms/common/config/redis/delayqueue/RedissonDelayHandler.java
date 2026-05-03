package com.xms.common.config.redis.delayqueue;


import cn.hutool.core.collection.ListUtil;
import com.xms.common.constant.RedisConstant;
import com.xms.common.constant.SysConstant;
import com.xms.common.thread.ExecutorRegionKit;
import com.xms.common.utils.uuid.IDUtils;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RDelayedQueue;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;


/**
 * 该版本队列名anyQueue 暂时写死，后续再说
 * redissonDelayHandler.add(new RedissonDelayOrder(orderNo, Func.toLong(delayTime), SysConstant.ONE));
 *
 * @author MIER
 */
@Component
@Slf4j
public class RedissonDelayHandler {
	private boolean start;
	@Autowired
	private RedissonClient redissonClient;
	/**
	 * 内部接口
	 */
	private ROnDelayedListener listener;

	public void start(ROnDelayedListener listener, String queueName) {
		if (start) {
			return;
		}
		log.info("======>>>>>>>>>>>>>RedissonDelayOrder启动OK<<<<<<<<<<<<<<<<================");
		start = true;
		this.listener = listener;
		// 先获取阻塞队列这
		RBlockingQueue<RedissonDelayOrder> blockingDeque = redissonClient.getBlockingQueue(queueName);
		// 当延迟队列的到期后会自动发到blockingDeque里，如果不写，存在的后果就是当服务器重启后，如果没有获取该队列，那么该队列的元素可能有一些不能被消费！
		// 声名激活放入延迟队列，作用的目标队列就是blockingDeque
		RDelayedQueue<RedissonDelayOrder> delayQueue = redissonClient.getDelayedQueue(blockingDeque);
		ExecutorRegionKit.getExecutorRegion().getBlockUserVirtualThreadExecutor(IDUtils.getSnowflakeId(), false).execute(() -> {
			try {
				while (true) {
					// 延迟队列的元素到期后把元素转到目标队列blockingDeque中的头部，并从其中取出！当服务器宕机等服务器重启时会重新获取，当redis重启会重新自动自动订阅原先的队列数据
					RedissonDelayOrder order = blockingDeque.take();
					log.info("到期元素在哪里????================>>>>>>>>>>>>{}", order);
					if (RedissonDelayHandler.this.listener != null) {
						RedissonDelayHandler.this.listener.onDelayedArrived(order);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	/**
	 * 添加延时队列
	 *
	 * @param order
	 */
	public void add(RedissonDelayOrder order) {
		log.info("开始添加延时消息：{}", order);
		// order.getStartTime()分钟以后将消息发送到指定队列
		// 相当于order.getStartTime()分钟后取消订单
		//延迟队列包含RedissonDelayOrder order.getStartTime()分钟，然后将其传输到blockingDeque中
		// 在order.getStartTime()分钟后就可以在blockingDeque 中获取RedissonDelayOrder了
		// 先获取阻塞队列这里可以用RQueue这个！获取延迟队列，该队列没有就自动定义，有获取队列
		RBlockingQueue<RedissonDelayOrder> blockingDeque = redissonClient.getBlockingQueue(order.getQueueName());
		// 放入延迟队列，作用的目标队列就是blockingDeque
		RDelayedQueue<RedissonDelayOrder> delayQueue = redissonClient.getDelayedQueue(blockingDeque);
		delayQueue.offer(order, order.getStartTime(), TimeUnit.SECONDS);
		log.info("延时消息添加成功，队列：{}，延时：{}秒", order.getQueueName(), order.getStartTime());
		// 当队列对象不再使用的时候应该主动关闭，要么就随着redisson的关闭而关闭
		// delayQueue.destroy(); // 注释掉这一行，它可能导致队列被销毁
	}

	/**
	 * 延迟缓存多删
	 *
	 * @param key 多个key，以英文逗号拼接
	 */
	public void addDelayCache(String key) {
		MultiDelayMsgDO delayMsg = new MultiDelayMsgDO(key, ListUtil.toList(2000L, 4000L));
		RedissonDelayOrder order = new RedissonDelayOrder(key, delayMsg.removeNextDelay() / 1000L, SysConstant.FOUR, delayMsg
			, RedisConstant.StreamMsgConstant.DELAY_DEL_CACHE);
		RBlockingQueue<RedissonDelayOrder> blockingDeque = redissonClient.getBlockingQueue(order.getQueueName());
		// 放入延迟队列，作用的目标队列就是blockingDeque
		RDelayedQueue<RedissonDelayOrder> delayQueue = redissonClient.getDelayedQueue(blockingDeque);
		delayQueue.offer(order, order.getStartTime(), TimeUnit.SECONDS);
		// 当队列对象不再使用的时候应该主动关闭，要么就随着redisson的关闭而关闭
		delayQueue.destroy();
	}

	/**
	 * 内部接口抽象方法
	 */
	public interface ROnDelayedListener {
		/**
		 * 处理具体业务的钩子
		 *
		 * @param order
		 */
		void onDelayedArrived(RedissonDelayOrder order);
	}
}
