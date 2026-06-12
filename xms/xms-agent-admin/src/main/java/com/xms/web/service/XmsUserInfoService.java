package com.xms.web.service;

import cn.hutool.core.lang.tree.Tree;
import com.xms.common.core.domain.AjaxResult;
import com.xms.dao.entity.bo.TeamUsersBo;
import com.xms.dao.entity.bo.UserInfoReqBo;
import com.xms.dao.entity.domain.UserInfo;
import com.xms.dao.entity.dto.UserNetBodyExportDto;

import java.util.List;

/**
 * 用户信息Service接口
 *
 *
 * @date 2023-07-28
 */
public interface XmsUserInfoService
{

    /**
     * 查询用户信息列表
     *
     * @param userInfo 用户信息
     * @return 用户信息集合
     */
    public List<UserInfo> selectUserInfoList(UserInfo userInfo);

	/**
	 * 查询下级团队用户
	 *
	 * @param userInfo 用户信息
	 * @return 用户信息集合
	 */
	public List<UserInfo> selectChildUserInfoList(UserInfo userInfo);

	/**
	 * 查询下级团队用户
	 *
	 * @param userId
	 * @return
	 */
	public List<Long> getChildUserIdList(Long userId);

	/**
	 * 统计团队用户数
	 * @param userId
	 * @return
	 */
	public List<TeamUsersBo> countTodayNewTeamUsers(Long userId);

	/**
	 * 更新用戶
	 * @param userInfo
	 * @return
	 */
	public AjaxResult updateUserInfo(UserInfoReqBo userInfo);

	/**
	 * 查询网体-树结构方法
	 * @param userId
	 * @return
	 */
	List<Tree<Long>> queryNetBody1(String userId);

	/**
	 * 导出网体关系树页面当前查询口径下的扁平数据。
	 *
	 * @param userId 钱包地址，可为空；为空时默认导出1000用户网体
	 * @return 网体树导出DTO列表
	 */
	List<UserNetBodyExportDto> exportNetBody(String userId);

	/**
	 * 根据id查询用户信息
	 * @param userId
	 * @return
	 */
	UserInfo getById(Long userId);
}
