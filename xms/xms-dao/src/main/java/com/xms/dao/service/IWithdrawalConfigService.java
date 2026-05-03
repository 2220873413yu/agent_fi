package com.xms.dao.service;

import java.util.List;
import com.xms.dao.service.XmsDataService;
import com.xms.dao.domain.WithdrawalConfig;

/**
 * 提现配置Service接口
 *
 * @author xms
 * @date 2026-02-06
 */
public interface IWithdrawalConfigService extends XmsDataService<WithdrawalConfig>
{

    /**
     * 查询提现配置列表
     *
     * @param withdrawalConfig 提现配置
     * @return 提现配置集合
     */
    public List<WithdrawalConfig> selectWithdrawalConfigList(WithdrawalConfig withdrawalConfig);

	/**
	 * 修改提现配置
	 * @param withdrawalConfig
	 * @return
	 */
	int updateByConfigById(WithdrawalConfig withdrawalConfig);
}
