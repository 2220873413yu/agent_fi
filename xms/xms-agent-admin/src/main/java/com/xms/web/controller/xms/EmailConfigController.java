package com.xms.web.controller.xms;

import java.util.Arrays;
import java.util.List;

import cn.hutool.core.lang.Validator;
import com.xms.common.annotation.RepeatSubmit;
import com.xms.common.config.redis.XmsRedis;
import com.xms.common.config.redis.delayqueue.config.RedissonTemplate;
import com.xms.common.constant.RedisConstant;
import com.xms.common.exception.ServiceException;
import jakarta.servlet.http.HttpServletResponse;
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
import com.xms.dao.domain.EmailConfig;
import com.xms.dao.service.IEmailConfigService;
import com.xms.common.utils.poi.ExcelUtil;
import com.xms.common.core.page.TableDataInfo;

/**
 * 谷歌邮箱配置Controller
 *
 * @author xms
 * @date 2025-09-18
 */
@RestController
@RequestMapping("/xms/emailConfig")
public class EmailConfigController extends BaseController
{
    @Autowired
    private IEmailConfigService emailConfigService;

	@Autowired
	private XmsRedis xmsRedis;

	@Autowired
	private RedissonTemplate redissonTemplate;
	/**
	 * 查询谷歌邮箱配置列表
	 */
	@PreAuthorize("@ss.hasPermi('xms:emailConfig:list')")
	@GetMapping("/list")
    public TableDataInfo list(EmailConfig emailConfig)
    {
        startPage();
        List<EmailConfig> list = emailConfigService.selectEmailConfigList(emailConfig);
        return getDataTable(list);
    }

    /**
     * 导出谷歌邮箱配置列表
     */
    @PreAuthorize("@ss.hasPermi('xms:emailConfig:export')")
    @Log(title = "谷歌邮箱配置", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, EmailConfig emailConfig)
    {
        List<EmailConfig> list = emailConfigService.selectEmailConfigList(emailConfig);
        ExcelUtil<EmailConfig> util = new ExcelUtil<EmailConfig>(EmailConfig.class);
        util.exportExcel(response, list, "谷歌邮箱配置数据");
    }

    /**
     * 获取谷歌邮箱配置详细信息
     */
    @PreAuthorize("@ss.hasPermi('xms:emailConfig:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(emailConfigService.getById(id));
    }

    /**
     * 新增谷歌邮箱配置
     */
    @PreAuthorize("@ss.hasPermi('xms:emailConfig:add')")
    @Log(title = "谷歌邮箱配置", businessType = BusinessType.INSERT)
    @PostMapping
    @RepeatSubmit
    public AjaxResult add(@RequestBody EmailConfig emailConfig) {
		if (!Validator.isEmail(emailConfig.getEmail())) {
			throw new ServiceException("邮箱格式不正确");
		}
		emailConfigService.save(emailConfig);
		xmsRedis.del(RedisConstant.GOOGLE_EMAIL_LIST);
		redissonTemplate.sendCleanCacheWithDelay(RedisConstant.GOOGLE_EMAIL_LIST);
        return toAjax(1);
    }

    /**
     * 修改谷歌邮箱配置
     */
    @PreAuthorize("@ss.hasPermi('xms:emailConfig:edit')")
    @Log(title = "谷歌邮箱配置", businessType = BusinessType.UPDATE)
    @PutMapping
    @RepeatSubmit
    public AjaxResult edit(@RequestBody EmailConfig emailConfig) {
		if (!Validator.isEmail(emailConfig.getEmail())) {
			throw new ServiceException("邮箱格式不正确");
		}
		boolean b = emailConfigService.updateById(emailConfig);
		xmsRedis.del(RedisConstant.GOOGLE_EMAIL_LIST);
		redissonTemplate.sendCleanCacheWithDelay(RedisConstant.GOOGLE_EMAIL_LIST);
        return toAjax(b);
    }

    /**
     * 删除谷歌邮箱配置
     */
    @PreAuthorize("@ss.hasPermi('xms:emailConfig:remove')")
    @Log(title = "谷歌邮箱配置", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
	@RepeatSubmit
    public AjaxResult remove(@PathVariable Long[] ids) {
		emailConfigService.deleteRecordById(Arrays.asList(ids));
		xmsRedis.del(RedisConstant.GOOGLE_EMAIL_LIST);
		redissonTemplate.sendCleanCacheWithDelay(RedisConstant.GOOGLE_EMAIL_LIST);
        return toAjax(1);
    }
}
