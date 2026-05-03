package com.xms.web.controller.xms;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.xms.system.service.ISysUserService;
import com.xms.web.service.BackExclusiveService;
import jakarta.servlet.http.HttpServletResponse;

import com.xms.common.annotation.RepeatSubmit;
import com.xms.common.constant.RedisConstant;
import com.xms.dao.entity.domain.SysPara;
import com.xms.dao.service.ISysParaService;
import org.springframework.data.redis.core.RedisTemplate;
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
 * 系统参数Controller
 *
 *
 * @date 2023-08-01
 */
@RestController
@RequestMapping("/xms/syspara")
public class SysParaController extends BaseController
{
	@Autowired
	private ISysParaService sysParaService;
	@Autowired
	private BackExclusiveService backExclusiveServiceImpl;

	@Autowired
	private RedisTemplate<String, String> redisTemplate;

	@Autowired
	private ISysUserService sysUserService;
	/**
	 * 查询系统参数列表
	 */
	@PreAuthorize("@ss.hasPermi('xms:syspara:list')")
	@GetMapping("/list")
	public TableDataInfo list(SysPara sysPara)
	{
		startPage();
		List<SysPara> list = sysParaService.selectSysParaList(sysPara);
		return getDataTable(list);
	}

	/**
	 * 导出系统参数列表
	 */
	@PreAuthorize("@ss.hasPermi('xms:syspara:export')")
	@Log(title = "系统参数", businessType = BusinessType.EXPORT)
	@PostMapping("/export")
	public void export(HttpServletResponse response, SysPara sysPara)
	{
		List<SysPara> list = sysParaService.selectSysParaList(sysPara);
		ExcelUtil<SysPara> util = new ExcelUtil<SysPara>(SysPara.class);
		util.exportExcel(response, list, "系统参数数据");
	}

	/**
	 * 获取系统参数详细信息
	 */
	@PreAuthorize("@ss.hasPermi('xms:syspara:query')")
	@GetMapping(value = "/{sysParaId}")
	public AjaxResult getInfo(@PathVariable("sysParaId") Long sysParaId) {
		return success(sysParaService.getById(sysParaId));
	}

	/**
	 * 新增系统参数
	 */
	@PreAuthorize("@ss.hasPermi('xms:syspara:add')")
	@Log(title = "系统参数", businessType = BusinessType.INSERT)
	@PostMapping
	public AjaxResult add(@RequestBody SysPara sysPara) {
		return toAjax(sysParaService.save(sysPara));
	}

	/**
	 * 修改系统参数
	 */
	@PreAuthorize("@ss.hasPermi('xms:syspara:edit')")
	@Log(title = "系统参数", businessType = BusinessType.UPDATE)
	@PutMapping
	@RepeatSubmit
	public AjaxResult edit(@RequestBody SysPara sysPara)throws  Exception {
		//验证码验证
		sysUserService.pubValidate(sysPara.getAutoCode());
		return toAjax(backExclusiveServiceImpl.updateSysPara(sysPara));
	}

	/**
	 * 删除系统参数
	 */
	@PreAuthorize("@ss.hasPermi('xms:syspara:remove')")
	@Log(title = "系统参数", businessType = BusinessType.DELETE)
	@DeleteMapping("/{sysParaIds}")
	public AjaxResult remove(@PathVariable Long[] sysParaIds) {
		Collection<String> cacheKeys = redisTemplate.keys(RedisConstant.XMS_PARAM + "*");
		redisTemplate.delete(cacheKeys);
		return toAjax(sysParaService.removeByIds(Arrays.asList(sysParaIds)));
	}
}
