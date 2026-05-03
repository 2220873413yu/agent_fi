package com.xms.dao.mapper;

import java.util.List;
import com.xms.dao.mapper.XmsMapper;

import com.xms.dao.domain.UserAddress;
import org.apache.ibatis.annotations.Param;

/**
 * 收货地址Mapper接口
 *
 * @author xms
 * @date 2026-02-26
 */
public interface UserAddressMapper extends XmsMapper<UserAddress>
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
	 * @param id
	 * @param userId
	 * @return
	 */
	int deleteAddressById(@Param("id") Long id, @Param("userId") Long userId);
}
