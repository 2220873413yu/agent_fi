package com.xms.dao.service;

import java.util.List;
import com.xms.dao.service.XmsDataService;
import com.xms.dao.domain.UserAddress;

/**
 * 收货地址Service接口
 *
 * @author xms
 * @date 2026-02-26
 */
public interface IUserAddressService extends XmsDataService<UserAddress>
{

    /**
     * 查询收货地址列表
     *
     * @param userAddress 收货地址
     * @return 收货地址集合
     */
    public List<UserAddress> selectUserAddressList(UserAddress userAddress);

	/**
	 * 删除收货地址
	 *
	 * @param id 收货地址ID
	 * @return 结果
	 */
	int deleteAddressById(Long id, Long frontUserId);
}
