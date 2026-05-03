package com.xms.app.service.impl;

import cn.hutool.system.SystemUtil;
import com.xms.app.entity.dto.NodeInfoDTO;
import com.xms.app.entity.dto.NodePackageOrderDto;
import com.xms.app.entity.resp.CreateOrderResp;
import com.xms.app.entity.vo.CreateNodeOrderVo;
import com.xms.app.service.BizNodeService;
import com.xms.common.config.redis.XmsRedis;
import com.xms.common.config.redis.lock.RedisLock;
import com.xms.common.constant.ConstantStatic;
import com.xms.common.constant.ConstantType;
import com.xms.common.constant.RedisConstant;
import com.xms.common.constant.SysConstant;
import com.xms.common.core.domain.api.ResultPista;
import com.xms.common.exception.ServiceException;
import com.xms.common.mq.dynamic.AsyncDynamicOrderSettlementService;
import com.xms.common.mq.dynamic.OrderMsgDO;
import com.xms.common.result.ResponseCode;
import com.xms.common.utils.SecurityUtils;
import com.xms.common.utils.SignUtil;
import com.xms.common.utils.uuid.IDUtils;
import com.xms.app.entity.bo.StakeOrderBo;
import com.xms.dao.domain.NodePackage;
import com.xms.dao.domain.NodePackageOrder;
import com.xms.dao.entity.domain.UserInfo;
import com.xms.dao.service.INodePackageOrderService;
import com.xms.dao.service.INodePackageService;
import com.xms.dao.service.UserInfoService;
import com.xms.dao.service.UserWalletService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.xms.app.service.impl.BizUserServiceImpl.checkWallet;

/**
 * 节点服务实现类
 *
 * @author xms
 * @date 2023/6/12
 */
@Service
@Slf4j
public class BizNodeServiceImpl implements BizNodeService {
	private static final long NODE_ORDER_EXPIRE_MILLIS = 5 * 60 * 1000L;

	@Autowired
	private INodePackageService nodePackageService;

	@Autowired
	private INodePackageOrderService nodePackageOrderService;

	@Autowired
	private UserInfoService userInfoService;

	@Autowired
	private XmsRedis xmsRedis;

	@Autowired
	private UserWalletService userWalletServiceImpl;

	@Autowired
	private AsyncDynamicOrderSettlementService asyncDynamicOrderSettlementServiceImpl;


	@Value("${lq.md5Key}")
	private String md5Key;

	@Override
	@Transactional(rollbackFor = Exception.class)
	@RedisLock(value = RedisConstant.LockConstant.XMS_NODE_APPLY, param = "#userId")
	public ResultPista<CreateOrderResp> createOrder(CreateNodeOrderVo req, Long userId) {
		UserInfo userInfo = userInfoService.lambdaQuery()
			.eq(UserInfo::getUserId, userId)
			.one();
		if (userInfo == null) {
			throw new ServiceException(ResponseCode.CODE_1007);
		}
		if (userInfo.getNodeLevel() != null && userInfo.getNodeLevel() > 0) {
			throw new ServiceException(ResponseCode.CODE_1268);
		}

		checkWallet(req.getRandomNum(), req.getSignature(), userInfo.getAccount(), xmsRedis);

		NodePackage nodePackage = nodePackageService.lambdaQuery()
			.eq(NodePackage::getLevel, req.getLevel())
			.eq(NodePackage::getStatus, 1)
			.last("limit 1")
			.one();
		if (nodePackage == null) {
			throw new ServiceException(ResponseCode.CODE_1269);
		}
		if (req.getUserAmount().compareTo(nodePackage.getPrice()) < 0) {
			throw new ServiceException(ResponseCode.CODE_1015);
		}

		NodePackageOrder unpaidOrder =nodePackageOrderService.lambdaQuery()
			.eq(NodePackageOrder::getUserId, userId)
			.eq(NodePackageOrder::getStatus, 0)
			.eq(NodePackageOrder::getSourceType, 0)
			.orderByDesc(NodePackageOrder::getId)
			.last("limit 1")
			.one();
		//NodePackageOrder unpaidOrder = findSameUnpaidOrder(userId, nodePackage);
		if (unpaidOrder != null) {
			//找最近的订单是否是这个等级
			if (!unpaidOrder.getPackageLevel().equals(req.getLevel())) {
				//新的订单。
				// 等级不一致时，只判断该未支付订单是否已过期（5分钟）。
				Date createTime = unpaidOrder.getCreateTime();
				boolean expired = (System.currentTimeMillis() - createTime.getTime()) >= NODE_ORDER_EXPIRE_MILLIS;
				if (expired) {
					//过期了
					NodePackageOrder insertOrder = new NodePackageOrder();
					insertOrder.setOrderNo(IDUtils.getSnowflakeStr());
					insertOrder.setUserId(userId);
					insertOrder.setAddress(userInfo.getAccount());
					fillOrderSnapshot(insertOrder, nodePackage);
					insertOrder.setSourceType(0);
					insertOrder.setStatus(0);
					insertOrder.setBizStatus(0);
					insertOrder.setCreateTime(new Date());
					boolean save = nodePackageOrderService.save(insertOrder);
					if (!save) {
						throw new ServiceException(ResponseCode.CODE_1002);
					}

					CreateOrderResp resp = new CreateOrderResp();
					resp.setOrderNo(insertOrder.getOrderNo());
					resp.setUsdtValue(insertOrder.getOrderValueUsdt());
					return ResultPista.data(resp);
					/*
					//判断现在的直推、间推是否一致 不一致的话去修改
					refreshUnpaidOrderSnapshot(unpaidOrder, nodePackage);
					CreateOrderResp resp = new CreateOrderResp();
					resp.setOrderNo(unpaidOrder.getOrderNo());
					resp.setUsdtValue(unpaidOrder.getOrderValueUsdt());
					return ResultPista.data(resp);*/
				}else{
					//没过期 先支付之前的
					throw new ServiceException(ResponseCode.CODE_1270);
				}
			}else{
				//还是支付之前的订单
				refreshUnpaidOrderSnapshot(unpaidOrder, nodePackage);
				CreateOrderResp resp = new CreateOrderResp();
				resp.setOrderNo(unpaidOrder.getOrderNo());
				resp.setUsdtValue(unpaidOrder.getOrderValueUsdt());
				return ResultPista.data(resp);
			}
		}

		NodePackageOrder insertOrder = new NodePackageOrder();
		insertOrder.setOrderNo(IDUtils.getSnowflakeStr());
		insertOrder.setUserId(userId);
		insertOrder.setAddress(userInfo.getAccount());
		fillOrderSnapshot(insertOrder, nodePackage);
		insertOrder.setSourceType(0);
		insertOrder.setStatus(0);
		insertOrder.setBizStatus(0);
		insertOrder.setCreateTime(new Date());
		boolean save = nodePackageOrderService.save(insertOrder);
		if (!save) {
			throw new ServiceException(ResponseCode.CODE_1002);
		}

		CreateOrderResp resp = new CreateOrderResp();
		resp.setOrderNo(insertOrder.getOrderNo());
		resp.setUsdtValue(insertOrder.getOrderValueUsdt());
		return ResultPista.data(resp);
	}

	private NodePackageOrder findSameUnpaidOrder(Long userId, NodePackage nodePackage) {
		return nodePackageOrderService.lambdaQuery()
			.eq(NodePackageOrder::getUserId, userId)
			.eq(NodePackageOrder::getStatus, 0)
			.eq(NodePackageOrder::getSourceType, 0)
			.eq(NodePackageOrder::getPackageLevel, nodePackage.getLevel())
			.orderByDesc(NodePackageOrder::getId)
			.last("limit 1")
			.one();
	}

	private void refreshUnpaidOrderSnapshot(NodePackageOrder order, NodePackage nodePackage) {
		if (samePackageSnapshot(order, nodePackage)) {
			return;
		}
		fillOrderSnapshot(order, nodePackage);
		order.setUpdateTime(new Date());
		boolean update = nodePackageOrderService.updateById(order);
		if (!update) {
			throw new ServiceException(ResponseCode.CODE_1002);
		}
	}

	private void fillOrderSnapshot(NodePackageOrder order, NodePackage nodePackage) {
		order.setPackageLevel(nodePackage.getLevel());
		order.setDirectReferralRate(nodePackage.getDirectReferralRate());
		order.setIndirectReferralRate(nodePackage.getIndirectReferralRate());
		order.setWeightMultiplier(nodePackage.getWeightMultiplier());
		order.setPredOrderFeeReliefRate(nodePackage.getPredOrderFeeReliefRate());
		order.setOrderValueUsdt(nodePackage.getPrice());
	}

	private boolean samePackageSnapshot(NodePackageOrder order, NodePackage nodePackage) {
		return sameDecimal(order.getOrderValueUsdt(), nodePackage.getPrice())
			&& sameDecimal(order.getDirectReferralRate(), nodePackage.getDirectReferralRate())
			&& sameDecimal(order.getIndirectReferralRate(), nodePackage.getIndirectReferralRate())
			&& sameDecimal(order.getWeightMultiplier(), nodePackage.getWeightMultiplier())
			&& sameDecimal(order.getPredOrderFeeReliefRate(), nodePackage.getPredOrderFeeReliefRate());
	}

	private boolean sameDecimal(BigDecimal a, BigDecimal b) {
		if (a == null || b == null) {
			return a == b;
		}
		return a.compareTo(b) == 0;
	}

	private CreateOrderResp buildCreateOrderResp(NodePackageOrder order) {
		CreateOrderResp resp = new CreateOrderResp();
		resp.setOrderNo(order.getOrderNo());
		resp.setUsdtValue(order.getOrderValueUsdt());
		return resp;
	}

	@Override
	public List<NodeInfoDTO> nodeInfo() {
		List<NodeInfoDTO> nodeInfoDTOS = nodePackageService.lambdaQuery()
			.eq(NodePackage::getStatus, 1)
			.list().stream().map(record -> {
				NodeInfoDTO nodeInfoDTO = new NodeInfoDTO();
				nodeInfoDTO.setPrice(record.getPrice());
				nodeInfoDTO.setLevel(record.getLevel());
				nodeInfoDTO.setWeightMultiplier(record.getWeightMultiplier());
				nodeInfoDTO.setDirectReferralRate(record.getDirectReferralRate());
				nodeInfoDTO.setIndirectReferralRate(record.getIndirectReferralRate());
				nodeInfoDTO.setPredOrderFeeReliefRate(record.getPredOrderFeeReliefRate());
				//nodeInfoDTO.setShareRatio(record.getShareRatio());
				return nodeInfoDTO;
			}).collect(Collectors.toList());
		return nodeInfoDTOS;
	}

	@Override
	public List<NodePackageOrderDto> list() {
		List<NodePackageOrderDto> result = nodePackageOrderService.lambdaQuery()
			.eq(NodePackageOrder::getUserId, SecurityUtils.getLoginAppUser().getUserId())
			.eq(NodePackageOrder::getStatus, 1)
			.list().stream()
			.map(record -> {
				NodePackageOrderDto nodePackageOrderDto = new NodePackageOrderDto();
				BeanUtils.copyProperties(record, nodePackageOrderDto);
				return nodePackageOrderDto;
			}).collect(Collectors.toList());
		return result;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public ResultPista<String> nodeOrderCallback(StakeOrderBo req) {
		log.info("节点订单回调 req:{}", req);

		// 将 RechargeCallbackBo 对象转换为 Map
		Map<String, Object> map = cn.hutool.core.bean.BeanUtil.beanToMap(req);
		String sign = SignUtil.getSign(map, false, false, md5Key);
		String osName = SystemUtil.getOsInfo().getName();
		if (!osName.toUpperCase().contains(SysConstant.OS_NAME_WINDOWS)) {
			if (!sign.equals(req.getSign())) {
				log.error("验签失败");
				return ResultPista.fail("验签失败");
			}
		}

		NodePackageOrder packageOrder = nodePackageOrderService.lambdaQuery()
			.eq(NodePackageOrder::getOrderNo, req.getOrderNo())
			.one();
		if (packageOrder == null) {
			log.error("订单不存在");
			//return ResultPista.fail("订单不存在");
			return ResultPista.data("success");
		}

		if(packageOrder.getStatus()==1){
			return ResultPista.data("success");
		}
		if(req.getAmount().compareTo(packageOrder.getOrderValueUsdt())<0){
			throw new ServiceException("支付金额小于订单金额");
		}
		UserInfo userInfo = userInfoService.lambdaQuery()
			.eq(UserInfo::getUserId, packageOrder.getUserId())
			.one();
		if(userInfo.getNodeLevel()>0){
			log.info("该用户已拥有节点 userId:{},orderNo:{}",userInfo.getUserId(),packageOrder.getOrderNo());
			//throw new ServiceException("用户已拥有节点");
			return ResultPista.data("success");
		}
		//更新订单状态
		boolean update = nodePackageOrderService.lambdaUpdate()
			.eq(NodePackageOrder::getId, packageOrder.getId())
			.eq(NodePackageOrder::getStatus, 0)
			.set(NodePackageOrder::getStatus, 1)
			.set(NodePackageOrder::getHash, req.getHash())
			.set(NodePackageOrder::getPayTime, new Date())
			.update();
		if (!update) {
			throw new ServiceException("更新订单状态失败");
		}
		//更新用户节点信息
		update = userInfoService.lambdaUpdate()
			.eq(UserInfo::getUserId, userInfo.getUserId())
			.eq(UserInfo::getNodeLevel, 0)
			.set(UserInfo::getNodeLevel, packageOrder.getPackageLevel())
			.set(UserInfo::getMinGameLevel, packageOrder.getPackageLevel())
			.update();
		if (!update) {
			throw new ServiceException("更新用户节点信息失败");
		}

		nodePackageService.lambdaUpdate()
			.eq(NodePackage::getId, packageOrder.getPackageLevel())
			.setSql("sales = sales +1")
			.update();

		//节点业务。后续业务逻辑分配
		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
			@Override
			public void afterCommit() {
				List<OrderMsgDO> orderMsgDOList = new ArrayList<>();
				OrderMsgDO orderMsgDO = new OrderMsgDO();
				orderMsgDO.setId(packageOrder.getId());
				orderMsgDO.setBizType(1);
				orderMsgDOList.add(orderMsgDO);
				asyncDynamicOrderSettlementServiceImpl.sendMessage(orderMsgDOList);
			}
		});
		return ResultPista.data("success");
	}
}
