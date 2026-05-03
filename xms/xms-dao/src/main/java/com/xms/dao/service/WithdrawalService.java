package com.xms.dao.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.pagehelper.PageInfo;
import com.xms.common.core.domain.AjaxResult;
import com.xms.common.core.domain.api.ResultPista;
import com.xms.dao.entity.bo.WithdrawalBo;
import com.xms.dao.entity.domain.Withdrawal;

import java.math.BigDecimal;
import java.util.List;


/**
 * <p>
 * 提现表 服务类
 * </p>
 *
 *
 * @since 2023-06-30
 */
public interface WithdrawalService extends IService<Withdrawal> {


	/**
	 * 查询提现列表
	 *
	 * @param withdrawal 提现
	 * @return 提现集合
	 */
	public List<Withdrawal> selectWithdrawalList(Withdrawal withdrawal);

	/**
	 * 审核提现
	 *
	 * @param withdrawal 提现
	 * @return 结果
	 */
	AjaxResult updateCheckStatusById(Withdrawal withdrawal);

	BigDecimal selectUsdtTotalBalance();

	BigDecimal selectBPayTotalBalance();

	/**
	 * 提现记录
	 * @param pageIndex 当前页 默认1
	 * @param pageSize 每页长度 默认20(最大20)
	 * @return
	 */
    PageInfo<WithdrawalBo> listWithdrawRecord(Integer coinType, Integer pageIndex, Integer pageSize, Long userId);
}
