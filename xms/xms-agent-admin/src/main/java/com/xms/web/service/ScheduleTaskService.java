package com.xms.web.service;

/**
 * @author MIER
 */
public interface ScheduleTaskService {
	void dealMiningOrder();


	void dealMiningShardingOrder();

	void dealShareEnergyPool() throws Exception;

}
