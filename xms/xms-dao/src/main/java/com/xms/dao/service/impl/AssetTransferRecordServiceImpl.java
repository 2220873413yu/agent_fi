package com.xms.dao.service.impl;

import java.util.List;
import com.xms.dao.service.impl.XmsDataServiceImpl;
import org.springframework.stereotype.Service;
import com.xms.dao.mapper.AssetTransferRecordMapper;
import com.xms.dao.domain.AssetTransferRecord;
import com.xms.dao.service.IAssetTransferRecordService;

/**
 * DF划转记录Service业务层处理
 *
 * @author xms
 * @date 2026-02-25
 */
@Service
public class AssetTransferRecordServiceImpl extends XmsDataServiceImpl<AssetTransferRecordMapper, AssetTransferRecord> implements IAssetTransferRecordService
{


    /**
     * 查询DF划转记录列表
     *
     *
     * @param assetTransferRecord DF划转记录
     * @return DF划转记录
     */
    @Override
    public List<AssetTransferRecord> selectAssetTransferRecordList(AssetTransferRecord assetTransferRecord)
    {
        return baseMapper.selectAssetTransferRecordList(assetTransferRecord);
    }

}
