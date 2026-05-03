package com.xms.dao.service.impl;

import java.util.List;
import com.xms.dao.service.impl.XmsDataServiceImpl;
import org.springframework.stereotype.Service;
import com.xms.dao.mapper.UserAddressMapper;
import com.xms.dao.domain.UserAddress;
import com.xms.dao.service.IUserAddressService;

/**
 * 收货地址Service业务层处理
 *
 * @author xms
 * @date 2026-02-26
 */
@Service
public class UserAddressServiceImpl extends XmsDataServiceImpl<UserAddressMapper, UserAddress> implements IUserAddressService
{


    /**
     * 查询收货地址列表
     *
     *
     * @param userAddress 收货地址
     * @return 收货地址
     */
    @Override
    public List<UserAddress> selectUserAddressList(UserAddress userAddress)
    {
        return baseMapper.selectUserAddressList(userAddress);
    }

	@Override
	public int deleteAddressById(Long id, Long frontUserId) {
		return baseMapper.deleteAddressById(id, frontUserId);
	}
}
