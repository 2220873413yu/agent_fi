package com.xms.app.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xms.dao.entity.domain.Banner;

/**
 * <p>
 * appBanner图 服务类
 * </p>
 *
 *
 * @since 2023-07-27
 */
public interface BannerService extends IService<Banner> {

	/**
	 * 查询广告列表
	 * @return
	 */
	Banner getBannerList();
}
