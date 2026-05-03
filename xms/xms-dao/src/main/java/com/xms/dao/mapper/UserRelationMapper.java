package com.xms.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xms.dao.entity.domain.UserInfo;
import com.xms.dao.entity.domain.UserRelation;

import java.util.List;

/**
 * <p>
 * 节点关系表 Mapper 接口
 * </p>
 *
 *
 * @since 2023-07-25
 */
public interface UserRelationMapper extends BaseMapper<UserRelation> {

    List<UserInfo> getParentUserList(Long userId);

	List<UserInfo> getSonListNotMeCurrentActive(Long userId);

	List<UserInfo> getSonListNotMeCurrentActiveByDepth(Long userId, Integer depth);
}
