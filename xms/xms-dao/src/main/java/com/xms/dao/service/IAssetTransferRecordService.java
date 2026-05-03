package com.xms.dao.service;

import java.util.List;
import com.xms.dao.service.XmsDataService;
import com.xms.dao.domain.AssetTransferRecord;

/**
 * DF划转记录Service接口
 *
 * @author xms
 * @date 2026-02-25
 */
public interface IAssetTransferRecordService extends XmsDataService<AssetTransferRecord>
{

    /**
     * 查询DF划转记录列表
     *
     * @param assetTransferRecord DF划转记录
     * @return DF划转记录集合
     */
    public List<AssetTransferRecord> selectAssetTransferRecordList(AssetTransferRecord assetTransferRecord);

}
