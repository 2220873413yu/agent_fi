package com.xms.dao.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.xms.common.config.mybatis.XmsSqlMethod;
import com.xms.common.core.domain.BaseEntity;
import com.xms.dao.mapper.XmsMapper;
import com.xms.dao.service.XmsDataService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;


/**
 * （ 泛型：M 是 mapper 对象，T 是实体 ， PK 是主键泛型 ）
 *
 * @author MIER
 */
@Validated
public class XmsDataServiceImpl<M extends XmsMapper<T>, T extends BaseEntity> extends ServiceImpl<M, T> implements XmsDataService<T> {

	@Override
	public boolean saveIgnore(T entity) {
		return SqlHelper.retBool(baseMapper.insertIgnore(entity));
	}

	@Override
	public boolean saveReplace(T entity) {
		return SqlHelper.retBool(baseMapper.replace(entity));
	}

	@Transactional(rollbackFor = Exception.class)
	@Override
	public boolean saveIgnoreBatch(Collection<T> entityList, int batchSize) {
		return saveBatch(entityList, batchSize, XmsSqlMethod.INSERT_IGNORE_ONE);
	}

	@Transactional(rollbackFor = Exception.class)
	@Override
	public boolean saveReplaceBatch(Collection<T> entityList, int batchSize) {
		return saveBatch(entityList, batchSize, XmsSqlMethod.REPLACE_ONE);
	}

	private boolean saveBatch(Collection<T> entityList, int batchSize, XmsSqlMethod sqlMethod) {
		String sqlStatement = xmsSqlStatement(sqlMethod);
		executeBatch(entityList, batchSize, (sqlSession, entity) -> sqlSession.insert(sqlStatement, entity));
		return true;
	}

	/**
	 * 获取 xmsSqlStatement
	 *
	 * @param sqlMethod ignore
	 * @return sql
	 */
	protected String xmsSqlStatement(XmsSqlMethod sqlMethod) {
		return SqlHelper.table(currentModelClass()).getSqlStatement(sqlMethod.getMethod());
	}

}
