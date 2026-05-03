package com.xms.dao.service.impl;

import com.xms.dao.domain.RewardPublisherEvent;
import com.xms.dao.service.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

/**
 * @author: GT63S
 * @createDate: 2024/8/29
 */
@Service
@Slf4j
@AllArgsConstructor
public class MsgBizServiceImpl implements MsgBizService {
	private final UserRelationService userRelationServiceImpl;
	private final ISysParaService sysParaServiceImpl;
	private final IMqTransactionLogService mqTransactionLogServiceImpl;
	private final UserWalletService userWalletServiceImpl;

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void msgRewardShareReceiver(RewardPublisherEvent body, String key) {
		try {
			boolean b = mqTransactionLogServiceImpl.removeById(key);
			if (!b) {
				log.warn("share mq msg having deal {}", body.getOrderId());
				return;
			}
		} catch (Exception e) {
			e.printStackTrace();
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
		}

	}
}
