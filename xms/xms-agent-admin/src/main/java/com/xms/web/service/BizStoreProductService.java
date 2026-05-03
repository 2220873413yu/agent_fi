package com.xms.web.service;

import com.xms.dao.entity.bo.BizProductDetailBo;
import com.xms.dao.entity.req.BizProductDetailReq;

public interface BizStoreProductService {
	BizProductDetailBo getById(String id);

	/**
	 * 保存
	 * @param diyStoreProduct
	 * @return
	 */
	int saveOrUpdate(BizProductDetailReq diyStoreProduct);
}
