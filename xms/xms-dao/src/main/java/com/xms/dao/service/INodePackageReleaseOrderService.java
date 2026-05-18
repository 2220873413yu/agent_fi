package com.xms.dao.service;

import com.xms.dao.domain.NodePackageReleaseOrder;

import java.util.List;

/**
 * 节点认购AFI线性释放订单Service接口。
 */
public interface INodePackageReleaseOrderService extends XmsDataService<NodePackageReleaseOrder> {

	/**
	 * 查询节点线性释放订单列表。
	 *
	 * @param nodePackageReleaseOrder 查询条件
	 * @return 释放订单列表
	 */
	List<NodePackageReleaseOrder> selectNodePackageReleaseOrderList(NodePackageReleaseOrder nodePackageReleaseOrder);
}
