package com.xms.dao.service.impl;

import com.xms.dao.domain.NodePackageReleaseOrder;
import com.xms.dao.mapper.NodePackageReleaseOrderMapper;
import com.xms.dao.service.INodePackageReleaseOrderService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 节点认购AFI线性释放订单Service实现。
 */
@Service
public class NodePackageReleaseOrderServiceImpl
	extends XmsDataServiceImpl<NodePackageReleaseOrderMapper, NodePackageReleaseOrder>
	implements INodePackageReleaseOrderService {

	/**
	 * 查询节点线性释放订单列表。
	 *
	 * @param nodePackageReleaseOrder 查询条件
	 * @return 释放订单列表
	 */
	@Override
	public List<NodePackageReleaseOrder> selectNodePackageReleaseOrderList(NodePackageReleaseOrder nodePackageReleaseOrder) {
		return baseMapper.selectNodePackageReleaseOrderList(nodePackageReleaseOrder);
	}
}
