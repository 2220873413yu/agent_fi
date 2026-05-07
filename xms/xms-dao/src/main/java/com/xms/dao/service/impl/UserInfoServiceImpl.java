package com.xms.dao.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageInfo;
import com.xms.common.config.redis.XmsRedis;
import com.xms.common.constant.ConstantStatic;
import com.xms.common.constant.SysConstant;
import com.xms.common.core.domain.api.ResultPista;
import com.xms.common.exception.ServiceException;
import com.xms.common.result.ResponseCode;
import com.xms.dao.entity.bo.*;
import com.xms.dao.entity.domain.UserInfo;
import com.xms.dao.entity.vo.ParentUserTaskVo;
import com.xms.dao.mapper.UserInfoMapper;
import com.xms.dao.service.UserInfoService;
import io.swagger.annotations.ApiOperation;
import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>
 * 用户信息表 服务实现类
 * </p>
 *
 * @since 2023-07-25
 */
@Service
@Slf4j
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements UserInfoService {


	@Autowired
	private XmsRedis xmsRedis;

	/**
	 * 获取大区业绩
	 * @param userInfos 直推列表
	 * @return
	 */
	public static BigDecimal getMaxTeamPerformance(List<UserInfo> userInfos) {
		BigDecimal maxPerformance = userInfos.stream()
			.map(item -> {
				return item.getPerformance().add(item.getUmbrellaPerformance());
			})
			.max(BigDecimal::compareTo)
			.orElse(BigDecimal.ZERO);
		return maxPerformance;
	}

	/**
	 * 用户详情
	 */
	@Override
	public UserInfoBo getUserInfo(Long userId) {
		UserInfo userInfo = this.lambdaQuery()
			.eq(UserInfo::getUserId, userId)
			.one();
		if (userInfo == null) {
			throw new ServiceException(ResponseCode.CODE_1007);
		}

		UserInfoBo userInfoBo = new UserInfoBo();
		userInfoBo.setAccount(userInfo.getAccount());
		userInfoBo.setUserId(userInfo.getUserId());
		userInfoBo.setNodeLevel(userInfo.getNodeLevel());
//		userInfoBo.setUserCode(userInfo.getUserCode());
		userInfoBo.setGameLevel(maxLevel(userInfo.getGameLevel(), userInfo.getMinGameLevel(), userInfo.getAdminGameLevel()));
		if(userInfo.getInviteUserId()!=null){
			UserInfo inviteUserInfo = lambdaQuery()
				.eq(UserInfo::getUserId, userInfo.getInviteUserId())
				.select(UserInfo::getAccount)
				.one();
			userInfoBo.setInviteUserAccount(inviteUserInfo == null ? "" : inviteUserInfo.getAccount());
		}else{
			userInfoBo.setInviteUserAccount("");
		}

		//userInfoBo.setIsValid(userInfo.getIsValid());
		userInfoBo.setSubNum(userInfo.getSubNum());
		userInfoBo.setUmbrellaNum(userInfo.getUmbrellaNum());
		userInfoBo.setSubNodePerformance(userInfo.getSubNodePerformance());
		userInfoBo.setNodeTeamPerformance(userInfo.getNodeTeamPerformance());
		//userInfoBo.setUmbrellaPerformance(userInfo.getUmbrellaPerformance());
		//userInfoBo.setPerformance(userInfo.getPerformance());
		//userInfoBo.setCommunityPerformance(userInfo.getCommunityPerformance());
		return userInfoBo;
	}

	private Integer maxLevel(Integer... levels) {
		Integer max = 0;
		if (levels == null) {
			return max;
		}
		for (Integer level : levels) {
			if (level != null && level > max) {
				max = level;
			}
		}
		return max;
	}

	/**
	 * 获取直推用户列表
	 *
	 * @param userId
	 * @return
	 */
	@Override
	public PageInfo<DirectUserBo> getDirectUserList(Long userId) {
		//查询用户
		UserInfo userInfo = this.lambdaQuery().eq(UserInfo::getUserId, userId).one();
		if (userInfo == null) {
			throw new ServiceException(ResponseCode.CODE_1007);
		}
		PageInfo<DirectUserBo> pageInfo = new PageInfo<>(this.baseMapper.getDirectUserList(userInfo.getUserCode()));
		return pageInfo;
	}

	/**
	 * 查询上级团队用户
	 *
	 * @param userId
	 * @return
	 */
	@Override
	public List<UserInfo> getParentUserList(Long userId) {
		return this.baseMapper.getParentUserList(userId);
	}

	/**
	 * 查询上级团队用户 只查询用户id 层级 等级相关
	 * @param userId
	 * @return
	 */
	@Override
	public List<ParentUserTaskVo> getParentUserTaskVo(Long userId) {
		return baseMapper.getParentUserTaskVo(userId);
	}

	@Override
	public List<ParentUserTaskVo> getValidParentUserTaskVo(Long userId, Integer limit) {
		return baseMapper.getValidParentUserTaskVo(userId, limit);
	}

	/**
	 * 查询直推用户
	 *
	 * @param userCode
	 * @return
	 */
	@Override
	public List<UserInfo> getDirectUserList(String userCode) {
		return this.lambdaQuery().eq(UserInfo::getInviteUserCode, userCode).list();
	}

	/**
	 * 查询下级团队用户
	 *
	 * @param userId
	 * @return
	 */
	@Override
	public List<UserInfo> getChildUserList(Long userId) {
		return this.baseMapper.getChildUserList(userId);
	}

	/**
	 * 查询下级团队用户
	 *
	 * @param userId
	 * @return
	 */
	@Override
	public List<Long> getChildUserIdList(Long userId) {
		return this.baseMapper.getChildUserIdList(userId);
	}

	/**
	 * 查询用户状态
	 *
	 * @param userId
	 * @return
	 */
	@Override
	public int getUserStatus(Long userId) {
		String value = xmsRedis.get(ConstantStatic.USER_STATUS + userId + ":");
		if (StringUtils.isBlank(value)) {
			//查询用户
			UserInfo userInfo = this.lambdaQuery().eq(UserInfo::getUserId, userId).one();
			if (userInfo == null) {
				throw new ServiceException(ResponseCode.CODE_1007);
			}
			value = String.valueOf(userInfo.getStatus());
			xmsRedis.set(ConstantStatic.USER_STATUS + userId + ":", value);
		}
		return Integer.parseInt(value);
	}

	/**
	 * 获取所有用户
	 *
	 * @return
	 */
	@Override
	public List<ChangeLevelUserBo> getChangeLevelUserBo(List<Long> parentList) {
		return baseMapper.getChangeLevelUserBo(parentList);
	}

	@Override
	public List<Long> getParentUserIdList(Long userId) {
		return baseMapper.getParentUserIdList(userId);
	}

	@Override
	public List<Long> getLimitedParentUserIdList(Long userId, Integer distance) {
		return baseMapper.getLimitedParentUserIdList(userId,distance);
	}

	/**
	 * 判断是否同线
	 * @param userId
	 * @param toUserId
	 * @return true代表同线,false代表不同的线
	 */
	@Override
	public boolean sameTeam(Long userId, Long toUserId) {
		//判断是否同线
		List<Long> parentUserList = baseMapper.getParentUserIdList(userId);
		for(Long info : parentUserList){
			if(info.intValue() == toUserId.intValue()){
				return true;
			}
		}
		List<Long> childUserList = baseMapper.getChildUserIdList(userId);
		for(Long info : childUserList){
			if(info.intValue() == toUserId.intValue()){
				return true;
			}
		}
		return false;
	}
	@Override
	public Map<Long, String> getUserAccountById(List<Long> userIds) {
		return lambdaQuery()
			.in(UserInfo::getUserId, userIds)
			.select(UserInfo::getUserId, UserInfo::getAccount)
			.list().stream().collect(Collectors.toMap(UserInfo::getUserId, UserInfo::getAccount, (k1, k2) -> k2));

	}

	@Override
	public Map<Long, UserInfo> getUserAById(List<Long> userIds) {
		return lambdaQuery()
			.in(UserInfo::getUserId, userIds)
			.select(UserInfo::getUserId, UserInfo::getAccount,UserInfo::getUserCode)
			.list().stream().collect(Collectors.toMap(UserInfo::getUserId, Function.identity(), (k1, k2) -> k2));

	}

	@Override
	public Map<Long, String> getUserCodeById(List<Long> userIds) {
		return lambdaQuery()
			.in(UserInfo::getUserId, userIds)
			.select(UserInfo::getUserId, UserInfo::getUserCode)
			.list().stream().collect(Collectors.toMap(UserInfo::getUserId, UserInfo::getUserCode, (k1, k2) -> k2));

	}



	@Override
	public List<UserComputingPowerBo> userComputingPower() {
		return baseMapper.userComputingPower();
	}

	@Override
	public List<UserLevelBo> getUserLevelList() {
		return baseMapper.getUserLevelList();
	}

	@Override
	public List<BatchUserBo> getBatchUser() {
		return baseMapper.getBatchUser();
	}
}


