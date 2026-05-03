package com.xms.web.controller.xms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.xms.common.annotation.RepeatSubmit;
import com.xms.dao.entity.domain.UserInfo;
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
import com.xms.dao.domain.AssetTransferRecord;
import com.xms.dao.service.IAssetTransferRecordService;
import com.xms.common.utils.poi.ExcelUtil;
import com.xms.common.core.page.TableDataInfo;

/**
 * DF划转记录Controller
 *
 * @author xms
 * @date 2026-02-25
 */
@RestController
@RequestMapping("/xms/assetTransferRecord")
public class AssetTransferRecordController extends BaseController
{
    @Autowired
    private IAssetTransferRecordService assetTransferRecordService;

	@Autowired
	private UserInfoService userInfoService;

/**
 * 查询DF划转记录列表
 */
@PreAuthorize("@ss.hasPermi('xms:assetTransferRecord:list')")
@GetMapping("/list")
    public TableDataInfo list(AssetTransferRecord assetTransferRecord)
    {


        startPage();
        List<AssetTransferRecord> list = assetTransferRecordService.selectAssetTransferRecordList(assetTransferRecord);


		return getDataTable(list);
    }

    /**
     * 导出DF划转记录列表
     */
    @PreAuthorize("@ss.hasPermi('xms:assetTransferRecord:export')")
    @Log(title = "DF划转记录", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, AssetTransferRecord assetTransferRecord)
    {
        List<AssetTransferRecord> list = assetTransferRecordService.selectAssetTransferRecordList(assetTransferRecord);
        ExcelUtil<AssetTransferRecord> util = new ExcelUtil<AssetTransferRecord>(AssetTransferRecord.class);
        util.exportExcel(response, list, "DF划转记录数据");
    }

    /**
     * 获取DF划转记录详细信息
     */
    @PreAuthorize("@ss.hasPermi('xms:assetTransferRecord:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id) {
        return success(assetTransferRecordService.getById(id));
    }

    /**
     * 新增DF划转记录
     */
    @PreAuthorize("@ss.hasPermi('xms:assetTransferRecord:add')")
    @Log(title = "DF划转记录", businessType = BusinessType.INSERT)
    @PostMapping
    @RepeatSubmit
    public AjaxResult add(@RequestBody AssetTransferRecord assetTransferRecord) {
        return toAjax(assetTransferRecordService.save(assetTransferRecord));
    }

    /**
     * 修改DF划转记录
     */
    @PreAuthorize("@ss.hasPermi('xms:assetTransferRecord:edit')")
    @Log(title = "DF划转记录", businessType = BusinessType.UPDATE)
    @PutMapping
    @RepeatSubmit
    public AjaxResult edit(@RequestBody AssetTransferRecord assetTransferRecord) {
        return toAjax(assetTransferRecordService.updateById(assetTransferRecord));
    }

    /**
     * 删除DF划转记录
     */
    @PreAuthorize("@ss.hasPermi('xms:assetTransferRecord:remove')")
    @Log(title = "DF划转记录", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable String[] ids) {
        return toAjax(assetTransferRecordService.removeByIds(Arrays.asList(ids)));
    }
}
