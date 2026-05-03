package com.xms.web.controller.xms;

import com.xms.common.annotation.Log;
import com.xms.common.annotation.RepeatSubmit;
import com.xms.common.constant.RedisConstant;
import com.xms.common.core.controller.BaseController;
import com.xms.common.core.domain.AjaxResult;
import com.xms.common.core.page.TableDataInfo;
import com.xms.common.enums.BusinessType;
import com.xms.common.utils.poi.ExcelUtil;
import com.xms.dao.entity.domain.Banner;
import com.xms.dao.service.impl.MqSendTemplate;
import com.xms.web.service.IBannerService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

/**
 * appBanner图Controller
 *
 * @date 2023-08-01
 */
@RestController
@RequestMapping("/xms/banner")
public class BannerController extends BaseController {
	@Autowired
	private IBannerService bannerService;
	@Autowired
	private MqSendTemplate mqSendTemplate;

	/**
	 * 查询appBanner图列表
	 */
	@PreAuthorize("@ss.hasPermi('xms:banner:list')")
	@GetMapping("/list")
	public TableDataInfo list(Banner banner) {
		startPage();
		List<Banner> list = bannerService.selectBannerList(banner);
		return getDataTable(list);
	}

	/**
	 * 导出appBanner图列表
	 */
	@PreAuthorize("@ss.hasPermi('xms:banner:export')")
	@Log(title = "appBanner图", businessType = BusinessType.EXPORT)
	@PostMapping("/export")
	public void export(HttpServletResponse response, Banner banner) {
		List<Banner> list = bannerService.selectBannerList(banner);
		ExcelUtil<Banner> util = new ExcelUtil<Banner>(Banner.class);
		util.exportExcel(response, list, "appBanner图数据");
	}

	/**
	 * 获取appBanner图详细信息
	 */
	@PreAuthorize("@ss.hasPermi('xms:banner:query')")
	@GetMapping(value = "/{id}")
	public AjaxResult getInfo(@PathVariable("id") Long id) {
		return success(bannerService.getById(id));
	}

	/**
	 * 新增appBanner图
	 */
	@PreAuthorize("@ss.hasPermi('xms:banner:add')")
	@Log(title = "appBanner图", businessType = BusinessType.INSERT)
	@PostMapping
	@RepeatSubmit
	public AjaxResult add(@RequestBody Banner banner) {
		boolean save = bannerService.save(banner);
//		mqSendTemplate.delayDoubleDelCache(RedisConstant.XMS_SYS_BANNER+ SysConstant.ONE);
//		mqSendTemplate.delayDoubleDelCache(RedisConstant.XMS_SYS_BANNER+ SysConstant.ZERO);
//		mqSendTemplate.delayDoubleDelCache(RedisConstant.XMS_SYS_BANNER+ SysConstant.TWO);
//		mqSendTemplate.delayDoubleDelCache(RedisConstant.XMS_SYS_BANNER+ SysConstant.THREE);
		return toAjax(save);
	}

	/**
	 * 修改appBanner图
	 */
	@PreAuthorize("@ss.hasPermi('xms:banner:edit')")
	@Log(title = "appBanner图", businessType = BusinessType.UPDATE)
	@PutMapping
	@RepeatSubmit
	public AjaxResult edit(@RequestBody Banner banner) {
		boolean b = bannerService.updateById(banner);
		mqSendTemplate.delayDoubleDelCache(RedisConstant.XMS_SYS_BANNER);
		return toAjax(b);
	}

	/**
	 * 删除appBanner图
	 */
	@PreAuthorize("@ss.hasPermi('xms:banner:remove')")
	@Log(title = "appBanner图", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
	public AjaxResult remove(@PathVariable Long[] ids) {
		boolean b = bannerService.removeByIds(Arrays.asList(ids));
		mqSendTemplate.delayDoubleDelCache(RedisConstant.XMS_SYS_BANNER);
		return toAjax(b);
	}
}
