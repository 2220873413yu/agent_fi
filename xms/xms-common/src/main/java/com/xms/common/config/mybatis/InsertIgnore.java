package com.xms.common.config.mybatis;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.toolkit.sql.SqlScriptUtils;
import com.xms.common.utils.StringUtils;
import org.apache.ibatis.executor.keygen.Jdbc3KeyGenerator;
import org.apache.ibatis.executor.keygen.KeyGenerator;
import org.apache.ibatis.executor.keygen.NoKeyGenerator;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;


/**
 * @author MIER
 */
public class InsertIgnore extends AbstractMethod {
	/**
	 * @param methodName 方法名
	 * @since 3.5.0
	 */
	public InsertIgnore(String methodName) {
		super(methodName);
	}

	/**
	 * 注入自定义 MappedStatement
	 *
	 * @param mapperClass mapper 接口
	 * @param modelClass  mapper 泛型
	 * @param tableInfo   数据库表反射信息
	 * @return MappedStatement
	 */
	@Override
	public MappedStatement injectMappedStatement(Class<?> mapperClass, Class<?> modelClass, TableInfo tableInfo) {
		KeyGenerator keyGenerator = new NoKeyGenerator();
		XmsSqlMethod sqlMethod = XmsSqlMethod.INSERT_IGNORE_ONE;
		String columnScript = SqlScriptUtils.convertTrim(tableInfo.getAllInsertSqlColumnMaybeIf(null),
			LEFT_BRACKET, RIGHT_BRACKET, null, COMMA);
		String valuesScript = SqlScriptUtils.convertTrim(tableInfo.getAllInsertSqlPropertyMaybeIf(null),
			LEFT_BRACKET, RIGHT_BRACKET, null, COMMA);
		String keyProperty = null;
		String keyColumn = null;
		// 表包含主键处理逻辑,如果不包含主键当普通字段处理
		if (StringUtils.isNotBlank(tableInfo.getKeyProperty())) {
			if (tableInfo.getIdType() == IdType.AUTO) {
				/* 自增主键 */
				keyGenerator = new Jdbc3KeyGenerator();
				keyProperty = tableInfo.getKeyProperty();
				keyColumn = tableInfo.getKeyColumn();
			} else {
				if (null != tableInfo.getKeySequence()) {
					keyGenerator = TableInfoHelper.genKeyGenerator(sqlMethod.getMethod(), tableInfo, builderAssistant);
					keyProperty = tableInfo.getKeyProperty();
					keyColumn = tableInfo.getKeyColumn();
				}
			}
		}
		String sql = String.format(sqlMethod.getSql(), tableInfo.getTableName(), columnScript, valuesScript);
		SqlSource sqlSource = languageDriver.createSqlSource(configuration, sql, modelClass);
		return this.addInsertMappedStatement(mapperClass, modelClass, sqlMethod.getMethod(), sqlSource, keyGenerator, keyProperty, keyColumn);

	}
}
