//package com.xms.app.controller;
//
//import com.xms.app.entity.req.UserAddressRemoveReq;
//import com.xms.app.entity.req.UserAddressReqVo;
//import com.xms.app.service.AppUserBizService;
//import com.xms.common.annotation.RepeatSubmit;
//import com.xms.common.core.domain.api.ResultPista;
//import com.xms.common.utils.SecurityUtils;
//import com.xms.dao.domain.UserAddress;
//import io.swagger.annotations.Api;
//import io.swagger.annotations.ApiOperation;
//import lombok.AllArgsConstructor;
//import org.springframework.validation.annotation.Validated;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
///**
// * 用户地址信息
// *
// * @author MIER
// * @since 2023-07-25
// */
//@Api(tags = "用户地址信息")
//@RestController
//@RequestMapping("/api/app")
//@AllArgsConstructor
//public class AppUserAddressController {
//	private final AppUserBizService appUserBizServiceImpl;
//
//	/**
//	 * 用户收货地址新增
//	 *
//	 * @return
//	 */
//	@ApiOperation(value = "用户收货地址新增")
//	@PostMapping("/userAddressAdd")
//	@RepeatSubmit
//	public ResultPista userAddressAdd(@RequestBody @Validated UserAddressReqVo req) throws Exception {
//		return appUserBizServiceImpl.userAddressAdd(req, SecurityUtils.getFrontUserId());
//	}
//
//	/**
//	 * 用户收货地址修改
//	 *
//	 * @return
//	 */
//	@ApiOperation(value = "用户收货地址修改")
//	@PostMapping("/userAddressUpdate")
//	@RepeatSubmit
//	public ResultPista userAddressUpdate(@RequestBody @Validated UserAddressReqVo req) throws Exception {
//		return appUserBizServiceImpl.userAddressUpdate(req, SecurityUtils.getFrontUserId());
//	}
//
//	/**
//	 * 用户收货地址删除
//	 *
//	 * @return
//	 */
//	@ApiOperation(value = "用户收货地址删除")
//	@PostMapping("/userAddressRemove")
//	public ResultPista userAddressRemove(@RequestBody UserAddressRemoveReq req) throws Exception {
//		return appUserBizServiceImpl.userAddressRemove(req.getId(), SecurityUtils.getFrontUserId());
//	}
//
//	/**
//	 * 用户收货地址列表
//	 *
//	 * @return
//	 */
//	@ApiOperation(value = "用户收货地址列表")
//	@GetMapping("/userAddressList")
//	public ResultPista<List<UserAddress>> userAddressList() {
//		return appUserBizServiceImpl.userAddressList();
//	}
//
//	/**
//	 * 查询用户默认收货地址
//	 *
//	 * @return
//	 */
//	@ApiOperation(value = "查询用户默认收货地址")
//	@GetMapping("/defaultUserAddress")
//	public ResultPista<UserAddress> defaultUserAddress() {
//		return appUserBizServiceImpl.defaultUserAddress();
//	}
//
//	/**
//	 * 查询收货地址根据id查询
//	 *
//	 * @return
//	 */
//	@ApiOperation(value = "查询收货地址根据id查询")
//	@GetMapping("/getUserAddressById")
//	public ResultPista<UserAddress> getUserAddressById(Long id) {
//		return appUserBizServiceImpl.getUserAddressById(id);
//	}
//
//	/**
//	 * 根据地址id修改为默认收货地址
//	 *
//	 * @return
//	 */
//	@ApiOperation(value = "根据地址id修改为默认收货地址")
//	@GetMapping("/updateDefaultAddressById")
//	public ResultPista updateDefaultAddressById(Long id) {
//		return appUserBizServiceImpl.updateDefaultAddressById(id);
//	}
//}
//
