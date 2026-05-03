package com.xms.web.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xms.dao.entity.domain.Banner;

import java.util.List;

/**
 * appBanner图Service接口
 *
 *
 * @date 2023-08-01
 */
public interface IBannerService extends IService<Banner>
{

	/**
	 * 查询appBanner图列表
	 *
	 * @param banner appBanner图
	 * @return appBanner图集合
	 */
	public List<Banner> selectBannerList(Banner banner);

}
