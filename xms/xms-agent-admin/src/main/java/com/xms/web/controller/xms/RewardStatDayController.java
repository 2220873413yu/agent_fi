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
import com.xms.dao.domain.RewardStatDay;
import com.xms.dao.service.IRewardStatDayService;
import com.xms.common.utils.poi.ExcelUtil;
import com.xms.common.core.page.TableDataInfo;

/**
 * 每日奖励汇总Controller
 *
 * @author xms
 * @date 2025-11-23
 */
@RestController
@RequestMapping("/xms/rewardStatDay")
public class RewardStatDayController extends BaseController
{
    @Autowired
    private IRewardStatDayService rewardStatDayService;

	@Autowired
	private UserInfoService userInfoService;

/**
 * 查询每日奖励汇总列表
 */
@PreAuthorize("@ss.hasPermi('xms:rewardStatDay:list')")
@GetMapping("/list")
    public TableDataInfo list(RewardStatDay rewardStatDay)
    {
		if(StrUtil.isNotBlank(rewardStatDay.getUserAccount())){
			UserInfo userInfo = userInfoService.lambdaQuery()
				.eq(UserInfo::getAccount, rewardStatDay.getUserAccount())
				.select(UserInfo::getUserId)
				.one();
			if(userInfo != null){
				rewardStatDay.setUserId(userInfo.getUserId());
			}else{
				return getDataTable(new ArrayList<>());
			}
		}

        startPage();
        List<RewardStatDay> list = rewardStatDayService.selectRewardStatDayList(rewardStatDay);

		if(CollectionUtil.isNotEmpty(list)){
			Map<Long, String> userIdMap = userInfoService.getUserAccountById(list.stream().map(RewardStatDay::getUserId).collect(Collectors.toList()));
			for (RewardStatDay order : list) {
				order.setUserAccount(userIdMap.get(order.getUserId()));
			}
		}
		return getDataTable(list);
    }

    /**
     * 导出每日奖励汇总列表
     */
    @PreAuthorize("@ss.hasPermi('xms:rewardStatDay:export')")
    @Log(title = "每日奖励汇总", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, RewardStatDay rewardStatDay)
    {
		if(StrUtil.isNotBlank(rewardStatDay.getUserAccount())){
			UserInfo userInfo = userInfoService.lambdaQuery()
				.eq(UserInfo::getAccount, rewardStatDay.getUserAccount())
				.select(UserInfo::getUserId)
				.one();
			if(userInfo != null){
				rewardStatDay.setUserId(userInfo.getUserId());
			}else{
				ExcelUtil<RewardStatDay> util = new ExcelUtil<RewardStatDay>(RewardStatDay.class);
				util.exportExcel(response, new ArrayList<>(), "每日奖励汇总数据");
			}
		}

        List<RewardStatDay> list = rewardStatDayService.selectRewardStatDayList(rewardStatDay);

		if(CollectionUtil.isNotEmpty(list)){
			Map<Long, String> userIdMap = userInfoService.getUserAccountById(list.stream().map(RewardStatDay::getUserId).collect(Collectors.toList()));
			for (RewardStatDay order : list) {
				order.setUserAccount(userIdMap.get(order.getUserId()));
			}
		}
        ExcelUtil<RewardStatDay> util = new ExcelUtil<RewardStatDay>(RewardStatDay.class);
        util.exportExcel(response, list, "每日奖励汇总数据");
    }

    /**
     * 获取每日奖励汇总详细信息
     */
    @PreAuthorize("@ss.hasPermi('xms:rewardStatDay:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(rewardStatDayService.getById(id));
    }

    /**
     * 新增每日奖励汇总
     */
    @PreAuthorize("@ss.hasPermi('xms:rewardStatDay:add')")
    @Log(title = "每日奖励汇总", businessType = BusinessType.INSERT)
    @PostMapping
    @RepeatSubmit
    public AjaxResult add(@RequestBody RewardStatDay rewardStatDay) {
        return toAjax(rewardStatDayService.save(rewardStatDay));
    }

    /**
     * 修改每日奖励汇总
     */
    @PreAuthorize("@ss.hasPermi('xms:rewardStatDay:edit')")
    @Log(title = "每日奖励汇总", businessType = BusinessType.UPDATE)
    @PutMapping
    @RepeatSubmit
    public AjaxResult edit(@RequestBody RewardStatDay rewardStatDay) {
        return toAjax(rewardStatDayService.updateById(rewardStatDay));
    }

    /**
     * 删除每日奖励汇总
     */
    @PreAuthorize("@ss.hasPermi('xms:rewardStatDay:remove')")
    @Log(title = "每日奖励汇总", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(rewardStatDayService.removeByIds(Arrays.asList(ids)));
    }
}
