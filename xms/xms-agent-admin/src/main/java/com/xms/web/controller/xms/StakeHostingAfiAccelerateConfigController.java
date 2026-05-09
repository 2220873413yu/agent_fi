package com.xms.web.controller.xms;

import com.xms.common.annotation.Log;
import com.xms.common.annotation.RepeatSubmit;
import com.xms.common.core.controller.BaseController;
import com.xms.common.core.domain.AjaxResult;
import com.xms.common.core.page.TableDataInfo;
import com.xms.common.enums.BusinessType;
import com.xms.common.utils.poi.ExcelUtil;
import com.xms.dao.domain.StakeHostingAfiAccelerateConfig;
import com.xms.dao.service.IStakeHostingAfiAccelerateConfigService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

/**
 * AFI质押加速配置Controller
 *
 * @author xms
 */
@RestController
@RequestMapping("/xms/stakeHostingAfiAccelerateConfig")
public class StakeHostingAfiAccelerateConfigController extends BaseController {
	private final IStakeHostingAfiAccelerateConfigService configService;

	public StakeHostingAfiAccelerateConfigController(IStakeHostingAfiAccelerateConfigService configService) {
		this.configService = configService;
	}

	@PreAuthorize("@ss.hasPermi('xms:stakeHostingAfiAccelerateConfig:list')")
	@GetMapping("/list")
	public TableDataInfo list(StakeHostingAfiAccelerateConfig config) {
		startPage();
		List<StakeHostingAfiAccelerateConfig> list = configService.selectStakeHostingAfiAccelerateConfigList(config);
		return getDataTable(list);
	}

	@PreAuthorize("@ss.hasPermi('xms:stakeHostingAfiAccelerateConfig:export')")
	@Log(title = "AFI质押加速配置", businessType = BusinessType.EXPORT)
	@PostMapping("/export")
	public void export(HttpServletResponse response, StakeHostingAfiAccelerateConfig config) {
		List<StakeHostingAfiAccelerateConfig> list = configService.selectStakeHostingAfiAccelerateConfigList(config);
		ExcelUtil<StakeHostingAfiAccelerateConfig> util = new ExcelUtil<>(StakeHostingAfiAccelerateConfig.class);
		util.exportExcel(response, list, "AFI质押加速配置数据");
	}

	@PreAuthorize("@ss.hasPermi('xms:stakeHostingAfiAccelerateConfig:query')")
	@GetMapping(value = "/{id}")
	public AjaxResult getInfo(@PathVariable("id") Long id) {
		return success(configService.getById(id));
	}

	@PreAuthorize("@ss.hasPermi('xms:stakeHostingAfiAccelerateConfig:add')")
	@Log(title = "AFI质押加速配置", businessType = BusinessType.INSERT)
	@PostMapping
	@RepeatSubmit
	public AjaxResult add(@RequestBody StakeHostingAfiAccelerateConfig config) {
		return toAjax(configService.save(config));
	}

	@PreAuthorize("@ss.hasPermi('xms:stakeHostingAfiAccelerateConfig:edit')")
	@Log(title = "AFI质押加速配置", businessType = BusinessType.UPDATE)
	@PutMapping
	@RepeatSubmit
	public AjaxResult edit(@RequestBody StakeHostingAfiAccelerateConfig config) {
		return toAjax(configService.updateById(config));
	}

	@PreAuthorize("@ss.hasPermi('xms:stakeHostingAfiAccelerateConfig:remove')")
	@Log(title = "AFI质押加速配置", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
	public AjaxResult remove(@PathVariable Long[] ids) {
		return toAjax(configService.removeByIds(Arrays.asList(ids)));
	}
}
