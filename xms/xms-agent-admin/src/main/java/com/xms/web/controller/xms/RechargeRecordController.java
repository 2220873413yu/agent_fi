package com.xms.web.controller.xms;

import java.util.List;
import com.xms.common.annotation.RepeatSubmit;
import com.xms.dao.service.UserInfoService;
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
import com.xms.dao.domain.RechargeRecord;
import com.xms.dao.service.IRechargeRecordService;
import com.xms.common.utils.poi.ExcelUtil;
import com.xms.common.core.page.TableDataInfo;

/**
 * 充值记录Controller
 *
 * @author xms
 * @date 2025-03-12
 */
@RestController
@RequestMapping("/xms/rechargeRecord")
public class RechargeRecordController extends BaseController
{
    @Autowired
    private IRechargeRecordService rechargeRecordService;

	@Autowired
	private UserInfoService userInfoService;

    /**
     * 查询充值记录列表
     */
    @PreAuthorize("@ss.hasPermi('xms:rechargeRecord:list')")
    @GetMapping("/list")
    public TableDataInfo list(RechargeRecord rechargeRecord)
    {

        startPage();
        List<RechargeRecord> list = rechargeRecordService.selectRechargeRecordList(rechargeRecord);
        return getDataTable(list);
    }

    /**
     * 导出充值记录列表
     */
    @PreAuthorize("@ss.hasPermi('xms:rechargeRecord:export')")
    @Log(title = "充值记录", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, RechargeRecord rechargeRecord)
    {
        List<RechargeRecord> list = rechargeRecordService.selectRechargeRecordList(rechargeRecord);
        ExcelUtil<RechargeRecord> util = new ExcelUtil<RechargeRecord>(RechargeRecord.class);
        util.exportExcel(response, list, "充值记录数据");
    }

    /**
     * 获取充值记录详细信息
     */
    @PreAuthorize("@ss.hasPermi('xms:rechargeRecord:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id) {
        return success(rechargeRecordService.getById(id));
    }
/*
    *//**
     * 新增充值记录
     *//*
    @PreAuthorize("@ss.hasPermi('xms:rechargeRecord:add')")
    @Log(title = "充值记录", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody RechargeRecord rechargeRecord) {
        return toAjax(rechargeRecordService.save(rechargeRecord));
    }*/

    /**
     * 修改充值记录
     */
    @PreAuthorize("@ss.hasPermi('xms:rechargeRecord:edit')")
    @Log(title = "充值记录", businessType = BusinessType.UPDATE)
    @PutMapping
	@RepeatSubmit
    public AjaxResult edit(@RequestBody RechargeRecord rechargeRecord) {
        return toAjax(rechargeRecordService.updateRecordById(rechargeRecord));
    }

/*    *//**
     * 删除充值记录
     *//*
    @PreAuthorize("@ss.hasPermi('xms:rechargeRecord:remove')")
    @Log(title = "充值记录", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable String[] ids) {
        return toAjax(rechargeRecordService.removeByIds(Arrays.asList(ids)));
    }*/
}
