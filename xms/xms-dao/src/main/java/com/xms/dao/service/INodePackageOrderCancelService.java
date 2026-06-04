package com.xms.dao.service;

import com.xms.dao.domain.NodePackageOrderCancel;

import java.util.List;

/**
 * 节点套餐取消订单归档Service接口。
 */
public interface INodePackageOrderCancelService extends XmsDataService<NodePackageOrderCancel> {
	/**
	 * 查询节点套餐取消订单归档列表。
	 *
	 * @param nodePackageOrderCancel 查询条件
	 * @return 取消归档记录集合
	 */
	List<NodePackageOrderCancel> selectNodePackageOrderCancelList(NodePackageOrderCancel nodePackageOrderCancel);
}
