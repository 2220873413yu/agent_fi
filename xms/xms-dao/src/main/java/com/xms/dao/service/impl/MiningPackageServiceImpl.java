package com.xms.dao.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.xms.common.config.redis.lock.RedisLock;
import com.xms.common.constant.RedisConstant;
import com.xms.common.exception.ServiceException;
import com.xms.common.mq.dynamic.AsyncDynamicOrderSettlementService;
import com.xms.common.mq.dynamic.OrderMsgDO;
import com.xms.common.result.ResponseCode;
import com.xms.common.utils.uuid.IDUtils;
import com.xms.dao.domain.MiningPackageOrder;
import com.xms.dao.entity.domain.UserInfo;
import com.xms.dao.entity.req.AdminAllocateMiningMachineRequest;
import com.xms.dao.service.IMiningPackageOrderService;
import com.xms.dao.service.UserInfoService;
import com.xms.dao.service.impl.XmsDataServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.xms.dao.mapper.MiningPackageMapper;
import com.xms.dao.domain.MiningPackage;
import com.xms.dao.service.IMiningPackageService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * 矿机套餐Service业务层处理
 *
 * @author xms
 * @date 2026-02-21
 */
@Service
public class MiningPackageServiceImpl extends XmsDataServiceImpl<MiningPackageMapper, MiningPackage> implements IMiningPackageService
{


	/**
     * 查询矿机套餐列表
     *
     *
     * @param miningPackage 矿机套餐
     * @return 矿机套餐
     */
    @Override
    public List<MiningPackage> selectMiningPackageList(MiningPackage miningPackage)
    {
        return baseMapper.selectMiningPackageList(miningPackage);
    }

	@Override
	public int updateMiningPackageById(MiningPackage miningPackage) {
		if(miningPackage.getPrice() == null || miningPackage.getPrice().compareTo(BigDecimal.ZERO)<=0){
			throw  new ServiceException("矿机价格不能小于等于0");
		}
		if(miningPackage.getAvailableStock() == null || miningPackage.getAvailableStock()<0){
			throw  new ServiceException("可售库存不能小于0");
		}
		if(StrUtil.isBlank(miningPackage.getRemark())){
			throw new ServiceException("算力不能为空");
		}
		BigDecimal bigDecimal = new BigDecimal(miningPackage.getRemark());
		if(bigDecimal.compareTo(BigDecimal.ZERO)<=0){
			throw new ServiceException("算力不能小于等于0");
		}
		lambdaUpdate()
			.eq(MiningPackage::getId, miningPackage.getId())
			.set(MiningPackage::getRemark, miningPackage.getRemark())
			.set(MiningPackage::getPrice, miningPackage.getPrice())
			.set(MiningPackage::getAvailableStock, miningPackage.getAvailableStock())
			.set(MiningPackage::getStatus, miningPackage.getStatus())
			.set(MiningPackage::getUpdateTime, new Date())
			.update();

		return 1;
	}
}
