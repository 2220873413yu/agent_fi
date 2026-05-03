package com.xms.web.core.receiver;

import com.xms.common.config.redis.XmsRedis;
import com.xms.common.config.redis.delayqueue.MultiDelayMsgDO;
import com.xms.common.config.redis.delayqueue.RedissonDelayHandler;
import com.xms.common.config.redis.delayqueue.RedissonDelayOrder;
import com.xms.common.constant.RedisConstant;
import com.xms.common.constant.SysConstant;
import com.xms.common.thread.ExecutorRegionKit;
import com.xms.common.utils.Func;
import com.xms.common.utils.uuid.IDUtils;
import com.xms.web.service.StoreOrderAutoService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;


/**
 * @author MIER
 */
@AllArgsConstructor
@Component
@Slf4j
public class DelayRedisQueueListener implements ApplicationRunner {

	private final RedissonDelayHandler delayHandler;
	private final XmsRedis xmsRedis;
	private final StoreOrderAutoService storeOrderAutoServiceImpl;
	/**
	 * Callback used to run the bean.
	 *
	 * @param args incoming application arguments
	 */
	@Override
	public void run(ApplicationArguments args) {
		delayHandler.start(order -> {
			//  Auto-generated method stub,处理业务的干活
			ExecutorRegionKit.getExecutorRegion().getBlockUserVirtualThreadExecutor(IDUtils.getSnowflakeId(), false).execute(() -> {
				log.debug("===>>>>>>>>>>>>延时队列到期处理结束");
				//  BY RENEGADE PISTA: 2023/12/9  加入redis，30分钟,延迟三十分钟自动取消的干活
				storeOrderAutoServiceImpl.hanlerOrder(order);
			});
		}, RedisConstant.StreamMsgConstant.DELAY_ORDER_TIMEOUT_QUEUE);
	}

	private void hanlerCacheOrder(RedissonDelayOrder msg) {
		if (!SysConstant.FOUR.equals(msg.getBizType())) {
			log.error("延迟消息消费有误：{}", msg);
		}
		List<String> list = Func.toStrList(msg.getOrderId());
		list.parallelStream().forEach(xmsRedis::del);
		//查询未支付的订单，判断是否存在延迟时间
		if (msg.getData() != null) {
			MultiDelayMsgDO delayMsg = (MultiDelayMsgDO) msg.getData();
			log.debug("剩下的延迟时间集合：{}", delayMsg.getDelayMills());
			if (delayMsg.hasNextDelay()) {
				//继续入队延迟队列
				delayHandler.add(new RedissonDelayOrder(msg.getOrderId(), delayMsg.removeNextDelay() / 1000L, SysConstant.FOUR, delayMsg
					, RedisConstant.StreamMsgConstant.DELAY_DEL_CACHE));
			}
		}
	}
}
