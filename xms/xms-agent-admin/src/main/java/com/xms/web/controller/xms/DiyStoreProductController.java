package com.xms.web.controller.xms;

import java.util.Arrays;
import java.util.List;

import com.xms.common.annotation.RepeatSubmit;
import com.xms.common.config.redis.XmsRedis;
import com.xms.common.config.redis.delayqueue.config.RedissonTemplate;
import com.xms.common.constant.RedisConstant;
import com.xms.dao.domain.DiyStoreProductRule;
import com.xms.dao.entity.req.BizProductDetailReq;
import com.xms.dao.service.IDiyStoreProductAttrService;
import com.xms.dao.service.IDiyStoreProductRuleService;
import com.xms.web.service.BizStoreProductService;
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
import com.xms.dao.domain.DiyStoreProduct;
import com.xms.dao.service.IDiyStoreProductService;
import com.xms.common.utils.poi.ExcelUtil;
import com.xms.common.core.page.TableDataInfo;

/**
 * 商品Controller
 *
 * @author xms
 * @date 2026-04-08
 */
@RestController
@RequestMapping("/xms/diyStoreProduct")
public class DiyStoreProductController extends BaseController
{
    @Autowired
    private IDiyStoreProductService diyStoreProductService;

	@Autowired
	private BizStoreProductService bizStoreProductService;

	@Autowired
	private IDiyStoreProductRuleService diyStoreProductRuleService;
	/**
	 * 查询规格列表
	 */
	@PreAuthorize("@ss.hasPermi('xms:diyStoreProduct:list')")
	@GetMapping("/getProductRuleList")
	public List<DiyStoreProductRule> getProductRuleList()
	{
		List<DiyStoreProductRule> ruleList = diyStoreProductRuleService.lambdaQuery()
			.list();
		return ruleList;
	}

	/**
	 * 查询商品列表
	 */
	@PreAuthorize("@ss.hasPermi('xms:diyStoreProduct:list')")
	@GetMapping("/list")
    public TableDataInfo list(DiyStoreProduct diyStoreProduct)
    {
        startPage();
        List<DiyStoreProduct> list = diyStoreProductService.selectDiyStoreProductList(diyStoreProduct);
        return getDataTable(list);
    }

    /**
     * 导出商品列表
     */
    @PreAuthorize("@ss.hasPermi('xms:diyStoreProduct:export')")
    @Log(title = "商品", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, DiyStoreProduct diyStoreProduct)
    {
        List<DiyStoreProduct> list = diyStoreProductService.selectDiyStoreProductList(diyStoreProduct);
        ExcelUtil<DiyStoreProduct> util = new ExcelUtil<DiyStoreProduct>(DiyStoreProduct.class);
        util.exportExcel(response, list, "商品数据");
    }

    /**
     * 获取商品详细信息
     */
    @PreAuthorize("@ss.hasPermi('xms:diyStoreProduct:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id) {
        return success(bizStoreProductService.getById(id));
    }

    /**
     * 新增/更新商品
     */
    @PreAuthorize("@ss.hasPermi('xms:diyStoreProduct:add')")
    @Log(title = "商品", businessType = BusinessType.INSERT)
    @PostMapping
    @RepeatSubmit
    public AjaxResult add(@RequestBody BizProductDetailReq diyStoreProduct) {
		bizStoreProductService.saveOrUpdate(diyStoreProduct);
		return toAjax(1);
    }

/*    *//**
     * 修改商品
     *//*
    @PreAuthorize("@ss.hasPermi('xms:diyStoreProduct:edit')")
    @Log(title = "商品", businessType = BusinessType.UPDATE)
    @PutMapping
    @RepeatSubmit
    public AjaxResult edit(@RequestBody DiyStoreProduct diyStoreProduct) {
        return toAjax(diyStoreProductService.updateById(diyStoreProduct));
    }*/

    /**
     * 删除商品
     */
    @PreAuthorize("@ss.hasPermi('xms:diyStoreProduct:remove')")
    @Log(title = "商品", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable String[] ids) {
        return toAjax(diyStoreProductService.removeByIds(Arrays.asList(ids)));
    }
}
