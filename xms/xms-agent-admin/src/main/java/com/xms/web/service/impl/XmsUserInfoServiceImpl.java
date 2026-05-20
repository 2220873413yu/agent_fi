package com.xms.web.service.impl;

import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.util.StrUtil;
import com.xms.common.config.redis.XmsRedis;
import com.xms.common.config.redis.delayqueue.config.RedissonTemplate;
import com.xms.common.constant.ConstantStatic;
import com.xms.common.core.domain.AjaxResult;
import com.xms.common.core.domain.entity.SysDictData;
import com.xms.common.exception.ServiceException;
import com.xms.common.utils.CollectionUtil;
import com.xms.common.utils.TreeBuildUtils;
import com.xms.dao.entity.bo.TeamUsersBo;
import com.xms.dao.entity.bo.UserInfoReqBo;
import com.xms.dao.entity.domain.UserInfo;
import com.xms.dao.entity.dto.UserNetBodyExportDto;
import com.xms.dao.mapper.UserInfoMapper;
import com.xms.dao.service.UserInfoService;
import com.xms.system.service.ISysUserService;
import com.xms.web.service.XmsUserInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户信息Service业务层处理
 *
 * @date 2023-07-28
 */
@Service
@Slf4j
public class XmsUserInfoServiceImpl implements XmsUserInfoService {
	@Autowired
	private XmsRedis xmsRedis;

	@Autowired
	private ISysUserService sysUserService;

	@Autowired
	private UserInfoService userInfoService;

	@Autowired
	private RedissonTemplate redissonTemplate;

	@Autowired
	private UserInfoMapper userInfoMapper;

	/**
	 * 查询用户信息列表
	 *
	 * @param userInfo 用户信息
	 * @return 用户信息
	 */
	@Override
	public List<UserInfo> selectUserInfoList(UserInfo userInfo) {
		return userInfoMapper.selectUserInfoList(userInfo);
	}

	/**
	 * 查询下级团队用户
	 *
	 * @param userInfo 用户信息
	 * @return 用户信息集合
	 */
	@Override
	public List<UserInfo> selectChildUserInfoList(UserInfo userInfo) {
		return userInfoMapper.selectChildUserInfoList(userInfo);
	}

	/**
	 * 查询下级团队用户
	 *
	 * @param userId
	 * @return
	 */
	@Override
	public List<Long> getChildUserIdList(Long userId) {
		return userInfoMapper.getChildUserIdList(userId);
	}

	/**
	 * 统计今团队用户数
	 * @param userId
	 * @return
	 */
	@Override
	public List<TeamUsersBo> countTodayNewTeamUsers(Long userId) {
		return userInfoMapper.countTodayNewTeamUsers(userId);
	}


	/**
	 * 更新后台用户资料。
	 *
	 * <p>该方法用于用户管理页面的修改弹框，只更新后台允许维护的字段：
	 * 管理员保底等级、托管指定收益率、钱包地址、账号/提现状态和后台备注。
	 * 更新前会校验用户存在、托管指定收益率不能小于0，并校验Google验证码。</p>
	 *
	 * @param req 用户修改请求，包含用户ID、状态、等级、提现开关、托管指定收益率和备注
	 * @return 更新结果
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public AjaxResult updateUserInfo(UserInfoReqBo req) {
		UserInfo queryUserInfo = userInfoService.lambdaQuery().eq(UserInfo::getUserId, req.getUserId()).one();
		if (queryUserInfo == null) {
			throw new ServiceException("用户不存在");
		}

		UserInfo updateUser = new UserInfo();

		updateUser.setAdminGameLevel(req.getAdminGameLevel());
		updateUser.setUserId(req.getUserId());
		if (req.getStakeHostingStaticRate() != null && req.getStakeHostingStaticRate().compareTo(BigDecimal.ZERO) < 0) {
			throw new ServiceException("托管指定静态收益率不能小于0");
		}
		updateUser.setStakeHostingStaticRate(req.getStakeHostingStaticRate() == null ? BigDecimal.ZERO : req.getStakeHostingStaticRate());

		if(StrUtil.isNotBlank(req.getAccount())){
			updateUser.setAccount(req.getAccount());
		}

		//账号状态
		updateUser.setStatus(req.getStatus());
		//提现状态
		updateUser.setWithdrawalOpenOrClose(req.getWithdrawalOpenOrClose());
		//后台备注允许被清空，直接使用前端提交值覆盖。
		updateUser.setRemark(req.getRemark());
		//验证码验证
		sysUserService.pubValidate(req.getAutoCode());

		int i = this.userInfoMapper.updateById(updateUser);
		String key1 = ConstantStatic.USER_STATUS + queryUserInfo.getUserId() + ":";
		xmsRedis.del(key1);
		xmsRedis.del(key1);
		redissonTemplate.sendCleanCacheWithDelay(key1);
		if (i > 0) {
			return AjaxResult.success();
		} else {
			return AjaxResult.error();
		}
	}
	/**
	 * 查询网体-树结构方法
	 * @param userId
	 * @return
	 */
	@Override
	public List<Tree<Long>> queryNetBody1(String userId) {
		UserInfo currentUser = null;
		if(StrUtil.isBlank(userId)){
			 currentUser = userInfoMapper.selectById(1000L);
		}else{
			currentUser = userInfoService.lambdaQuery()
				.eq(UserInfo::getAccount, userId)
				.one();
		}

		if (currentUser == null) {
			throw new ServiceException("查询的用户不存在");
		}
		Map<Integer, String> nodeLevelMap = this.userInfoMapper.selectDictDataByType("t_node_plan_node_level")
			.stream()
			.collect(Collectors.toMap(
				data -> Integer.parseInt(data.getDictValue()),
				SysDictData::getDictLabel,
				(existing, replacement) -> existing  // 或选择一个策略来处理冲突
			));
		List<UserInfo> userList =  userInfoMapper.queryNetBodyChildUser(currentUser.getUserId());

		if(CollectionUtil.isEmpty(userList)){
			currentUser.setParentId(currentUser.getInviteUserId());
			userList.addFirst(currentUser);
			return TreeBuildUtils.build(userList, ((user, tree) -> {
				tree.setId(user.getUserId());
				tree.setParentId(user.getInviteUserId());
				tree.setName(user.getAccount());
				fillNodeTreeExtra(user, tree, nodeLevelMap);
			}));
		}
		userList.addFirst(currentUser);
		for (UserInfo userInfo : userList) {
			userInfo.setParentId(userInfo.getInviteUserId());
		}
		return TreeBuildUtils.build(userList, ((user, tree) -> {
			tree.setId(user.getUserId());
			tree.setParentId(user.getParentId());
			tree.setName(user.getAccount());
			fillNodeTreeExtra(user, tree, nodeLevelMap);
		}));
	}

	private void fillNodeTreeExtra(UserInfo user, Tree<Long> tree, Map<Integer, String> nodeLevelMap) {
		//钱包地址
		tree.putExtra("account", user.getAccount());
		//节点等级
		tree.putExtra("level", nodeLevelMap.get(user.getNodeLevel()));
		//直推节点数量
		tree.putExtra("subNodePerformance", user.getSubNodePerformance());
		//团队节点数量
		tree.putExtra("nodeTeamPerformance", user.getNodeTeamPerformance());
		//直推用户数保留给前端判断是否还有子节点
		tree.putExtra("subNum", user.getSubNum());
		//增加团队节点支付
		BigDecimal umbrellaNodePerformance = defaultAmount(user.getUmbrellaNodePerformance());
		BigDecimal adminUmbrellaNodePerformance = defaultAmount(user.getAdminUmbrellaNodePerformance());
		tree.putExtra("umbrellaNodePerformance", umbrellaNodePerformance);
		//增加节点金额(用户购买的+后台拨付的)
		tree.putExtra("allUmbrellaNodePerformance", umbrellaNodePerformance.add(adminUmbrellaNodePerformance));
		//团队用户数
		tree.putExtra("umbrellaNum", user.getUmbrellaNum());
	}

	/**
	 * 导出网体树页面当前查询用户下的扁平用户数据。
	 *
	 * <p>该方法沿用queryNetBody1的根用户定位和下级查询范围，但返回独立导出DTO，不影响树形接口和用户信息列表导出。
	 * 导出的团队节点金额=用户购买团队节点支付+后台拨付团队节点业绩。</p>
	 *
	 * @param userId 钱包地址，可为空；为空时默认导出1000用户网体
	 * @return 网体树导出DTO列表
	 */
	@Override
	public List<UserNetBodyExportDto> exportNetBody(String userId) {
		UserInfo currentUser;
		if (StrUtil.isBlank(userId)) {
			currentUser = userInfoMapper.selectById(1000L);
		} else {
			currentUser = userInfoService.lambdaQuery()
				.eq(UserInfo::getAccount, userId)
				.one();
		}

		if (currentUser == null) {
			throw new ServiceException("查询的用户不存在");
		}

		Map<Integer, String> nodeLevelMap = this.userInfoMapper.selectDictDataByType("t_node_plan_node_level")
			.stream()
			.collect(Collectors.toMap(
				data -> Integer.parseInt(data.getDictValue()),
				SysDictData::getDictLabel,
				(existing, replacement) -> existing
			));

		// 导出页面“所有数据”按后端网体查询范围导出，不依赖前端是否已展开节点。
		List<UserInfo> userList = userInfoMapper.queryNetBodyChildUser(currentUser.getUserId());
		userList.addFirst(currentUser);
		return userList.stream()
			.map(user -> buildNetBodyExportDto(user, nodeLevelMap))
			.collect(Collectors.toList());
	}

	/**
	 * 将用户信息转换为网体树导出行。
	 *
	 * @param user 用户信息
	 * @param nodeLevelMap 节点等级字典映射
	 * @return Excel导出行DTO
	 */
	private UserNetBodyExportDto buildNetBodyExportDto(UserInfo user, Map<Integer, String> nodeLevelMap) {
		BigDecimal umbrellaNodePerformance = defaultAmount(user.getUmbrellaNodePerformance());
		BigDecimal adminUmbrellaNodePerformance = defaultAmount(user.getAdminUmbrellaNodePerformance());
		UserNetBodyExportDto dto = new UserNetBodyExportDto();
		dto.setUserId(user.getUserId());
		dto.setAccount(user.getAccount());
		dto.setNodeLevel(nodeLevelMap.get(user.getNodeLevel()));
		dto.setSubNum(user.getSubNum());
		dto.setUmbrellaNum(user.getUmbrellaNum());
		dto.setSubNodePerformance(defaultAmount(user.getSubNodePerformance()));
		dto.setNodeTeamPerformance(defaultAmount(user.getNodeTeamPerformance()));
		dto.setUmbrellaNodePerformance(umbrellaNodePerformance);
		dto.setAdminUmbrellaNodePerformance(adminUmbrellaNodePerformance);
		dto.setAllUmbrellaNodePerformance(umbrellaNodePerformance.add(adminUmbrellaNodePerformance));
		dto.setCreateTime(user.getCreateTime());
		return dto;
	}

	/**
	 * 金额字段空值兜底为0，避免历史数据为空时导出失败。
	 *
	 * @param value 数据库金额字段
	 * @return 非空金额
	 */
	private BigDecimal defaultAmount(BigDecimal value) {
		return value == null ? BigDecimal.ZERO : value;
	}

	@Override
	public UserInfo getById(Long userId) {
		return userInfoService.lambdaQuery()
			.eq(UserInfo::getUserId, userId)
			.one();
	}

}
