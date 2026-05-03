package com.xms.dao.mapper;

import com.xms.dao.domain.MqTransactionLog;

import java.util.List;

/**
 * mq可靠投递日志Mapper接口
 *
 * @author xms
 * @date 2024-07-09
 */
public interface MqTransactionLogMapper extends XmsMapper<MqTransactionLog>
{
    /**
     * 查询mq可靠投递日志列表
     *
     * @param mqTransactionLog mq可靠投递日志
     * @return mq可靠投递日志集合
     */
    public List<MqTransactionLog> selectMqTransactionLogList(MqTransactionLog mqTransactionLog);

}
