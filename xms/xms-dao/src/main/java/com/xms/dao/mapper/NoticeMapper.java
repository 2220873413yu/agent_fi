package com.xms.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xms.dao.entity.domain.Notice;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 通知公告表 Mapper 接口
 * </p>
 *
 *
 * @since 2023-07-27
 */
public interface NoticeMapper extends BaseMapper<Notice> {

	/**
	 * 公告列表
	 * @return
	 */
	List<Notice> getSysNoticeList( @Param("type") Integer type);
}
