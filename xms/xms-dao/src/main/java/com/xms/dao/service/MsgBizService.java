package com.xms.dao.service;

import com.xms.dao.domain.RewardPublisherEvent;

/**
 * @author: renengadePISTA
 * @createDate: 2024/8/29
 */
public interface MsgBizService {
	void msgRewardShareReceiver(RewardPublisherEvent body, String key);
}
