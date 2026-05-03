package com.xms.app.service;

import com.xms.app.entity.req.UserAddressReqVo;
import com.xms.common.core.domain.api.ResultPista;
import com.xms.dao.domain.UserAddress;

import java.util.List;

/**
 * @author: renengadePISTA
 * @createDate: 2023/9/12
 */
public interface AppUserBizService {
	ResultPista userAddressAdd(UserAddressReqVo req, Long frontUserId);

	ResultPista userAddressUpdate(UserAddressReqVo req, Long frontUserId);

	ResultPista userAddressRemove(Long id, Long frontUserId);

	ResultPista<List<UserAddress>> userAddressList();


	/**
	 * 查询用户默认收货地址
	 *
	 * @return
	 */
	ResultPista<UserAddress> defaultUserAddress();

	/**
	 * 查询收货地址根据id查询
	 *
	 * @param id
	 * @return
	 */
	ResultPista<UserAddress> getUserAddressById(Long id);

	/**
	 * 根据地址id修改为默认收货地址
	 *
	 * @param id
	 * @return
	 */
	ResultPista updateDefaultAddressById(Long id);


}
