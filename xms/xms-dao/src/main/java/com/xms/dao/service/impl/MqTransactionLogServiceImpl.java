package com.xms.dao.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xms.dao.domain.MqTransactionLog;
import com.xms.dao.mapper.MqTransactionLogMapper;
import com.xms.dao.service.IMqTransactionLogService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * mq可靠投递日志Service业务层处理
 *
 * @author xms
 * @date 2024-07-09
 */
@Service
public class MqTransactionLogServiceImpl extends ServiceImpl<MqTransactionLogMapper, MqTransactionLog> implements IMqTransactionLogService
{


    /**
     * 查询mq可靠投递日志列表
     *
     *
     * @param mqTransactionLog mq可靠投递日志
     * @return mq可靠投递日志
     */
    @Override
    public List<MqTransactionLog> selectMqTransactionLogList(MqTransactionLog mqTransactionLog)
    {
        return baseMapper.selectMqTransactionLogList(mqTransactionLog);
    }

}
