package com.xms.dao.service.impl;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.nio.charset.StandardCharsets;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import cn.hutool.core.util.StrUtil;
import com.xms.common.exception.ServiceException;
import com.xms.common.utils.CollectionUtil;
import com.xms.common.utils.uuid.IDUtils;
import com.xms.dao.domain.NodePackage;
import com.xms.dao.domain.NodePackageOrderCancel;
import com.xms.dao.domain.NodePackageReleaseOrder;
import com.xms.dao.entity.domain.UserInfo;
import com.xms.dao.entity.req.AllocateNodePackReq;
import com.xms.dao.mapper.NodePackageOrderCancelMapper;
import com.xms.dao.service.INodePackageReleaseOrderService;
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
	private static final int NODE_ORDER_STATUS_PAID = 1;
	private static final int NODE_ORDER_BIZ_STATUS_DONE = 1;
	private static final int NODE_ORDER_SOURCE_USER_BUY = 0;
	private static final int NODE_ORDER_SOURCE_ADMIN_GRANT = 1;
	private static final int NODE_RELEASE_STATUS_PENDING = 0;
	private static final int NODE_RELEASE_STATUS_RELEASING = 1;
	private static final int NODE_RELEASE_STATUS_PAUSED = 4;

	@Autowired
	private UserInfoService userInfoService;

	@Autowired
	private INodePackageService nodePackageService;

	@Autowired
	private INodePackageReleaseOrderService nodePackageReleaseOrderService;

	@Autowired
	private NodePackageOrderCancelMapper nodePackageOrderCancelMapper;

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
			BigDecimal p1 = nodePackage.getPrice().subtract(queryOrder.getOrderValueUsdt());
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
	 * 取消已生效节点订单，并把原订单迁移到取消归档表。
	 *
	 * <p>该方法不处理退款，也不追扣已经释放的AFI。取消成功后主表订单会被删除，用户节点权益、
	 * 节点套餐销量、节点团队业绩会按原订单快照回滚，后续购买链路可以重新创建节点订单。</p>
	 *
	 * @param id 节点订单id
	 * @param cancelBy 后台取消操作人
	 * @return 1表示取消成功
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public int cancelNodePackageOrder(Long id, String cancelBy) {
		if (id == null) {
			throw new ServiceException("节点订单不能为空");
		}

		// 锁定主表订单，避免取消和修改节点等级、异步补偿任务并发处理同一条订单。
		NodePackageOrder order = lambdaQuery()
			.eq(NodePackageOrder::getId, id)
			.last("limit 1 for update")
			.one();
		if (order == null) {
			throw new ServiceException("节点订单不存在或已取消");
		}
		if (!Integer.valueOf(NODE_ORDER_STATUS_PAID).equals(order.getStatus())) {
			throw new ServiceException("仅支持取消已支付成功的节点订单");
		}
		if (!Integer.valueOf(NODE_ORDER_BIZ_STATUS_DONE).equals(order.getBizStatus())) {
			throw new ServiceException("节点订单业务处理中，请稍后再取消");
		}

		UserInfo userInfo = userInfoService.lambdaQuery()
			.eq(UserInfo::getUserId, order.getUserId())
			.last("limit 1 for update")
			.one();
		if (userInfo == null) {
			throw new ServiceException("节点订单用户不存在");
		}

		NodePackageReleaseOrder releaseOrder = nodePackageReleaseOrderService.lambdaQuery()
			.eq(NodePackageReleaseOrder::getNodeOrderId, order.getId())
			.eq(NodePackageReleaseOrder::getDeleted, 0)
			.orderByDesc(NodePackageReleaseOrder::getId)
			.last("limit 1")
			.one();

		// 先归档原订单和释放订单快照，确保后续删除主表订单后仍可审计取消来源。
		NodePackageOrderCancel archive = buildCancelArchive(order, releaseOrder, cancelBy);
		if (nodePackageOrderCancelMapper.insert(archive) != 1) {
			throw new ServiceException("归档取消节点订单失败");
		}

		// 已存在AFI线性释放订单时，只暂停后续释放，保留已释放和剩余金额快照。
		pauseReleaseOrderIfNeeded(releaseOrder);

		// 回滚用户节点权益，使用户后续可以重新走购买节点链路。
		resetUserNodeLevel(order);

		// 回滚套餐销量和节点业绩，购买订单与后台拨付订单使用不同销售额口径。
		rollbackNodePackageSales(order);
		rollbackNodePerformance(order, userInfo);

		if (!removeById(order.getId())) {
			throw new ServiceException("删除原节点订单失败");
		}
		return 1;
	}

	/**
	 * 构建节点取消归档快照。
	 *
	 * @param order 原节点订单
	 * @param releaseOrder 当前节点订单对应的AFI释放订单，可为空
	 * @param cancelBy 取消操作人
	 * @return 待插入的取消归档记录
	 */
	private NodePackageOrderCancel buildCancelArchive(NodePackageOrder order, NodePackageReleaseOrder releaseOrder, String cancelBy) {
		NodePackageOrderCancel archive = new NodePackageOrderCancel();
		archive.setOriginOrderId(order.getId());
		archive.setOrderNo(order.getOrderNo());
		archive.setUserId(order.getUserId());
		archive.setAddress(order.getAddress());
		archive.setHash(order.getHash());
		archive.setPackageLevel(order.getPackageLevel());
		archive.setDirectReferralRate(order.getDirectReferralRate());
		archive.setIndirectReferralRate(order.getIndirectReferralRate());
		archive.setWeightMultiplier(order.getWeightMultiplier());
		archive.setPredOrderFeeReliefRate(order.getPredOrderFeeReliefRate());
		archive.setOrderValueUsdt(order.getOrderValueUsdt());
		archive.setSourceType(order.getSourceType());
		archive.setStatus(order.getStatus());
		archive.setBizStatus(order.getBizStatus());
		archive.setCreateTime(order.getCreateTime());
		archive.setUpdateTime(order.getUpdateTime());
		archive.setPayTime(order.getPayTime());
		archive.setCancelTime(new Date());
		archive.setCancelBy(cancelBy);
		if (releaseOrder != null) {
			archive.setReleaseOrderId(releaseOrder.getId());
			archive.setReleaseStatusBefore(releaseOrder.getStatus());
			archive.setReleasedAmountSnapshot(releaseOrder.getReleasedAmount());
			archive.setRemainingAmountSnapshot(releaseOrder.getRemainingAmount());
		}
		return archive;
	}

	/**
	 * 暂停节点订单后续AFI线性释放。
	 *
	 * <p>只抢占待释放和释放中状态，已完成、异常或已经暂停的释放订单不再改动；
	 * 该方法不会写钱包扣减，也不会追扣历史已释放AFI。</p>
	 *
	 * @param releaseOrder 需要暂停的释放订单，可为空
	 */
	private void pauseReleaseOrderIfNeeded(NodePackageReleaseOrder releaseOrder) {
		if (releaseOrder == null) {
			return;
		}
		boolean update = nodePackageReleaseOrderService.lambdaUpdate()
			.eq(NodePackageReleaseOrder::getId, releaseOrder.getId())
			.in(NodePackageReleaseOrder::getStatus, NODE_RELEASE_STATUS_PENDING, NODE_RELEASE_STATUS_RELEASING)
			.set(NodePackageReleaseOrder::getStatus, NODE_RELEASE_STATUS_PAUSED)
			.set(NodePackageReleaseOrder::getUpdateTime, new Date())
			.update();
		if (!update && (Integer.valueOf(NODE_RELEASE_STATUS_PENDING).equals(releaseOrder.getStatus())
			|| Integer.valueOf(NODE_RELEASE_STATUS_RELEASING).equals(releaseOrder.getStatus()))) {
			throw new ServiceException("暂停节点AFI释放订单失败");
		}
	}

	/**
	 * 回滚用户节点等级和节点赠送等级。
	 *
	 * @param order 取消的原节点订单
	 */
	private void resetUserNodeLevel(NodePackageOrder order) {
		boolean update = userInfoService.lambdaUpdate()
			.eq(UserInfo::getUserId, order.getUserId())
			.eq(UserInfo::getNodeLevel, order.getPackageLevel())
			.set(UserInfo::getNodeLevel, 0)
			.set(UserInfo::getMinGameLevel, 0)
			.update();
		if (!update) {
			throw new ServiceException("回滚用户节点等级失败,请刷新后重试");
		}
	}

	/**
	 * 回滚节点套餐销量。
	 *
	 * @param order 取消的原节点订单
	 */
	private void rollbackNodePackageSales(NodePackageOrder order) {
		nodePackageService.lambdaUpdate()
			.eq(NodePackage::getId, order.getPackageLevel())
			.setSql("sales = GREATEST(CAST(IFNULL(sales, '0') AS SIGNED) - 1, 0)")
			.update();
	}

	/**
	 * 按订单来源回滚上级节点数量和销售额业绩。
	 *
	 * <p>用户购买订单回滚购买销售额口径；后台拨付订单只回滚后台拨付销售额口径。
	 * 所有扣减使用GREATEST保护，避免历史异常数据被扣成负数。</p>
	 *
	 * @param order 取消的原节点订单
	 * @param userInfo 订单用户快照
	 */
	private void rollbackNodePerformance(NodePackageOrder order, UserInfo userInfo) {
		List<Long> parentIds = userInfo.getParentIds();
		if (CollectionUtil.isEmpty(parentIds)) {
			return;
		}

		if (userInfo.getInviteUserId() != null) {
			userInfoService.lambdaUpdate()
				.eq(UserInfo::getUserId, userInfo.getInviteUserId())
				.setSql("sub_node_performance = GREATEST(IFNULL(sub_node_performance, 0) - 1, 0)")
				.update();
		}

		userInfoService.lambdaUpdate()
			.in(UserInfo::getUserId, parentIds)
			.setSql("node_team_performance = GREATEST(IFNULL(node_team_performance, 0) - 1, 0)")
			.update();

		String orderAmount = defaultAmount(order.getOrderValueUsdt()).toPlainString();
		if (Integer.valueOf(NODE_ORDER_SOURCE_USER_BUY).equals(order.getSourceType())) {
			rollbackUserBuyAmountPerformance(userInfo, parentIds, orderAmount);
		}
		if (Integer.valueOf(NODE_ORDER_SOURCE_ADMIN_GRANT).equals(order.getSourceType())) {
			rollbackAdminGrantAmountPerformance(parentIds, orderAmount);
		}
	}

	/**
	 * 回滚用户购买节点订单产生的直推和团队节点销售额。
	 *
	 * @param userInfo 订单用户快照
	 * @param parentIds 订单用户的上级链用户id
	 * @param orderAmount 订单USDT金额字符串
	 */
	private void rollbackUserBuyAmountPerformance(UserInfo userInfo, List<Long> parentIds, String orderAmount) {
		if (userInfo.getInviteUserId() != null) {
			userInfoService.lambdaUpdate()
				.eq(UserInfo::getUserId, userInfo.getInviteUserId())
				.setSql("sub_umbrella_node_performance = GREATEST(IFNULL(sub_umbrella_node_performance, 0) - " + orderAmount + ", 0)")
				.update();
		}
		userInfoService.lambdaUpdate()
			.in(UserInfo::getUserId, parentIds)
			.setSql("umbrella_node_performance = GREATEST(IFNULL(umbrella_node_performance, 0) - " + orderAmount + ", 0)")
			.update();
	}

	/**
	 * 回滚后台拨付节点订单产生的团队后台拨付销售额。
	 *
	 * @param parentIds 订单用户的上级链用户id
	 * @param orderAmount 订单USDT金额字符串
	 */
	private void rollbackAdminGrantAmountPerformance(List<Long> parentIds, String orderAmount) {
		userInfoService.lambdaUpdate()
			.in(UserInfo::getUserId, parentIds)
			.setSql("admin_umbrella_node_performance = GREATEST(IFNULL(admin_umbrella_node_performance, 0) - " + orderAmount + ", 0)")
			.update();
	}

	/**
	 * 返回订单金额，空值按0处理，避免历史脏数据导致SQL扣减拼接空值。
	 *
	 * @param amount 订单金额，单位USDT
	 * @return 非空订单金额
	 */
	private BigDecimal defaultAmount(BigDecimal amount) {
		return amount == null ? BigDecimal.ZERO : amount;
	}

	/**
	 * 恢复用户历史取消节点时暂停的AFI线性释放订单。
	 *
	 * <p>恢复只改变释放订单状态和来源节点订单关联，不改释放金额、剩余金额、运行天数或总释放金额。
	 * 这样可以延续取消前的释放进度，避免重新初始化导致重复释放AFI。</p>
	 *
	 * @param newOrder 新购买或后台拨付后已经生效的节点订单
	 * @return 1表示恢复成功，0表示没有符合条件的暂停释放订单
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public int restorePausedReleaseOrderIfNeeded(NodePackageOrder newOrder) {
		if (newOrder == null || newOrder.getId() == null || newOrder.getUserId() == null) {
			return 0;
		}

		// 找该用户最近一次带释放订单快照的取消记录，作为恢复暂停释放订单的来源。
		NodePackageOrderCancel cancelArchive = nodePackageOrderCancelMapper.selectOne(Wrappers.lambdaQuery(NodePackageOrderCancel.class)
			.eq(NodePackageOrderCancel::getUserId, newOrder.getUserId())
			.isNotNull(NodePackageOrderCancel::getReleaseOrderId)
			.orderByDesc(NodePackageOrderCancel::getCancelTime)
			.orderByDesc(NodePackageOrderCancel::getId)
			.last("limit 1"));
		if (cancelArchive == null || cancelArchive.getReleaseOrderId() == null) {
			return 0;
		}

		NodePackageReleaseOrder releaseOrder = nodePackageReleaseOrderService.lambdaQuery()
			.eq(NodePackageReleaseOrder::getId, cancelArchive.getReleaseOrderId())
			.eq(NodePackageReleaseOrder::getStatus, NODE_RELEASE_STATUS_PAUSED)
			.eq(NodePackageReleaseOrder::getDeleted, 0)
			.gt(NodePackageReleaseOrder::getRemainingAmount, BigDecimal.ZERO)
			.last("limit 1")
			.one();
		if (releaseOrder == null) {
			return 0;
		}

		// runDays=0说明还没开始释放，恢复到待释放；否则恢复到释放中，让每日释放任务继续推进。
		int targetStatus = releaseOrder.getRunDays() == null || releaseOrder.getRunDays() <= 0
			? NODE_RELEASE_STATUS_PENDING
			: NODE_RELEASE_STATUS_RELEASING;

		boolean update = nodePackageReleaseOrderService.lambdaUpdate()
			.eq(NodePackageReleaseOrder::getId, releaseOrder.getId())
			.eq(NodePackageReleaseOrder::getStatus, NODE_RELEASE_STATUS_PAUSED)
			.gt(NodePackageReleaseOrder::getRemainingAmount, BigDecimal.ZERO)
			.set(NodePackageReleaseOrder::getNodeOrderId, newOrder.getId())
			.set(NodePackageReleaseOrder::getNodeOrderNo, newOrder.getOrderNo())
			.set(NodePackageReleaseOrder::getStatus, targetStatus)
			.set(NodePackageReleaseOrder::getUpdateTime, new Date())
			.update();
		return update ? 1 : 0;
	}


	/**
	 * 后台拨付节点。
	 *
	 * <p>后台直接生成已支付、已处理节点订单，并同步写入用户节点权益、套餐销量和后台拨付节点业绩。
	 * 如果该用户此前取消节点时暂停了AFI线性释放订单，则在新节点权益生效后恢复原释放订单。</p>
	 *
	 * @param req 后台拨付节点请求
	 * @return 1表示拨付成功
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
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

		restorePausedReleaseOrderIfNeeded(insertOrder);

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
				.setSql("admin_umbrella_node_performance = admin_umbrella_node_performance + "+ nodePackage.getPrice())
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
