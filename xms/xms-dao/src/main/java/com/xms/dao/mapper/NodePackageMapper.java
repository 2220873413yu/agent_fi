package com.xms.dao.mapper;

import java.util.List;
import com.xms.dao.mapper.XmsMapper;

import com.xms.dao.domain.NodePackage;

/**
 * 节点套餐Mapper接口
 *
 * @author xms
 * @date 2026-04-28
 */
public interface NodePackageMapper extends XmsMapper<NodePackage>
{
    /**
     * 查询节点套餐列表
     *
     * @param nodePackage 节点套餐
     * @return 节点套餐集合
     */
    public List<NodePackage> selectNodePackageList(NodePackage nodePackage);

}
