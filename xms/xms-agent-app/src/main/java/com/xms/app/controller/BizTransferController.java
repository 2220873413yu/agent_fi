//package com.xms.app.controller;
//
//import com.github.pagehelper.PageInfo;
//import com.xms.app.entity.vo.TransferOrderVo;
//import com.xms.app.service.BizTransferService;
//import com.xms.common.annotation.RepeatSubmit;
//import com.xms.common.core.domain.api.ResultPista;
//import com.xms.common.utils.SecurityUtils;
//import io.swagger.annotations.Api;
//import jakarta.validation.Valid;
//import lombok.Data;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.ArrayList;
//
///**
// * 转账相关业务
// *
// *
// * @since 2023-06-12
// */
//@Api(tags = "转账相关业务")
//@RestController
//@RequestMapping("/api/transfer")
//public class BizTransferController {
//
//	@Autowired
//	private BizTransferService bizTransferService;
//
//	/**
//	 * 发起转账
//	 * @param req
//	 * @return
//	 * @throws Exception
//	 */
//	@PostMapping(value = "/outer/transfer")
//	@RepeatSubmit
//	public ResultPista outerTransfer(@Valid @RequestBody TransferOrderVo req) throws Exception {
//		return bizTransferService.createOrder(req, SecurityUtils.getLoginAppUser().getUserId());
//	}
//
//	/**
//	 * 转账记录
//	 * @param lastId lastId
//	 * @param type 1:转账记录,2:收款记录,3:查询转账+收款记录
//	 * @param coinType 1:USDT,2:DFC,3:OORT,4:锁定USDT,5:产出DFC
//	 * @return
//	 */
//	@GetMapping(value = "/outer/listTransferLog")
//	public ResultPista listTransferLog(Long lastId, Integer type, Integer coinType) {
//		if(coinType == null){
//			return ResultPista.data(new ArrayList<>());
//		}
//		if(!(coinType == 1 || coinType == 2 || coinType == 3 || coinType == 4 || coinType == 5)){
//			return ResultPista.data(new ArrayList<>());
//		}
//		return bizTransferService.listTransferRecord(lastId,type,coinType);
//	}
//}
