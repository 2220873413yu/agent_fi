package com.xms.app.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.xms.app.service.SysNoticeService;
import com.xms.common.config.LocaleContextHolder;
import com.xms.common.constant.Constants;
import com.xms.common.constant.SysConstant;
import com.xms.dao.entity.domain.Notice;
import com.xms.dao.mapper.NoticeMapper;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 通知公告表 服务实现类
 * </p>
 *
 *
 * @since 2023-07-27
 */
@Service
public class SysNoticeServiceImpl extends ServiceImpl<NoticeMapper, Notice> implements SysNoticeService {

	/**
	 * 公告列表
	 *
	 * @param pageIndex 当前页 默认1
	 * @param type 语言类型 1:简体中文,2:繁体,3:英文,4:日本,5:韩文
	 * @param pageSize  每页长度 默认10(最大10)
	 * @return
	 */
	@Override
	public PageInfo<Notice> getSysNoticeList(Integer pageIndex, Integer pageSize, Integer type) {
		if(type == null){
			type = 1;
		}
		PageHelper.startPage(pageIndex, pageSize);
		PageInfo<Notice> pageInfo = new PageInfo<>(this.baseMapper.getSysNoticeList(type));
		return pageInfo;
	}

	/**
	 * 公告详情
	 *
	 * @param noticeId
	 * @return
	 */
	@Override
	public Notice getSysNoticeInfo(Integer noticeId) {
		return this.lambdaQuery().eq(Notice::getNoticeId, noticeId).one();
	}
}
