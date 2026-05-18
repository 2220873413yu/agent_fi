package com.xms.web.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.xms.common.config.redis.delayqueue.RedissonDelayOrder;
import com.xms.common.constant.ConstantStatic;
import com.xms.common.constant.ConstantType;
import com.xms.common.constant.SysConstant;
import com.xms.common.exception.ServiceException;
import com.xms.common.result.ResponseCode;
import com.xms.common.utils.uuid.IDUtils;
import com.xms.dao.domain.*;
import com.xms.dao.entity.domain.UserInfo;
import com.xms.dao.entity.domain.UserMoney;
import com.xms.dao.service.*;
import com.xms.web.service.StoreOrderAutoService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author: renengadePISTA
 * @createDate: 2023/12/9
 */
@Service
@AllArgsConstructor
@Slf4j
public class StoreOrderAutoServiceImpl implements StoreOrderAutoService {

	private static final String SQL_VALID_NUM3 = "UPDATE t_user_money SET update_time=?,gt_id=?,valid_num3=valid_num3+?,source_code=?,source_type=?,source_id=? WHERE id=? ";



	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private UserInfoService userInfoService;


	@Autowired
	private ISwapOrderService swapOrderService;

	@Autowired
	private IPolymarketOrderService polymarketOrderService;

	/**
	 * 处理Redisson延迟队列到期消息。
	 *
	 * <p>该入口是后台统一延迟任务分发器。Polymarket使用bizType=30，消息到期后只按marketSlug触发市场级结算；
	 * 真正是否能结算由市场表状态和Polymarket resolved结果共同决定。</p>
	 *
	 * @param order 延迟队列消息，orderId用于承载业务主键
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void hanlerOrder(RedissonDelayOrder order) {
		log.info("活期基金延迟到账、任务 order:{},date:{}", order,new Date());
		if(order.getBizType()!=null){
			if(order.getBizType().equals(2)){
				//查询用户是否已经支付了节点订单
				task2(order);
			}else if(order.getBizType().equals(3)){
				//增加提现额度
				task3(order);
			}else if(order.getBizType().equals(4)){
				//激活订单过期关闭
				task4(order);
			}else if(order.getBizType().equals(SysConstant.THIRTY)){
				// Polymarket市场结束后的延迟触发；状态抢占和批量兑付在订单服务里完成。
				task30(order);
			}
		}

	}

	/**
	 * 触发Polymarket市场级结算。
	 *
	 * <p>该方法预留给后续Redisson延迟队列真正启用时使用。消费者只处理已经被派发为结算中的市场。</p>
	 *
	 * @param order 延迟消息，orderId为Polymarket marketSlug
	 */
	private void task30(RedissonDelayOrder order) {
		if (StrUtil.isBlank(order.getOrderId())) {
			log.warn("Polymarket市场结算延迟消息缺少marketSlug: {}", order);
			return;
		}
		polymarketOrderService.processSettlingMarket(order.getOrderId());
	}

	private void task4(RedissonDelayOrder order) {
	}

	private void task3(RedissonDelayOrder order) {
		SwapOrder swapOrder = swapOrderService.lambdaQuery()
			.eq(SwapOrder::getId, Integer.valueOf(order.getOrderId()))
			.eq(SwapOrder::getBizStatus,2)
			.eq(SwapOrder::getBizStatus1,0)
			.one();
		if(swapOrder !=null && swapOrder.getUserId()!=0L){
			userInfoService.lambdaUpdate()
				.eq(UserInfo::getUserId, swapOrder.getUserId())
				.setSql("have_withdrawal_balance = have_withdrawal_balance + " + swapOrder.getAvailableAmount())
				.update();
			boolean update = swapOrderService.lambdaUpdate()
				.eq(SwapOrder::getId, swapOrder.getId())
				.eq(SwapOrder::getBizStatus1, 0)
				.eq(SwapOrder::getBizStatus, 2)
				.set(SwapOrder::getBizStatus1, 1)
				.set(SwapOrder::getUpdateTime, new Date())
				.update();
			if(!update){
				throw new ServiceException("更新订单状态失败");
			}
		}else{
			log.info("swap订单已经处理了 订单id:{}", order.getOrderId());
		}
	}

	/**
	 * 查询用户是否已经支付了
	 * @param order
	 */
	private void task2(RedissonDelayOrder order) {
	}

	private void task1(RedissonDelayOrder order) {
//		MiningPackageOrder miningPackageOrder = miningPackageOrderService.lambdaQuery()
//			.eq(MiningPackageOrder::getOrderNo, order.getOrderId())
//			.in(MiningPackageOrder::getReturnedBizStatus, SysConstant.TWO)
//			.eq(MiningPackageOrder::getPrincipalReturned, 1)
//			.one();
//		if(miningPackageOrder == null){
//			return;
//		}
//
//		if(miningPackageOrder.getType().equals(0)){
//			//活期直接退本
//			Integer rowCount = userWalletServiceImpl.handerUserMoney(miningPackageOrder.getBuyPrice(), miningPackageOrder.getOrderNo(),
//				miningPackageOrder.getUserId(), miningPackageOrder.getUserId(),
//				ConstantType.user_money_log_source_type.type_22,
//				ConstantType.user_money_coin_type.type_1);
//			if (rowCount != 1) {
//				throw new ServiceException(ResponseCode.CODE_1005);
//			}
//		}else{
//			//1.1处理定期矿机退本业务逻辑
//			Integer rowCount = userWalletServiceImpl.handerUserMoney(miningPackageOrder.getBuyPrice(), miningPackageOrder.getOrderNo(),
//				miningPackageOrder.getUserId(), miningPackageOrder.getUserId(),
//				ConstantType.user_money_log_source_type.type_22,
//				ConstantType.user_money_coin_type.type_1);
//			if (rowCount != 1) {
//				throw new ServiceException(ResponseCode.CODE_1005);
//			}
//			// 基础违约金比例
//			BigDecimal penaltyRate = miningPackageOrder.getPenaltyRate();
//			// 已运行天数
//			Integer runDays = miningPackageOrder.getDays() - miningPackageOrder.getHaveDays();
//			// 每日递减比例
//			BigDecimal dailyPenaltyReduction = miningPackageOrder.getDailyPenaltyReduction();
//			// 本金
//			BigDecimal principal = miningPackageOrder.getBuyPrice();
//			// 计算当前违约金比例 = 基础比例 - (运行天数 × 每日递减比例)
//			BigDecimal currentPenaltyRate = penaltyRate.subtract(
//				new BigDecimal(runDays).multiply(dailyPenaltyReduction)
//					.setScale(ConstantStatic.newScale, ConstantStatic.roundingModeNew)
//			);
//
//			//违约金
//			BigDecimal currentPenalty = BigDecimal.ZERO;
//			// 确保违约金比例不为负数
//			if (currentPenaltyRate.compareTo(miningPackageOrder.getMinPenaltyRate()) < 0) {
//				//违约金如果小于最低违约金比例。就取最低的
//				currentPenaltyRate =  miningPackageOrder.getMinPenaltyRate();
//			}
//			currentPenalty = principal.multiply(currentPenaltyRate.divide(SysConstant.BAIFENBI, ConstantStatic.newScale, ConstantStatic.roundingModeNew))
//				.setScale(ConstantStatic.newScale, ConstantStatic.roundingModeNew);
//			//违约金
//			if(currentPenalty.compareTo(BigDecimal.ZERO) > 0){
//				rowCount = userWalletServiceImpl.handerUserMoney(currentPenalty.negate(), miningPackageOrder.getOrderNo(),
//					miningPackageOrder.getUserId(), miningPackageOrder.getUserId(),
//					ConstantType.user_money_log_source_type.type_26,
//					ConstantType.user_money_coin_type.type_1);
//				if (rowCount != 1) {
//					throw new ServiceException(ResponseCode.CODE_1015);
//				}
//			}
//		}
//
//
//		boolean update = miningPackageOrderService.lambdaUpdate()
//			.eq(MiningPackageOrder::getOrderNo, order.getOrderId())
//			.eq(MiningPackageOrder::getReturnedBizStatus, SysConstant.TWO)
//			.eq(MiningPackageOrder::getPrincipalReturned, 1)
//			.set(MiningPackageOrder::getReturnedBizStatus, SysConstant.THREE)
//			.update();
//		if(!update){
//			throw new ServiceException(ResponseCode.CODE_1002);
//		}
	}

	/**
	 * 对佣金钱包资产增加
	 *
	 * @param userMoneyList
	 */
	private void bachUpdateMoneyValid3(List<UserMoney> userMoneyList) {
		int[] ints = jdbcTemplate.batchUpdate(SQL_VALID_NUM3, new BatchPreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException {

				ps.setTimestamp(1, new java.sql.Timestamp(userMoneyList.get(i).getUpdateTime().getTime()));
				ps.setString(2, userMoneyList.get(i).getGtId());
				ps.setBigDecimal(3, userMoneyList.get(i).getValidNum3());
				ps.setString(4, userMoneyList.get(i).getSourceCode());
				ps.setInt(5, userMoneyList.get(i).getSourceType());
				ps.setLong(6, userMoneyList.get(i).getSourceId());
				ps.setLong(7, userMoneyList.get(i).getId());
			}

			@Override
			public int getBatchSize() {
				return userMoneyList.size();
			}
		});
		if (ArrayUtil.contains(ints, 0)) {
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			log.error("结算更新回滚了");
			throw new ServiceException("更新资产结算更新回滚了");
		}
	}
}
