package com.xms.app.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xms.app.service.SysVersionInfoService;
import com.xms.dao.entity.bo.SysVersionInfoBo;
import com.xms.dao.entity.domain.SysVersionInfo;
import com.xms.dao.mapper.SysVersionInfoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 版本表 服务实现类
 * </p>
 *
 *
 * @since 2023-06-13
 */
@Service
public class SysVersionInfoServiceImpl extends ServiceImpl<SysVersionInfoMapper, SysVersionInfo> implements SysVersionInfoService {

	@Autowired
	private SysVersionInfoMapper sysVersionInfoMapper;

	/**
	 * 查询最新的版本
	 */
	@Override
	public SysVersionInfoBo getLastVersion(String deviceType) {
		return sysVersionInfoMapper.getLastVersion(deviceType);
	}

}
