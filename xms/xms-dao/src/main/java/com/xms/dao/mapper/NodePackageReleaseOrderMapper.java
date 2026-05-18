package com.xms.dao.mapper;

import com.xms.dao.domain.NodePackageReleaseOrder;

import java.util.List;

/**
 * 节点认购AFI线性释放订单Mapper接口。
 *
 * <p>用于后台查询导出释放订单，以及定时任务批量更新释放进度。</p>
 */
public interface NodePackageReleaseOrderMapper extends XmsMapper<NodePackageReleaseOrder> {

	/**
	 * 查询节点线性释放订单列表。
	 *
	 * @param nodePackageReleaseOrder 查询条件
	 * @return 释放订单列表
	 */
	List<NodePackageReleaseOrder> selectNodePackageReleaseOrderList(NodePackageReleaseOrder nodePackageReleaseOrder);
}
