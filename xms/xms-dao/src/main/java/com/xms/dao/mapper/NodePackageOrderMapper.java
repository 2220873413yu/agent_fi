package com.xms.dao.mapper;

import java.util.List;
import com.xms.dao.mapper.XmsMapper;

import com.xms.dao.domain.NodePackageOrder;

/**
 * 节点购买记录Mapper接口
 *
 * @author xms
 * @date 2026-04-28
 */
public interface NodePackageOrderMapper extends XmsMapper<NodePackageOrder>
{
    /**
     * 查询节点购买记录列表
     *
     * @param nodePackageOrder 节点购买记录
     * @return 节点购买记录集合
     */
    public List<NodePackageOrder> selectNodePackageOrderList(NodePackageOrder nodePackageOrder);

}
