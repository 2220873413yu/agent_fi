package com.xms.common.config.mybatis;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.core.enums.SqlMethod;
import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.toolkit.sql.SqlScriptUtils;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.ibatis.executor.keygen.Jdbc3KeyGenerator;
import org.apache.ibatis.executor.keygen.KeyGenerator;
import org.apache.ibatis.executor.keygen.NoKeyGenerator;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;

import java.util.List;
import java.util.function.Predicate;

/**
 * 批量新增数据,自选字段 insert ignore
 *
 * @author MIER
 */
public class InsertIgnoreBatch extends AbstractMethod {

	private static final String MAPPER_METHOD = "insertIgnoreBatch";

	/**
	 * 字段筛选条件
	 */
	@Setter
	@Accessors(chain = true)
	private Predicate<TableFieldInfo> predicate;

	/**
	 * @param methodName 方法名
	 * @since 3.5.0
	 */
	public InsertIgnoreBatch(String methodName) {
		super(methodName);
	}

	@SuppressWarnings("Duplicates")
	@Override
	public MappedStatement injectMappedStatement(Class<?> mapperClass, Class<?> modelClass, TableInfo tableInfo) {
		KeyGenerator keyGenerator = new NoKeyGenerator();
		XmsSqlMethod sqlMethod = XmsSqlMethod.INSERT_IGNORE_ONE;
		List<TableFieldInfo> fieldList = tableInfo.getFieldList();
		String insertSqlColumn = tableInfo.getKeyInsertSqlColumn(false, null, false) +
			this.filterTableFieldInfo(fieldList, predicate, TableFieldInfo::getInsertSqlColumn, EMPTY);
		String columnScript = LEFT_BRACKET + insertSqlColumn.substring(0, insertSqlColumn.length() - 1) + RIGHT_BRACKET;
		String insertSqlProperty = tableInfo.getKeyInsertSqlProperty(false, ENTITY_DOT, false) +
			this.filterTableFieldInfo(fieldList, predicate, i -> i.getInsertSqlProperty(ENTITY_DOT), EMPTY);
		insertSqlProperty = LEFT_BRACKET + insertSqlProperty.substring(0, insertSqlProperty.length() - 1) + RIGHT_BRACKET;
		String valuesScript = SqlScriptUtils.convertForeach(insertSqlProperty, "list", null, ENTITY, COMMA);
		String keyProperty = null;
		String keyColumn = null;
		// 表包含主键处理逻辑,如果不包含主键当普通字段处理
		if (tableInfo.havePK()) {
			if (tableInfo.getIdType() == IdType.AUTO) {
				/* 自增主键 */
				keyGenerator = new Jdbc3KeyGenerator();
				keyProperty = tableInfo.getKeyProperty();
				keyColumn = tableInfo.getKeyColumn();
			} else {
				if (null != tableInfo.getKeySequence()) {
					keyGenerator = TableInfoHelper.genKeyGenerator(MAPPER_METHOD, tableInfo, builderAssistant);
					keyProperty = tableInfo.getKeyProperty();
					keyColumn = tableInfo.getKeyColumn();
				}
			}
		}
		String sql = String.format(sqlMethod.getSql(), tableInfo.getTableName(), columnScript, valuesScript);
		SqlSource sqlSource = languageDriver.createSqlSource(configuration, sql, modelClass);
		return this.addInsertMappedStatement(mapperClass, modelClass, MAPPER_METHOD, sqlSource, keyGenerator, keyProperty, keyColumn);
	}


}
