package com.xms.dao.service.impl;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.nio.charset.StandardCharsets;

import cn.hutool.core.util.StrUtil;
import com.xms.common.exception.ServiceException;
import com.xms.common.utils.CollectionUtil;
import com.xms.common.utils.uuid.IDUtils;
import com.xms.dao.domain.NodePackage;
import com.xms.dao.entity.domain.UserInfo;
import com.xms.dao.entity.req.AllocateNodePackReq;
import com.xms.dao.service.INodePackageService;
import com.xms.dao.service.UserInfoService;
import com.xms.dao.service.impl.XmsDataServiceImpl;
import org.springframework.transaction.annotation.Transactional;
import org.web3j.crypto.Hash;
import org.web3j.utils.Numeric;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.xms.dao.mapper.NodePackageOrderMapper;
import com.xms.dao.domain.NodePackageOrder;
import com.xms.dao.service.INodePackageOrderService;

/**
 * 节点购买记录Service业务层处理
 *
 * @author xms
 * @date 2026-04-28
 */
@Service
public class NodePackageOrderServiceImpl extends XmsDataServiceImpl<NodePackageOrderMapper, NodePackageOrder> implements INodePackageOrderService
{

	@Autowired
	private UserInfoService userInfoService;

	@Autowired
	private INodePackageService nodePackageService;

	@Override
	@Transactional(rollbackFor = Exception.class)
	public int updateOrderById(NodePackageOrder req) {
		NodePackageOrder queryOrder = lambdaQuery()
			.eq(NodePackageOrder::getId, req.getId())
			.one();
		if(req.getPackageLevel().equals(queryOrder.getPackageLevel())){
			throw new ServiceException("节点等级未发生变化");
		}
		//查询用户
		UserInfo userInfo = userInfoService.lambdaQuery()
			.eq(UserInfo::getUserId, queryOrder.getUserId())
			.one();
		//查询等级套餐
		NodePackage nodePackage = nodePackageService.lambdaQuery()
			.eq(NodePackage::getLevel, req.getPackageLevel())
			.one();

		//修改订单
		boolean update = lambdaUpdate()
			.eq(NodePackageOrder::getId, req.getId())
			.eq(NodePackageOrder::getPackageLevel, queryOrder.getPackageLevel())
			.set(NodePackageOrder::getPackageLevel, nodePackage.getLevel())
			.set(NodePackageOrder::getDirectReferralRate, nodePackage.getDirectReferralRate())
			.set(NodePackageOrder::getIndirectReferralRate, nodePackage.getIndirectReferralRate())
			.set(NodePackageOrder::getWeightMultiplier, nodePackage.getWeightMultiplier())
			.set(NodePackageOrder::getPredOrderFeeReliefRate, nodePackage.getPredOrderFeeReliefRate())
			.set(NodePackageOrder::getOrderValueUsdt, nodePackage.getPrice())
			.set(NodePackageOrder::getUpdateTime, new Date())
			.update();
		if(!update){
			throw new ServiceException("用户节点已经被修改了.请刷新页面后重试");
		}

		//修改用户节点等级、赠送节点信息
		update = userInfoService.lambdaUpdate()
			.eq(UserInfo::getUserId, queryOrder.getUserId())
			.eq(UserInfo::getNodeLevel, queryOrder.getPackageLevel())
			.set(UserInfo::getNodeLevel, req.getPackageLevel())
			.set(UserInfo::getMinGameLevel, req.getPackageLevel())
			.update();
		if(!update){
			throw new ServiceException("用户节点已经被修改了.请刷新页面后重试");
		}
		List<Long> parentIds = userInfo.getParentIds();
		if(CollectionUtil.isNotEmpty(parentIds)){
			BigDecimal p1 = queryOrder.getOrderValueUsdt().add(nodePackage.getPrice());
			//直推
			userInfoService.lambdaUpdate()
				.eq(UserInfo::getUserId, userInfo.getInviteUserId())
				.setSql("sub_umbrella_node_performance = sub_umbrella_node_performance + " + p1)
				.update();

			//修改团队业绩
			userInfoService.lambdaUpdate()
				.in(UserInfo::getUserId, parentIds)
				.setSql("umbrella_node_performance = umbrella_node_performance + " + p1)
				.update();
		}
		return 1;
	}


	/**
	 * 后台拨付节点
	 * @param req
	 * @return
	 */
	@Override
	public int saveNodePackageOrder(AllocateNodePackReq req) {
		if(StrUtil.isBlank(req.getAddress())){
			throw new ServiceException("拨付的用户地址不能为空");
		}

		UserInfo userInfo = userInfoService.lambdaQuery()
			.eq(UserInfo::getAccount, req.getAddress())
			.one();
		if(userInfo == null ){
			throw new ServiceException("用户不存在");
		}

		if(userInfo.getNodeLevel() >0){
			throw new ServiceException("该用户已拥有节点");
		}

		NodePackage nodePackage = nodePackageService.lambdaQuery()
			.eq(NodePackage::getLevel, req.getPackageLevel())
			.one();

		//插入订单
		NodePackageOrder insertOrder = new NodePackageOrder();
		insertOrder.setOrderNo(IDUtils.getSnowflakeStr());
		insertOrder.setUserId(userInfo.getUserId());
		insertOrder.setAddress(userInfo.getAccount());
		insertOrder.setHash(Numeric.toHexString(Hash.sha3( insertOrder.getOrderNo().getBytes(StandardCharsets.UTF_8))));
		insertOrder.setPackageLevel(req.getPackageLevel());
		insertOrder.setDirectReferralRate(nodePackage.getDirectReferralRate());
		insertOrder.setIndirectReferralRate(nodePackage.getIndirectReferralRate());
		insertOrder.setWeightMultiplier(nodePackage.getWeightMultiplier());
		insertOrder.setPredOrderFeeReliefRate(nodePackage.getPredOrderFeeReliefRate());
		insertOrder.setOrderValueUsdt(nodePackage.getPrice());
		insertOrder.setSourceType(1);
		insertOrder.setStatus(1);
		insertOrder.setBizStatus(1);
		insertOrder.setCreateTime(new Date());
		save(insertOrder);

		boolean update1 = userInfoService.lambdaUpdate()
			.eq(UserInfo::getUserId, userInfo.getUserId())
			.eq(UserInfo::getNodeLevel, 0)
			.set(UserInfo::getNodeLevel, req.getPackageLevel())
			.set(UserInfo::getMinGameLevel, req.getPackageLevel())
			.update();
		if (!update1) {
			throw new ServiceException("更新用户节点等级失败,请刷新后重试");
		}

		nodePackageService.lambdaUpdate()
			.eq(NodePackage::getId, req.getPackageLevel())
			.setSql("sales = sales +1")
			.update();

		if(userInfo.getInviteUserId()!=null){
			userInfoService.lambdaUpdate()
				.eq(UserInfo::getUserId, userInfo.getInviteUserId())
				.setSql("sub_node_performance = sub_node_performance + 1")
				.update();
			userInfoService.lambdaUpdate()
				.in(UserInfo::getUserId, userInfo.getParentIds())
				.setSql("node_team_performance = node_team_performance + 1")
				.update();
		}
		return 1;
	}

	/**
     * 查询节点购买记录列表
     *
     *
     * @param nodePackageOrder 节点购买记录
     * @return 节点购买记录
     */
    @Override
    public List<NodePackageOrder> selectNodePackageOrderList(NodePackageOrder nodePackageOrder)
    {
        return baseMapper.selectNodePackageOrderList(nodePackageOrder);
    }

}
