package com.xms.web.service;


import com.xms.common.core.domain.AjaxResult;
import com.xms.dao.entity.domain.SysPara;
import com.xms.web.domain.DiyMiningOrderReq;

/**
 *  admin专属业务
 * @author: GT63S
 * @createDate: 2024/8/17
 */
public interface BackExclusiveService {

	int updateSysPara(SysPara sysPara) throws Exception;

}
