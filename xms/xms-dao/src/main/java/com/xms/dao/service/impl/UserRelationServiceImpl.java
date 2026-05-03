package com.xms.dao.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xms.common.config.redis.XmsRedis;
import com.xms.common.constant.RedisConstant;
import com.xms.common.constant.SysConstant;
import com.xms.dao.entity.domain.UserInfo;
import com.xms.dao.entity.domain.UserRelation;
import com.xms.dao.mapper.UserRelationMapper;
import com.xms.dao.service.UserRelationService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 节点关系表 服务实现类
 * </p>
 *
 * @author MIER
 * @since 2023-07-25
 */
@Service
@AllArgsConstructor
public class UserRelationServiceImpl extends ServiceImpl<UserRelationMapper, UserRelation> implements UserRelationService {
	private final XmsRedis xmsRedis;

	/**
	 * 获取上级用户，包含自己的
	 */
	@Override
	public List<UserRelation> getParentList(Long userId) {
		return this.lambdaQuery().eq(UserRelation::getPosUserId, userId)
			.orderByAsc(UserRelation::getDistance).list();
	}
	/**
	 * 获取上级用户，包含自己的
	 */
	@Override
	public List<UserRelation> getParentListCache(Long userId) {
		return xmsRedis.get(RedisConstant.USER_PARENT_ME_LIST + userId, () -> this.lambdaQuery().eq(UserRelation::getPosUserId, userId)
			.orderByAsc(UserRelation::getDistance).list(), RedisConstant.DAY_EXPIRE_TIME, TimeUnit.DAYS);

	}

	/**
	 * 获取上级用户 不包含自己，注册了不会在改变
	 */
	@Override
	public List<UserRelation> getParentListNoMe(Long userId) {
		return xmsRedis.get(RedisConstant.USER_PARENT_NOME_LIST + userId, () -> this.lambdaQuery().eq(UserRelation::getPosUserId, userId)
			.ne(UserRelation::getDistance, SysConstant.ZERO)
			.orderByAsc(UserRelation::getDistance).list(), RedisConstant.DAY_EXPIRE_TIME, TimeUnit.DAYS);
	}


	/**
	 * 获取上级用户 不包含自己，注册了不会在改变,userinfo返回体,缓存版
	 */
	@Override
	public List<UserInfo> getParentListNotMe(Long userId) {
		return xmsRedis.get(RedisConstant.USER_PARENT_NOT_ME_LIST + userId, () -> baseMapper.getParentUserList(userId), RedisConstant.DAY_EXPIRE_TIME, TimeUnit.DAYS);
	}

	@Override
	public List<UserInfo> getParentListNotMeCurrent(Long userId) {
		return  baseMapper.getParentUserList(userId);
	}

	@Override
	public List<UserInfo> getSonListNotMeCurrentActive(Long userId) {
		return  baseMapper.getSonListNotMeCurrentActive(userId);
	}

	@Override
	public List<UserInfo> getSonListNotMeCurrentActiveByDepth(Long userId, Integer depth) {
		return  baseMapper.getSonListNotMeCurrentActiveByDepth(userId,depth);
	}
}
