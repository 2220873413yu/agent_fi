package com.xms.common.config.mybatis.handler;

import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.injector.DefaultSqlInjector;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.xms.common.config.mybatis.InsertIgnore;
import com.xms.common.config.mybatis.InsertIgnoreBatch;
import com.xms.common.config.mybatis.ReplaceSql;

import java.util.List;

/**
 * 自定义sql注入器，增加通用方法
 *
 * @author MIER
 */
public class CustomerSqlInjector extends DefaultSqlInjector {
	/**
	 * mapper 对应的方法名
	 */
	private static final String MAPPER_METHOD = "insertIgnoreBatch";
	private static final String INSERT_IGNORE = "insertIgnore";
	private static final String REPLACE = "replace";
	@Override
	public List<AbstractMethod> getMethodList(Class<?> mapperClass, TableInfo tableInfo) {
		List<AbstractMethod> methodList = super.getMethodList(mapperClass,tableInfo);
		// 插入数据，如果中已经存在相同的记录，则忽略当前新数据
		methodList.add(new InsertIgnore(INSERT_IGNORE));
		// 批量插入数据，如果中已经存在相同的记录，则忽略当前新数据
		methodList.add(new InsertIgnoreBatch(MAPPER_METHOD));
		// 替换数据，如果中已经存在相同的记录，则覆盖旧数据
		methodList.add(new ReplaceSql(REPLACE));
		return methodList;
	}

}
