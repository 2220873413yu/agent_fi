package com.xms.dao.service;

import java.util.List;
import com.xms.dao.service.XmsDataService;
import com.xms.dao.domain.UserTransfer;

/**
 * 用户转账记录Service接口
 *
 * @author xms
 * @date 2025-03-12
 */
public interface IUserTransferService extends XmsDataService<UserTransfer>
{

    /**
     * 查询用户转账记录列表
     *
     * @param userTransfer 用户转账记录
     * @return 用户转账记录集合
     */
    public List<UserTransfer> selectUserTransferList(UserTransfer userTransfer);

}
