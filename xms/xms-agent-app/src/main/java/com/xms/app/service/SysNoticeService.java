package com.xms.app.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.pagehelper.PageInfo;
import com.xms.dao.entity.domain.Notice;

/**
 * <p>
 * 通知公告表 服务类
 * </p>
 *
 *
 * @since 2023-07-27
 */
public interface SysNoticeService extends IService<Notice> {

	/**
	 * 公告列表
	 *
	 * @param pageIndex 当前页 默认1
	 * @param type 语言类型 1:简体中文,2:繁体,3:英文,4:日本,5:韩文
	 * @param pageSize  每页长度 默认10(最大10)
	 * @return
	 */
	PageInfo<Notice> getSysNoticeList(Integer pageIndex, Integer pageSize,Integer type);

	/**
	 * 公告详情
	 * @param noticeId
	 * @return
	 */
	Notice getSysNoticeInfo(Integer noticeId);
}
