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

	/**
	 * 修改节点购买记录
	 * @param nodePackageOrder
	 * @return
	 */
	int updateOrderById(NodePackageOrder nodePackageOrder);

	/**
	 * 取消已生效节点订单。
	 *
	 * <p>取消时先归档原订单，再回滚用户节点权益、节点销量和节点业绩，最后移出主表订单，
	 * 避免取消订单继续参与节点权益、AFI释放和手续费减免等业务。</p>
	 *
	 * @param id 节点订单id
	 * @param cancelBy 后台取消操作人
	 * @return 影响业务条数，1表示取消成功
	 */
	int cancelNodePackageOrder(Long id, String cancelBy);

	/**
	 * 恢复用户历史取消节点时暂停的AFI线性释放订单。
	 *
	 * <p>用户重新购买或后台重新拨付节点成功后调用。只恢复已暂停且仍有剩余AFI的释放订单，
	 * 并把释放订单关联到新的节点订单；不重置已释放金额、剩余金额、运行天数和总释放金额。</p>
	 *
	 * @param newOrder 新生效的节点订单
	 * @return 1表示恢复了暂停释放订单，0表示没有符合条件的释放订单
	 */
	int restorePausedReleaseOrderIfNeeded(NodePackageOrder newOrder);
}
