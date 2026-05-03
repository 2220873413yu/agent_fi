package com.xms.dao.service;

import java.util.List;
import com.xms.dao.service.XmsDataService;
import com.xms.dao.domain.NodePackage;

/**
 * 节点套餐Service接口
 *
 * @author xms
 * @date 2026-04-28
 */
public interface INodePackageService extends XmsDataService<NodePackage>
{

    /**
     * 查询节点套餐列表
     *
     * @param nodePackage 节点套餐
     * @return 节点套餐集合
     */
    public List<NodePackage> selectNodePackageList(NodePackage nodePackage);

	/**
	 * 修改节点套餐
	 * @param nodePackage
	 * @return
	 */
	int updateNodePackageById(NodePackage nodePackage);
}
