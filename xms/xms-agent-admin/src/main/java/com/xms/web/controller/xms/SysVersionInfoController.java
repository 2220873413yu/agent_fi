package com.xms.web.controller.xms;

import com.xms.common.annotation.Log;
import com.xms.common.annotation.RepeatSubmit;
import com.xms.common.core.controller.BaseController;
import com.xms.common.core.domain.AjaxResult;
import com.xms.common.core.page.TableDataInfo;
import com.xms.common.enums.BusinessType;
import com.xms.common.utils.poi.ExcelUtil;
import com.xms.dao.entity.domain.SysVersionInfo;
import com.xms.web.service.ISysVersionInfoService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

/**
 * 版本Controller
 *
 *
 * @date 2023-07-31
 */
@RestController
@RequestMapping("/xms/sysversionInfo")
public class SysVersionInfoController extends BaseController
{
	@Autowired
	private ISysVersionInfoService sysVersionInfoService;

	/**
	 * 查询版本列表
	 */
	@PreAuthorize("@ss.hasPermi('xms:sysversionInfo:list')")
	@GetMapping("/list")
	public TableDataInfo list(SysVersionInfo sysVersionInfo)
	{
		startPage();
		List<SysVersionInfo> list = sysVersionInfoService.selectSysVersionInfoList(sysVersionInfo);
		return getDataTable(list);
	}

	/**
	 * 导出版本列表
	 */
	@PreAuthorize("@ss.hasPermi('xms:sysversionInfo:export')")
	@Log(title = "版本", businessType = BusinessType.EXPORT)
	@PostMapping("/export")
	public void export(HttpServletResponse response, SysVersionInfo sysVersionInfo)
	{
		List<SysVersionInfo> list = sysVersionInfoService.selectSysVersionInfoList(sysVersionInfo);
		ExcelUtil<SysVersionInfo> util = new ExcelUtil<SysVersionInfo>(SysVersionInfo.class);
		util.exportExcel(response, list, "版本数据");
	}

	/**
	 * 获取版本详细信息
	 */
	@PreAuthorize("@ss.hasPermi('xms:sysversionInfo:query')")
	@GetMapping(value = "/{id}")
	public AjaxResult getInfo(@PathVariable("id") Long id) {
		return success(sysVersionInfoService.getById(id));
	}

	/**
	 * 新增版本
	 */
	@PreAuthorize("@ss.hasPermi('xms:sysversionInfo:add')")
	@Log(title = "版本", businessType = BusinessType.INSERT)
	@PostMapping
	@RepeatSubmit
	public AjaxResult add(@RequestBody SysVersionInfo sysVersionInfo) {
		return toAjax(sysVersionInfoService.save(sysVersionInfo));
	}

	/**
	 * 修改版本
	 */
	@PreAuthorize("@ss.hasPermi('xms:sysversionInfo:edit')")
	@Log(title = "版本", businessType = BusinessType.UPDATE)
	@PutMapping
	@RepeatSubmit
	public AjaxResult edit(@RequestBody SysVersionInfo sysVersionInfo) {
		return toAjax(sysVersionInfoService.updateById(sysVersionInfo));
	}

	/**
	 * 删除版本
	 */
	@PreAuthorize("@ss.hasPermi('xms:sysversionInfo:remove')")
	@Log(title = "版本", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
	public AjaxResult remove(@PathVariable Long[] ids) {
		return toAjax(sysVersionInfoService.removeByIds(Arrays.asList(ids)));
	}
}
