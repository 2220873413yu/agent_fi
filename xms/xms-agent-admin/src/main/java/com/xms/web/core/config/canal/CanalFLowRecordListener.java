package com.xms.web.core.config.canal;

import com.xms.common.config.redis.stream.RenegadeStreamTemplate;
import com.xms.common.constant.RedisConstant;
import com.xms.common.exception.ServiceException;
import com.xms.common.utils.Func;
import com.xms.dao.entity.domain.UserMoneyCanal;
import com.xms.dao.entity.domain.UserMoneyCanalWrapper;
import com.xms.web.core.serializer.IMessageSerializer;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.stereotype.Component;
import top.javatool.canal.client.annotation.CanalTable;
import top.javatool.canal.client.handler.EntryHandler;
import top.javatool.canal.client.spring.boot.properties.CanalProperties;

/**
 * 监听某个表名
 *
 * @author: renengadePISTA
 * @createDate: 2023/8/3
 */
@CanalTable("t_user_money")
@Component
@Slf4j
@AllArgsConstructor
@ConditionalOnProperty(value = CanalProperties.CANAL_MODE, havingValue = "simpleDiy", matchIfMissing = true)
public class CanalFLowRecordListener implements EntryHandler<UserMoneyCanal> {

	private final RenegadeStreamTemplate streamTemplate;

	private final IMessageSerializer messageSerializer;

	/**
	 * 只监听修改的。
	 * 字段的值只有变更以后，变更前的字段才会有值，否则变更前的字段都是null
	 *
	 * @param before
	 * @param after
	 */
	@Override
	public void update(UserMoneyCanal before, UserMoneyCanal after) {
		log.debug("binlog日志前：t_user_money update before===>>>>>>>>,{}", before);
		//更新后的干活
		log.info("binlog日志后：t_user_money update after=========>>>,{}", after);
		UserMoneyCanalWrapper userMoneyCanalWrapper = UserMoneyCanalWrapper.builder()
			.before(before)
			.after(after)
			.build();
		//发送redis stream 队列，天然持久化，保证日志不消失
		RecordId res = streamTemplate.send(RedisConstant.StreamMsgConstant.CANAL_MSG, after.getGt_id(), userMoneyCanalWrapper, messageSerializer::serialize);
		if (res == null || Func.isAllEmpty(res.getTimestamp())) {
			throw new ServiceException("异常了，需要canal那边重发的干活");
		}
	}
}
