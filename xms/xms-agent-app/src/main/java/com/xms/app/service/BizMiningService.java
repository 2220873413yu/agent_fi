package com.xms.app.service;

import com.github.pagehelper.PageInfo;
import com.xms.app.entity.TotalEarningsDto;
import com.xms.app.entity.bo.DestroyCallbackBo;
import com.xms.app.entity.bo.DestroyInfoBo;
import com.xms.app.entity.dto.*;
import com.xms.app.entity.req.NodePackageReq;
import com.xms.app.entity.req.SwapOrderCallbackReq;
import com.xms.dao.entity.dto.DestroyOrderDto;
import com.xms.dao.entity.dto.InterestPackDto;
import com.xms.dao.entity.dto.InterestStatDayDto;
import com.xms.app.entity.dto.mining.PackageOrderDto;
import com.xms.app.entity.req.RedeemVo;
import com.xms.app.entity.req.ReleaseOrderReq;
import com.xms.app.entity.resp.CreateOrderResp;
import com.xms.app.entity.vo.*;
import com.xms.common.core.domain.api.ResultPista;
import com.xms.dao.domain.MiningPackage;
import jakarta.validation.Valid;

import java.util.List;

public interface BizMiningService {

	/**
	 * 获取矿机信息
	 * @return
	 */
    MiningPackageDto miningInfo();

//	/**
//	 * 创建矿机订单
//	 */
//	ResultPista<CreateOrderResp> miningOrder(CreateMiningOrderVo req,Long userId);

	/**
	 * 质押矿机
	 */
	ResultPista<CreateOrderResp> stakeMiningOrder(StakeMiningOrderVo req, Long userId);

	/**
	 * 我的矿机信息 矿机总量、已经质押的矿机、今日产出dfc
	 * @return
	 */
	MyMiningInfoDto myMiningInfo();

	/**
	 * 我的矿机列表
	 * @param lastId id
	 * @param bizType 0:未质押,1:已质押,2:自提订单
	 * @return
	 */
	List<MyMiningListDto> myMiningList(Long lastId, Integer bizType);

	/**
	 * 获取矿机质押需要多少dfc和产出根据矿机id获取
	 * @param id
	 * @return
	 */
	ResultPista<MiningPackageTierDto> getPackageTierById(Long id);

}
