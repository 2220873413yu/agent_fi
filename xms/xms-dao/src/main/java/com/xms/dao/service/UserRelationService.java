package com.xms.dao.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xms.dao.entity.domain.UserInfo;
import com.xms.dao.entity.domain.UserRelation;

import java.util.List;

/**
 * <p>
 * 节点关系表 服务类
 * </p>
 *
 *
 * @since 2023-07-25
 */
public interface UserRelationService extends IService<UserRelation> {

	/**
	 *
	* @Title: getParentList
	* @param:
	* @Description: 获取上级用户
	* @return List<UserRelation>
	 */
	List<UserRelation> getParentList(Long userId);

	List<UserRelation> getParentListCache(Long userId);

	List<UserRelation> getParentListNoMe(Long userId);

	List<UserInfo> getParentListNotMe(Long userId);

	List<UserInfo> getParentListNotMeCurrent(Long userId);

	List<UserInfo> getSonListNotMeCurrentActive(Long userId);


	List<UserInfo> getSonListNotMeCurrentActiveByDepth(Long userId, Integer depth);
}
