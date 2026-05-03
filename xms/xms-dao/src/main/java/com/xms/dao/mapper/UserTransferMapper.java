package com.xms.dao.mapper;

import java.util.List;
import com.xms.dao.mapper.XmsMapper;

import com.xms.dao.domain.UserTransfer;

/**
 * 用户转账记录Mapper接口
 *
 * @author xms
 * @date 2025-03-12
 */
public interface UserTransferMapper extends XmsMapper<UserTransfer>
{
    /**
     * 查询用户转账记录列表
     *
     * @param userTransfer 用户转账记录
     * @return 用户转账记录集合
     */
    public List<UserTransfer> selectUserTransferList(UserTransfer userTransfer);

}
