package com.xms.dao.mapper;

import java.util.Date;
import java.util.List;

import com.xms.dao.entity.bo.RewardRecordBo;
import com.xms.dao.mapper.XmsMapper;

import com.xms.dao.domain.RewardRecord;
import org.apache.ibatis.annotations.Param;

/**
 * 奖金记录Mapper接口
 *
 * @author xms
 * @date 2025-11-19
 */
public interface RewardRecordMapper extends XmsMapper<RewardRecord>
{
    /**
     * 查询奖金记录列表
     *
     * @param rewardRecord 奖金记录
     * @return 奖金记录集合
     */
    public List<RewardRecord> selectRewardRecordList(RewardRecord rewardRecord);

	/**
	 * 奖金记录(收益记录)
	 * @param userId
	 * @param bizType
	 * @return
	 */
	List<RewardRecordBo> rewardList(@Param("userId") Long userId,
									@Param("bizType") Integer bizType,
									@Param("startTime") Date startTime,
									@Param("endTime") Date endTime);
}
