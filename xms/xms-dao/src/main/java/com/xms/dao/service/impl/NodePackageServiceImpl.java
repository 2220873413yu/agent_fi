package com.xms.dao.service.impl;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import com.xms.dao.service.impl.XmsDataServiceImpl;
import org.springframework.stereotype.Service;
import com.xms.dao.mapper.NodePackageMapper;
import com.xms.dao.domain.NodePackage;
import com.xms.dao.service.INodePackageService;

/**
 * 节点套餐Service业务层处理
 *
 * @author xms
 * @date 2026-04-28
 */
@Service
public class NodePackageServiceImpl extends XmsDataServiceImpl<NodePackageMapper, NodePackage> implements INodePackageService
{


    /**
     * 查询节点套餐列表
     *
     *
     * @param nodePackage 节点套餐
     * @return 节点套餐
     */
    @Override
    public List<NodePackage> selectNodePackageList(NodePackage nodePackage)
    {
        return baseMapper.selectNodePackageList(nodePackage);
    }

	@Override
	public int updateNodePackageById(NodePackage req) {
		if(req.getPrice() == null || req.getPrice().compareTo(BigDecimal.ZERO) <= 0){
			throw new RuntimeException("价格不能小于0");
		}
		if(req.getDirectReferralRate() == null || req.getDirectReferralRate().compareTo(BigDecimal.ZERO) < 0){
			throw new RuntimeException("直推比例不能小于0");
		}
		if(req.getIndirectReferralRate() == null || req.getIndirectReferralRate().compareTo(BigDecimal.ZERO) < 0){
			throw new RuntimeException("间推比例不能小于0");
		}
		if(req.getWeightMultiplier() == null || req.getWeightMultiplier().compareTo(BigDecimal.ZERO) < 0){
			throw new RuntimeException("权重系数不能小于0");
		}
		if(req.getPredOrderFeeReliefRate() == null || req.getPredOrderFeeReliefRate().compareTo(BigDecimal.ZERO) < 0){
			throw new RuntimeException("预测下单手续费减免比例不能小于0");
		}
		lambdaUpdate()
			.eq(NodePackage::getLevel, req.getLevel())
			.set(NodePackage::getPrice,req.getPrice())
			.set(NodePackage::getDirectReferralRate,req.getDirectReferralRate())
			.set(NodePackage::getIndirectReferralRate,req.getIndirectReferralRate())
			.set(NodePackage::getWeightMultiplier,req.getWeightMultiplier())
			.set(NodePackage::getPredOrderFeeReliefRate,req.getPredOrderFeeReliefRate())
			.set(NodePackage::getStatus,req.getStatus())
			.set(NodePackage::getUpdateTime,new Date())
			.update();
		return 1;
	}
}
