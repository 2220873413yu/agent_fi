package com.xms.dao.service.impl;

import com.xms.dao.domain.RechargeRecord;
import com.xms.dao.mapper.RechargeRecordMapper;
import com.xms.dao.service.IRechargeRecordService;
import com.xms.dao.service.UserWalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * 充值记录Service业务层处理
 *
 * @author xms
 * @date 2025-03-12
 */
@Service
public class RechargeRecordServiceImpl extends XmsDataServiceImpl<RechargeRecordMapper, RechargeRecord> implements IRechargeRecordService
{
	@Autowired
	private  UserWalletService userWalletServiceImpl;
    /**
     * 查询充值记录列表
     *
     *
     * @param rechargeRecord 充值记录
     * @return 充值记录
     */
    @Override
    public List<RechargeRecord> selectRechargeRecordList(RechargeRecord rechargeRecord)
    {
        return baseMapper.selectRechargeRecordList(rechargeRecord);
    }

	@Override
	@Transactional(rollbackFor = Exception.class)
	public int updateRecordById(RechargeRecord req) {
		return 1;
	}



	@Override
	public BigDecimal selectTotalRechargeBalance() {
		return baseMapper.selectTotalRechargeBalance();
	}

	@Override
	public BigDecimal selectTodayRechargeBalance() {
		return baseMapper.selectTodayRechargeBalance();
	}
}
