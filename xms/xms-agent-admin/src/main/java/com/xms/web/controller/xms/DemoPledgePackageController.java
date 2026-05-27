package com.xms.web.controller.xms;

import com.xms.common.annotation.Log;
import com.xms.common.annotation.RepeatSubmit;
import com.xms.common.core.controller.BaseController;
import com.xms.common.core.domain.AjaxResult;
import com.xms.common.core.page.TableDataInfo;
import com.xms.common.enums.BusinessType;
import com.xms.common.utils.poi.ExcelUtil;
import com.xms.dao.domain.DemoPledgePackage;
import com.xms.dao.service.IDemoPledgePackageService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

/**
 * 示例质押套餐Controller。
 */
@RestController
@RequestMapping("/xms/demoPledgePackage")
public class DemoPledgePackageController extends BaseController {
	private final IDemoPledgePackageService demoPledgePackageService;

	public DemoPledgePackageController(IDemoPledgePackageService demoPledgePackageService) {
		this.demoPledgePackageService = demoPledgePackageService;
	}

	/**
	 * 查询示例质押套餐列表。
	 *
	 * @param demoPledgePackage 查询条件
	 * @return 分页套餐数据
	 */
	@PreAuthorize("@ss.hasPermi('xms:demoPledgePackage:list')")
	@GetMapping("/list")
	public TableDataInfo list(DemoPledgePackage demoPledgePackage) {
		startPage();
		List<DemoPledgePackage> list = demoPledgePackageService.selectDemoPledgePackageList(demoPledgePackage);
		return getDataTable(list);
	}

	/**
	 * 导出示例质押套餐列表。
	 *
	 * @param response HTTP响应
	 * @param demoPledgePackage 查询条件
	 */
	@PreAuthorize("@ss.hasPermi('xms:demoPledgePackage:export')")
	@Log(title = "示例质押套餐", businessType = BusinessType.EXPORT)
	@PostMapping("/export")
	public void export(HttpServletResponse response, DemoPledgePackage demoPledgePackage) {
		List<DemoPledgePackage> list = demoPledgePackageService.selectDemoPledgePackageList(demoPledgePackage);
		ExcelUtil<DemoPledgePackage> util = new ExcelUtil<>(DemoPledgePackage.class);
		util.exportExcel(response, list, "示例质押套餐数据");
	}

	/**
	 * 获取示例质押套餐详细信息。
	 *
	 * @param id 套餐ID
	 * @return 套餐详情
	 */
	@PreAuthorize("@ss.hasPermi('xms:demoPledgePackage:query')")
	@GetMapping(value = "/{id}")
	public AjaxResult getInfo(@PathVariable("id") Long id) {
		return success(demoPledgePackageService.getById(id));
	}

	/**
	 * 新增示例质押套餐。
	 *
	 * @param demoPledgePackage 套餐表单
	 * @return 操作结果
	 */
	@PreAuthorize("@ss.hasPermi('xms:demoPledgePackage:add')")
	@Log(title = "示例质押套餐", businessType = BusinessType.INSERT)
	@PostMapping
	@RepeatSubmit
	public AjaxResult add(@RequestBody DemoPledgePackage demoPledgePackage) {
		return toAjax(demoPledgePackageService.save(demoPledgePackage));
	}

	/**
	 * 修改示例质押套餐。
	 *
	 * @param demoPledgePackage 套餐表单
	 * @return 操作结果
	 */
	@PreAuthorize("@ss.hasPermi('xms:demoPledgePackage:edit')")
	@Log(title = "示例质押套餐", businessType = BusinessType.UPDATE)
	@PutMapping
	@RepeatSubmit
	public AjaxResult edit(@RequestBody DemoPledgePackage demoPledgePackage) {
		return toAjax(demoPledgePackageService.updateById(demoPledgePackage));
	}

	/**
	 * 删除示例质押套餐。
	 *
	 * @param ids 套餐ID数组
	 * @return 操作结果
	 */
	@PreAuthorize("@ss.hasPermi('xms:demoPledgePackage:remove')")
	@Log(title = "示例质押套餐", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
	public AjaxResult remove(@PathVariable Long[] ids) {
		return toAjax(demoPledgePackageService.removeByIds(Arrays.asList(ids)));
	}
}
