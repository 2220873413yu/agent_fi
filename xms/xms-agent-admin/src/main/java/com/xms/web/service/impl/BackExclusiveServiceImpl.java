package com.xms.web.service.impl;

import cn.hutool.system.SystemUtil;
import com.google.protobuf.ServiceException;
import com.xms.common.config.redis.XmsRedis;
import com.xms.common.constant.ConstantStatic;
import com.xms.common.constant.RedisConstant;
import com.xms.common.constant.SysConstant;
import com.xms.dao.entity.domain.SysPara;
import com.xms.dao.service.ISysParaService;
import com.xms.system.service.ISysUserService;
import com.xms.web.service.BackExclusiveService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author: GT63S
 * @createDate: 2024/8/17
 */
@Service
@AllArgsConstructor
public class BackExclusiveServiceImpl implements BackExclusiveService {
	private final XmsRedis xmsRedis;
	private final ISysParaService sysParaServiceImpl;
	private final ISysUserService sysUserService;

	@Override
	@Transactional(rollbackFor = Exception.class)
	public int updateSysPara(SysPara sysPara) throws Exception {
/*		StrategyService handlerService = StrategyFactory.doStrategy(sysPara.getParaCode(), StrategyService.class);
		if (handlerService != null) {
			SysParamContext sysParamContext = new SysParamContext();
			sysParamContext.setSysPara(sysPara);
			handlerService.handle(sysParamContext);
		}*/
		if (!SystemUtil.getOsInfo().getName().toUpperCase().contains(ConstantStatic.OS_NAME_WINDOWS)) {
			sysUserService.pubValidate(sysPara.getAutoCode());
		}
/*		if(sysPara.getParaCode().equalsIgnoreCase(SysConstant.MINE_TRADE_TIME_LIMIT)){
			if(!isValidTimeRangeFormat(sysPara.getParaValue())){
				throw new ServiceException("时间格式不正确");
			}
		}*/

		boolean rs = sysParaServiceImpl.updateById(sysPara);
		xmsRedis.del(RedisConstant.XMS_PARAM + sysPara.getParaCode());
		return rs ? 1 : 0;
	}

	public boolean isValidTimeRangeFormat(String paraValue) {
		// 检查格式是否为 HH:MM-HH:MM
		String regex = "^([01]?\\d|2[0-3]):([0-5]\\d)-([01]?\\d|2[0-3]):([0-5]\\d)$";
		if (!paraValue.matches(regex)) {
			return false;
		}

		// 提取开始和结束时间
		String[] parts = paraValue.split("-");
		String startTime = parts[0];
		String endTime = parts[1];

		// 将时间转换为分钟，便于比较
		String[] startParts = startTime.split(":");
		String[] endParts = endTime.split(":");

		int startHour = Integer.parseInt(startParts[0]);
		int startMinute = Integer.parseInt(startParts[1]);
		int endHour = Integer.parseInt(endParts[0]);
		int endMinute = Integer.parseInt(endParts[1]);

		int startTotalMinutes = startHour * 60 + startMinute;
		int endTotalMinutes = endHour * 60 + endMinute;

		// 检查结束时间是否大于等于开始时间
		return endTotalMinutes >= startTotalMinutes;
	}
}
