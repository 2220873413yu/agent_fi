package com.xms.app.service;

import com.xms.app.entity.bo.StakeOrderBo;
import com.xms.app.entity.dto.NodeInfoDTO;
import com.xms.app.entity.dto.NodePackageOrderDto;
import com.xms.app.entity.resp.CreateOrderResp;
import com.xms.app.entity.vo.CreateNodeOrderVo;
import com.xms.common.core.domain.api.ResultPista;

import java.util.List;

public interface BizNodeService {

	/**
	 * 节点信息
	 * @return
	 */
	List<NodeInfoDTO> nodeInfo();

	/**
	 * 节点质押记录
	 * @return
	 */
	List<NodePackageOrderDto> list();

	/**
	 * 创建质押订单
	 * @param req
	 * @param userId
	 * @return
	 */
	ResultPista<CreateOrderResp> createOrder(CreateNodeOrderVo req, Long userId);

	/**
	 * 节点订单回调
	 * @param bo
	 * @return
	 */
	ResultPista<String> nodeOrderCallback(StakeOrderBo bo);
}
