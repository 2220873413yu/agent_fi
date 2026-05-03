package com.xms.dao.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.xms.common.config.redis.lock.RedisLock;
import com.xms.common.constant.ConstantType;
import com.xms.common.constant.RedisConstant;
import com.xms.common.constant.SysConstant;
import com.xms.common.core.domain.api.ResultPista;
import com.xms.common.core.domain.entity.SysDictData;
import com.xms.common.utils.DictUtils;
import com.xms.common.utils.Func;
import com.xms.common.utils.StringUtils;
import com.xms.common.utils.spring.SpringUtils;
import com.xms.common.utils.uuid.IDUtils;
import com.xms.dao.domain.RechargeRecord;
import com.xms.dao.entity.bo.UserMoneyLogBo;
import com.xms.dao.entity.domain.UserMoneyCanal;
import com.xms.dao.entity.domain.UserMoneyLog;
import com.xms.dao.mapper.UserMoneyLogMapper;
import com.xms.dao.service.UserMoneyLogService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author MIER
 * @createDate: 2023/7/31 15:22
 */
@Service
@AllArgsConstructor
public class UserMoneyLogServiceImpl extends ServiceImpl<UserMoneyLogMapper, UserMoneyLog> implements UserMoneyLogService {


	@Override
	public List<UserMoneyLog> incomeDetailsList(Long lastId, Long userId) {
		List<UserMoneyLog> userMoneyLogList = lambdaQuery()
			.eq(UserMoneyLog::getUserId, userId)
			.in(UserMoneyLog::getCoinType, 1, 2)
			.in(UserMoneyLog::getSourceType,ConstantType.user_money_log_source_type.type_21,
				ConstantType.user_money_log_source_type.type_23,ConstantType.user_money_log_source_type.type_24,
				ConstantType.user_money_log_source_type.type_25,ConstantType.user_money_log_source_type.type_29)
			.lt(Func.isNotEmpty(lastId), UserMoneyLog::getId, lastId)
			.orderByDesc(UserMoneyLog::getId)
			.last(SysConstant.PAGE_LIMIT)
			.list();

		return userMoneyLogList;
	}

	@Override
	public BigDecimal totalAmountV9() {
		return baseMapper.totalAmountV9();
	}

	/**
	 * 查询钱包流水列表
	 *
	 * @param userMoneyLog 钱包流水
	 * @return 钱包流水
	 */
	@Override
	public List<UserMoneyLog> selectUserMoneyLogList(UserMoneyLog userMoneyLog) {
		return baseMapper.selectUserMoneyLogList(userMoneyLog);
	}


	/**
	 * 查询昨日收益
	 * @param userId
	 * @return
	 */
	@Override
	public BigDecimal queryYesterdayReward(Long userId) {
		return baseMapper.queryYesterdayReward(userId);
	}


	/**
	 * 查询当月收益
	 * @param userId
	 * @return
	 */
	@Override
	public BigDecimal queryCurrentMonthReward(Long userId) {
		return baseMapper.queryCurrentMonthReward(userId);
	}

	@Override
	@RedisLock(value = RedisConstant.LockConstant.CANAL_MSG_IDEMPOTENT, param = "#after.gt_id")
	public boolean handerMsg(UserMoneyCanal before, UserMoneyCanal after) {
		//查询里面是否记录过
		Long count = lambdaQuery().select(UserMoneyLog::getId).eq(UserMoneyLog::getGtId, after.getGt_id()).eq(UserMoneyLog::getUserId, after.getId()).count();
		if (count > 0) {
			return true;
		}
		//处理数据
		List<UserMoneyLog> addUserMoneyList = Lists.newArrayList();
		//唯一ID
		String gtId = after.getGt_id();
		if (before.getValid_num1() != null) {
			UserMoneyLog userMoneyLog = UserMoneyLog.builder()
				.userId(after.getId())
				.gtId(gtId)
				.sourceId(after.getSource_id())
				.sourceCode(after.getSource_code())
				.sourceType(after.getSource_type())
				.beforeBalance(before.getValid_num1())
				.afterBalance(after.getValid_num1())
				.coinType(1)
				.changeBalance(after.getValid_num1().subtract(before.getValid_num1()))
				.serialCode(IDUtils.getSnowflake().nextIdStr()).build();
			userMoneyLog.setCreateTime(after.getUpdate_time());
			addUserMoneyList.add(userMoneyLog);
		}
		if (before.getValid_num2() != null) {
			UserMoneyLog userMoneyLog = UserMoneyLog.builder()
				.userId(after.getId())
				.gtId(gtId)
				.sourceId(after.getSource_id())
				.sourceCode(after.getSource_code())
				.sourceType(after.getSource_type())
				.beforeBalance(before.getValid_num2())
				.afterBalance(after.getValid_num2())
				.changeBalance(after.getValid_num2().subtract(before.getValid_num2()))
				.coinType(ConstantType.user_money_coin_type.type_2)
				.serialCode(IDUtils.getSnowflake().nextIdStr()).build();
			userMoneyLog.setCreateTime(after.getUpdate_time());
			addUserMoneyList.add(userMoneyLog);
		}
		if (before.getValid_num3() != null) {
			UserMoneyLog userMoneyLog = UserMoneyLog.builder()
				.userId(after.getId())
				.gtId(gtId)
				.sourceId(after.getSource_id())
				.sourceCode(after.getSource_code())
				.sourceType(after.getSource_type())
				.beforeBalance(before.getValid_num3())
				.afterBalance(after.getValid_num3())
				.changeBalance(after.getValid_num3().subtract(before.getValid_num3()))
				.coinType(ConstantType.user_money_coin_type.type_3)
				.serialCode(IDUtils.getSnowflake().nextIdStr()).build();
			userMoneyLog.setCreateTime(after.getUpdate_time());
			addUserMoneyList.add(userMoneyLog);
		}
		if (before.getValid_num4() != null) {
			UserMoneyLog userMoneyLog = UserMoneyLog.builder()
				.userId(after.getId())
				.gtId(gtId)
				.sourceId(after.getSource_id())
				.sourceCode(after.getSource_code())
				.sourceType(after.getSource_type())
				.beforeBalance(before.getValid_num4())
				.afterBalance(after.getValid_num4())
				.changeBalance(after.getValid_num4().subtract(before.getValid_num4()))
				.coinType(ConstantType.user_money_coin_type.type_4)
				.serialCode(IDUtils.getSnowflake().nextIdStr()).build();
			userMoneyLog.setCreateTime(after.getUpdate_time());
			addUserMoneyList.add(userMoneyLog);
		}
		if (before.getValid_num5() != null) {
			UserMoneyLog userMoneyLog = UserMoneyLog.builder()
				.userId(after.getId())
				.gtId(gtId)
				.sourceId(after.getSource_id())
				.sourceCode(after.getSource_code())
				.sourceType(after.getSource_type())
				.beforeBalance(before.getValid_num5())
				.afterBalance(after.getValid_num5())
				.changeBalance(after.getValid_num5().subtract(before.getValid_num5()))
				.coinType(ConstantType.user_money_coin_type.type_5)
				.serialCode(IDUtils.getSnowflake().nextIdStr()).build();
			userMoneyLog.setCreateTime(after.getUpdate_time());
			addUserMoneyList.add(userMoneyLog);
		}
		if (before.getValid_num6() != null) {
			UserMoneyLog userMoneyLog = UserMoneyLog.builder()
				.userId(after.getId())
				.gtId(gtId)
				.sourceId(after.getSource_id())
				.sourceCode(after.getSource_code())
				.sourceType(after.getSource_type())
				.beforeBalance(before.getValid_num6())
				.afterBalance(after.getValid_num6())
				.changeBalance(after.getValid_num6().subtract(before.getValid_num6()))
				.coinType(ConstantType.user_money_coin_type.type_6)
				.serialCode(IDUtils.getSnowflake().nextIdStr()).build();
			userMoneyLog.setCreateTime(after.getUpdate_time());
			addUserMoneyList.add(userMoneyLog);
		}
		if (before.getValid_num7() != null) {
			UserMoneyLog userMoneyLog = UserMoneyLog.builder()
				.userId(after.getId())
				.gtId(gtId)
				.sourceId(after.getSource_id())
				.sourceCode(after.getSource_code())
				.sourceType(after.getSource_type())
				.beforeBalance(before.getValid_num7())
				.afterBalance(after.getValid_num7())
				.changeBalance(after.getValid_num7().subtract(before.getValid_num7()))
				.coinType(ConstantType.user_money_coin_type.type_7)
				.serialCode(IDUtils.getSnowflake().nextIdStr()).build();
			userMoneyLog.setCreateTime(after.getUpdate_time());
			addUserMoneyList.add(userMoneyLog);
		}
		if (before.getValid_num8() != null) {
			UserMoneyLog userMoneyLog = UserMoneyLog.builder()
				.userId(after.getId())
				.gtId(gtId)
				.sourceId(after.getSource_id())
				.sourceCode(after.getSource_code())
				.sourceType(after.getSource_type())
				.beforeBalance(before.getValid_num8())
				.afterBalance(after.getValid_num8())
				.changeBalance(after.getValid_num8().subtract(before.getValid_num8()))
				.coinType(ConstantType.user_money_coin_type.type_8)
				.serialCode(IDUtils.getSnowflake().nextIdStr()).build();
			userMoneyLog.setCreateTime(after.getUpdate_time());
			addUserMoneyList.add(userMoneyLog);
		}
		if (before.getValid_num9() != null) {
			UserMoneyLog userMoneyLog = UserMoneyLog.builder()
				.userId(after.getId())
				.gtId(gtId)
				.sourceId(after.getSource_id())
				.sourceCode(after.getSource_code())
				.sourceType(after.getSource_type())
				.beforeBalance(before.getValid_num9())
				.afterBalance(after.getValid_num9())
				.changeBalance(after.getValid_num9().subtract(before.getValid_num9()))
				.coinType(ConstantType.user_money_coin_type.type_9)
				.serialCode(IDUtils.getSnowflake().nextIdStr()).build();
			userMoneyLog.setCreateTime(after.getUpdate_time());
			addUserMoneyList.add(userMoneyLog);
		}
		if (!addUserMoneyList.isEmpty()) {
			return SpringUtils.getAopProxy(this).saveBatch(addUserMoneyList);
		}
		return true;
	}


	/**
	 * @param pageIndex
	 * @param pageSize
	 * @param userId
	 * @param coinType
	 * @param sourceType
	 * @return List<UserMoneyLogBo>
	 * @Title: getUserMoneyLogList
	 * @param:
	 * @Description: 钱包流水
	 */
	@Override
	public PageInfo<UserMoneyLogBo> getUserMoneyLogList(Integer pageIndex, Integer pageSize, Long userId, Integer coinType, Integer sourceType) {
		PageHelper.startPage(pageIndex, pageSize);
		PageInfo<UserMoneyLogBo> pageInfo = new PageInfo<>(this.baseMapper.getUserMoneyLogList(userId, coinType,sourceType));
		return pageInfo;
	}

	@Override
	public ResultPista<List<SysDictData>> listRecordBiz(String dictType) {
		try {
			List<SysDictData> dictDatas = DictUtils.getDictCache(dictType);
			if (StringUtils.isNotEmpty(dictDatas)) {
				return ResultPista.data(dictDatas);
			}
			dictDatas = baseMapper.selectDictDataByType(dictType);
			if (StringUtils.isNotEmpty(dictDatas)) {
				DictUtils.setDictCache(dictType, dictDatas);
				return ResultPista.data(dictDatas);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ResultPista.fail();
	}
}
