package com.xms.app.service;

import com.xms.app.entity.bo.DestroyCallbackBo;
import com.xms.app.entity.bo.DfTransferBo;
import com.xms.app.entity.dto.RechargeRecordDto;
import com.xms.app.entity.req.CreateRechargeOrder;
import com.xms.app.entity.req.JuNotifyReq;
import com.xms.app.entity.resp.CreateOrderResp;
import com.xms.common.core.domain.api.ResultPista;

import java.util.List;

public interface BizRechargeService {

	/**
	 * 充值记录
	 * @param lastId
	 * @return
	 */
	ResultPista<List<RechargeRecordDto>> listRechargeRecord(Integer coinType, Long lastId);


	/**
	 * 充值回调
	 * @param req
	 * @return
	 */
	ResultPista<String> rechargeCallback(DestroyCallbackBo req);

    void addRechargeLog(JuNotifyReq req);

	/**
	 * df资产划转
	 * 从旧系统的df资产划转到本系统锁定usdt资产
	 * @param req df划转请求参数
	 * @return
	 */
	ResultPista<String> dfTransfer(DfTransferBo req);
}
