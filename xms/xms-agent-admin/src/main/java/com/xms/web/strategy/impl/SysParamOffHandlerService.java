package com.xms.web.strategy.impl;


import cn.hutool.core.util.NumberUtil;
import com.google.protobuf.ServiceException;
import com.xms.common.config.redis.XmsRedis;
import com.xms.common.constant.SysConstant;
import com.xms.common.utils.Func;
import com.xms.dao.entity.domain.SysPara;
import com.xms.web.enums.StrategyTypeEnum;
import com.xms.web.strategy.DefaultStrategy;
import com.xms.web.strategy.StrategyService;
import com.xms.web.strategy.context.SysParamContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;

/**
 * 指定1和2 开关策略
 *
 * @author MIER
 */
@Slf4j
@DefaultStrategy
@Service
public class SysParamOffHandlerService implements StrategyService<SysParamContext> {


	@Override
	public List<String> listType() {
		return Collections.singletonList(StrategyTypeEnum.WITHDRAWAL_OPEN_OR_CLOSE.name());
	}

	@Override
	public boolean handle(SysParamContext context) throws Exception {
		SysPara sysPara = context.getSysPara();
		if (sysPara == null || Func.isAnyBlank(sysPara.getParaCode()) || Func.isAnyBlank(sysPara.getParaValue())) {
			throw new ServiceException("请认真检查参数!");
		}
		try {
			int integer = NumberUtil.toBigInteger(sysPara.getParaValue()).intValue();
			if (integer < 1 || integer > 2) {
				throw new ServiceException("请不要填写1和2以外的数字哟!");
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException("请不要填写1和2以外的数字哟");
		}
		return true;
	}


}
