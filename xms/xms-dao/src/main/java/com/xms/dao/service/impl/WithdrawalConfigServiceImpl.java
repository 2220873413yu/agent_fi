package com.xms.dao.service.impl;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import com.xms.common.exception.ServiceException;
import com.xms.dao.service.impl.XmsDataServiceImpl;
import org.springframework.stereotype.Service;
import com.xms.dao.mapper.WithdrawalConfigMapper;
import com.xms.dao.domain.WithdrawalConfig;
import com.xms.dao.service.IWithdrawalConfigService;

/**
 * 提现配置Service业务层处理
 *
 * @author xms
 * @date 2026-02-06
 */
@Service
public class WithdrawalConfigServiceImpl extends XmsDataServiceImpl<WithdrawalConfigMapper, WithdrawalConfig> implements IWithdrawalConfigService
{


    /**
     * 查询提现配置列表
     *
     *
     * @param withdrawalConfig 提现配置
     * @return 提现配置
     */
    @Override
    public List<WithdrawalConfig> selectWithdrawalConfigList(WithdrawalConfig withdrawalConfig)
    {
        return baseMapper.selectWithdrawalConfigList(withdrawalConfig);
    }


	@Override
	public int updateByConfigById(WithdrawalConfig req) {
		if(req.getFeeRatio() == null || req.getFeeRatio().compareTo(BigDecimal.ZERO) < 0){
			throw new ServiceException("提现手续费率不能小于0");
		}

		if(req.getMinWithdrawAmount() == null || req.getMinWithdrawAmount().compareTo(BigDecimal.ZERO) <= 0){
			throw new ServiceException("最小提现金额小于等于0");
		}

		if(req.getDailyFreeAuditCount()<0){
			throw new ServiceException("单日免审核次数不能小于0");
		}

		if(req.getWithdrawLimit().compareTo(BigDecimal.ZERO)<0){
			throw new ServiceException("单日提现额度不能小于0");
		}

		boolean update = lambdaUpdate()
			.eq(WithdrawalConfig::getId, req.getId())
			.set(WithdrawalConfig::getWithdrawOpen, req.getWithdrawOpen())
			.set(WithdrawalConfig::getMinWithdrawAmount, req.getMinWithdrawAmount())
			.set(WithdrawalConfig::getDailyFreeAuditCount, req.getDailyFreeAuditCount())
			.set(WithdrawalConfig::getWithdrawLimit, req.getWithdrawLimit())
			.set(WithdrawalConfig::getFeeRatio, req.getFeeRatio())
			.set(WithdrawalConfig::getUpdateTime, new Date())
			.update();
		if(!update){
			throw new ServiceException("更新提现配置失败,请刷新页面后重试");
		}
		return 1;
	}
}
