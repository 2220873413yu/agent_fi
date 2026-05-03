package com.xms.web.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xms.dao.entity.domain.SysVersionInfo;
import com.xms.dao.mapper.SysVersionInfoMapper;
import com.xms.web.service.ISysVersionInfoService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 版本Service业务层处理
 *
 *
 * @date 2023-07-31
 */
@Service
public class SysVersionInfoServiceImpl extends ServiceImpl<SysVersionInfoMapper, SysVersionInfo> implements ISysVersionInfoService
{
	/**
	 * 查询版本列表
	 *
	 *
	 * @param sysVersionInfo 版本
	 * @return 版本
	 */
	@Override
	public List<SysVersionInfo> selectSysVersionInfoList(SysVersionInfo sysVersionInfo)
	{
		return baseMapper.selectSysVersionInfoList(sysVersionInfo);
	}

}
