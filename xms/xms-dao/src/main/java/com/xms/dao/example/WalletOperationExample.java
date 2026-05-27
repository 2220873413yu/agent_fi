package com.xms.dao.example;

import com.xms.common.constant.ConstantType;
import com.xms.common.exception.ServiceException;
import com.xms.common.result.ResponseCode;
import com.xms.common.utils.uuid.IDUtils;
import com.xms.dao.service.UserWalletService;

import java.math.BigDecimal;

/**
 * 单个用户钱包余额变动示例。
 *
 * <p>适用于充值入账、提现扣减、驳回退回、单笔奖励等只影响 1-2 个用户的场景。
 * 真实业务复制本示例时，需要按业务确认 sourceType、sourceId 和 coinType。</p>
 */
public class WalletOperationExample {

	/**
	 * 给单个用户增加 USDT 可用余额，并通过钱包标准入口保留流水追踪字段。
	 *
	 * @param userWalletService 钱包服务
	 * @param userId 钱包所属用户 ID
	 * @param sourceOrderNo 来源订单号
	 * @param sourceId 来源业务主键，优先使用订单 ID
	 * @param amount 入账金额，单位 USDT，必须为正数
	 */
	public void addUsdt(UserWalletService userWalletService, Long userId, String sourceOrderNo, Long sourceId, BigDecimal amount) {
		int rows = userWalletService.handerUserMoney(
			amount,
			sourceOrderNo,
			userId,
			sourceId,
			ConstantType.user_money_log_source_type.type_38,
			ConstantType.user_money_coin_type.type_1
		);
		checkOneRow(rows);
	}

	/**
	 * 从单个用户 AFI 可用余额扣款，适合下单、扣费、质押等单笔扣减场景。
	 *
	 * @param userWalletService 钱包服务
	 * @param userId 钱包所属用户 ID
	 * @param sourceOrderNo 来源订单号
	 * @param sourceId 来源业务主键，优先使用订单 ID
	 * @param amount 扣减金额，单位 AFI，传入正数，方法内部取反
	 */
	public void deductAfi(UserWalletService userWalletService, Long userId, String sourceOrderNo, Long sourceId, BigDecimal amount) {
		String gtId = IDUtils.getSnowflake(ConstantType.user_money_coin_type.type_2).nextIdStr();
		int rows = userWalletService.handerUserMoney(
			amount.negate(),
			sourceOrderNo,
			userId,
			sourceId,
			ConstantType.user_money_log_source_type.type_40,
			ConstantType.user_money_coin_type.type_2,
			gtId
		);
		checkOneRow(rows);
	}

	/**
	 * 校验单笔钱包变动必须只影响一行，失败时抛业务异常让外层事务回滚。
	 *
	 * @param rows 钱包更新影响行数
	 */
	private void checkOneRow(int rows) {
		if (rows != 1) {
			throw new ServiceException(ResponseCode.CODE_1015);
		}
	}
}
