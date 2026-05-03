package com.xms.web.controller.xms;

import java.util.Arrays;
import java.util.List;

import com.xms.common.annotation.RepeatSubmit;
import com.xms.common.exception.ServiceException;
import com.xms.dao.entity.domain.UserInfo;
import com.xms.dao.entity.req.AdminAllocateMiningMachineRequest;
import com.xms.dao.service.UserInfoService;
import com.xms.web.service.AdminMiningPackageService;
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
import com.xms.dao.domain.MiningPackage;
import com.xms.dao.service.IMiningPackageService;
import com.xms.common.utils.poi.ExcelUtil;
import com.xms.common.core.page.TableDataInfo;

/**
 * 矿机套餐Controller
 *
 * @author xms
 * @date 2026-02-21
 */
@RestController
@RequestMapping("/xms/miningPackage")
public class MiningPackageController extends BaseController
{
    @Autowired
    private IMiningPackageService miningPackageService;

	@Autowired
    private AdminMiningPackageService adminMiningPackageService;

	@Autowired
	private UserInfoService userInfoService;

/**
 * 查询矿机套餐列表
 */
@PreAuthorize("@ss.hasPermi('xms:miningPackage:list')")
@GetMapping("/list")
    public TableDataInfo list(MiningPackage miningPackage)
    {
        startPage();
        List<MiningPackage> list = miningPackageService.selectMiningPackageList(miningPackage);
        return getDataTable(list);
    }

    /**
     * 导出矿机套餐列表
     */
    @PreAuthorize("@ss.hasPermi('xms:miningPackage:export')")
    @Log(title = "矿机套餐", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, MiningPackage miningPackage)
    {
        List<MiningPackage> list = miningPackageService.selectMiningPackageList(miningPackage);
        ExcelUtil<MiningPackage> util = new ExcelUtil<MiningPackage>(MiningPackage.class);
        util.exportExcel(response, list, "矿机套餐数据");
    }

    /**
     * 获取矿机套餐详细信息
     */
    @PreAuthorize("@ss.hasPermi('xms:miningPackage:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(miningPackageService.getById(id));
    }

    /**
     * 新增矿机套餐
     */
    @PreAuthorize("@ss.hasPermi('xms:miningPackage:add')")
    @Log(title = "矿机套餐", businessType = BusinessType.INSERT)
    @PostMapping
    @RepeatSubmit
    public AjaxResult add(@RequestBody MiningPackage miningPackage) {
        return toAjax(miningPackageService.save(miningPackage));
    }

//	/**
//     * 拨付矿机(康合没有这个功能)
//     */
//    @PreAuthorize("@ss.hasPermi('xms:miningPackage:add')")
//    @Log(title = "拨付矿机", businessType = BusinessType.INSERT)
//    @PostMapping("/adminAllocateMiningMachine")
//    @RepeatSubmit
//    public AjaxResult adminAllocateMiningMachine(@RequestBody AdminAllocateMiningMachineRequest req) {
//		UserInfo userInfo = userInfoService.lambdaQuery()
//			.eq(UserInfo::getAccount, req.getAccount())
//			.one();
//		if(userInfo == null){
//			throw new ServiceException("用户不存在");
//		}
//        return toAjax(adminMiningPackageService.adminAllocateMiningMachine(req,userInfo.getUserId(),userInfo));
//    }

    /**
     * 修改矿机套餐
     */
    @PreAuthorize("@ss.hasPermi('xms:miningPackage:edit')")
    @Log(title = "矿机套餐", businessType = BusinessType.UPDATE)
    @PutMapping
    @RepeatSubmit
    public AjaxResult edit(@RequestBody MiningPackage miningPackage) {
        return toAjax(miningPackageService.updateMiningPackageById(miningPackage));
    }

    /**
     * 删除矿机套餐
     */
    @PreAuthorize("@ss.hasPermi('xms:miningPackage:remove')")
    @Log(title = "矿机套餐", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(miningPackageService.removeByIds(Arrays.asList(ids)));
    }
}
