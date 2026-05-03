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
import com.xms.dao.entity.domain.Withdrawal;
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
import com.xms.dao.domain.UserAddress;
import com.xms.dao.service.IUserAddressService;
import com.xms.common.utils.poi.ExcelUtil;
import com.xms.common.core.page.TableDataInfo;

/**
 * 收货地址Controller
 *
 * @author xms
 * @date 2026-02-26
 */
@RestController
@RequestMapping("/xms/userAddress")
public class UserAddressController extends BaseController
{
    @Autowired
    private IUserAddressService userAddressService;


	@Autowired
	private UserInfoService userInfoService;

/**
 * 查询收货地址列表
 */
@PreAuthorize("@ss.hasPermi('xms:userAddress:list')")
@GetMapping("/list")
    public TableDataInfo list(UserAddress userAddress)
    {
		if(StrUtil.isNotBlank(userAddress.getUserAccount())){
			UserInfo userInfo = userInfoService.lambdaQuery()
				.eq(UserInfo::getAccount, userAddress.getUserAccount())
				.one();
			if(userInfo != null){
				userAddress.setUserId(userInfo.getUserId());
			}else{
				return getDataTable(new ArrayList<>());
			}
		}
        startPage();
        List<UserAddress> list = userAddressService.selectUserAddressList(userAddress);

		if(CollectionUtil.isNotEmpty(list)){
			Map<Long, String> userAccountMap = userInfoService.getUserAccountById(list.stream().map(UserAddress::getUserId).collect(Collectors.toList()));
			for (UserAddress bankVo : list) {
				bankVo.setUserAccount(userAccountMap.get(bankVo.getUserId()));
			}
		}
        return getDataTable(list);
    }

    /**
     * 导出收货地址列表
     */
    @PreAuthorize("@ss.hasPermi('xms:userAddress:export')")
    @Log(title = "收货地址", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, UserAddress userAddress)
    {
		if(StrUtil.isNotBlank(userAddress.getUserAccount())){
			UserInfo userInfo = userInfoService.lambdaQuery()
				.eq(UserInfo::getAccount, userAddress.getUserAccount())
				.one();
			if(userInfo != null){
				userAddress.setUserId(userInfo.getUserId());
			}else{
				ExcelUtil<Withdrawal> util = new ExcelUtil<Withdrawal>(Withdrawal.class);
				util.exportExcel(response, new ArrayList<>(), "提现数据");
			}
		}

        List<UserAddress> list = userAddressService.selectUserAddressList(userAddress);

		if(CollectionUtil.isNotEmpty(list)){
			Map<Long, String> userAccountMap = userInfoService.getUserAccountById(list.stream().map(UserAddress::getUserId).collect(Collectors.toList()));
			for (UserAddress bankVo : list) {
				bankVo.setUserAccount(userAccountMap.get(bankVo.getUserId()));
			}
		}
		ExcelUtil<UserAddress> util = new ExcelUtil<UserAddress>(UserAddress.class);
        util.exportExcel(response, list, "收货地址数据");
    }

    /**
     * 获取收货地址详细信息
     */
    @PreAuthorize("@ss.hasPermi('xms:userAddress:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(userAddressService.getById(id));
    }

    /**
     * 新增收货地址
     */
    @PreAuthorize("@ss.hasPermi('xms:userAddress:add')")
    @Log(title = "收货地址", businessType = BusinessType.INSERT)
    @PostMapping
    @RepeatSubmit
    public AjaxResult add(@RequestBody UserAddress userAddress) {
        return toAjax(userAddressService.save(userAddress));
    }

    /**
     * 修改收货地址
     */
    @PreAuthorize("@ss.hasPermi('xms:userAddress:edit')")
    @Log(title = "收货地址", businessType = BusinessType.UPDATE)
    @PutMapping
    @RepeatSubmit
    public AjaxResult edit(@RequestBody UserAddress userAddress) {
        return toAjax(userAddressService.updateById(userAddress));
    }

    /**
     * 删除收货地址
     */
    @PreAuthorize("@ss.hasPermi('xms:userAddress:remove')")
    @Log(title = "收货地址", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(userAddressService.removeByIds(Arrays.asList(ids)));
    }
}
