package com.xms.dao.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xms.common.core.domain.entity.SysDictData;
import com.xms.dao.entity.domain.SysPara;

import java.util.List;

/**
 * 系统参数Service接口
 *
 *
 * @date 2023-08-01
 */
public interface ISysParaService extends IService<SysPara>
{

	/**
	 * 查询系统参数列表
	 *
	 * @param sysPara 系统参数
	 * @return 系统参数集合
	 */
	public List<SysPara> selectSysParaList(SysPara sysPara);

	/**
	 * 更新系统参数
	 * @param sysPara
	 * @return
	 */
	public int updateSysPara(SysPara sysPara);

	/**
	 * 更新系统参数 根据code更新
	 * @param code
	 * @param value
	 * @return
	 */
	public void updateSysParaByCode(String code,String value);

    String getValue(String code);

	List<SysPara> listParamValues(String code);

	List<SysDictData> listDictValues(String code);
}
