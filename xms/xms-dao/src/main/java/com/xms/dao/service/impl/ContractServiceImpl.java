package com.xms.dao.service.impl;

import com.xms.common.constant.SysConstant;
import com.xms.common.core.domain.BaseEntity;
import com.xms.dao.domain.Contract;
import com.xms.dao.mapper.ContractMapper;
import com.xms.dao.service.IContractService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 合同协议Service业务层处理
 *
 * @author xms
 * @date 2023-12-22
 */
@Service
public class ContractServiceImpl extends XmsDataServiceImpl<ContractMapper, Contract> implements IContractService {


	/**
	 * 查询合同协议列表
	 *
	 * @param contract 合同协议
	 * @return 合同协议
	 */
	@Override
	public List<Contract> selectContractList(Contract contract) {
		contract.setDeleted(SysConstant.ZERO);
		return baseMapper.selectContractList(contract);
	}

	@Override
	public int updateContractById(Contract contract) {
		Contract queryContract = lambdaQuery()
			.eq(BaseEntity::getDeleted, SysConstant.ZERO)
			.eq(Contract::getBizType, contract.getBizType())
			.eq(Contract::getType, contract.getType()).one();
		if (queryContract != null) {
			if (!queryContract.getId().equals(contract.getId())) {
				throw new SecurityException("已有相类的合同和协议");
			}
		}
		int i = baseMapper.updateById(contract);
		return i;
	}

	@Override
	public int saveContract(Contract contract) {
		Contract queryContract = lambdaQuery()
			.eq(BaseEntity::getDeleted, SysConstant.ZERO)
			.eq(Contract::getBizType, contract.getBizType())
			.eq(Contract::getType, contract.getType()).one();
		if (queryContract != null) {
			throw new SecurityException("已有相类的合同和协议");
		}
		int i = baseMapper.insertIgnore(contract);
		return i;
	}

	/**
	 * deleteRecordById
	 * @param idList
	 * @return
	 */
	@Override
	public int deleteRecordById(List<Long> idList) {
		return baseMapper.deleteRecordById(idList);
	}
}
