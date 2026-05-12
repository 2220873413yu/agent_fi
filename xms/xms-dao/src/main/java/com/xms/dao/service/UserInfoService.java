package com.xms.dao.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.pagehelper.PageInfo;
import com.xms.dao.entity.bo.*;
import com.xms.dao.entity.domain.UserInfo;
import com.xms.dao.entity.vo.ParentUserTaskVo;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 用户信息表 服务类
 * </p>
 *
 *
 * @since 2023-07-25
 */
public interface UserInfoService extends IService<UserInfo> {
		/**
	 *
	* @Title: getUserInfo
	* @param:
	* @Description: 用户详情
	* @return UserInfoBo
	 */
	UserInfoBo getUserInfo(Long userId);



	/**
	 * 获取直推用户列表
	 * @param userId
	 * @return
	 */
	PageInfo<DirectUserBo> getDirectUserList( Long userId);

	/**
	 * 查询上级团队用户
	 * @param userId
	 * @return
	 */
	List<UserInfo> getParentUserList(Long userId);

	/**
	 * 查询上级团队用户 只查询用户id 层级 等级相关
	 * @param userId
	 * @return
	 */
	List<ParentUserTaskVo> getParentUserTaskVo(Long userId);

	/**
	 * 查询距离当前用户最近的有效上级用户
	 * 按距离升序返回，第一位可作为直推，第二位可作为间推
	 * @param userId 当前用户ID
	 * @param limit 返回数量
	 * @return 有效上级用户列表
	 */
	List<ParentUserTaskVo> getValidParentUserTaskVo(Long userId, Integer limit);
	/**
	 * 查询上级团队用户
	 * @param userId
	 * @return
	 */
	public List<Long> getParentUserIdList(Long userId);

	/**
	 * 查询指定距离的团队用户
	 * @param userId 用户ID
	 * @param distance  距离
	 * @return
	 */
	public List<Long> getLimitedParentUserIdList(Long userId, Integer distance);

	/**
	 * 查询直推用户
	 * @param userCode
	 * @return
	 */
	List<UserInfo> getDirectUserList(String userCode);

	/**
	 * 查询下级团队用户
	 * @param userId
	 * @return
	 */
	List<UserInfo> getChildUserList(Long userId);

	/**
	 * 查询下级团队用户id
	 * @param userId
	 * @return
	 */
	List<Long> getChildUserIdList(Long userId);

	/**
	 * 查询用户状态
	 * @param userId
	 * @return
	 */
	int getUserStatus(Long userId);

	/**
	 * parentList为空就查询获取所有用户
	 * @return
	 */
	List<ChangeLevelUserBo> getChangeLevelUserBo(List<Long> parentList);


	/**
	 * 判断是否同线
	 * @param userId
	 * @param toUserId
	 * @return
	 */
	boolean sameTeam(Long userId,Long toUserId);

	/**
	 * 根据用户id获取用户昵称
	 * @param userIds
	 * @return
	 */
	Map<Long,String> getUserAccountById(List<Long> userIds);

	/**
	 * 根据用户id获取用户昵称
	 * @param userIds
	 * @return
	 */
	Map<Long,UserInfo> getUserAById(List<Long> userIds);


	/**
	 * 根据用户id获取用户编码
	 * @param userIds
	 * @return
	 */
	Map<Long,String> getUserCodeById(List<Long> userIds);


	/**
	 * 获取用户算力
	 * @return
	 */
	List<UserComputingPowerBo> userComputingPower();

	/**
	 * 获取用户等级
	 * @return
	 */
	List<UserLevelBo> getUserLevelList();

	/**
	 * 获取批量注册的用户
	 * @return
	 */
	List<BatchUserBo> getBatchUser();

	/**
	 * 首次开通OpenAI聊天时原子标记用户已扣费。
	 *
	 * @param userId 用户ID
	 * @return 1表示本次首次标记成功；0表示已标记或用户不存在
	 */
	int markOpenAiPaidIfUnpaid(Long userId);
}
