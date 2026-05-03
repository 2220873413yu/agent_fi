package com.xms.dao.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xms.dao.domain.MqTransactionLog;

import java.util.List;

/**
 * mq可靠投递日志Service接口
 *
 * @author xms
 * @date 2024-07-09
 */
public interface IMqTransactionLogService extends IService<MqTransactionLog>
{

    /**
     * 查询mq可靠投递日志列表
     *
     * @param mqTransactionLog mq可靠投递日志
     * @return mq可靠投递日志集合
     */
    public List<MqTransactionLog> selectMqTransactionLogList(MqTransactionLog mqTransactionLog);

}
