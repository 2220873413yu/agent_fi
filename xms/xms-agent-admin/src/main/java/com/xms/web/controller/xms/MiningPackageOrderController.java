package com.xms.web.controller.xms;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import com.xms.common.annotation.RepeatSubmit;
import com.xms.common.exception.ServiceException;
import com.xms.common.utils.CollectionUtil;
import com.xms.dao.domain.MiningPackage;
import com.xms.dao.domain.MiningPackageTier;
import com.xms.dao.service.IMiningPackageService;
import com.xms.dao.service.IMiningPackageTierService;
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
import com.xms.dao.domain.MiningPackageOrder;
import com.xms.dao.service.IMiningPackageOrderService;
import com.xms.common.utils.poi.ExcelUtil;
import com.xms.common.core.page.TableDataInfo;

/**
 * 矿机订单Controller
 *
 * @author xms
 * @date 2026-02-23
 */
@RestController
@RequestMapping("/xms/miningPackageOrder")
public class MiningPackageOrderController extends BaseController
{
    @Autowired
    private IMiningPackageOrderService miningPackageOrderService;

	@Autowired
	private IMiningPackageTierService miningPackageTierService;

/**
 * 查询矿机订单列表
 */
@PreAuthorize("@ss.hasPermi('xms:miningPackageOrder:list')")
@GetMapping("/list")
    public TableDataInfo list(MiningPackageOrder miningPackageOrder)
    {
        startPage();
        List<MiningPackageOrder> list = miningPackageOrderService.selectMiningPackageOrderList(miningPackageOrder);
		if(CollectionUtil.isNotEmpty(list)){
			List<MiningPackageTier> tierList = miningPackageTierService.lambdaQuery()
				.orderByAsc(MiningPackageTier::getStartIndex)
				.list();

			for (MiningPackageOrder packageOrder : list) {
				if(packageOrder.getDayReward().compareTo(BigDecimal.ZERO)<=0){
					packageOrder.setDayReward(matchMiningPackageTier(tierList, packageOrder.getId().intValue()));
				}
			}
		}
        return getDataTable(list);
    }

	private BigDecimal matchMiningPackageTier(List<MiningPackageTier> list, Integer miningNo) {
		if (list == null || list.isEmpty()) {
			throw new ServiceException("质押档位未配置");
		}
		for (MiningPackageTier tier : list) {
			Integer startIndex = tier.getStartIndex();
			Integer endIndex = tier.getEndIndex();
			if (startIndex == null || endIndex == null) {
				continue;
			}
			if (miningNo >= startIndex && miningNo <= endIndex) {
				return tier.getDayReward();
			}
		}
		return list.get(list.size() - 1).getDayReward();
	}

    /**
     * 导出矿机订单列表
     */
    @PreAuthorize("@ss.hasPermi('xms:miningPackageOrder:export')")
    @Log(title = "矿机订单", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, MiningPackageOrder miningPackageOrder)
    {
        List<MiningPackageOrder> list = miningPackageOrderService.selectMiningPackageOrderList(miningPackageOrder);
		if(CollectionUtil.isNotEmpty(list)){
			List<MiningPackageTier> tierList = miningPackageTierService.lambdaQuery()
				.orderByAsc(MiningPackageTier::getStartIndex)
				.list();

			for (MiningPackageOrder packageOrder : list) {
				if(packageOrder.getDayReward().compareTo(BigDecimal.ZERO)<=0){
					packageOrder.setDayReward(matchMiningPackageTier(tierList, packageOrder.getId().intValue()));
				}
			}
		}
        ExcelUtil<MiningPackageOrder> util = new ExcelUtil<MiningPackageOrder>(MiningPackageOrder.class);
        util.exportExcel(response, list, "矿机订单数据");
    }

    /**
     * 获取矿机订单详细信息
     */
    @PreAuthorize("@ss.hasPermi('xms:miningPackageOrder:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(miningPackageOrderService.getById(id));
    }

    /**
     * 新增矿机订单
     */
    @PreAuthorize("@ss.hasPermi('xms:miningPackageOrder:add')")
    @Log(title = "矿机订单", businessType = BusinessType.INSERT)
    @PostMapping
    @RepeatSubmit
    public AjaxResult add(@RequestBody MiningPackageOrder miningPackageOrder) {
        return toAjax(miningPackageOrderService.save(miningPackageOrder));
    }

    /**
     * 修改矿机订单
     */
    @PreAuthorize("@ss.hasPermi('xms:miningPackageOrder:edit')")
    @Log(title = "矿机订单", businessType = BusinessType.UPDATE)
    @PutMapping
    @RepeatSubmit
    public AjaxResult edit(@RequestBody MiningPackageOrder miningPackageOrder) {
        return toAjax(miningPackageOrderService.updateById(miningPackageOrder));
    }

	/**
	 * 修改每日收益
	 */
	@PreAuthorize("@ss.hasPermi('xms:miningPackageOrder:edit')")
	@Log(title = "修改每日收益", businessType = BusinessType.UPDATE)
	@PostMapping("updateDayReward")
	@RepeatSubmit
	public AjaxResult updateDayReward(@RequestBody MiningPackageOrder miningPackageOrder) {
		return toAjax(miningPackageOrderService.updateDayReward(miningPackageOrder));
	}
	/**
	 * 暂停或者启用矿机
	 */
	@PreAuthorize("@ss.hasPermi('xms:miningPackageOrder:edit')")
	@Log(title = "暂停或者启用矿机", businessType = BusinessType.UPDATE)
	@PostMapping("stopOrOpenOrder")
	@RepeatSubmit
	public AjaxResult stopOrOpenOrder(@RequestBody MiningPackageOrder miningPackageOrder) {
		return toAjax(miningPackageOrderService.stopOrOpenOrder(miningPackageOrder));
	}

	/**
     * 矿机订单发货
     */
    @PreAuthorize("@ss.hasPermi('xms:miningPackageOrder:edit')")
    @Log(title = "矿机订单发货", businessType = BusinessType.UPDATE)
    @PostMapping("/processShipment")
    @RepeatSubmit
    public AjaxResult processShipment(@RequestBody MiningPackageOrder miningPackageOrder) {
        return toAjax(miningPackageOrderService.processShipment(miningPackageOrder));
    }

    /**
     * 删除矿机订单
     */
    @PreAuthorize("@ss.hasPermi('xms:miningPackageOrder:remove')")
    @Log(title = "矿机订单", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(miningPackageOrderService.removeByIds(Arrays.asList(ids)));
    }
}
