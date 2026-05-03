package com.xms.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xms.dao.entity.domain.Banner;

import java.util.List;

/**
 * <p>
 * appBanner图 Mapper 接口
 * </p>
 *
 *
 * @since 2023-07-27
 */
public interface BannerMapper extends BaseMapper<Banner> {

	/**
	 * 查询广告列表
	 * @return
	 */
	Banner getBannerList();

	//****************管理后台***************

	/**
	 * 查询appBanner图列表
	 *
	 * @param banner appBanner图
	 * @return appBanner图集合
	 */
	public List<Banner> selectBannerList(Banner banner);
}
