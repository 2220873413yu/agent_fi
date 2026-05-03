package com.xms.dao.service;

import java.util.List;
import com.xms.dao.service.XmsDataService;
import com.xms.dao.domain.MiningPackageOrder;

/**
 * 矿机订单Service接口
 *
 * @author xms
 * @date 2026-02-23
 */
public interface IMiningPackageOrderService extends XmsDataService<MiningPackageOrder>
{

    /**
     * 查询矿机订单列表
     *
     * @param miningPackageOrder 矿机订单
     * @return 矿机订单集合
     */
    public List<MiningPackageOrder> selectMiningPackageOrderList(MiningPackageOrder miningPackageOrder);

	/**
     * 矿机订单发货
     * @param miningPackageOrder
     * @return
     */
    int processShipment(MiningPackageOrder miningPackageOrder);

	/**
	 * 停止或开启订单
	 * @param miningPackageOrder
	 * @return
	 */
    int stopOrOpenOrder(MiningPackageOrder miningPackageOrder);

	/**
	 * 修改每日收益
	 * @param miningPackageOrder
	 * @return
	 */
	int updateDayReward(MiningPackageOrder miningPackageOrder);
}
