package com.xms.web.controller.xms;

import java.util.Arrays;
import java.util.List;

import com.xms.common.annotation.RepeatSubmit;
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
import com.xms.dao.domain.StakeReleaseBucket;
import com.xms.dao.service.IStakeReleaseBucketService;
import com.xms.common.utils.poi.ExcelUtil;
import com.xms.common.core.page.TableDataInfo;

/**
 * 质押收益线性释放Controller
 *
 * @author xms
 * @date 2026-02-27
 */
@RestController
@RequestMapping("/xms/stakeReleaseBucket")
public class StakeReleaseBucketController extends BaseController
{
    @Autowired
    private IStakeReleaseBucketService stakeReleaseBucketService;

/**
 * 查询质押收益线性释放列表
 */
@PreAuthorize("@ss.hasPermi('xms:stakeReleaseBucket:list')")
@GetMapping("/list")
    public TableDataInfo list(StakeReleaseBucket stakeReleaseBucket)
    {
        startPage();
        List<StakeReleaseBucket> list = stakeReleaseBucketService.selectStakeReleaseBucketList(stakeReleaseBucket);
        return getDataTable(list);
    }

    /**
     * 导出质押收益线性释放列表
     */
    @PreAuthorize("@ss.hasPermi('xms:stakeReleaseBucket:export')")
    @Log(title = "质押收益线性释放", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, StakeReleaseBucket stakeReleaseBucket)
    {
        List<StakeReleaseBucket> list = stakeReleaseBucketService.selectStakeReleaseBucketList(stakeReleaseBucket);
        ExcelUtil<StakeReleaseBucket> util = new ExcelUtil<StakeReleaseBucket>(StakeReleaseBucket.class);
        util.exportExcel(response, list, "质押收益线性释放数据");
    }

    /**
     * 获取质押收益线性释放详细信息
     */
    @PreAuthorize("@ss.hasPermi('xms:stakeReleaseBucket:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(stakeReleaseBucketService.getById(id));
    }

    /**
     * 新增质押收益线性释放
     */
    @PreAuthorize("@ss.hasPermi('xms:stakeReleaseBucket:add')")
    @Log(title = "质押收益线性释放", businessType = BusinessType.INSERT)
    @PostMapping
    @RepeatSubmit
    public AjaxResult add(@RequestBody StakeReleaseBucket stakeReleaseBucket) {
        return toAjax(stakeReleaseBucketService.save(stakeReleaseBucket));
    }

    /**
     * 修改质押收益线性释放
     */
    @PreAuthorize("@ss.hasPermi('xms:stakeReleaseBucket:edit')")
    @Log(title = "质押收益线性释放", businessType = BusinessType.UPDATE)
    @PutMapping
    @RepeatSubmit
    public AjaxResult edit(@RequestBody StakeReleaseBucket stakeReleaseBucket) {
        return toAjax(stakeReleaseBucketService.updateById(stakeReleaseBucket));
    }

    /**
     * 删除质押收益线性释放
     */
    @PreAuthorize("@ss.hasPermi('xms:stakeReleaseBucket:remove')")
    @Log(title = "质押收益线性释放", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(stakeReleaseBucketService.removeByIds(Arrays.asList(ids)));
    }
}
