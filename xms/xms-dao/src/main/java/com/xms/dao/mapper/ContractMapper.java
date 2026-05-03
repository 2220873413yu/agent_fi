package com.xms.dao.mapper;


import com.xms.dao.domain.Contract;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 合同协议Mapper接口
 *
 * @author xms
 * @date 2023-12-22
 */
public interface ContractMapper extends XmsMapper<Contract>
{
    /**
     * 查询合同协议列表
     *
     * @param contract 合同协议
     * @return 合同协议集合
     */
    public List<Contract> selectContractList(Contract contract);

	/**
	 * 根据id删除记录
	 * @param idList
	 * @return
	 */
    int deleteRecordById(@Param("idList") List<Long> idList);
}
