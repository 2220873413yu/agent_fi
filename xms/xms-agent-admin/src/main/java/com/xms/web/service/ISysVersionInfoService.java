package com.xms.web.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xms.dao.entity.domain.SysVersionInfo;

import java.util.List;

/**
 * 版本Service接口
 *
 *
 * @date 2023-07-31
 */
public interface ISysVersionInfoService extends IService<SysVersionInfo>
{

	/**
	 * 查询版本列表
	 *
	 * @param sysVersionInfo 版本
	 * @return 版本集合
	 */
	public List<SysVersionInfo> selectSysVersionInfoList(SysVersionInfo sysVersionInfo);

}
