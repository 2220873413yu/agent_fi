package com.xms.dao.example;

import com.xms.common.constant.ConstantType;
import com.xms.common.exception.ServiceException;
import com.xms.common.result.ResponseCode;
import com.xms.dao.service.UserWalletService;

import java.math.BigDecimal;

/**
 * 充值和提现钱包处理示例。
 *
 * <p>充值、提现都必须先有业务记录和状态，再通过钱包标准入口入账或扣减。
 * 回调和审核场景需要按业务主键做幂等判断，避免重复入账或重复退款。</p>
 */
public class RechargeWithdrawExample {

	/**
	 * 充值成功后给用户 USDT 入账。
	 *
	 * @param userWalletService 钱包服务
	 * @param userId 充值用户 ID
	 * @param rechargeNo 充值订单号
	 * @param rechargeId 充值记录主键
	 * @param amount 充值金额，单位 USDT
	 */
	public void confirmRecharge(UserWalletService userWalletService, Long userId, String rechargeNo,
								Long rechargeId, BigDecimal amount) {
		int rows = userWalletService.handerUserMoney(
			amount,
			rechargeNo,
			userId,
			rechargeId,
			ConstantType.user_money_log_source_type.type_38,
			ConstantType.user_money_coin_type.type_1
		);
		checkOneRow(rows);
	}

	/**
	 * 用户提交提现申请时扣减可用余额。
	 *
	 * @param userWalletService 钱包服务
	 * @param userId 提现用户 ID
	 * @param withdrawalNo 提现订单号
	 * @param withdrawalId 提现记录主键
	 * @param amount 扣减金额，传入正数，方法内部取反
	 * @param coinType 提现币种
	 */
	public void applyWithdrawal(UserWalletService userWalletService, Long userId, String withdrawalNo,
								Long withdrawalId, BigDecimal amount, Integer coinType) {
		int rows = userWalletService.handerUserMoney(
			amount.negate(),
			withdrawalNo,
			userId,
			withdrawalId,
			ConstantType.user_money_log_source_type.type_4,
			coinType
		);
		checkOneRow(rows);
	}

	/**
	 * 提现审核驳回时退回用户余额。
	 *
	 * @param userWalletService 钱包服务
	 * @param userId 提现用户 ID
	 * @param withdrawalNo 提现订单号
	 * @param withdrawalId 提现记录主键
	 * @param amount 退回金额，单位由 coinType 决定
	 * @param coinType 提现币种
	 */
	public void rejectWithdrawal(UserWalletService userWalletService, Long userId, String withdrawalNo,
								 Long withdrawalId, BigDecimal amount, Integer coinType) {
		int rows = userWalletService.handerUserMoney(
			amount,
			withdrawalNo,
			userId,
			withdrawalId,
			ConstantType.user_money_log_source_type.type_5,
			coinType
		);
		checkOneRow(rows);
	}

	/**
	 * 校验充值或提现钱包变动必须只影响一行。
	 *
	 * @param rows 钱包更新影响行数
	 */
	private void checkOneRow(int rows) {
		if (rows != 1) {
			throw new ServiceException(ResponseCode.CODE_1015);
		}
	}
}
