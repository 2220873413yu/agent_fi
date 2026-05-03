package com.xms.dao.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.pagehelper.PageInfo;
import com.xms.common.core.domain.api.ResultPista;
import com.xms.common.core.domain.entity.SysDictData;
import com.xms.dao.entity.bo.UserMoneyLogBo;
import com.xms.dao.entity.domain.UserMoneyCanal;
import com.xms.dao.entity.domain.UserMoneyLog;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

/**
 * <p>
 * 钱包流水表 服务类
 * </p>
 *
 *
 * @since 2023-07-25
 */
public interface UserMoneyLogService extends IService<UserMoneyLog> {



	/**
	 *
	 * @Title: getUserMoneyLogList
	 * @param:
	 * @Description: 钱包流水
	 * @return List<UserMoneyLogBo>
	 */
	PageInfo<UserMoneyLogBo> getUserMoneyLogList(Integer pageIndex, Integer pageSize,
												 Long userId, Integer coinType, Integer sourceType);

    ResultPista<List<SysDictData>> listRecordBiz(String dictType);

	/**
	 * 查询钱包流水列表
	 *
	 * @param userMoneyLog 钱包流水
	 * @return 钱包流水集合
	 */
	public List<UserMoneyLog> selectUserMoneyLogList(UserMoneyLog userMoneyLog);

	boolean handerMsg(UserMoneyCanal before, UserMoneyCanal after);

	/**
	 * 查询昨日收益
	 * @param userId
	 * @return
	 */
    BigDecimal queryYesterdayReward(Long userId);

	/**
	 * 查询当月收益
	 * @param userId
	 * @return
	 */
	public BigDecimal queryCurrentMonthReward(Long userId);

	List<UserMoneyLog> incomeDetailsList(Long lastId, Long userId);

	/**
	 * 累计加速消耗mai
	 * @return
	 */
    BigDecimal totalAmountV9();
}
