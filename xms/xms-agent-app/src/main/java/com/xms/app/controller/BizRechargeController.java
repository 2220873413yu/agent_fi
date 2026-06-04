package com.xms.app.controller;


import com.github.pagehelper.PageInfo;
import com.xms.app.entity.dto.RechargeRecordDto;
import com.xms.app.service.BizRechargeService;
import com.xms.common.core.domain.api.ResultPista;
import com.xms.dao.service.XmsCommonService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户充值相关 前端控制器
 *
 *
 * @since 2023-06-12
 */
@Api(tags = "用户充值相关")
@RestController
@RequestMapping("/api/recharge")
public class BizRechargeController {

	@Autowired
	private BizRechargeService bizRechargeService;

	/**
	 * 充值记录
	 *
	 * @param coinType   币种 2:AFI,1:USDT
	 * @param lastId   当前记录最后一个ID
	 * @return
	 */
	@ApiOperation("充值记录")
	@GetMapping("/listRechargeRecord")
	public ResultPista<List<RechargeRecordDto>> listRechargeRecord(Integer coinType, Long lastId) {
		if(coinType == null){
			return ResultPista.data(new ArrayList<>());
		}
		return bizRechargeService.listRechargeRecord(coinType,lastId);
	}
}
