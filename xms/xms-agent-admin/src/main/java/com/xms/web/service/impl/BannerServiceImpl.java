package com.xms.web.service.impl;

import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xms.dao.entity.domain.Banner;
import com.xms.dao.mapper.BannerMapper;
import com.xms.web.service.IBannerService;
import org.springframework.stereotype.Service;

/**
 * appBanner图Service业务层处理
 *
 *
 * @date 2023-08-01
 */
@Service
public class BannerServiceImpl extends ServiceImpl<BannerMapper, Banner> implements IBannerService
{
	/**
	 * 查询appBanner图列表
	 *
	 *
	 * @param banner appBanner图
	 * @return appBanner图
	 */
	@Override
	public List<Banner> selectBannerList(Banner banner)
	{
		return baseMapper.selectBannerList(banner);
	}

}
