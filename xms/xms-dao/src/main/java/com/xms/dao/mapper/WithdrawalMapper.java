package com.xms.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.pagehelper.PageInfo;
import com.xms.common.core.domain.api.ResultPista;
import com.xms.dao.entity.bo.WithdrawalBo;
import com.xms.dao.entity.domain.Withdrawal;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * 提现表 Mapper 接口
 * </p>
 *
 *
 * @since 2023-06-30
 */
public interface WithdrawalMapper  extends BaseMapper<Withdrawal> {

	//****************管理后台***************

	/**
	 * 查询提现列表
	 *
	 * @param withdrawal 提现
	 * @return 提现集合
	 */
	public List<Withdrawal> selectWithdrawalList(Withdrawal withdrawal);

	@Select("select sum(change_balance) from t_withdrawal where user_id =#{userId} and type =#{type} and to_days(create_time)=to_days(now()) and status in (0,1,3)")
	BigDecimal sumNum(@Param("userId") Long userId, @Param("type") Integer type);

	@Select("SELECT COALESCE(SUM(change_balance), 0) AS total_change_balance FROM t_withdrawal WHERE coin_type = 1 and status = 3")
	BigDecimal selectUsdtTotalBalance();

	@Select("SELECT COALESCE(SUM(change_balance), 0) AS total_change_balance FROM t_withdrawal WHERE coin_type = 2 AND status = 3")
	BigDecimal selectBPayTotalBalance();

	/**
	 * 提现记录
	 * @param userId userId
	 * @return
	 */
    List<WithdrawalBo> listWithdrawRecord(@Param("coinType") Integer coinType,@Param("userId")Long userId);
}
