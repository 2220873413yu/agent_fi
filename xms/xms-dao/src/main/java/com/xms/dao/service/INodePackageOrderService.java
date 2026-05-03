package com.xms.dao.service;

import java.util.List;

import com.xms.dao.entity.req.AllocateNodePackReq;
import com.xms.dao.service.XmsDataService;
import com.xms.dao.domain.NodePackageOrder;

/**
 * 节点购买记录Service接口
 *
 * @author xms
 * @date 2026-04-28
 */
public interface INodePackageOrderService extends XmsDataService<NodePackageOrder>
{

    /**
     * 查询节点购买记录列表
     *
     * @param nodePackageOrder 节点购买记录
     * @return 节点购买记录集合
     */
    public List<NodePackageOrder> selectNodePackageOrderList(NodePackageOrder nodePackageOrder);

	/**
	 * 后台拨付节点
	 * @param req
	 * @return
	 */
	int saveNodePackageOrder(AllocateNodePackReq req);
}
