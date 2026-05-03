package com.xms.web.controller.xms;

import java.util.Arrays;
import java.util.List;

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
import com.xms.dao.domain.UserTransfer;
import com.xms.dao.service.IUserTransferService;
import com.xms.common.utils.poi.ExcelUtil;
import com.xms.common.core.page.TableDataInfo;

/**
 * 用户转账记录Controller
 *
 * @author xms
 * @date 2025-03-12
 */
@RestController
@RequestMapping("/xms/userTransfer")
public class UserTransferController extends BaseController
{
    @Autowired
    private IUserTransferService userTransferService;

	@Autowired
	private UserInfoService userInfoServiceImpl;
    /**
     * 查询用户转账记录列表
     */
    @PreAuthorize("@ss.hasPermi('xms:userTransfer:list')")
    @GetMapping("/list")
    public TableDataInfo list(UserTransfer userTransfer)
    {
        startPage();
        List<UserTransfer> list = userTransferService.selectUserTransferList(userTransfer);
		return getDataTable(list);
    }

    /**
     * 导出用户转账记录列表
     */
    @PreAuthorize("@ss.hasPermi('xms:userTransfer:export')")
    @Log(title = "用户转账记录", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, UserTransfer userTransfer)
    {

        List<UserTransfer> list = userTransferService.selectUserTransferList(userTransfer);
        ExcelUtil<UserTransfer> util = new ExcelUtil<UserTransfer>(UserTransfer.class);
        util.exportExcel(response, list, "用户转账记录数据");
    }

    /**
     * 获取用户转账记录详细信息
     */
    @PreAuthorize("@ss.hasPermi('xms:userTransfer:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(userTransferService.getById(id));
    }

    /**
     * 新增用户转账记录
     */
    @PreAuthorize("@ss.hasPermi('xms:userTransfer:add')")
    @Log(title = "用户转账记录", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody UserTransfer userTransfer) {
        return toAjax(userTransferService.save(userTransfer));
    }

    /**
     * 修改用户转账记录
     */
    @PreAuthorize("@ss.hasPermi('xms:userTransfer:edit')")
    @Log(title = "用户转账记录", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody UserTransfer userTransfer) {
        return toAjax(userTransferService.updateById(userTransfer));
    }

    /**
     * 删除用户转账记录
     */
    @PreAuthorize("@ss.hasPermi('xms:userTransfer:remove')")
    @Log(title = "用户转账记录", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(userTransferService.removeByIds(Arrays.asList(ids)));
    }
}
