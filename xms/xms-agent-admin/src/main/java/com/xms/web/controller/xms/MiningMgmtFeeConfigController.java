package com.xms.web.controller.xms;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import com.xms.common.annotation.RepeatSubmit;
import com.xms.common.exception.ServiceException;
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
import com.xms.dao.domain.MiningMgmtFeeConfig;
import com.xms.dao.service.IMiningMgmtFeeConfigService;
import com.xms.common.utils.poi.ExcelUtil;
import com.xms.common.core.page.TableDataInfo;

/**
 * 矿机管理费配置Controller
 *
 * @author xms
 * @date 2026-02-27
 */
@RestController
@RequestMapping("/xms/miningMgmtFeeConfig")
public class MiningMgmtFeeConfigController extends BaseController
{
    @Autowired
    private IMiningMgmtFeeConfigService miningMgmtFeeConfigService;

/**
 * 查询矿机管理费配置列表
 */
@PreAuthorize("@ss.hasPermi('xms:miningMgmtFeeConfig:list')")
@GetMapping("/list")
    public TableDataInfo list(MiningMgmtFeeConfig miningMgmtFeeConfig)
    {
        startPage();
        List<MiningMgmtFeeConfig> list = miningMgmtFeeConfigService.selectMiningMgmtFeeConfigList(miningMgmtFeeConfig);
        return getDataTable(list);
    }

    /**
     * 导出矿机管理费配置列表
     */
    @PreAuthorize("@ss.hasPermi('xms:miningMgmtFeeConfig:export')")
    @Log(title = "矿机管理费配置", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, MiningMgmtFeeConfig miningMgmtFeeConfig)
    {
        List<MiningMgmtFeeConfig> list = miningMgmtFeeConfigService.selectMiningMgmtFeeConfigList(miningMgmtFeeConfig);
        ExcelUtil<MiningMgmtFeeConfig> util = new ExcelUtil<MiningMgmtFeeConfig>(MiningMgmtFeeConfig.class);
        util.exportExcel(response, list, "矿机管理费配置数据");
    }

    /**
     * 获取矿机管理费配置详细信息
     */
    @PreAuthorize("@ss.hasPermi('xms:miningMgmtFeeConfig:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(miningMgmtFeeConfigService.getById(id));
    }

    /**
     * 新增矿机管理费配置
     */
    @PreAuthorize("@ss.hasPermi('xms:miningMgmtFeeConfig:add')")
    @Log(title = "矿机管理费配置", businessType = BusinessType.INSERT)
    @PostMapping
    @RepeatSubmit
    public AjaxResult add(@RequestBody MiningMgmtFeeConfig miningMgmtFeeConfig) {
        return toAjax(miningMgmtFeeConfigService.save(miningMgmtFeeConfig));
    }

    /**
     * 修改矿机管理费配置
     */
    @PreAuthorize("@ss.hasPermi('xms:miningMgmtFeeConfig:edit')")
    @Log(title = "矿机管理费配置", businessType = BusinessType.UPDATE)
    @PutMapping
    @RepeatSubmit
    public AjaxResult edit(@RequestBody MiningMgmtFeeConfig miningMgmtFeeConfig) {
		if(
			miningMgmtFeeConfig.getAgentDiffCountyRatio() == null ||
			miningMgmtFeeConfig.getAgentDiffCountyRatio().compareTo(BigDecimal.ZERO)<0){
			throw new ServiceException("县代理比例不能小于0");
		}
		if(miningMgmtFeeConfig.getAgentDiffAreaRatio() == null ||
			miningMgmtFeeConfig.getAgentDiffAreaRatio().compareTo(BigDecimal.ZERO)<0){
			throw new ServiceException("区代理比例不能小于0");
		}
		if(miningMgmtFeeConfig.getAgentDiffCityRatio() == null ||
			miningMgmtFeeConfig.getAgentDiffCityRatio().compareTo(BigDecimal.ZERO)<0){
			throw new ServiceException("市代理比例不能小于0");
		}
		if(miningMgmtFeeConfig.getAgentDiffProvinceRatio() == null ||
			miningMgmtFeeConfig.getAgentDiffProvinceRatio().compareTo(BigDecimal.ZERO)<0){
			throw new ServiceException("省代理比例不能小于0");
		}
		if(miningMgmtFeeConfig.getAgentDiffNationalRatio() == null ||
			miningMgmtFeeConfig.getAgentDiffNationalRatio().compareTo(BigDecimal.ZERO)<0){
			throw new ServiceException("全国代理比例不能小于0");
		}
		if(miningMgmtFeeConfig.getNationalSameLevelRatio() == null ||
			miningMgmtFeeConfig.getNationalSameLevelRatio().compareTo(BigDecimal.ZERO)<0){
			throw new ServiceException("全国代理平级奖比例不能小于0");
		}
		if(miningMgmtFeeConfig.getPlatformFeeRatio() == null ||
			miningMgmtFeeConfig.getPlatformFeeRatio().compareTo(BigDecimal.ZERO)<0){
			throw new ServiceException("平台管理费比例不能小于0");
		}
		if(miningMgmtFeeConfig.getDirectPushRatio() == null ||
			miningMgmtFeeConfig.getDirectPushRatio().compareTo(BigDecimal.ZERO)<0){
			throw new ServiceException("直推奖励比例不能小于0");
		}
		if(miningMgmtFeeConfig.getIndirectPushRatio() == null ||
			miningMgmtFeeConfig.getIndirectPushRatio().compareTo(BigDecimal.ZERO)<0){
			throw new ServiceException("间推奖励比例不能小于0");
		}
		if(miningMgmtFeeConfig.getServiceCenterRatio() == null ||
			miningMgmtFeeConfig.getServiceCenterRatio().compareTo(BigDecimal.ZERO)<0){
			throw new ServiceException("服务中心比例不能小于0");
		}

        return toAjax(miningMgmtFeeConfigService.updateById(miningMgmtFeeConfig));
    }

    /**
     * 删除矿机管理费配置
     */
    @PreAuthorize("@ss.hasPermi('xms:miningMgmtFeeConfig:remove')")
    @Log(title = "矿机管理费配置", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(miningMgmtFeeConfigService.removeByIds(Arrays.asList(ids)));
    }
}
