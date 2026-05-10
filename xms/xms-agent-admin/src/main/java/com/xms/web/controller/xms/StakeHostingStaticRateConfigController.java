package com.xms.web.controller.xms;

import com.xms.common.annotation.Log;
import com.xms.common.annotation.RepeatSubmit;
import com.xms.common.core.controller.BaseController;
import com.xms.common.core.domain.AjaxResult;
import com.xms.common.core.page.TableDataInfo;
import com.xms.common.enums.BusinessType;
import com.xms.common.exception.ServiceException;
import com.xms.common.utils.poi.ExcelUtil;
import com.xms.dao.domain.StakeHostingStaticRateConfig;
import com.xms.dao.service.IStakeHostingStaticRateConfigService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

/**
 * 托管G7静态收益率区间配置Controller
 *
 * @author xms
 */
@RestController
@RequestMapping("/xms/stakeHostingStaticRateConfig")
public class StakeHostingStaticRateConfigController extends BaseController {
	private final IStakeHostingStaticRateConfigService configService;

	public StakeHostingStaticRateConfigController(IStakeHostingStaticRateConfigService configService) {
		this.configService = configService;
	}

	/**
	 * 查询托管G7静态收益率区间配置列表。
	 *
	 * @param config 查询条件
	 * @return 分页列表
	 */
	@PreAuthorize("@ss.hasPermi('xms:stakeHostingStaticRateConfig:list')")
	@GetMapping("/list")
	public TableDataInfo list(StakeHostingStaticRateConfig config) {
		startPage();
		List<StakeHostingStaticRateConfig> list = configService.selectStakeHostingStaticRateConfigList(config);
		return getDataTable(list);
	}

	/**
	 * 导出托管G7静态收益率区间配置列表。
	 *
	 * @param response HTTP响应
	 * @param config 查询条件
	 */
	@PreAuthorize("@ss.hasPermi('xms:stakeHostingStaticRateConfig:export')")
	@Log(title = "G7静态收益率配置", businessType = BusinessType.EXPORT)
	@PostMapping("/export")
	public void export(HttpServletResponse response, StakeHostingStaticRateConfig config) {
		List<StakeHostingStaticRateConfig> list = configService.selectStakeHostingStaticRateConfigList(config);
		ExcelUtil<StakeHostingStaticRateConfig> util = new ExcelUtil<>(StakeHostingStaticRateConfig.class);
		util.exportExcel(response, list, "G7静态收益率配置数据");
	}

	/**
	 * 获取托管G7静态收益率区间配置详情。
	 *
	 * @param id 配置ID
	 * @return 配置详情
	 */
	@PreAuthorize("@ss.hasPermi('xms:stakeHostingStaticRateConfig:query')")
	@GetMapping(value = "/{id}")
	public AjaxResult getInfo(@PathVariable("id") Long id) {
		return success(configService.getById(id));
	}

	/**
	 * 新增托管G7静态收益率区间配置。
	 *
	 * @param config 配置参数
	 * @return 操作结果
	 */
	@PreAuthorize("@ss.hasPermi('xms:stakeHostingStaticRateConfig:add')")
	@Log(title = "G7静态收益率配置", businessType = BusinessType.INSERT)
	@PostMapping
	@RepeatSubmit
	public AjaxResult add(@RequestBody StakeHostingStaticRateConfig config) {
		validateConfig(config);
		return toAjax(configService.save(config));
	}

	/**
	 * 修改托管G7静态收益率区间配置。
	 *
	 * @param config 配置参数
	 * @return 操作结果
	 */
	@PreAuthorize("@ss.hasPermi('xms:stakeHostingStaticRateConfig:edit')")
	@Log(title = "G7静态收益率配置", businessType = BusinessType.UPDATE)
	@PutMapping
	@RepeatSubmit
	public AjaxResult edit(@RequestBody StakeHostingStaticRateConfig config) {
		validateConfig(config);
		return toAjax(configService.updateById(config));
	}

	/**
	 * 删除托管G7静态收益率区间配置。
	 *
	 * @param ids 配置ID数组
	 * @return 操作结果
	 */
	@PreAuthorize("@ss.hasPermi('xms:stakeHostingStaticRateConfig:remove')")
	@Log(title = "G7静态收益率配置", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
	public AjaxResult remove(@PathVariable Long[] ids) {
		return toAjax(configService.removeByIds(Arrays.asList(ids)));
	}

	/**
	 * 校验托管G7静态收益率区间配置的数值边界。
	 *
	 * @param config 配置参数
	 */
	private void validateConfig(StakeHostingStaticRateConfig config) {
		if (config.getMinG() == null) {
			throw new ServiceException("Gsmooth下限不能为空");
		}
		if (config.getMaxG() != null && config.getMaxG().compareTo(config.getMinG()) <= 0) {
			throw new ServiceException("Gsmooth上限必须大于下限");
		}
		if (config.getStaticRate() == null || config.getStaticRate().compareTo(BigDecimal.ZERO) < 0) {
			throw new ServiceException("日化静态收益率不能小于0");
		}
		if (config.getStatus() == null) {
			throw new ServiceException("状态不能为空");
		}
	}
}
