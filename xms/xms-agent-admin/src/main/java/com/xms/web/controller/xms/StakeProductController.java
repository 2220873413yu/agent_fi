package com.xms.web.controller.xms;

import java.math.BigDecimal;
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
import com.xms.dao.domain.StakeProduct;
import com.xms.dao.service.IStakeProductService;
import com.xms.common.utils.poi.ExcelUtil;
import com.xms.common.core.page.TableDataInfo;

/**
 * 质押套餐Controller
 *
 * @author xms
 * @date 2026-02-27
 */
@RestController
@RequestMapping("/xms/stakeProduct")
public class StakeProductController extends BaseController
{
    @Autowired
    private IStakeProductService stakeProductService;

/**
 * 查询质押套餐列表
 */
@PreAuthorize("@ss.hasPermi('xms:stakeProduct:list')")
@GetMapping("/list")
    public TableDataInfo list(StakeProduct stakeProduct)
    {
        startPage();
        List<StakeProduct> list = stakeProductService.selectStakeProductList(stakeProduct);
        return getDataTable(list);
    }

    /**
     * 导出质押套餐列表
     */
    @PreAuthorize("@ss.hasPermi('xms:stakeProduct:export')")
    @Log(title = "质押套餐", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, StakeProduct stakeProduct)
    {
        List<StakeProduct> list = stakeProductService.selectStakeProductList(stakeProduct);
        ExcelUtil<StakeProduct> util = new ExcelUtil<StakeProduct>(StakeProduct.class);
        util.exportExcel(response, list, "质押套餐数据");
    }

    /**
     * 获取质押套餐详细信息
     */
    @PreAuthorize("@ss.hasPermi('xms:stakeProduct:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(stakeProductService.getById(id));
    }

    /**
     * 新增质押套餐
     */
    @PreAuthorize("@ss.hasPermi('xms:stakeProduct:add')")
    @Log(title = "质押套餐", businessType = BusinessType.INSERT)
    @PostMapping
    @RepeatSubmit
    public AjaxResult add(@RequestBody StakeProduct stakeProduct) {
        return toAjax(stakeProductService.save(stakeProduct));
    }

    /**
     * 修改质押套餐
     */
    @PreAuthorize("@ss.hasPermi('xms:stakeProduct:edit')")
    @Log(title = "质押套餐", businessType = BusinessType.UPDATE)
    @PutMapping
    @RepeatSubmit
    public AjaxResult edit(@RequestBody StakeProduct stakeProduct) {
		if(stakeProduct.getStakeUnitAmountMin()== null ||
			stakeProduct.getStakeUnitAmountMin().compareTo(BigDecimal.ZERO) <= 0){
			throw new SecurityException("最低质押数量不能小于等于0");
		}

		if(stakeProduct.getMaxStakeAmount()== null || stakeProduct.getMaxStakeAmount().compareTo(BigDecimal.ZERO) <= 0){
			throw new SecurityException("最高质押不能小于等于0");
		}

		if(stakeProduct.getStaticRatio()== null ||
			stakeProduct.getStaticRatio().compareTo(BigDecimal.ZERO) <= 0){
			throw new SecurityException("静态日利率不能小于等于0");
		}
		if(stakeProduct.getExitMultiplier()== null ||
			stakeProduct.getExitMultiplier().compareTo(BigDecimal.ZERO) <= 0){
			throw new SecurityException("出局倍数不能小于等于0");
		}
        return toAjax(stakeProductService.updateById(stakeProduct));
    }

    /**
     * 删除质押套餐
     */
    @PreAuthorize("@ss.hasPermi('xms:stakeProduct:remove')")
    @Log(title = "质押套餐", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(stakeProductService.removeByIds(Arrays.asList(ids)));
    }
}
