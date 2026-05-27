package com.xms.web.controller.xms;

import com.xms.common.annotation.Log;
import com.xms.common.annotation.RepeatSubmit;
import com.xms.common.core.controller.BaseController;
import com.xms.common.core.domain.AjaxResult;
import com.xms.common.core.page.TableDataInfo;
import com.xms.common.enums.BusinessType;
import com.xms.common.utils.poi.ExcelUtil;
import com.xms.dao.domain.DemoPledgeLevelConfig;
import com.xms.dao.service.IDemoPledgeLevelConfigService;
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
 * 示例质押等级配置Controller。
 */
@RestController
@RequestMapping("/xms/demoPledgeLevelConfig")
public class DemoPledgeLevelConfigController extends BaseController {
	private final IDemoPledgeLevelConfigService demoPledgeLevelConfigService;

	public DemoPledgeLevelConfigController(IDemoPledgeLevelConfigService demoPledgeLevelConfigService) {
		this.demoPledgeLevelConfigService = demoPledgeLevelConfigService;
	}

	/**
	 * 查询示例质押等级配置列表。
	 *
	 * @param demoPledgeLevelConfig 查询条件
	 * @return 分页等级配置数据
	 */
	@PreAuthorize("@ss.hasPermi('xms:demoPledgeLevelConfig:list')")
	@GetMapping("/list")
	public TableDataInfo list(DemoPledgeLevelConfig demoPledgeLevelConfig) {
		startPage();
		List<DemoPledgeLevelConfig> list = demoPledgeLevelConfigService.selectDemoPledgeLevelConfigList(demoPledgeLevelConfig);
		return getDataTable(list);
	}

	/**
	 * 导出示例质押等级配置列表。
	 *
	 * @param response HTTP响应
	 * @param demoPledgeLevelConfig 查询条件
	 */
	@PreAuthorize("@ss.hasPermi('xms:demoPledgeLevelConfig:export')")
	@Log(title = "示例质押等级配置", businessType = BusinessType.EXPORT)
	@PostMapping("/export")
	public void export(HttpServletResponse response, DemoPledgeLevelConfig demoPledgeLevelConfig) {
		List<DemoPledgeLevelConfig> list = demoPledgeLevelConfigService.selectDemoPledgeLevelConfigList(demoPledgeLevelConfig);
		ExcelUtil<DemoPledgeLevelConfig> util = new ExcelUtil<>(DemoPledgeLevelConfig.class);
		util.exportExcel(response, list, "示例质押等级配置数据");
	}

	/**
	 * 获取示例质押等级配置详情。
	 *
	 * @param id 配置ID
	 * @return 配置详情
	 */
	@PreAuthorize("@ss.hasPermi('xms:demoPledgeLevelConfig:query')")
	@GetMapping(value = "/{id}")
	public AjaxResult getInfo(@PathVariable("id") Long id) {
		return success(demoPledgeLevelConfigService.getById(id));
	}

	/**
	 * 新增示例质押等级配置。
	 *
	 * @param demoPledgeLevelConfig 等级配置表单
	 * @return 操作结果
	 */
	@PreAuthorize("@ss.hasPermi('xms:demoPledgeLevelConfig:add')")
	@Log(title = "示例质押等级配置", businessType = BusinessType.INSERT)
	@PostMapping
	@RepeatSubmit
	public AjaxResult add(@RequestBody DemoPledgeLevelConfig demoPledgeLevelConfig) {
		return toAjax(demoPledgeLevelConfigService.save(demoPledgeLevelConfig));
	}

	/**
	 * 修改示例质押等级配置。
	 *
	 * @param demoPledgeLevelConfig 等级配置表单
	 * @return 操作结果
	 */
	@PreAuthorize("@ss.hasPermi('xms:demoPledgeLevelConfig:edit')")
	@Log(title = "示例质押等级配置", businessType = BusinessType.UPDATE)
	@PutMapping
	@RepeatSubmit
	public AjaxResult edit(@RequestBody DemoPledgeLevelConfig demoPledgeLevelConfig) {
		return toAjax(demoPledgeLevelConfigService.updateById(demoPledgeLevelConfig));
	}

	/**
	 * 删除示例质押等级配置。
	 *
	 * @param ids 配置ID数组
	 * @return 操作结果
	 */
	@PreAuthorize("@ss.hasPermi('xms:demoPledgeLevelConfig:remove')")
	@Log(title = "示例质押等级配置", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
	public AjaxResult remove(@PathVariable Long[] ids) {
		return toAjax(demoPledgeLevelConfigService.removeByIds(Arrays.asList(ids)));
	}
}
