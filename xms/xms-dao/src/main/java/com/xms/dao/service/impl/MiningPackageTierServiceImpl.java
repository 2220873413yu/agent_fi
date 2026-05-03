package com.xms.dao.service.impl;

import java.math.BigDecimal;
import java.util.List;

import com.xms.common.utils.CollectionUtil;
import com.xms.common.exception.ServiceException;
import org.springframework.stereotype.Service;
import com.xms.dao.mapper.MiningPackageTierMapper;
import com.xms.dao.domain.MiningPackageTier;
import com.xms.dao.service.IMiningPackageTierService;

/**
 * 矿机质押区间配置Service业务层处理
 *
 * @author xms
 * @date 2026-02-23
 */
@Service
public class MiningPackageTierServiceImpl extends XmsDataServiceImpl<MiningPackageTierMapper, MiningPackageTier> implements IMiningPackageTierService
{


    /**
     * 查询矿机质押区间配置列表
     *
     *
     * @param miningPackageTier 矿机质押区间配置
     * @return 矿机质押区间配置
     */
    @Override
    public List<MiningPackageTier> selectMiningPackageTierList(MiningPackageTier miningPackageTier)
    {
        return baseMapper.selectMiningPackageTierList(miningPackageTier);
    }

	@Override
	public int saveMiningPackageTier(MiningPackageTier miningPackageTier) {
		if(miningPackageTier.getStakeAmount() == null || miningPackageTier.getStakeAmount().compareTo(BigDecimal.ZERO)<=0){
			throw new ServiceException("质押金额不能小于0");
		}

		if(miningPackageTier.getDayReward() == null || miningPackageTier.getDayReward().compareTo(BigDecimal.ZERO)<=0){
			throw new ServiceException("日产出不能小于0");
		}
		validateRange(miningPackageTier, null);
		boolean save = save(miningPackageTier);
		if (!save) {
			throw new ServiceException("新增矿机质押区间配置失败");
		}
		return 1;
	}

	@Override
	public int updateMiningPackageTier(MiningPackageTier miningPackageTier) {
		if (miningPackageTier.getId() == null) {
			throw new ServiceException("缺少主键id");
		}

		if(miningPackageTier.getStakeAmount() == null || miningPackageTier.getStakeAmount().compareTo(BigDecimal.ZERO)<=0){
			throw new ServiceException("质押金额不能小于0");
		}

		if(miningPackageTier.getDayReward() == null || miningPackageTier.getDayReward().compareTo(BigDecimal.ZERO)<=0){
			throw new ServiceException("日产出不能小于0");
		}

		validateRange(miningPackageTier, miningPackageTier.getId());
		int updated = baseMapper.updateById(miningPackageTier);
		if (updated < 1) {
			throw new ServiceException("修改矿机质押区间配置失败");
		}
		return 1;
	}

	@Override
	public int removeRecordById(List<Long> list) {
		if(CollectionUtil.isEmpty(list)){
			return 1;
		}
		baseMapper.deleteRecordById(list);

		Long count = lambdaQuery()
			.count();
		if(count<=0){
			throw new ServiceException("请至少保留一条数据");
		}
		return 1;
	}

	private void validateRange(MiningPackageTier miningPackageTier, Long excludeId) {
		Integer startIndex = miningPackageTier.getStartIndex();
		Integer endIndex = miningPackageTier.getEndIndex();
		if (startIndex == null || endIndex == null) {
			throw new ServiceException("区间起始和结束不能为空");
		}
		if (startIndex > endIndex) {
			throw new ServiceException("区间起始不能大于结束");
		}
		int overlapCount = baseMapper.countOverlap(startIndex, endIndex, excludeId);
		if (overlapCount > 0) {
			throw new ServiceException("区间范围重复,请调整起止范围");
		}
	}
}
