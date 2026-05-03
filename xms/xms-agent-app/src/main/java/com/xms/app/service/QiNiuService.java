package com.xms.app.service;


import com.xms.common.core.domain.api.ResultPista;
import com.xms.dao.entity.bo.QiNiuBo;

/**
 * @author MIER
 */
public interface QiNiuService {

	/**
	 *
	* @Title: getQiNiuToken
	* @param:
	* @Description: 查询七牛token
	* @return String
	 */
	ResultPista<QiNiuBo> getQiNiuToken();
}
