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
import com.xms.dao.domain.RewardRecord;
import com.xms.dao.service.IRewardRecordService;
import com.xms.common.utils.poi.ExcelUtil;
import com.xms.common.core.page.TableDataInfo;

/**
 * 奖金记录Controller
 *
 * @author xms
 * @date 2025-11-19
 */
@RestController
@RequestMapping("/xms/rewardRecord")
public class RewardRecordController extends BaseController
{
    @Autowired
    private IRewardRecordService rewardRecordService;

	@Autowired
	private UserInfoService userInfoService;
/**
 * 查询奖金记录列表
 */
@PreAuthorize("@ss.hasPermi('xms:rewardRecord:list')")
@GetMapping("/list")
    public TableDataInfo list(RewardRecord rewardRecord)
    {

		if(StrUtil.isNotBlank(rewardRecord.getUserAccount())){
			UserInfo userInfo = userInfoService.lambdaQuery()
				.eq(UserInfo::getUserCode, rewardRecord.getUserAccount())
				.select(UserInfo::getUserId)
				.one();
			if(userInfo != null){
				rewardRecord.setUserId(userInfo.getUserId());
			}else{
				return getDataTable(new ArrayList<>());
			}
		}

        startPage();
        List<RewardRecord> list = rewardRecordService.selectRewardRecordList(rewardRecord);

		return getDataTable(list);
    }

    /**
     * 导出奖金记录列表
     */
    @PreAuthorize("@ss.hasPermi('xms:rewardRecord:export')")
    @Log(title = "奖金记录", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, RewardRecord rewardRecord)
    {
		if(StrUtil.isNotBlank(rewardRecord.getUserAccount())){
			UserInfo userInfo = userInfoService.lambdaQuery()
				.eq(UserInfo::getAccount, rewardRecord.getUserAccount())
				.select(UserInfo::getUserId)
				.one();
			if(userInfo != null){
				rewardRecord.setUserId(userInfo.getUserId());
			}else{
				ExcelUtil<RewardRecord> util = new ExcelUtil<RewardRecord>(RewardRecord.class);
				util.exportExcel(response, new ArrayList<>(), "奖金记录数据");
			}
		}


		List<RewardRecord> list = rewardRecordService.selectRewardRecordList(rewardRecord);

		if(CollectionUtil.isNotEmpty(list)){
			Map<Long, String> userIdMap = userInfoService.getUserAccountById(list.stream().map(RewardRecord::getUserId).collect(Collectors.toList()));
			for (RewardRecord order : list) {
				order.setUserAccount(userIdMap.get(order.getUserId()));
			}
		}
        ExcelUtil<RewardRecord> util = new ExcelUtil<RewardRecord>(RewardRecord.class);
        util.exportExcel(response, list, "奖金记录数据");
    }

    /**
     * 获取奖金记录详细信息
     */
    @PreAuthorize("@ss.hasPermi('xms:rewardRecord:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(rewardRecordService.getById(id));
    }

    /**
     * 新增奖金记录
     */
    @PreAuthorize("@ss.hasPermi('xms:rewardRecord:add')")
    @Log(title = "奖金记录", businessType = BusinessType.INSERT)
    @PostMapping
    @RepeatSubmit
    public AjaxResult add(@RequestBody RewardRecord rewardRecord) {
        return toAjax(rewardRecordService.save(rewardRecord));
    }

    /**
     * 修改奖金记录
     */
    @PreAuthorize("@ss.hasPermi('xms:rewardRecord:edit')")
    @Log(title = "奖金记录", businessType = BusinessType.UPDATE)
    @PutMapping
    @RepeatSubmit
    public AjaxResult edit(@RequestBody RewardRecord rewardRecord) {
        return toAjax(rewardRecordService.updateById(rewardRecord));
    }

    /**
     * 删除奖金记录
     */
    @PreAuthorize("@ss.hasPermi('xms:rewardRecord:remove')")
    @Log(title = "奖金记录", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(rewardRecordService.removeByIds(Arrays.asList(ids)));
    }
}
