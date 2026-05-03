package com.xms.app.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xms.dao.entity.bo.SysVersionInfoBo;
import com.xms.dao.entity.domain.SysVersionInfo;

/**
 * <p>
 * 版本表 服务类
 * </p>
 *
 *
 * @since 2023-06-13
 */
public interface SysVersionInfoService extends IService<SysVersionInfo> {


	/**
	 * @return SysVersionInfoBo
	 * @Title: getLastVersion
	 * @param:
	 * @Description: 查询最新的版本
	 */
	SysVersionInfoBo getLastVersion(String deviceType);
}
