package com.xms.web.controller.xms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.xms.common.exception.ServiceException;
import com.xms.dao.domain.RechargeRecord;
import com.xms.dao.entity.domain.UserInfo;
import com.xms.dao.service.UserInfoService;
import com.xms.dao.service.UserMoneyLogService;
import jakarta.servlet.http.HttpServletResponse;

import com.xms.dao.entity.domain.UserMoneyLog;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.xms.common.annotation.Log;
import com.xms.common.core.controller.BaseController;
import com.xms.common.core.domain.AjaxResult;
import com.xms.common.enums.BusinessType;
import com.xms.common.utils.poi.ExcelUtil;
import com.xms.common.core.page.TableDataInfo;

/**
 * 钱包流水Controller
 *
 *
 * @date 2023-07-31
 */
@RestController
@RequestMapping("/xms/usermoneylog")
public class UserMoneyLogController extends BaseController
{
	@Autowired
	private UserMoneyLogService userMoneyLogService;

	@Autowired
	private UserInfoService userInfoService;
	/**
	 * 查询钱包流水列表
	 */
	@PreAuthorize("@ss.hasPermi('xms:usermoneylog:list')")
	@GetMapping("/list")
	public TableDataInfo list(UserMoneyLog userMoneyLog)
	{
		if(StrUtil.isNotBlank(userMoneyLog.getUserAccount())){
			UserInfo userInfo = userInfoService.lambdaQuery()
				.eq(UserInfo::getAccount, userMoneyLog.getUserAccount())
				.one();
			if(userInfo != null){
				userMoneyLog.setUserId(userInfo.getUserId());
			}else{
				return getDataTable(new ArrayList<>());
			}
		}

		startPage();
		List<UserMoneyLog> list = userMoneyLogService.selectUserMoneyLogList(userMoneyLog);

		if(CollectionUtil.isNotEmpty(list)){
			Map<Long, String> userAccountMap = userInfoService.getUserAccountById(list.stream().map(UserMoneyLog::getUserId).collect(Collectors.toList()));
			for (UserMoneyLog bankVo : list) {
				bankVo.setUserAccount(userAccountMap.get(bankVo.getUserId()));
			}
		}
		return getDataTable(list);
	}

	/**
	 * 导出钱包流水列表
	 */
	@PreAuthorize("@ss.hasPermi('xms:usermoneylog:export')")
	@Log(title = "钱包流水", businessType = BusinessType.EXPORT)
	@PostMapping("/export")
	public void export(HttpServletResponse response, UserMoneyLog userMoneyLog)
	{
		if(StrUtil.isNotBlank(userMoneyLog.getUserAccount())){
			UserInfo userInfo = userInfoService.lambdaQuery()
				.eq(UserInfo::getAccount, userMoneyLog.getUserAccount())
				.one();
			if(userInfo != null){
				userMoneyLog.setUserId(userInfo.getUserId());
			}else{
				ExcelUtil<UserMoneyLog> util = new ExcelUtil<UserMoneyLog>(UserMoneyLog.class);
				util.exportExcel(response, new ArrayList<>(), "钱包流水数据");
				return;
			}
		}
		List<UserMoneyLog> list = userMoneyLogService.selectUserMoneyLogList(userMoneyLog);
		if (list.size() > 1000000) {
			throw new ServiceException("导出数据上限100W,,请根据条件筛选");
		}

		if(CollectionUtil.isNotEmpty(list)){
			Map<Long, String> userAccountMap = userInfoService.getUserAccountById(list.stream().map(UserMoneyLog::getUserId).collect(Collectors.toList()));
			for (UserMoneyLog bankVo : list) {
				bankVo.setUserAccount(userAccountMap.get(bankVo.getUserId()));
			}
		}
		ExcelUtil<UserMoneyLog> util = new ExcelUtil<UserMoneyLog>(UserMoneyLog.class);
		util.exportExcel(response, list, "钱包流水数据");
	}

	/**
	 * 获取钱包流水详细信息
	 */
	@PreAuthorize("@ss.hasPermi('xms:usermoneylog:query')")
	@GetMapping(value = "/{id}")
	public AjaxResult getInfo(@PathVariable("id") Long id) {
		return success(userMoneyLogService.getById(id));
	}

	/**
	 * 新增钱包流水
	 */
	@PreAuthorize("@ss.hasPermi('xms:usermoneylog:add')")
	@Log(title = "钱包流水", businessType = BusinessType.INSERT)
	@PostMapping
	public AjaxResult add(@RequestBody UserMoneyLog userMoneyLog) {
		return toAjax(userMoneyLogService.save(userMoneyLog));
	}

	/**
	 * 修改钱包流水
	 */
	@PreAuthorize("@ss.hasPermi('xms:usermoneylog:edit')")
	@Log(title = "钱包流水", businessType = BusinessType.UPDATE)
	@PutMapping
	public AjaxResult edit(@RequestBody UserMoneyLog userMoneyLog) {
		return toAjax(userMoneyLogService.updateById(userMoneyLog));
	}

	/**
	 * 删除钱包流水
	 */
	@PreAuthorize("@ss.hasPermi('xms:usermoneylog:remove')")
	@Log(title = "钱包流水", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
	public AjaxResult remove(@PathVariable Long[] ids) {
		return toAjax(userMoneyLogService.removeByIds(Arrays.asList(ids)));
	}
}
