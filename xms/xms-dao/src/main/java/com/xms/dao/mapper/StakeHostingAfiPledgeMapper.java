package com.xms.dao.mapper;

import com.xms.dao.domain.StakeHostingAfiPledge;

import java.util.List;

/**
 * 托管订单AFI质押记录Mapper接口
 *
 * @author xms
 */
public interface StakeHostingAfiPledgeMapper extends XmsMapper<StakeHostingAfiPledge> {
	List<StakeHostingAfiPledge> selectStakeHostingAfiPledgeList(StakeHostingAfiPledge pledge);
}
