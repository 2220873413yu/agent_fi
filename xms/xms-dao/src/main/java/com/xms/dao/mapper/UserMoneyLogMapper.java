package com.xms.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xms.common.core.domain.entity.SysDictData;
import com.xms.dao.entity.bo.UserMoneyLogBo;
import com.xms.dao.entity.domain.UserMoneyLog;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;

/**
 * <p>
 * 钱包流水表 Mapper 接口
 * </p>
 *
 *
 * @since 2023-07-25
 */
public interface UserMoneyLogMapper extends BaseMapper<UserMoneyLog> {

	/**
	 *
	 * @Title: getUserMoneyLogList
	 * @param:
	 * @Description: 钱包流水
	 * @return List<UserMoneyLogDto>
	 */
	List<UserMoneyLogBo> getUserMoneyLogList(@Param("userId") Long userId,
											 @Param("coinType") Integer coinType, @Param("sourceType") Integer sourceType);

	//****************管理后台***************

	/**
	 * 查询钱包流水列表
	 *
	 * @param userMoneyLog 钱包流水
	 * @return 钱包流水集合
	 */
	public List<UserMoneyLog> selectUserMoneyLogList(UserMoneyLog userMoneyLog);
	@Select("select * from sys_dict_data where status = '0' and dict_type = #{dictType} order by dict_sort asc")
	List<SysDictData> selectDictDataByType(String sxRecordType);


	/**
	 * 查询昨日数据
	 * @param userId
	 * @return
	 */
    BigDecimal queryYesterdayReward(@Param("userId") Long userId);

	/**
	 * 查询当月数据
	 * @param userId
	 * @return
	 */
    BigDecimal queryCurrentMonthReward(@Param("userId") Long userId);

	/**
	 * 获取V9总充值金额
	 * @return
	 */
    BigDecimal totalAmountV9();
}
