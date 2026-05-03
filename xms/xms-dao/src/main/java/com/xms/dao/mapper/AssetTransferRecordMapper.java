package com.xms.dao.mapper;

import java.util.List;
import com.xms.dao.mapper.XmsMapper;

import com.xms.dao.domain.AssetTransferRecord;

/**
 * DF划转记录Mapper接口
 *
 * @author xms
 * @date 2026-02-25
 */
public interface AssetTransferRecordMapper extends XmsMapper<AssetTransferRecord>
{
    /**
     * 查询DF划转记录列表
     *
     * @param assetTransferRecord DF划转记录
     * @return DF划转记录集合
     */
    public List<AssetTransferRecord> selectAssetTransferRecordList(AssetTransferRecord assetTransferRecord);

}
