package com.xms.web.strategy.impl;


import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.NumberUtil;
import com.google.protobuf.ServiceException;
import com.xms.common.core.domain.api.ResultPista;
import com.xms.common.result.ResponseCode;
import com.xms.common.utils.DateUtils;
import com.xms.common.utils.Func;
import com.xms.dao.entity.domain.SysPara;
import com.xms.web.enums.StrategyTypeEnum;
import com.xms.web.strategy.DefaultStrategy;
import com.xms.web.strategy.StrategyService;
import com.xms.web.strategy.context.SysParamContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 检测时间格式是否正确
 *
 * @author MIER
 */
@Slf4j
@DefaultStrategy
@Service
public class SysParamSpecialHandlerService implements StrategyService<SysParamContext> {


	@Override
	public List<String> listType() {
		return List.of(StrategyTypeEnum.mine_trade_time_limit.name());
	}

	@Override
	public boolean handle(SysParamContext context) throws Exception {
		SysPara sysPara = context.getSysPara();
		if (sysPara == null || Func.isAnyBlank(sysPara.getParaCode()) || Func.isAnyBlank(sysPara.getParaValue())) {
			throw new ServiceException("请认真检查参数!");
		}
		//检测时间格式是否正确
		try {
			String[] strs = sysPara.getParaValue().split("-");
			DateUtil.parseTimeToday(strs[0]);
			DateUtil.parseTimeToday(strs[1]);
		} catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException("请参考 00:00-08:00 这种格式填写参数!");
		}
		return true;
	}


}
