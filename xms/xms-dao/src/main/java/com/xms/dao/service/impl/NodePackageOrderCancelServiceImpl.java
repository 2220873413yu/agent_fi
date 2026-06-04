package com.xms.dao.service.impl;

import com.xms.dao.domain.NodePackageOrderCancel;
import com.xms.dao.mapper.NodePackageOrderCancelMapper;
import com.xms.dao.service.INodePackageOrderCancelService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 节点套餐取消订单归档Service实现。
 */
@Service
public class NodePackageOrderCancelServiceImpl
	extends XmsDataServiceImpl<NodePackageOrderCancelMapper, NodePackageOrderCancel>
	implements INodePackageOrderCancelService {

	/**
	 * 查询节点套餐取消订单归档列表。
	 *
	 * <p>该列表用于后台审计已取消节点订单，只读展示原订单快照、取消人、取消时间和释放订单暂停快照。</p>
	 *
	 * @param nodePackageOrderCancel 查询条件
	 * @return 取消归档记录集合
	 */
	@Override
	public List<NodePackageOrderCancel> selectNodePackageOrderCancelList(NodePackageOrderCancel nodePackageOrderCancel) {
		return baseMapper.selectNodePackageOrderCancelList(nodePackageOrderCancel);
	}
}
