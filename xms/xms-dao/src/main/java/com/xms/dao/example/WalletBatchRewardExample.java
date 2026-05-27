package com.xms.dao.example;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ArrayUtil;
import com.xms.common.constant.ConstantType;
import com.xms.common.exception.ServiceException;
import com.xms.common.result.ResponseCode;
import com.xms.common.utils.uuid.IDUtils;
import com.xms.dao.entity.domain.UserMoney;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 批量奖励或定时结算钱包入账示例。
 *
 * <p>适用于定时任务发奖、批量结算、多人兑付等预计影响 5 个以上用户的场景。
 * 真实业务应先收集 {@link UserMoney} 增量，每 1000 条 flush 一次，失败时回滚外层事务。</p>
 */
public class WalletBatchRewardExample {
	private static final int WALLET_BATCH_SIZE = 1000;
	private static final String SQL_VALID_NUM2 =
		"UPDATE t_user_money SET update_time=?,gt_id=?,valid_num2=valid_num2+?,source_code=?,source_type=?,source_id=? WHERE id=? ";

	/**
	 * 收集 AFI 批量奖励并按批次写入钱包 validNum2。
	 *
	 * @param jdbcTemplate JDBC 批量更新入口
	 * @param rewards 已确认可发放的奖励列表
	 * @param now 本次结算时间
	 */
	public void grantAfiRewards(JdbcTemplate jdbcTemplate, List<RewardGrant> rewards, Date now) {
		if (CollectionUtil.isEmpty(rewards)) {
			return;
		}
		List<UserMoney> walletIncrements = new ArrayList<>(Math.min(rewards.size(), WALLET_BATCH_SIZE));
		for (RewardGrant reward : rewards) {
			// 只收集已计算好的奖励结果，循环里不逐条调用单笔钱包入口。
			walletIncrements.add(buildAfiWalletIncrement(reward, now));
			if (walletIncrements.size() >= WALLET_BATCH_SIZE) {
				batchUpdateMoneyValid2(jdbcTemplate, walletIncrements);
				walletIncrements.clear();
			}
		}
		batchUpdateMoneyValid2(jdbcTemplate, walletIncrements);
		walletIncrements.clear();
	}

	/**
	 * 构造 AFI 钱包 validNum2 增量记录，保留来源订单号、来源业务 ID 和 gtId。
	 *
	 * @param reward 已确认可发放的奖励
	 * @param now 本次结算时间
	 * @return 可用于批量入账的 UserMoney 增量
	 */
	private UserMoney buildAfiWalletIncrement(RewardGrant reward, Date now) {
		UserMoney userMoney = new UserMoney();
		userMoney.setId(reward.userId());
		userMoney.setValidNum2(reward.amount());
		userMoney.setGtId(IDUtils.getSnowflake(ConstantType.user_money_coin_type.type_2).nextIdStr());
		userMoney.setSourceCode(reward.sourceCode());
		userMoney.setSourceType(reward.sourceType());
		userMoney.setSourceId(reward.sourceId());
		userMoney.setUpdateTime(now);
		return userMoney;
	}

	/**
	 * 批量增加用户 AFI 可用余额 validNum2。
	 *
	 * <p>任一行更新失败时标记当前事务回滚，并抛出钱包更新异常。</p>
	 *
	 * @param jdbcTemplate JDBC 批量更新入口
	 * @param userMoneyList AFI 钱包增量列表
	 */
	private void batchUpdateMoneyValid2(JdbcTemplate jdbcTemplate, List<UserMoney> userMoneyList) {
		if (CollectionUtil.isEmpty(userMoneyList)) {
			return;
		}
		int[] rows = jdbcTemplate.batchUpdate(SQL_VALID_NUM2, new BatchPreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException {
				UserMoney userMoney = userMoneyList.get(i);
				ps.setTimestamp(1, new java.sql.Timestamp(userMoney.getUpdateTime().getTime()));
				ps.setString(2, userMoney.getGtId());
				ps.setBigDecimal(3, userMoney.getValidNum2());
				ps.setString(4, userMoney.getSourceCode());
				ps.setInt(5, userMoney.getSourceType());
				ps.setLong(6, userMoney.getSourceId());
				ps.setLong(7, userMoney.getId());
			}

			@Override
			public int getBatchSize() {
				return userMoneyList.size();
			}
		});
		if (ArrayUtil.contains(rows, 0)) {
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			throw new ServiceException(ResponseCode.CODE_1015);
		}
	}

	/**
	 * 批量奖励已计算结果。
	 *
	 * @param userId 收款用户 ID
	 * @param amount AFI 奖励金额，单位 AFI
	 * @param sourceCode 来源订单号或结算批次号
	 * @param sourceType 钱包流水业务类型
	 * @param sourceId 来源业务主键
	 */
	public record RewardGrant(Long userId, BigDecimal amount, String sourceCode, Integer sourceType, Long sourceId) {
	}
}
