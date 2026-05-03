package com.xms.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xms.common.core.domain.entity.SysDictData;
import com.xms.dao.entity.domain.SysPara;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;


/**
 * <p>
 * 系统参数表 Mapper 接口
 * </p>
 *
 *
 * @since 2023-03-15
 */
public interface SysParaMapper extends BaseMapper<SysPara> {

	String getValue(@Param("code") String code);

	//****************管理后台***************

	/**
	 * 查询系统参数列表
	 *
	 * @param sysPara 系统参数
	 * @return 系统参数集合
	 */
	public List<SysPara> selectSysParaList(SysPara sysPara);

	List<SysPara> listParams(String[] stringList);


	@Select("select * from sys_dict_data  where status = '0' and dict_type = #{dictType} order by dict_sort asc")
	List<SysDictData> listDicValues(String code);

	@Select("SELECT COALESCE(SUM(amount), 0) FROM t_compound_interest_dividend_log WHERE type = 1 AND biz_type = 1")
    BigDecimal totalCompoundFee();
}
