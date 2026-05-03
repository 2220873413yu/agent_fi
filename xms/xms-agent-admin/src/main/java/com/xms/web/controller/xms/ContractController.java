package com.xms.web.controller.xms;

import cn.hutool.core.collection.CollectionUtil;
import com.xms.common.annotation.Log;
import com.xms.common.annotation.RepeatSubmit;
import com.xms.common.core.controller.BaseController;
import com.xms.common.core.domain.AjaxResult;
import com.xms.common.core.page.TableDataInfo;
import com.xms.common.enums.BusinessType;
import com.xms.common.utils.poi.ExcelUtil;
import com.xms.dao.domain.Contract;
import com.xms.dao.service.IContractService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

/**
 * 合同协议Controller
 *
 * @author xms
 * @date 2023-12-22
 */
@RestController
@RequestMapping("/xms/contract")
public class ContractController extends BaseController
{
    @Autowired
    private IContractService contractService;

    /**
     * 查询合同协议列表
     */
    @PreAuthorize("@ss.hasPermi('xms:contract:list')")
    @GetMapping("/list")
    public TableDataInfo list(Contract contract)
    {
        startPage();
        List<Contract> list = contractService.selectContractList(contract);
        return getDataTable(list);
    }

    /**
     * 导出合同协议列表
     */
    @PreAuthorize("@ss.hasPermi('xms:contract:export')")
    @Log(title = "合同协议", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, Contract contract)
    {
        List<Contract> list = contractService.selectContractList(contract);
        ExcelUtil<Contract> util = new ExcelUtil<Contract>(Contract.class);
        util.exportExcel(response, list, "合同协议数据");
    }

    /**
     * 获取合同协议详细信息
     */
    @PreAuthorize("@ss.hasPermi('xms:contract:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(contractService.getById(id));
    }

    /**
     * 新增合同协议
     */
    @PreAuthorize("@ss.hasPermi('xms:contract:add')")
    @Log(title = "合同协议", businessType = BusinessType.INSERT)
    @PostMapping
	@RepeatSubmit
    public AjaxResult add(@Validated @RequestBody Contract contract) {
        return toAjax(contractService.saveContract(contract));
    }

    /**
     * 修改合同协议
     */
    @PreAuthorize("@ss.hasPermi('xms:contract:edit')")
    @Log(title = "合同协议", businessType = BusinessType.UPDATE)
    @PutMapping
	@RepeatSubmit
    public AjaxResult edit(@Validated @RequestBody  Contract contract) {
        return toAjax(contractService.updateContractById(contract));
    }

    /**
     * 删除合同协议
     */
    @PreAuthorize("@ss.hasPermi('xms:contract:remove')")
    @Log(title = "合同协议", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
		List<Long> idList = Arrays.asList(ids);
		if(CollectionUtil.isEmpty(idList)){
			return success();
		}
		contractService.deleteRecordById(idList);
		return success();
    }
}
