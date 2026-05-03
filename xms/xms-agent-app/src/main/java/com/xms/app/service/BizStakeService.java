package com.xms.app.service;

import com.xms.app.entity.bo.DestroyCallbackBo;
import com.xms.app.entity.bo.StakeOrderBo;
import com.xms.app.entity.dto.*;
import com.xms.app.entity.resp.CreateStakeOrderResp;
import com.xms.app.entity.vo.CreateStakeOrderVo;
import com.xms.common.core.domain.api.ResultPista;
import jakarta.validation.Valid;

import java.util.List;

public interface BizStakeService {

	/**
	 * 获取质押信息 没有上架的话可能为空
	 * @return
	 */
	StakeInfoDTO stakeInfo();

	/**
	 * 质押
	 * @return
	 */
	ResultPista<CreateStakeOrderResp> createStakeOrder(CreateStakeOrderVo req, Long userId);


	/**
	 * 质押订单列表
	 * @param lastId
	 * @param status 1:产出中,2:已出局
	 * @return
	 */
	List<MyStakeInfoListDto> destroyOrderList(Long lastId,Integer status);

	/**
	 * 锁仓订单列表
	 * @param lastId
	 * @return
	 */
	List<MyReleaseBucketListDto> myReleaseBucketList(Long lastId);

	/**
	 * 质押订单回调
	 * @param req
	 * @return
	 */
	ResultPista<String> stakeOrderCallback(StakeOrderBo req);

	/**
	 * 获取商品列表
	 * @return
	 */
	List<DiyProductListDto> diyProductList();

	/**
	 * 获取商品详情
	 * @param productId
	 * @return
	 */
	DiyProductDetailDto diyProductDetail(Long productId);

	/**
	 * 发货订单列表
	 * @param bizType(不传递查询所有的) 0:待发货,1:已发货
	 * @return
	 * @throws Exception
	 */
	List<MyProductOrderDto> myProductOrderList(Integer bizType);
}
