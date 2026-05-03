package com.xms.dao.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xms.common.config.redis.XmsRedis;
import com.xms.common.constant.RedisConstant;
import com.xms.common.core.domain.entity.SysDictData;
import com.xms.common.utils.Func;
import com.xms.dao.entity.domain.SysPara;
import com.xms.dao.mapper.SysParaMapper;
import com.xms.dao.service.ISysParaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 系统参数Service业务层处理
 *
 *
 * @author MIER
 * @date 2023-08-01
 */
@Service
public class SysParaServiceImpl extends ServiceImpl<SysParaMapper, SysPara> implements ISysParaService
{

	@Autowired
	private XmsRedis xmsRedis;
	/**
	 * 查询系统参数列表
	 *
	 *
	 * @param sysPara 系统参数
	 * @return 系统参数
	 */
	@Override
	public List<SysPara> selectSysParaList(SysPara sysPara)
	{
		//biz_today_max_transfer_count
		List<SysPara> sysParas = baseMapper.selectSysParaList(sysPara);
		return sysParas;
	}

	/**
	 * 更新系统参数
	 *
	 * @param sysPara
	 * @return
	 */
	@Override
	public int updateSysPara(SysPara sysPara) {
		int rs = this.baseMapper.updateById(sysPara);
		xmsRedis.del(RedisConstant.XMS_PARAM + sysPara.getParaCode());
		return rs;
	}

	/**
	 * 更细系统参数 根据code更新
	 * @param code
	 * @param value
	 * @return
	 */
	@Override
	public void updateSysParaByCode(String code,String value) {
		lambdaUpdate()
			.eq(SysPara::getParaCode, code)
			.set(SysPara::getParaValue, value)
			.set(SysPara::getUpdateTime,new Date())
			.update();
		xmsRedis.del(RedisConstant.XMS_PARAM + code);
	}
	/**
	 * 查询系统参数
	 *
	 * @param code
	 * @return
	 */
	@Override
	public String getValue(String code) {
		return xmsRedis.get(RedisConstant.XMS_PARAM + code, () -> baseMapper.getValue(code), 15L, TimeUnit.DAYS);
	}
	@Override
	public List<SysPara> listParamValues(String code) {
		String[] stringList = Func.split(code, ",");
		return baseMapper.listParams(stringList);
	}

	@Override
	public List<SysDictData> listDictValues(String code) {
		return baseMapper.listDicValues(code);
	}
}
