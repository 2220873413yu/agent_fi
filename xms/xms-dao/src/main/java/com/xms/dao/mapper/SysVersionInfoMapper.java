package com.xms.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xms.dao.entity.bo.SysVersionInfoBo;
import com.xms.dao.entity.domain.SysVersionInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * 版本表 Mapper 接口
 * </p>
 *
 *
 * @since 2023-06-13
 */
public interface SysVersionInfoMapper extends BaseMapper<SysVersionInfo> {

	/**
	 *
	* @Title: getLastVersion
	* @param:
	* @Description: 查询最新的版本
	* @return SysVersionInfoBo
	 */
	SysVersionInfoBo getLastVersion(@Param("deviceType") String deviceType);

	//****************管理后台***************

	/**
	 * 查询版本列表
	 *
	 * @param sysVersionInfo 版本
	 * @return 版本集合
	 */
	public List<SysVersionInfo> selectSysVersionInfoList(SysVersionInfo sysVersionInfo);
}
