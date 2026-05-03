package com.xms.web.controller.xms;

import cn.hutool.core.util.StrUtil;
import com.xms.common.annotation.Log;
import com.xms.common.annotation.RepeatSubmit;
import com.xms.common.core.controller.BaseController;
import com.xms.common.core.domain.AjaxResult;
import com.xms.common.core.page.TableDataInfo;
import com.xms.common.enums.BusinessType;
import com.xms.common.utils.poi.ExcelUtil;
import com.xms.dao.entity.domain.UserInfo;
import com.xms.dao.entity.domain.UserMoney;
import com.xms.dao.entity.vo.UserMoneyVo;
import com.xms.dao.service.UserInfoService;
import com.xms.system.service.ISysUserService;
import com.xms.dao.service.IUserMoneyService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户钱包Controller
 *
 * @date 2023-07-31
 */
@RestController
@RequestMapping("/xms/usermoney")
public class UserMoneyController extends BaseController {
	@Autowired
	private IUserMoneyService userMoneyService;

	@Autowired
	private UserInfoService userInfoServiceImpl;

	@Autowired
	private ISysUserService userService;


	/**
	 * 查询用户钱包列表
	 */
	@PreAuthorize("@ss.hasPermi('xms:usermoney:list')")
	@GetMapping("/list")
	public TableDataInfo list(UserMoney userMoney) {
		if (!StrUtil.isBlank(userMoney.getAccount())) {
			UserInfo userInfo = userInfoServiceImpl.lambdaQuery()
				.eq(UserInfo::getAccount, userMoney.getAccount())
				.one();
			if(userInfo ==null){
				return null;
			}
			userMoney.setId(userInfo.getUserId());
		}

		startPage();
		List<UserMoney> list = userMoneyService.selectUserMoneyList(userMoney);
		return getDataTable(list);
	}

	/**
	 * 导出用户钱包列表
	 */
	@PreAuthorize("@ss.hasPermi('xms:usermoney:export')")
	@Log(title = "用户钱包", businessType = BusinessType.EXPORT)
	@PostMapping("/export")
	public void export(HttpServletResponse response, UserMoney userMoney) {
		if (!StrUtil.isBlank(userMoney.getAccount())) {
			UserInfo userInfo = userInfoServiceImpl.lambdaQuery()
				.eq(UserInfo::getAccount, userMoney.getAccount())
				.one();
			if(userInfo ==null){
				ExcelUtil<UserMoney> util = new ExcelUtil<UserMoney>(UserMoney.class);
				util.exportExcel(response, new ArrayList<>(), "用户钱包数据");
				return;
			}
			userMoney.setId(userInfo.getUserId());
		}
		List<UserMoney> list = userMoneyService.selectUserMoneyList(userMoney);
		ExcelUtil<UserMoney> util = new ExcelUtil<UserMoney>(UserMoney.class);
		util.exportExcel(response, list, "用户钱包数据");
	}

	/**
	 * 获取用户钱包详细信息
	 */
	@PreAuthorize("@ss.hasPermi('xms:usermoney:query')")
	@GetMapping(value = "/{id}")
	public AjaxResult getInfo(@PathVariable("id") Long id) {
		UserMoney byId = userMoneyService.getById(id);
		if(byId!=null){
			UserInfo userInfo = userInfoServiceImpl.lambdaQuery()
				.eq(UserInfo::getUserId, byId.getId())
				.select(UserInfo::getAccount)
				.one();
			byId.setAccount(userInfo.getAccount());
		}
		return success(byId);
	}
	/*

	 */
/**
 * 新增用户钱包
 *//*

	@PreAuthorize("@ss.hasPermi('xms:usermoney:add')")
	@Log(title = "用户钱包", businessType = BusinessType.INSERT)
	@PostMapping
	public AjaxResult add(@RequestBody UserMoney userMoney) {
		return toAjax(userMoneyService.save(userMoney));
	}
*/

	/**
	 * 修改用户钱包
	 */
	@PreAuthorize("@ss.hasPermi('xms:usermoney:edit')")
	@Log(title = "用户钱包", businessType = BusinessType.UPDATE)
	@PutMapping
	@RepeatSubmit
	public AjaxResult edit(@RequestBody UserMoneyVo userMoneyVo) {
		//谷歌验证码验证
		userService.pubValidate(userMoneyVo.getAutoCode());
		return toAjax(userMoneyService.updateUserMoney(userMoneyVo));
	}

	/*	*//**
	 * 删除用户钱包
	 *//*
	@PreAuthorize("@ss.hasPermi('xms:usermoney:remove')")
	@Log(title = "用户钱包", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
	public AjaxResult remove(@PathVariable Long[] ids) {
		return toAjax(userMoneyService.removeByIds(Arrays.asList(ids)));
	}*/
}
