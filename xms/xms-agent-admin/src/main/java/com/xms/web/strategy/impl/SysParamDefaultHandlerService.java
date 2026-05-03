package com.xms.web.strategy.impl;


import com.google.protobuf.ServiceException;
import com.xms.common.utils.Func;
import com.xms.dao.entity.domain.SysPara;
import com.xms.web.enums.StrategyTypeEnum;
import com.xms.web.strategy.DefaultStrategy;
import com.xms.web.strategy.StrategyService;
import com.xms.web.strategy.context.SysParamContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * 默认策略，都不允许非空
 *
 * @author MIER
 */
@Slf4j
@DefaultStrategy
@Service
public class SysParamDefaultHandlerService implements StrategyService<SysParamContext> {


	@Override
	public List<String> listType() {
		return Collections.singletonList(StrategyTypeEnum.NPE_STRATEGY.name());
	}

	@Override
	public boolean handle(SysParamContext context) throws Exception {
		SysPara sysPara = context.getSysPara();
		if (sysPara == null || Func.isAnyBlank(sysPara.getParaCode()) || Func.isAnyBlank(sysPara.getParaValue())) {
			throw new ServiceException("请认真检查参数!");
		}
		return true;
	}


}
