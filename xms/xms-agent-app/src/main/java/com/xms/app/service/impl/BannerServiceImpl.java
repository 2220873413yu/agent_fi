package com.xms.app.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xms.app.service.BannerService;
import com.xms.dao.entity.domain.Banner;
import com.xms.dao.mapper.BannerMapper;
import org.springframework.stereotype.Service;

/**
 * <p>
 * appBanner图 服务实现类
 * </p>
 *
 *
 * @since 2023-07-27
 */
@Service
public class BannerServiceImpl extends ServiceImpl<BannerMapper, Banner> implements BannerService {

	/**
	 * 查询广告列表
	 *
	 * @return
	 */
	@Override
	public Banner getBannerList() {
		Banner bannerList = this.baseMapper.getBannerList();
		return bannerList;
	}
}
