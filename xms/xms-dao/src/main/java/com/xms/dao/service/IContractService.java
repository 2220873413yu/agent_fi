package com.xms.dao.service;



import com.xms.dao.domain.Contract;

import java.util.List;

/**
 * 合同协议Service接口
 *
 * @author xms
 * @date 2023-12-22
 */
public interface IContractService extends XmsDataService<Contract>
{

    /**
     * 查询合同协议列表
     *
     * @param contract 合同协议
     * @return 合同协议集合
     */
    public List<Contract> selectContractList(Contract contract);

	/**
	 * 修改
	 * @param contract
	 * @return
	 */
	int updateContractById(Contract contract);

	/**
	 * 新增
	 * @param contract
	 * @return
	 */
	int saveContract(Contract contract);

	/**
	 * 根据id删除
	 * @param idList
	 * @return
	 */
	int deleteRecordById(List<Long> idList);
}
