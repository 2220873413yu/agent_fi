package com.xms.web.controller.xms;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import com.xms.common.annotation.Log;
import com.xms.common.annotation.RepeatSubmit;
import com.xms.common.core.controller.BaseController;
import com.xms.common.core.domain.AjaxResult;
import com.xms.common.core.page.TableDataInfo;
import com.xms.common.enums.BusinessType;
import com.xms.common.exception.ServiceException;
import com.xms.common.utils.CollectionUtil;
import com.xms.common.utils.poi.ExcelUtil;
import com.xms.dao.domain.*;
import com.xms.dao.entity.domain.UserRelation;
import com.xms.dao.entity.bo.UserInfoReqBo;
import com.xms.dao.entity.domain.UserInfo;
import com.xms.dao.service.*;
import com.xms.dao.service.impl.UserInfoServiceImpl;
import com.xms.web.service.XmsUserInfoService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户信息Controller
 *
 * @date 2023-07-28
 */
@RestController
@RequestMapping("/xms/userinfo")
public class UserInfoController extends BaseController {
	@Autowired
	private XmsUserInfoService xmsUserInfoService;

	@Autowired
	private IMiningPackageOrderService miningPackageOrderService;

	/**
	 * 查询用户信息列表
	 */
	@PreAuthorize("@ss.hasPermi('xms:userinfo:list')")
	@GetMapping("/list")
	public TableDataInfo list(UserInfo userInfo) {
		startPage();
		List<UserInfo> list = xmsUserInfoService.selectUserInfoList(userInfo);
		return getDataTable(list);
	}


	/**
	 * 导出用户信息列表
	 */
	@PreAuthorize("@ss.hasPermi('xms:userinfo:export')")
	@Log(title = "用户信息", businessType = BusinessType.EXPORT)
	@PostMapping("/export")
	public void export(HttpServletResponse response, UserInfo userInfo) {
		List<UserInfo> list = xmsUserInfoService.selectUserInfoList(userInfo);
		ExcelUtil<UserInfo> util = new ExcelUtil<UserInfo>(UserInfo.class);
		util.exportExcel(response, list, "用户信息数据");
	}




	/**
	 * 导出用户信息列表
	 */
	@PreAuthorize("@ss.hasPermi('xms:userinfo:export')")
	@Log(title = "导出直推用户信息", businessType = BusinessType.EXPORT)
	@PostMapping("/subUserExport")
	public void subUserExport(HttpServletResponse response, UserInfo userInfo) {
		if(ObjectUtil.isNotEmpty(userInfo.getParams().get("topUserId"))){
			UserInfo topUserInfo = xmsUserInfoService.getById(MapUtil.getLong(userInfo.getParams(), "topUserId"));
			userInfo.setInviteUserCode(topUserInfo.getUserCode());
		}

		startPage();
		List<UserInfo> list = xmsUserInfoService.selectChildUserInfoList(userInfo);
		ExcelUtil<UserInfo> util = new ExcelUtil<UserInfo>(UserInfo.class);
		util.exportExcel(response, list, "直推用户信息");
	}

	/**
	 * 查询下级团队用户
	 *
	 * @param userInfo
	 * @return
	 */
	@PreAuthorize("@ss.hasPermi('xms:userinfo:childList')")
	@GetMapping("/subUserList")
	public TableDataInfo subUserList(UserInfo userInfo) {
		if(ObjectUtil.isNotEmpty(userInfo.getParams().get("topUserId"))){
			UserInfo topUserInfo = xmsUserInfoService.getById(MapUtil.getLong(userInfo.getParams(), "topUserId"));
			userInfo.setInviteUserCode(topUserInfo.getUserCode());
		}
		startPage();
		List<UserInfo> list = xmsUserInfoService.selectChildUserInfoList(userInfo);
		return getDataTable(list);
	}

	/**
	 * 查询下级团队用户
	 *
	 * @param userInfo
	 * @return
	 */
	@PreAuthorize("@ss.hasPermi('xms:userinfo:childList')")
	@GetMapping("/childList")
	public TableDataInfo childList(UserInfo userInfo) {
		startPage();
		List<UserInfo> list = xmsUserInfoService.selectChildUserInfoList(userInfo);
		return getDataTable(list);
	}

	/**
	 * 导出团队用户信息列表
	 */
	@PreAuthorize("@ss.hasPermi('xms:userinfo:export')")
	@Log(title = "导出团队用户信息", businessType = BusinessType.EXPORT)
	@PostMapping("/childUserExport")
	public void childUserExport(HttpServletResponse response, UserInfo userInfo) {
		List<UserInfo> list = xmsUserInfoService.selectChildUserInfoList(userInfo);
		ExcelUtil<UserInfo> util = new ExcelUtil<UserInfo>(UserInfo.class);
		util.exportExcel(response, list, "团队用户信息");
	}

	/**
	 * 获取用户信息详细信息
	 */
	@PreAuthorize("@ss.hasPermi('xms:userinfo:query')")
	@GetMapping(value = "/{userId}")
	public AjaxResult getInfo(@PathVariable("userId") Long userId) {
		return success(xmsUserInfoService.getById(userId));
	}


	/**
	 * 修改用户信息
	 */
	@PreAuthorize("@ss.hasPermi('xms:userinfo:edit')")
	@Log(title = "用户信息", businessType = BusinessType.UPDATE)
	@PutMapping
	@RepeatSubmit
	public AjaxResult edit(@RequestBody UserInfoReqBo userInfo) {
		return xmsUserInfoService.updateUserInfo(userInfo);
	}





	/**
	 * 查询网体-树结构方法
	 *
	 */
	@GetMapping("/queryNetBody1")
	public AjaxResult queryNetBody1(@RequestParam(value = "userId",required = false) String userId) {
		return success(xmsUserInfoService.queryNetBody1(userId));
	}

	/**
	 * 关闭团队静态挖矿
	 *
	 */
	@GetMapping("/closeTeamMining")
	public AjaxResult closeTeamMining(@RequestParam(value = "userId",required = true) Long userId) {
		if(userId==null){
			throw new ServiceException("所选用户不能为空");
		}
		return success();
	}




	/**
	 * 关闭团队提现功能
	 *
	 */
	@GetMapping("/offTeamWithdrawal")
	public AjaxResult offTeamWithdrawal(@RequestParam(value = "userId",required = true) Long userId,
										@RequestParam(value = "bizType",required = true) Integer bizType) {
		if(userId==null){
			throw new ServiceException("所选用户不能为空");
		}
		if(bizType == null){
			throw new ServiceException("业务类型不能为空");
		}
		bizType =bizType == 0 ? 0:1;
		return success();
	}
}
