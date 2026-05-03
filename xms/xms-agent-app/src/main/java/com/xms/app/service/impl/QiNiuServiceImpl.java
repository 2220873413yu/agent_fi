package com.xms.app.service.impl;

import cn.hutool.core.map.MapUtil;
import com.xms.app.service.QiNiuService;
import com.xms.common.config.oss.QiniuUtils;
import com.xms.common.config.redis.XmsRedis;
import com.xms.common.constant.ConstantStatic;
import com.xms.common.core.domain.api.ResultPista;
import com.xms.common.utils.Kv;
import com.xms.dao.entity.bo.QiNiuBo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * @ClassName: QiNiuServiceImpl
 * @Description: 七牛云
 * @date 2023年7月24日 下午5:13:23
 */
@Service
public class QiNiuServiceImpl implements QiNiuService {

	@Autowired
	private QiniuUtils qiniuUtils;
	@Autowired
	private XmsRedis xmsRedis;

	/**
	 * 查询七牛云token
	 */
	@Override
	public ResultPista<QiNiuBo> getQiNiuToken() {
		if (!qiniuUtils.getEnabled()) {
			return ResultPista.fail("七牛云已关闭");
		}
		String token = xmsRedis.get(ConstantStatic.QINIU_TOKE);
		if (StringUtils.isBlank(token)) {
			Kv kv = qiniuUtils.getQiNiuToken();
			token = MapUtil.getStr(kv, "token");
			xmsRedis.set(ConstantStatic.QINIU_TOKE, token, 60 * 60L, TimeUnit.SECONDS);
		}
		return ResultPista.data(QiNiuBo.builder().token(token).domain(qiniuUtils.getPath()).build());
	}
}
