package com.xms.dao.service.impl;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import com.xms.common.exception.ServiceException;
import com.xms.dao.service.impl.XmsDataServiceImpl;
import org.springframework.stereotype.Service;
import com.xms.dao.mapper.MiningPackageOrderMapper;
import com.xms.dao.domain.MiningPackageOrder;
import com.xms.dao.service.IMiningPackageOrderService;

/**
 * 矿机订单Service业务层处理
 *
 * @author xms
 * @date 2026-02-23
 */
@Service
public class MiningPackageOrderServiceImpl extends XmsDataServiceImpl<MiningPackageOrderMapper, MiningPackageOrder> implements IMiningPackageOrderService
{


    /**
     * 查询矿机订单列表
     *
     *
     * @param miningPackageOrder 矿机订单
     * @return 矿机订单
     */
    @Override
    public List<MiningPackageOrder> selectMiningPackageOrderList(MiningPackageOrder miningPackageOrder)
    {
        return baseMapper.selectMiningPackageOrderList(miningPackageOrder);
    }

	@Override
	public int processShipment(MiningPackageOrder req) {
		MiningPackageOrder miningPackageOrder = lambdaQuery()
			.eq(MiningPackageOrder::getId, req.getId())
			.one();
		if(miningPackageOrder.getStakeType()!= null && miningPackageOrder.getStakeType() == 2) {
			lambdaUpdate()
				.eq(MiningPackageOrder::getId, req.getId())
				.set(MiningPackageOrder::getShippingStatus,1)
				.set(MiningPackageOrder::getShippingCompany,req.getShippingCompany())
				.set(MiningPackageOrder::getTrackingNo,req.getTrackingNo())
				.update();
		}
		return 1;
	}

	@Override
	public int stopOrOpenOrder(MiningPackageOrder req) {
		MiningPackageOrder queryOrder = lambdaQuery()
			.eq(MiningPackageOrder::getId, req.getId())
			.one();
		if(queryOrder!=null){
			if(queryOrder.getStatus()>1){
				//修改状态
				lambdaUpdate()
					.eq(MiningPackageOrder::getId, req.getId())
					.set(MiningPackageOrder::getStatus,req.getStatus())
					.update();
			}
		}
		return 1;
	}

	@Override
	public int updateDayReward(MiningPackageOrder req) {
		if(req.getDayReward() == null || req.getDayReward().compareTo(BigDecimal.ZERO) < 0){
			throw new ServiceException("每日收益数不能小于等于0");
		}
		lambdaUpdate()
			.eq(MiningPackageOrder::getId, req.getId())
			.set(MiningPackageOrder::getDayReward,req.getDayReward())
			.set(MiningPackageOrder::getUpdateTime,new Date())
			.update();
		return 1;
	}
}
