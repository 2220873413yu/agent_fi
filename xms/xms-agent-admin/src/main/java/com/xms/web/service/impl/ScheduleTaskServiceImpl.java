package com.xms.web.service.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.xms.dao.service.*;
import com.xms.web.mapper.ScheduleTaskMapper;
import com.xms.web.service.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Service;

/**
 * @author: renengadePISTA
 * @createDate: 2024/1/9
 */
@Service
@Slf4j
@AllArgsConstructor
@Import(cn.hutool.extra.spring.SpringUtil.class)
public class ScheduleTaskServiceImpl implements ScheduleTaskService {
	private final IAsyncTaskService asyncTaskServiceImpl;
	private final UserRelationService userRelationServiceImpl;
	private final ISysParaService sysParaServiceImpl;
	private final ScheduleTaskMapper scheduleTaskMapper;
	private final IUserMoneyService userMoneyServiceImpl;

	public static void main(String[] args) {
		String today = DateUtil.today();
		DateTime parse = DateUtil.parse(today);
		DateTime dateTime = DateUtil.offsetHour(parse, -24);
		System.out.println(today);
		System.out.println(parse);
		System.out.println(dateTime);
	}

	@Override
	public void dealMiningOrder() {

	}


	@Override
	public void dealMiningShardingOrder() {

	}



	@Override
	public void dealShareEnergyPool() throws Exception {

	}

}
