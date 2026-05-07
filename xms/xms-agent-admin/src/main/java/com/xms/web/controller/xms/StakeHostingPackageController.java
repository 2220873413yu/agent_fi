package com.xms.web.controller.xms;

import com.xms.common.annotation.Log;
import com.xms.common.annotation.RepeatSubmit;
import com.xms.common.core.controller.BaseController;
import com.xms.common.core.domain.AjaxResult;
import com.xms.common.core.page.TableDataInfo;
import com.xms.common.enums.BusinessType;
import com.xms.common.utils.poi.ExcelUtil;
import com.xms.dao.domain.StakeHostingPackage;
import com.xms.dao.service.IStakeHostingPackageService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

/**
 * 托管套餐Controller
 *
 * @author xms
 */
@RestController
@RequestMapping("/xms/stakeHostingPackage")
public class StakeHostingPackageController extends BaseController {
	private final IStakeHostingPackageService stakeHostingPackageService;

	public StakeHostingPackageController(IStakeHostingPackageService stakeHostingPackageService) {
		this.stakeHostingPackageService = stakeHostingPackageService;
	}

	/**
	 * 查询托管套餐列表
	 */
	@PreAuthorize("@ss.hasPermi('xms:stakeHostingPackage:list')")
	@GetMapping("/list")
	public TableDataInfo list(StakeHostingPackage stakeHostingPackage) {
		startPage();
		List<StakeHostingPackage> list = stakeHostingPackageService.selectStakeHostingPackageList(stakeHostingPackage);
		return getDataTable(list);
	}

	/**
	 * 导出托管套餐列表
	 */
	@PreAuthorize("@ss.hasPermi('xms:stakeHostingPackage:export')")
	@Log(title = "托管套餐", businessType = BusinessType.EXPORT)
	@PostMapping("/export")
	public void export(HttpServletResponse response, StakeHostingPackage stakeHostingPackage) {
		List<StakeHostingPackage> list = stakeHostingPackageService.selectStakeHostingPackageList(stakeHostingPackage);
		ExcelUtil<StakeHostingPackage> util = new ExcelUtil<>(StakeHostingPackage.class);
		util.exportExcel(response, list, "托管套餐数据");
	}

	/**
	 * 获取托管套餐详细信息
	 */
	@PreAuthorize("@ss.hasPermi('xms:stakeHostingPackage:query')")
	@GetMapping(value = "/{id}")
	public AjaxResult getInfo(@PathVariable("id") Long id) {
		return success(stakeHostingPackageService.getById(id));
	}

	/**
	 * 新增托管套餐
	 */
	@PreAuthorize("@ss.hasPermi('xms:stakeHostingPackage:add')")
	@Log(title = "托管套餐", businessType = BusinessType.INSERT)
	@PostMapping
	@RepeatSubmit
	public AjaxResult add(@RequestBody StakeHostingPackage stakeHostingPackage) {
		return toAjax(stakeHostingPackageService.save(stakeHostingPackage));
	}

	/**
	 * 修改托管套餐
	 */
	@PreAuthorize("@ss.hasPermi('xms:stakeHostingPackage:edit')")
	@Log(title = "托管套餐", businessType = BusinessType.UPDATE)
	@PutMapping
	@RepeatSubmit
	public AjaxResult edit(@RequestBody StakeHostingPackage stakeHostingPackage) {
		return toAjax(stakeHostingPackageService.updateById(stakeHostingPackage));
	}

	/**
	 * 删除托管套餐
	 */
	@PreAuthorize("@ss.hasPermi('xms:stakeHostingPackage:remove')")
	@Log(title = "托管套餐", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
	public AjaxResult remove(@PathVariable Long[] ids) {
		return toAjax(stakeHostingPackageService.removeByIds(Arrays.asList(ids)));
	}
}
