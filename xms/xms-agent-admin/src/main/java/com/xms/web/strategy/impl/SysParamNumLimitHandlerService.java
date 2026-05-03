package com.xms.web.strategy.impl;


import cn.hutool.core.util.NumberUtil;
import com.google.protobuf.ServiceException;
import com.xms.common.config.redis.XmsRedis;
import com.xms.common.utils.Func;
import com.xms.dao.entity.domain.SysPara;
import com.xms.web.enums.StrategyTypeEnum;
import com.xms.web.strategy.DefaultStrategy;
import com.xms.web.strategy.StrategyService;
import com.xms.web.strategy.context.SysParamContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

/**
 * 0-1 策略
 *
 * @author MIER
 */
@Slf4j
@DefaultStrategy
@Service
public class SysParamNumLimitHandlerService implements StrategyService<SysParamContext> {

	@Override
	public List<String> listType() {
		return Collections.singletonList(StrategyTypeEnum.WITHDRAWAL_RATIO.name());
	}

	@Override
	public boolean handle(SysParamContext context) throws Exception {
		SysPara sysPara = context.getSysPara();
		if (sysPara == null || Func.isAnyBlank(sysPara.getParaCode()) || Func.isAnyBlank(sysPara.getParaValue())) {
			throw new ServiceException("请认真检查参数!");
		}
		try {
			if (NumberUtil.toBigDecimal(sysPara.getParaValue()).compareTo(BigDecimal.ZERO) < 0
				|| NumberUtil.toBigDecimal(sysPara.getParaValue()).compareTo(BigDecimal.ONE) >= 0) {
				throw new ServiceException("请填写0-1之间的数字");
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException("请填写0-1之间的数字");
		}

		return true;
	}


}
