package com.xms.dao.mapper;

import java.util.List;
import com.xms.dao.mapper.XmsMapper;

import com.xms.dao.domain.WithdrawalConfig;

/**
 * 提现配置Mapper接口
 *
 * @author xms
 * @date 2026-02-06
 */
public interface WithdrawalConfigMapper extends XmsMapper<WithdrawalConfig>
{
    /**
     * 查询提现配置列表
     *
     * @param withdrawalConfig 提现配置
     * @return 提现配置集合
     */
    public List<WithdrawalConfig> selectWithdrawalConfigList(WithdrawalConfig withdrawalConfig);

}
