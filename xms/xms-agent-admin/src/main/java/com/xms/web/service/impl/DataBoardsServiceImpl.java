package com.xms.web.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.xms.common.config.redis.XmsRedis;
import com.xms.common.constant.ConstantStatic;
import com.xms.common.constant.ConstantType;
import com.xms.common.constant.RedisConstant;
import com.xms.common.constant.SysConstant;
import com.xms.common.thread.asyncTool.AsyncPlus;
import com.xms.common.thread.asyncTool.WorkerWrapper;
import com.xms.common.utils.Kv;
import com.xms.common.utils.SecurityUtils;
import com.xms.dao.domain.NodePackageOrder;
import com.xms.dao.domain.RechargeRecord;
import com.xms.dao.domain.UserTransfer;
import com.xms.dao.entity.domain.UserInfo;
import com.xms.dao.entity.domain.Withdrawal;
import com.xms.dao.entity.vo.IndexDataPanelVo;
import com.xms.dao.service.*;
import com.xms.dao.service.impl.WithdrawalServiceImpl;
import com.xms.web.domain.LineDataDO;
import com.xms.web.mapper.DataBoardMapper;
import com.xms.web.service.DataBoardsService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author: GT63S
 * @createDate: 2024/9/4
 */
@AllArgsConstructor
@Service
@Slf4j
public class DataBoardsServiceImpl implements DataBoardsService {
	private final UserInfoService userInfoServiceImpl;
	private final DataBoardMapper dataBoardMapper;
	private final IRechargeRecordService rechargeRecordService;
	private final XmsRedis xmsRedis;
	private final IndexDataService indexDataServiceImpl;
	private final IUserTransferService userTransferService;
	private final INodePackageOrderService nodePackageOrderService;

	@Override
	public Map<String, Object> getUserDataBoard() throws Exception {
		//查询充值数据
		WorkerWrapper<Long, Map<String, Double>> rechargeWrapper = getRechargeWrapper();
		//查询提现数据
		WorkerWrapper<Long, Map<String, BigDecimal>> withdrawWrapper = getWithdrawWrapper();
		//查询用户数据
		WorkerWrapper<Long, Map<String, Object>> userTotalWrapper = getUserTotalWrapper();
		AsyncPlus.beginWork(120000L, userTotalWrapper, rechargeWrapper, withdrawWrapper);
		Map<String, Object> set = Kv.create();
		if (userTotalWrapper.getWorkResult() != null) {
			set.putAll(userTotalWrapper.getWorkResult().getResult());
		}
		if (rechargeWrapper.getWorkResult() != null) {
			set.putAll(rechargeWrapper.getWorkResult().getResult());
		}
		if (withdrawWrapper.getWorkResult() != null) {
			set.putAll(withdrawWrapper.getWorkResult().getResult());
		}

		IndexDataPanelVo indexDataPanelVo = indexDataServiceImpl.getIndexDataPanelVo();
//		set.put("v3", indexDataPanelVo.getV3());
//		set.put("v4", indexDataPanelVo.getV4());
		set.put("v6", indexDataPanelVo.getV6());
		set.put("v7", indexDataPanelVo.getV7());
		set.put("v8", indexDataPanelVo.getV8());
		set.put("v9", indexDataPanelVo.getV9());
		set.put("v10", indexDataPanelVo.getV10());
		set.put("v11", indexDataPanelVo.getV11());
		set.put("v12", indexDataPanelVo.getV12());
		set.put("v13", indexDataPanelVo.getV13());
//		set.put("v14", indexDataPanelVo.getV14());
//		set.put("v15", indexDataPanelVo.getV15());
//		set.put("v16", indexDataPanelVo.getV16());
//		set.put("v17", indexDataPanelVo.getV17());
//		set.put("v24", indexDataPanelVo.getV24());
//		set.put("v25", indexDataPanelVo.getV25());
		set.put("v26", indexDataPanelVo.getV26());
		set.put("v27", indexDataPanelVo.getV27());
		set.put("v28", indexDataPanelVo.getV28());

		set.put("v29", indexDataPanelVo.getV29());
		set.put("v30", indexDataPanelVo.getV30());
		set.put("v31", indexDataPanelVo.getV31());
		set.put("v32", indexDataPanelVo.getV32());
		set.put("v33", indexDataPanelVo.getV33());
		set.put("v34", indexDataPanelVo.getV34());
		set.put("v35", indexDataPanelVo.getV35());
		set.put("v36", indexDataPanelVo.getV36());
		set.put("v37", indexDataPanelVo.getV37());
		set.put("v38", indexDataPanelVo.getV38());
		set.put("v39", indexDataPanelVo.getV39());
		set.put("v40", indexDataPanelVo.getV40());

		List<Withdrawal> withdrawalList = SpringUtil.getBean(WithdrawalServiceImpl.class).lambdaQuery()
			.in(Withdrawal::getStatus, 3)
			.apply("create_time >= CURDATE()")
			.select(Withdrawal::getChangeBalance, Withdrawal::getCoinType)
			.list();

		BigDecimal v18= withdrawalList.stream()
			.filter(record->{
				return record.getCoinType() ==1;
			})
			.map(Withdrawal::getChangeBalance).reduce(BigDecimal.ZERO, BigDecimal::add);

//		BigDecimal v19= withdrawalList.stream()
//			.filter(record->{
//				return record.getCoinType() ==2;
//			})
//			.map(Withdrawal::getChangeBalance).reduce(BigDecimal.ZERO, BigDecimal::add);

		set.put("v18", v18);
		//bpay今日提现
		//set.put("v19", v19);
		set.put("v20", SpringUtil.getBean(WithdrawalServiceImpl.class).selectUsdtTotalBalance());
		//set.put("v21", SpringUtil.getBean(WithdrawalServiceImpl.class).selectBPayTotalBalance());


//		set.put("v22", rechargeRecordService.selectTotalRechargeBalance());
//		set.put("v23", rechargeRecordService.selectTodayRechargeBalance());
		return set;
	}

	@Override
	public Map<String, Object> listGroupTotal() throws Exception {
		Map<String, Object> set = Kv.create();
		//查询用户数据
		WorkerWrapper<Long, Map<String, Object>> userRegisterGroupWrapper = getUserRegisterGroupWrapper();

		//查询充值数据
		//WorkerWrapper<Long, Map<String, Object>> lineRechargeChartData = getLineRechargeChartData();
		//查询提现数据
		WorkerWrapper<Long, Map<String, Object>> lineWdwChartData = getLineWdwChartData();
		//查询节点订单数据
		WorkerWrapper<Long, Map<String, Object>> lineNodeOrderChartData = getLineNodeOrderChartData();
		/*//查询转账数据
		WorkerWrapper<Long, Map<String, Object>> lineTransferChartData = getLineTransferChartData();*/
		AsyncPlus.beginWork(120000L, userRegisterGroupWrapper, lineWdwChartData, lineNodeOrderChartData);
		if (userRegisterGroupWrapper.getWorkResult() != null) {
			set.put("userRegisterGroup", userRegisterGroupWrapper.getWorkResult().getResult());
		}
		if (lineWdwChartData.getWorkResult() != null) {
			set.put("lineWdwChartData", lineWdwChartData.getWorkResult().getResult());
		}
		if (lineNodeOrderChartData.getWorkResult() != null) {
			set.put("lineNodeOrderChartData", lineNodeOrderChartData.getWorkResult().getResult());
		}
		/*if (lineRechargeChartData.getWorkResult() != null) {
			set.put("lineRechargeChartData", lineRechargeChartData.getWorkResult().getResult());
		}*/
	/*	if (lineTransferChartData.getWorkResult() != null) {
			set.put("lineTransferChartData", lineTransferChartData.getWorkResult().getResult());
		}*/
		return set;
	}

	@Override
	public Map<String, Object> listOrderGroupTotal() throws Exception {
		//查询订单报单数量
		WorkerWrapper<Long, List<LineDataDO>> orderWrapper = getOrderWrapper();
		//查询拨比数量
		WorkerWrapper<Long, List<LineDataDO>> rewardWrapper = getRewardWrapper();
		AsyncPlus.beginWork(120000L, orderWrapper, rewardWrapper);
		Map<String, Object> set = Kv.create();
		TreeMap<String, LineDataDO> finalResult;
		if (orderWrapper.getWorkResult() != null) {
			List<LineDataDO> orderList = orderWrapper.getWorkResult().getResult();
			finalResult = new TreeMap<>(orderList.stream().collect(Collectors.toMap(LineDataDO::getXkey, p -> p)));
			List<LineDataDO> dos = null;
			if (rewardWrapper.getWorkResult() != null) {
				dos = rewardWrapper.getWorkResult().getResult();
				Map<String, LineDataDO> tempResult = dos.stream().collect(Collectors.toMap(LineDataDO::getXkey, p -> p));
				finalResult.putAll(tempResult);
			}
			//组装数据
			List<BigDecimal> orderAmount = new ArrayList<>();
			List<BigDecimal> rewardAmount = new ArrayList<>();
			List<String> everyDate = new ArrayList<>();
			for (Map.Entry<String, LineDataDO> entry : finalResult.entrySet()) {
				everyDate.add(entry.getKey());
				orderList.stream().filter(lineDataDO -> lineDataDO.getXkey().equals(entry.getKey())).findFirst().
					ifPresentOrElse(lineDataDO ->
							orderAmount.add(lineDataDO.getExpectedRewardData()),
						() -> orderAmount.add(BigDecimal.ZERO));
				if (dos != null) {
					dos.stream().filter(lineDataDO -> lineDataDO.getXkey().equals(entry.getKey())).findFirst().
						ifPresentOrElse(lineDataDO -> rewardAmount.add(lineDataDO.getExpectedRewardDataV2()),
							() -> rewardAmount.add(BigDecimal.ZERO));
				} else {
					rewardAmount.add(BigDecimal.ZERO);
				}
			}
			set.put("key", everyDate);
			set.put("orderAmount", orderAmount);
			set.put("rewardAmount", rewardAmount);
		}
		return set;
	}

	private WorkerWrapper<Long, List<LineDataDO>> getRewardWrapper() {
		return new WorkerWrapper.Builder<Long, List<LineDataDO>>()
			.id("8")
			.worker((Long userId, Map<String, WorkerWrapper> allWrappers) -> {
				String yes = DateUtil.formatDate(DateUtil.yesterday());
				List<LineDataDO> result = xmsRedis.get(RedisConstant.USER_REWARD_GROUP + yes, dataBoardMapper::listRewardGroupTotal,
					RedisConstant.SECONDS_EXPIRE_TIME / SysConstant.TWO, TimeUnit.MINUTES);
				//计算今天的
				BigDecimal reward = dataBoardMapper.listRewardGroupTotalToday();
				if (reward != null) {
					if (result == null || result.isEmpty()) {
						result = new ArrayList<>();
					}
					LineDataDO lineDataDO = new LineDataDO();
					lineDataDO.setXkey(DateUtil.formatDate(new Date()));
					lineDataDO.setExpectedRewardDataV2(reward);
					result.add(lineDataDO);
				}
				return result;
			})
			.param(SecurityUtils.getUserId()).build();
	}

	private WorkerWrapper<Long, List<LineDataDO>> getOrderWrapper() {
		return new WorkerWrapper.Builder<Long, List<LineDataDO>>()
			.id("7")
			.worker((Long userId, Map<String, WorkerWrapper> allWrappers) -> {
				String yes = DateUtil.formatDate(DateUtil.yesterday());
				List<LineDataDO> result = new ArrayList<>();
				if (result == null || result.isEmpty()) {
					result = new ArrayList<>();
				}
				LineDataDO lineDataDO = new LineDataDO();
				BigDecimal usdtNum = BigDecimal.ZERO;
				lineDataDO.setExpectedRewardData(usdtNum);
				result.add(lineDataDO);
				return result;
			})
			.param(SecurityUtils.getUserId()).build();
	}

	private WorkerWrapper<Long, Map<String, Object>> getLineWdwChartData(String param) {
		return new WorkerWrapper.Builder<Long, Map<String, Object>>()
			.id("6")
			.worker((Long userId, Map<String, WorkerWrapper> allWrappers) -> {
				Map<String, Object> realData = new HashMap<>(2);
				String yes = DateUtil.formatDate(DateUtil.yesterday());
				Map<String, Object> yesData = xmsRedis.get(RedisConstant.USER_WITHDRAW_GROUP + yes, () -> {
					Map<String, Object> map = new HashMap<>(2);
					map.put("expectedData", null);
					map.put("key", null);
					List<LineDataDO> felTotal = dataBoardMapper.getLineWdwChartData();
					if (!felTotal.isEmpty()) {
						List<BigDecimal> price = new ArrayList<>();
						List<String> everyDate = new ArrayList<>();
						for (LineDataDO objectMap : felTotal) {
							price.add(objectMap.getExpectedRewardData());
							everyDate.add(objectMap.getXkey());
						}
						map.put("expectedRewardData", price);
						map.put("key", everyDate);
					}
					return map;
				}, RedisConstant.SECONDS_EXPIRE_TIME / SysConstant.TWO, TimeUnit.MINUTES);
				realData.putAll(yesData);
				//计算今天的
				BigDecimal felTotalToday = dataBoardMapper.getLineWdwChartDataToday();
				if (felTotalToday != null) {
					if (realData.get("key") != null) {
						List<String> keys = (List<String>) realData.get("key");
						List<BigDecimal> expectedDatas = (List<BigDecimal>) realData.get("expectedRewardData");
						keys.add(DateUtil.today());
						expectedDatas.add(felTotalToday);
					} else {
						List<String> keys = new ArrayList<>();
						List<BigDecimal> expectedDatas = new ArrayList<>();
						keys.add(DateUtil.today());
						expectedDatas.add(felTotalToday);
						realData.put("expectedRewardData", expectedDatas);
						realData.put("key", keys);
					}
				}
				return realData;
			})
			.param(SecurityUtils.getUserId())
			.build();
	}

	/**
	 * 统计后台首页近7天提现趋势图数据。
	 *
	 * <p>当前图表只展示USDT和AFI两条提现曲线：`coin_type=1`累计到`withdrawUsdt`，
	 * `coin_type=2`累计到`withdrawAfi`。缓存key使用`:v2`后缀，避免读取历史只包含USDT或旧DFC字段的缓存结构。</p>
	 *
	 * @return 异步任务包装结果，包含key日期列表、lineType币种列表、withdrawUsdt和withdrawAfi数据
	 */
	private WorkerWrapper<Long, Map<String, Object>> getLineWdwChartData() {
		return new WorkerWrapper.Builder<Long, Map<String, Object>>()
			.id("6")
			.worker((Long userId, Map<String, WorkerWrapper> allWrappers) -> {
				Map<String, Object> realData = new HashMap<>(4);
				String yes = DateUtil.formatDate(DateUtil.yesterday());
				// 昨天以前的近7天数据走Redis缓存；缓存版本升级到v2，确保返回USDT+AFI结构。
				Map<String, Object> yesData = xmsRedis.get(RedisConstant.USER_WITHDRAW_GROUP + yes + ":v2", () -> {
					Map<String, Object> map = new HashMap<>(8);
					map.put("withdrawUsdt", null);
					map.put("withdrawAfi", null);
					map.put("key", null);

					List<Withdrawal> list1 = SpringUtil.getBean(WithdrawalServiceImpl.class).lambdaQuery()
						.in(Withdrawal::getStatus, 3)
						.apply(" create_time < CURDATE()  AND create_time >= DATE_SUB(CURDATE(), INTERVAL 7 DAY)")
						.orderByAsc(Withdrawal::getId)
						.list();

					List<BigDecimal> priceUsdt = new ArrayList<>();
					List<BigDecimal> priceAfi = new ArrayList<>();
					List<String> everyDate = new ArrayList<>();
					if (!list1.isEmpty()) {
						// 按自然日分组后分别累计USDT和AFI提现数量，其他历史币种暂不在本图表展示。
						TreeMap<String, List<Withdrawal>> temp = new TreeMap<>(list1.stream().collect(Collectors.groupingBy(recharge ->
							DateUtil.formatDate(recharge.getCreateTime()))));
						for (Map.Entry<String, List<Withdrawal>> entry : temp.entrySet()) {
							everyDate.add(entry.getKey());
							BigDecimal usdtNum = BigDecimal.ZERO;
							BigDecimal afiNum = BigDecimal.ZERO;
							for (Withdrawal withdrawal : entry.getValue()) {
								if (withdrawal.getCoinType() != null
									&& withdrawal.getCoinType() == ConstantType.user_money_coin_type.type_1) {
									usdtNum = usdtNum.add(withdrawal.getChangeBalance());
								} else if (withdrawal.getCoinType() != null
									&& withdrawal.getCoinType() == ConstantType.user_money_coin_type.type_2) {
									afiNum = afiNum.add(withdrawal.getChangeBalance());
								}
							}
							priceUsdt.add(usdtNum);
							priceAfi.add(afiNum);
						}
						map.put("withdrawUsdt", priceUsdt);
						map.put("withdrawAfi", priceAfi);
						map.put("key", everyDate);
					}
					return map;
				}, RedisConstant.SECONDS_EXPIRE_TIME / SysConstant.TWO, TimeUnit.MINUTES);
				realData.putAll(yesData);

				// 今天数据不进缓存，每次实时查询后追加到历史日期和两条币种曲线后面。
				List<Withdrawal> list = SpringUtil.getBean(WithdrawalServiceImpl.class).lambdaQuery()
					.in(Withdrawal::getStatus, 3)
					.apply(" create_time >=CURDATE() ").orderByAsc(Withdrawal::getId).list();

				if (list != null && !list.isEmpty()) {
					List<BigDecimal> priceUsdt = new ArrayList<>();
					List<BigDecimal> priceAfi = new ArrayList<>();
					List<String> everyDate = new ArrayList<>();
					if (realData.get("key") != null) {
						everyDate = (List<String>) realData.get("key");
						priceUsdt = (List<BigDecimal>) realData.get("withdrawUsdt");
						priceAfi = (List<BigDecimal>) realData.get("withdrawAfi");
					}
					everyDate.add(DateUtil.formatDate(list.getFirst().getCreateTime()));
					BigDecimal usdtNum = BigDecimal.ZERO;
					BigDecimal afiNum = BigDecimal.ZERO;
					for (Withdrawal withdrawal : list) {
						if (withdrawal.getCoinType() != null
							&& withdrawal.getCoinType() == ConstantType.user_money_coin_type.type_1) {
							usdtNum = usdtNum.add(withdrawal.getChangeBalance());
						} else if (withdrawal.getCoinType() != null
							&& withdrawal.getCoinType() == ConstantType.user_money_coin_type.type_2) {
							afiNum = afiNum.add(withdrawal.getChangeBalance());
						}
					}
					priceUsdt.add(usdtNum);
					priceAfi.add(afiNum);
					realData.put("withdrawUsdt", priceUsdt);
					realData.put("withdrawAfi", priceAfi);
					realData.put("key", everyDate);
				}
				// 前端BarChart按lineType数组从withdrawUsdt/withdrawAfi中取对应数据渲染多条柱状线。
				realData.put("lineType", Arrays.asList("USDT", "AFI"));
				return realData;
			})
			.param(SecurityUtils.getUserId())
			.build();
	}

	private WorkerWrapper<Long, Map<String, Object>> getLineNodeOrderChartData() {
		return new WorkerWrapper.Builder<Long, Map<String, Object>>()
			.id("10")
			.worker((Long userId, Map<String, WorkerWrapper> allWrappers) -> {
				Map<String, Object> realData = new HashMap<>(4);
				String yes = DateUtil.formatDate(DateUtil.yesterday());
				Map<String, Object> yesData = xmsRedis.get(RedisConstant.USER_ORDER_GROUP + "node:" + yes, () -> {
					Map<String, Object> map = new HashMap<>(4);
					map.put("withdrawUsdt", null);
					map.put("withdrawEnergyPool", null);
					map.put("key", null);

					List<NodePackageOrder> list = nodePackageOrderService.lambdaQuery()
						.eq(NodePackageOrder::getStatus, 1)
						.apply(" create_time < CURDATE() AND create_time >= DATE_SUB(CURDATE(), INTERVAL 7 DAY)")
						.orderByAsc(NodePackageOrder::getId)
						.list();
					if (list != null && !list.isEmpty()) {
						TreeMap<String, List<NodePackageOrder>> temp = new TreeMap<>(list.stream()
							.collect(Collectors.groupingBy(order -> DateUtil.formatDate(order.getCreateTime()))));
						List<BigDecimal> nodeOrderAmountList = new ArrayList<>();
						List<BigDecimal> nodeOrderCountList = new ArrayList<>();
						List<String> everyDate = new ArrayList<>();
						for (Map.Entry<String, List<NodePackageOrder>> entry : temp.entrySet()) {
							everyDate.add(entry.getKey());
							BigDecimal nodeOrderAmount = entry.getValue().stream()
								.map(order -> order.getOrderValueUsdt() == null ? BigDecimal.ZERO : order.getOrderValueUsdt())
								.reduce(BigDecimal.ZERO, BigDecimal::add);
							nodeOrderAmountList.add(nodeOrderAmount);
							nodeOrderCountList.add(BigDecimal.valueOf(entry.getValue().size()));
						}
						map.put("withdrawUsdt", nodeOrderAmountList);
						map.put("withdrawEnergyPool", nodeOrderCountList);
						map.put("key", everyDate);
					}
					return map;
				}, RedisConstant.SECONDS_EXPIRE_TIME / SysConstant.TWO, TimeUnit.MINUTES);
				realData.putAll(yesData);

				List<NodePackageOrder> todayList = nodePackageOrderService.lambdaQuery()
					.eq(NodePackageOrder::getStatus, 1)
					.apply(" create_time >= CURDATE()")
					.orderByAsc(NodePackageOrder::getId)
					.list();
				if (todayList != null && !todayList.isEmpty()) {
					List<String> everyDate = realData.get("key") == null
						? new ArrayList<>()
						: (List<String>) realData.get("key");
					List<BigDecimal> nodeOrderAmountList = realData.get("withdrawUsdt") == null
						? new ArrayList<>()
						: (List<BigDecimal>) realData.get("withdrawUsdt");
					List<BigDecimal> nodeOrderCountList = realData.get("withdrawEnergyPool") == null
						? new ArrayList<>()
						: (List<BigDecimal>) realData.get("withdrawEnergyPool");

					everyDate.add(DateUtil.formatDate(todayList.getFirst().getCreateTime()));
					BigDecimal todayNodeOrderAmount = todayList.stream()
						.map(order -> order.getOrderValueUsdt() == null ? BigDecimal.ZERO : order.getOrderValueUsdt())
						.reduce(BigDecimal.ZERO, BigDecimal::add);
					nodeOrderAmountList.add(todayNodeOrderAmount);
					nodeOrderCountList.add(BigDecimal.valueOf(todayList.size()));

					realData.put("withdrawUsdt", nodeOrderAmountList);
					realData.put("withdrawEnergyPool", nodeOrderCountList);
					realData.put("key", everyDate);
				}
				realData.put("lineType", "节点订单金额");
				realData.put("lineTypePool", "订单量");
				return realData;
			})
			.param(SecurityUtils.getUserId())
			.build();
	}

	private WorkerWrapper<Long, Map<String, Object>> getLineTransferChartData() {
		return new WorkerWrapper.Builder<Long, Map<String, Object>>()
			.id("9")
			.worker((Long userId, Map<String, WorkerWrapper> allWrappers) -> {
				Map<String, Object> realData = new HashMap<>(8);
				String yes = DateUtil.formatDate(DateUtil.yesterday());
				Map<String, Object> yesData = xmsRedis.get(RedisConstant.USER_TRANSFER_GROUP + yes, () -> {
					Map<String, Object> map = new HashMap<>(8);
					map.put("transferUsdt", null);
					map.put("transferDfc", null);
					map.put("transferOort", null);
					map.put("transferLockedUsdt", null);
					map.put("transferOutputDfc", null);
					map.put("key", null);

					List<UserTransfer> list1 = userTransferService.lambdaQuery()
						.apply(" create_time < CURDATE()  AND create_time >= DATE_SUB(CURDATE(), INTERVAL 7 DAY)")
						.orderByAsc(UserTransfer::getId)
						.list();

					List<BigDecimal> priceUsdt = new ArrayList<>();
					List<BigDecimal> priceDfc = new ArrayList<>();
					List<BigDecimal> priceOort = new ArrayList<>();
					List<BigDecimal> priceLockedUsdt = new ArrayList<>();
					List<BigDecimal> priceOutputDfc = new ArrayList<>();
					List<String> everyDate = new ArrayList<>();
					if (!list1.isEmpty()) {
						TreeMap<String, List<UserTransfer>> temp = new TreeMap<>(list1.stream()
							.collect(Collectors.groupingBy(transfer -> DateUtil.formatDate(transfer.getCreateTime()))));
						for (Map.Entry<String, List<UserTransfer>> entry : temp.entrySet()) {
							everyDate.add(entry.getKey());
							BigDecimal usdtNum = BigDecimal.ZERO;
							BigDecimal dfcNum = BigDecimal.ZERO;
							BigDecimal oortNum = BigDecimal.ZERO;
							BigDecimal lockedUsdtNum = BigDecimal.ZERO;
							BigDecimal outputDfcNum = BigDecimal.ZERO;
							for (UserTransfer transfer : entry.getValue()) {
								if (transfer.getCoinType() != null && transfer.getCoinType() == 2) {
									dfcNum = dfcNum.add(transfer.getChangeBalance());
								} else if (transfer.getCoinType() != null && transfer.getCoinType() == 3) {
									oortNum = oortNum.add(transfer.getChangeBalance());
								} else if (transfer.getCoinType() != null && transfer.getCoinType() == 4) {
									lockedUsdtNum = lockedUsdtNum.add(transfer.getChangeBalance());
								} else if (transfer.getCoinType() != null && transfer.getCoinType() == 5) {
									outputDfcNum = outputDfcNum.add(transfer.getChangeBalance());
								} else {
									usdtNum = usdtNum.add(transfer.getChangeBalance());
								}
							}
							priceUsdt.add(usdtNum);
							priceDfc.add(dfcNum);
							priceOort.add(oortNum);
							priceLockedUsdt.add(lockedUsdtNum);
							priceOutputDfc.add(outputDfcNum);
						}
						map.put("transferUsdt", priceUsdt);
						map.put("transferDfc", priceDfc);
						map.put("transferOort", priceOort);
						map.put("transferLockedUsdt", priceLockedUsdt);
						map.put("transferOutputDfc", priceOutputDfc);
						map.put("key", everyDate);
					}
					return map;
				}, RedisConstant.SECONDS_EXPIRE_TIME / SysConstant.TWO, TimeUnit.MINUTES);
				realData.putAll(yesData);

				// 计算今天的
				List<UserTransfer> list = userTransferService.lambdaQuery()
					.apply(" create_time >=CURDATE() ").orderByAsc(UserTransfer::getId).list();
				if (list != null && !list.isEmpty()) {
					List<BigDecimal> priceUsdt = new ArrayList<>();
					List<BigDecimal> priceDfc = new ArrayList<>();
					List<BigDecimal> priceOort = new ArrayList<>();
					List<BigDecimal> priceLockedUsdt = new ArrayList<>();
					List<BigDecimal> priceOutputDfc = new ArrayList<>();
					List<String> everyDate = new ArrayList<>();
					if (realData.get("key") != null) {
						everyDate = (List<String>) realData.get("key");
						priceUsdt = (List<BigDecimal>) realData.get("transferUsdt");
						priceDfc = (List<BigDecimal>) realData.get("transferDfc");
						priceOort = (List<BigDecimal>) realData.get("transferOort");
						priceLockedUsdt = (List<BigDecimal>) realData.get("transferLockedUsdt");
						priceOutputDfc = (List<BigDecimal>) realData.get("transferOutputDfc");
					}
					everyDate.add(DateUtil.formatDate(list.getFirst().getCreateTime()));
					BigDecimal usdtNum = BigDecimal.ZERO;
					BigDecimal dfcNum = BigDecimal.ZERO;
					BigDecimal oortNum = BigDecimal.ZERO;
					BigDecimal lockedUsdtNum = BigDecimal.ZERO;
					BigDecimal outputDfcNum = BigDecimal.ZERO;
					for (UserTransfer transfer : list) {
						if (transfer.getCoinType() != null && transfer.getCoinType() == 2) {
							dfcNum = dfcNum.add(transfer.getChangeBalance());
						} else if (transfer.getCoinType() != null && transfer.getCoinType() == 3) {
							oortNum = oortNum.add(transfer.getChangeBalance());
						} else if (transfer.getCoinType() != null && transfer.getCoinType() == 4) {
							lockedUsdtNum = lockedUsdtNum.add(transfer.getChangeBalance());
						} else if (transfer.getCoinType() != null && transfer.getCoinType() == 5) {
							outputDfcNum = outputDfcNum.add(transfer.getChangeBalance());
						} else {
							usdtNum = usdtNum.add(transfer.getChangeBalance());
						}
					}
					priceUsdt.add(usdtNum);
					priceDfc.add(dfcNum);
					priceOort.add(oortNum);
					priceLockedUsdt.add(lockedUsdtNum);
					priceOutputDfc.add(outputDfcNum);
					realData.put("transferUsdt", priceUsdt);
					realData.put("transferDfc", priceDfc);
					realData.put("transferOort", priceOort);
					realData.put("transferLockedUsdt", priceLockedUsdt);
					realData.put("transferOutputDfc", priceOutputDfc);
					realData.put("key", everyDate);
				}
				realData.put("lineType", Arrays.asList("USDT", "DFC", "OORT", "锁定USDT", "产出DFC"));
				return realData;
			})
			.param(SecurityUtils.getUserId())
			.build();
	}

	private WorkerWrapper<Long, Map<String, Object>> getLineRechargeChartData() {
		return new WorkerWrapper.Builder<Long, Map<String, Object>>()
			.id("5")
			.worker((Long userId, Map<String, WorkerWrapper> allWrappers) -> {
				Map<String, Object> realData = new HashMap<>(4);
				String yes = DateUtil.formatDate(DateUtil.yesterday());
				Map<String, Object> yesData = xmsRedis.get(RedisConstant.USER_RECHARGE_GROUP + yes, () -> {
					Map<String, Object> map = new HashMap<>(8);
					map.put("rechargeUsdt", null);
					map.put("rechargeDfc", null);
					map.put("rechargeOort", null);
					map.put("key", null);
					List<RechargeRecord> list1 = rechargeRecordService.lambdaQuery()
						.eq(RechargeRecord::getStatus, 1)
						.apply(" create_time < CURDATE()  AND create_time >= DATE_SUB(CURDATE(), INTERVAL 7 DAY)")
						.orderByAsc(RechargeRecord::getId)
						.list();

					List<BigDecimal> priceUsdt = new ArrayList<>();
					List<BigDecimal> priceDfc = new ArrayList<>();
					List<BigDecimal> priceOort = new ArrayList<>();
					List<String> everyDate = new ArrayList<>();
					if (!list1.isEmpty()) {
						TreeMap<String, List<RechargeRecord>> temp = new TreeMap<>(list1.stream().collect(Collectors.groupingBy(recharge ->
							DateUtil.formatDate(recharge.getCreateTime()))));
						for (Map.Entry<String, List<RechargeRecord>> entry : temp.entrySet()) {
							everyDate.add(entry.getKey());
							BigDecimal usdtNum = BigDecimal.ZERO;
							BigDecimal dfcNum = BigDecimal.ZERO;
							BigDecimal oortNum = BigDecimal.ZERO;
							for (RechargeRecord recharge : entry.getValue()) {
								if (recharge.getCoinType() != null && recharge.getCoinType() == 2) {
									dfcNum = dfcNum.add(recharge.getRechargeAmount());
								} else if (recharge.getCoinType() != null && recharge.getCoinType() == 3) {
									oortNum = oortNum.add(recharge.getRechargeAmount());
								} else {
									usdtNum = usdtNum.add(recharge.getRechargeAmount());
								}
							}
							priceUsdt.add(usdtNum);
							priceDfc.add(dfcNum);
							priceOort.add(oortNum);
						}
						map.put("rechargeUsdt", priceUsdt);
						map.put("rechargeDfc", priceDfc);
						map.put("rechargeOort", priceOort);
						map.put("key", everyDate);
					}
					return map;
				}, RedisConstant.SECONDS_EXPIRE_TIME / SysConstant.TWO, TimeUnit.MINUTES);
				realData.putAll(yesData);

				//计算今天的
		/*		List<Recharge> list = rechargeServiceImpl.lambdaQuery().eq(Recharge::getStatus, 1)
					.apply(" create_time >=CURDATE() ").orderByAsc(Recharge::getId).list();
*/
				List<RechargeRecord> list1 = rechargeRecordService.lambdaQuery()
					.eq(RechargeRecord::getStatus, 1).apply(" create_time >=CURDATE() ").orderByAsc(RechargeRecord::getId).list();
				if (list1 != null && !list1.isEmpty()) {
					List<BigDecimal> priceUsdt = new ArrayList<>();
					List<BigDecimal> priceDfc = new ArrayList<>();
					List<BigDecimal> priceOort = new ArrayList<>();
					List<String> everyDate = new ArrayList<>();
					if (realData.get("key") != null) {
						everyDate = (List<String>) realData.get("key");
						priceUsdt = (List<BigDecimal>) realData.get("rechargeUsdt");
						priceDfc = (List<BigDecimal>) realData.get("rechargeDfc");
						priceOort = (List<BigDecimal>) realData.get("rechargeOort");
					}
					everyDate.add(DateUtil.formatDate(list1.getFirst().getCreateTime()));
					BigDecimal usdtNum = BigDecimal.ZERO;
					BigDecimal dfcNum = BigDecimal.ZERO;
					BigDecimal oortNum = BigDecimal.ZERO;
					for (RechargeRecord recharge : list1) {
						if (recharge.getCoinType() != null && recharge.getCoinType() == 2) {
							dfcNum = dfcNum.add(recharge.getRechargeAmount()
								.setScale(ConstantStatic.newScale, ConstantStatic.roundingModeNew));
						} else if (recharge.getCoinType() != null && recharge.getCoinType() == 3) {
							oortNum = oortNum.add(recharge.getRechargeAmount()
								.setScale(ConstantStatic.newScale, ConstantStatic.roundingModeNew));
						} else {
							usdtNum = usdtNum.add(recharge.getRechargeAmount()
								.setScale(ConstantStatic.newScale, ConstantStatic.roundingModeNew));
						}
					}
					priceUsdt.add(usdtNum);
					priceDfc.add(dfcNum);
					priceOort.add(oortNum);
					realData.put("rechargeUsdt", priceUsdt);
					realData.put("rechargeDfc", priceDfc);
					realData.put("rechargeOort", priceOort);
					realData.put("key", everyDate);
				}
				realData.put("lineType", Arrays.asList("USDT", "DFC", "OORT"));
				//realData.put("lineTypePool", "CNY");
				return realData;
			})
			.param(SecurityUtils.getUserId()).build();
	}


	private WorkerWrapper<Long, Map<String, Object>> getUserRegisterGroupWrapper() {
		return new WorkerWrapper.Builder<Long, Map<String, Object>>()
			.id("4")
			.worker((Long userId, Map<String, WorkerWrapper> allWrappers) -> {
				Map<String, Object> realData = new HashMap<>(2);
				realData.put("expectedData", null);
				realData.put("key", null);
				//获取当前月份
				String format = DateUtil.formatDate(DateUtil.yesterday());
				List<LineDataDO> felTotal = xmsRedis.get(RedisConstant.USER_REGISTER_GROUP + format,
					dataBoardMapper::listUserGroupTotal, RedisConstant.SECONDS_EXPIRE_TIME / SysConstant.TEN, TimeUnit.HOURS);
				if (felTotal != null && !felTotal.isEmpty()) {
					List<Integer> price = new ArrayList<>(felTotal.size());
					List<String> everyDate = new ArrayList<>(felTotal.size());
					for (LineDataDO objectMap : felTotal) {
						everyDate.add(objectMap.getXkey());
						price.add(objectMap.getExpectedData());
					}
					realData.put("expectedData", price);
					realData.put("key", everyDate);
				}
				Long count = userInfoServiceImpl.lambdaQuery().apply(" create_time >= CURDATE()").eq(UserInfo::getDeleted,0).count();
				if (count > 0) {
					List<Integer> expectedData = new ArrayList<>();
					List<String> everyDate = new ArrayList<>();
					if (realData.get("key") != null) {
						everyDate = (List<String>) realData.get("key");
						expectedData = (List<Integer>) realData.get("expectedData");
					}
					everyDate.add(DateUtil.today());
					expectedData.add(count.intValue());
					realData.put("expectedData", expectedData);
					realData.put("key", everyDate);
				}
				return realData;
			})
			.param(SecurityUtils.getUserId()).build();
	}

	private WorkerWrapper<Long, Map<String, BigDecimal>> getWithdrawWrapper() {
		return new WorkerWrapper.Builder<Long, Map<String, BigDecimal>>()
			.id("1")
			.worker((Long userId, Map<String, WorkerWrapper> allWrappers) -> {
				Map<String, BigDecimal> result = new HashMap<>(8);
				result.put("wdwUsdt", dataBoardMapper.countWdwNum());
				BigDecimal wdwToday = dataBoardMapper.countWdwNumToday();
				result.put("wdwToday", wdwToday);
				return result;
			})
			.param(SecurityUtils.getUserId())
			.build();
	}

	private WorkerWrapper<Long, Map<String, Double>> getRechargeWrapper() {
		return new WorkerWrapper.Builder<Long, Map<String, Double>>()
			.id("2")
			.worker((Long userId, Map<String, WorkerWrapper> allWrappers) -> {
				Map<String, Double> result = new HashMap<>(8);
				result.put("rechargeUsdt", 0D);
				//result.put("rechargeEnergyPool", 0D);
				result.put("rechargeUsdtToday", 0D);
				//result.put("rechargeEnergyPoolToday", 0D);
				List<RechargeRecord> list1 = rechargeRecordService.lambdaQuery().eq(RechargeRecord::getStatus, 1).list();
				for (RechargeRecord rechargeRecord : list1) {
					result.put("rechargeUsdt", result.get("rechargeUsdt") + rechargeRecord.getRechargeAmount()
						.setScale(ConstantStatic.eightScale, ConstantStatic.roundingModeNew).doubleValue());
//					else {
//						result.put("rechargeEnergyPool", result.get("rechargeEnergyPool") + rechargeRecord.getRechargeAmount()
//							.setScale(ConstantStatic.newScale, ConstantStatic.roundingModeNew)
//							.doubleValue());
//					}

					//当日
					if (DateUtil.formatDate(rechargeRecord.getCreateTime()).equals(DateUtil.formatDate(new Date()))) {
						result.put("rechargeUsdtToday", result.get("rechargeUsdtToday") + rechargeRecord.getRechargeAmount()
							.setScale(ConstantStatic.eightScale, ConstantStatic.roundingModeNew).doubleValue());
//						else {
//							result.put("rechargeEnergyPoolToday", result.get("rechargeEnergyPoolToday") + rechargeRecord.getRechargeAmount()
//								.setScale(ConstantStatic.newScale, ConstantStatic.roundingModeNew)
//								.doubleValue());
//						}
					}

				}
				log.debug("result={}", result);
				return result;
			})
			.param(SecurityUtils.getUserId())
			.build();
	}

	private WorkerWrapper<Long, Map<String, Object>> getUserTotalWrapper() {
		return new WorkerWrapper.Builder<Long, Map<String, Object>>()
			.id("3")
			.worker((Long userId, Map<String, WorkerWrapper> allWrappers) -> {
				Map<String, Object> map = new HashMap<>();
				Long count = userInfoServiceImpl.lambdaQuery()
					.eq(UserInfo::getDeleted,0)
					.count();
				map.put("userTotal", count);
				Long todayUserCount = userInfoServiceImpl.lambdaQuery().apply("create_time >= CURDATE()").count();
				map.put("todayUserCount", todayUserCount);
				return map;
			})
			.param(SecurityUtils.getUserId())
			.build();
	}

}
