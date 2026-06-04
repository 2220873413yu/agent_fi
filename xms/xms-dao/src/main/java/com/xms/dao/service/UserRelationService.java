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
	 * 查询指定用户的闭包上级关系，包含用户自己。
	 *
	 * <p>注册新用户时传入邀请人ID，返回结果会作为新用户关系表的模板：
	 * 邀请人自己的 distance=0 会转换成新用户到邀请人的 distance=1，
	 * 邀请人的祖先会依次转换成新用户的更高层级祖先。</p>
	 *
	 * @param userId 用户ID
	 * @return 按 distance 升序排列的闭包关系，包含 distance=0 的自己
	 */
	List<UserRelation> getParentList(Long userId);

	List<UserRelation> getParentListCache(Long userId);

	List<UserRelation> getParentListNoMe(Long userId);

	List<UserInfo> getParentListNotMe(Long userId);

	List<UserInfo> getParentListNotMeCurrent(Long userId);

	List<UserInfo> getSonListNotMeCurrentActive(Long userId);


	List<UserInfo> getSonListNotMeCurrentActiveByDepth(Long userId, Integer depth);
}
