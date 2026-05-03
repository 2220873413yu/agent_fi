package com.xms.web.service.impl;

import com.xms.dao.service.UserRelationService;
import com.xms.web.service.IAsyncUserUpgradeService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author: renengadePISTA
 * @createDate: 2024/1/9 14:22
 */
@Service
@Slf4j
@AllArgsConstructor
public class AsyncUserUpgradeServiceImpl implements IAsyncUserUpgradeService {
	private final UserRelationService userRelationServiceImpl;

	/**
	 * 处理全网会员升级
	 */
	@Override
	public void dealUserUpgrade() {
	}

}
