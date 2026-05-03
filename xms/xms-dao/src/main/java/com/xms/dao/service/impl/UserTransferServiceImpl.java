package com.xms.dao.service.impl;

import java.util.List;
import com.xms.dao.service.impl.XmsDataServiceImpl;
import org.springframework.stereotype.Service;
import com.xms.dao.mapper.UserTransferMapper;
import com.xms.dao.domain.UserTransfer;
import com.xms.dao.service.IUserTransferService;

/**
 * 用户转账记录Service业务层处理
 *
 * @author xms
 * @date 2025-03-12
 */
@Service
public class UserTransferServiceImpl extends XmsDataServiceImpl<UserTransferMapper, UserTransfer> implements IUserTransferService
{


    /**
     * 查询用户转账记录列表
     *
     *
     * @param userTransfer 用户转账记录
     * @return 用户转账记录
     */
    @Override
    public List<UserTransfer> selectUserTransferList(UserTransfer userTransfer)
    {
        return baseMapper.selectUserTransferList(userTransfer);
    }

}
