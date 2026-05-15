package com.xms.web.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 托管极差奖/平级奖本地计算测试工具。
 *
 * <p>该类只用于本地右键运行 main 验证奖励算法，不连接数据库、不写钱包、不写结算明细。
 * 修改等级比例、奖励基数或上级链路即可快速验证不同业务场景。</p>
 */
public class StakeHostingTeamRewardMainTest {

	private static final String REWARD_TYPE_DIFF = "DIFF";
	private static final String REWARD_TYPE_SAME_LEVEL = "SAME_LEVEL";
	private static final String REWARD_TYPE_NO_DIFF = "NO_DIFF";
	private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");
	private static final int SCALE = 6;

	/**
	 * 本地运行入口。
	 *
	 * <p>parentUsers 必须按真实查询顺序配置：直属上级在前，上级的上级在后，
	 * 即 t_user_relation.distance ASC。</p>
	 *
	 * @param args 未使用
	 */
	public static void main(String[] args) {
		Map<Integer, BigDecimal> levelRatioMap = defaultLevelRatioMap();
		BigDecimal netReward = new BigDecimal("100");

		runCase("案例1：穿透低等级找F5平级，遇F6终止",
			levelRatioMap,
			netReward,
			Arrays.asList(
				parent(1027L, 3, true),
				parent(1026L, 5, true),
				parent(1025L, 2, true),
				parent(1024L, 5, true),
				parent(1023L, 3, true),
				parent(1015L, 5, true),
				parent(1014L, 5, true),
				parent(1013L, 6, true),
				parent(1009L, 5, true),
				parent(1006L, 6, true)
			));

		runCase("案例2：F5 -> F6 -> F5，后面的F5不参与前面F5平级",
			levelRatioMap,
			netReward,
			Arrays.asList(
				parent(3003L, 5, true),
				parent(3002L, 6, true),
				parent(3001L, 5, true)
			));

		runCase("案例3：F4 -> F2 -> F4，F5以下不触发平级",
			levelRatioMap,
			netReward,
			Arrays.asList(
				parent(4003L, 4, true),
				parent(4002L, 2, true),
				parent(4001L, 4, true)
			));

		runCase("案例4：无未出局订单用户过滤，不占平级份额",
			levelRatioMap,
			netReward,
			Arrays.asList(
				parent(5005L, 5, true),
				parent(5004L, 2, true),
				parent(5003L, 5, false),
				parent(5002L, 5, true),
				parent(5001L, 6, true)
			));
	}

	/**
	 * 运行一个极差/平级奖励测试案例并打印结果。
	 *
	 * @param caseName 案例名称
	 * @param levelRatioMap F等级团队奖励比例配置，单位%
	 * @param netReward 用户到账静态净收益，单位USDT，也是极差/平级奖励计算基数
	 * @param parentUsers 源用户上级链路，必须按近到远排序
	 */
	private static void runCase(String caseName, Map<Integer, BigDecimal> levelRatioMap, BigDecimal netReward,
								List<ParentUser> parentUsers) {
		System.out.println();
		System.out.println("============================================================");
		System.out.println(caseName);
		System.out.println("netReward = " + money(netReward) + " USDT");
		System.out.println("parents   = " + parentChain(parentUsers));
		System.out.println("------------------------------------------------------------");

		RewardContext context = distributeDiffAndSameLevelReward(levelRatioMap, netReward, parentUsers);
		for (RewardLine line : context.lines) {
			System.out.println(line);
		}
		System.out.println("------------------------------------------------------------");
		System.out.println("arrivedTotal = " + money(context.arrivedTotal) + " USDT");
		System.out.println("finalCoveredRatio = " + percent(context.coveredRatio) + "%");
	}

	/**
	 * 按上级链路模拟发放极差奖和平级奖。
	 *
	 * <p>无未出局托管订单的上级先过滤，等同查询不到。F5及以上拿到极差后，以该极差金额作为平级池，
	 * 向上穿透低等级继续找同级；第一个同级只拿极差，后续同级重新按人数拆完整个平级池。</p>
	 *
	 * @param levelRatioMap F等级团队奖励比例配置，单位%
	 * @param netReward 用户到账静态净收益，单位USDT
	 * @param parentUsers 源用户上级链路，按近到远排序
	 * @return 本案例计算上下文，包含明细、汇总和最终覆盖比例
	 */
	private static RewardContext distributeDiffAndSameLevelReward(Map<Integer, BigDecimal> levelRatioMap,
																  BigDecimal netReward,
																  List<ParentUser> parentUsers) {
		RewardContext context = new RewardContext();
		List<ParentUser> rewardParentUsers = filterRewardParentUsers(parentUsers);
		BigDecimal coveredRatio = BigDecimal.ZERO;
		for (int i = 0; i < rewardParentUsers.size(); i++) {
			ParentUser parent = rewardParentUsers.get(i);
			BigDecimal levelRatio = levelRatioMap.getOrDefault(parent.level, BigDecimal.ZERO);
			if (levelRatio.compareTo(BigDecimal.ZERO) <= 0) {
				continue;
			}

			BigDecimal diffRatio = levelRatio.subtract(coveredRatio);
			if (diffRatio.compareTo(BigDecimal.ZERO) <= 0) {
				context.lines.add(RewardLine.noDiff(parent, coveredRatio, levelRatio, diffRatio));
				continue;
			}

			// F5及以上收集穿透式同级组；F5以下只让当前用户拿极差。
			SameLevelGroup sameLevelGroup = parent.level >= 5
				? collectSameLevelGroupUntilHigher(rewardParentUsers, i, parent.level)
				: SameLevelGroup.single(i);
			BigDecimal beforeCoveredRatio = coveredRatio;
			BigDecimal diffRewardAmount = calculateReward(netReward, diffRatio);

			ParentUser diffUser = rewardParentUsers.get(sameLevelGroup.sameIndexes.get(0));
			context.arrivedTotal = context.arrivedTotal.add(diffRewardAmount);
			context.lines.add(RewardLine.arrived(diffUser, REWARD_TYPE_DIFF, beforeCoveredRatio, levelRatio, diffRatio, diffRewardAmount));

			coveredRatio = levelRatio;
			if (parent.level >= 5) {
				collectSameLevelReward(context, rewardParentUsers, sameLevelGroup.rewardSameIndexes(),
					beforeCoveredRatio, levelRatio, diffRatio, diffRewardAmount);
			}

			// 低等级已被当前平级组穿透跳过；遇到更高等级时下一轮从更高等级继续。
			i = sameLevelGroup.nextIndex - 1;
		}
		context.coveredRatio = coveredRatio;
		return context;
	}

	/**
	 * 过滤极差/平级奖励使用的有效上级链路。
	 *
	 * <p>无未出局托管订单的上级等同查询不到，不占同级平级份额，也不输出 skipped 明细。</p>
	 *
	 * @param parentUsers 原始上级链路，按近到远排序
	 * @return 只包含持有未出局托管订单的上级链路
	 */
	private static List<ParentUser> filterRewardParentUsers(List<ParentUser> parentUsers) {
		List<ParentUser> result = new ArrayList<>();
		for (ParentUser parentUser : parentUsers) {
			if (parentUser.hasUnexitedOrder) {
				result.add(parentUser);
			}
		}
		return result;
	}

	/**
	 * 收集穿透低等级后的同级平级组。
	 *
	 * @param parentUsers 有效上级链路，按近到远排序
	 * @param startIndex 起始下标
	 * @param level 当前F等级
	 * @return 同级用户下标和下一轮应处理的位置
	 */
	private static SameLevelGroup collectSameLevelGroupUntilHigher(List<ParentUser> parentUsers, int startIndex, Integer level) {
		List<Integer> sameIndexes = new ArrayList<>();
		int nextIndex = parentUsers.size();
		for (int i = startIndex; i < parentUsers.size(); i++) {
			Integer currentLevel = parentUsers.get(i).level;
			if (currentLevel > level) {
				nextIndex = i;
				break;
			}
			if (level.equals(currentLevel)) {
				sameIndexes.add(i);
			}
		}
		return new SameLevelGroup(sameIndexes, nextIndex);
	}

	/**
	 * 收集并打印同级组的平级奖。
	 *
	 * @param context 当前测试案例上下文
	 * @param parentUsers 源用户上级链路，按近到远排序
	 * @param sameIndexes 本次平级组中参与平级的后续同级用户下标，不包含第一个拿极差的同级
	 * @param beforeCoveredRatio 发放本段极差前的已覆盖比例，单位%
	 * @param levelRatio 当前F等级团队奖励比例，单位%
	 * @param diffRatio 本段极差比例，单位%
	 * @param sameLevelPool 平级池金额，单位USDT
	 */
	private static void collectSameLevelReward(RewardContext context, List<ParentUser> parentUsers,
											   List<Integer> sameIndexes, BigDecimal beforeCoveredRatio,
											   BigDecimal levelRatio, BigDecimal diffRatio, BigDecimal sameLevelPool) {
		if (sameIndexes.isEmpty() || sameLevelPool.compareTo(BigDecimal.ZERO) <= 0) {
			return;
		}
		for (int sameIndex = 0; sameIndex < sameIndexes.size(); sameIndex++) {
			ParentUser rewardUser = parentUsers.get(sameIndexes.get(sameIndex));
			BigDecimal sameLevelReward = calculateSameLevelReward(sameLevelPool, sameIndex + 1, sameIndexes.size());
			context.arrivedTotal = context.arrivedTotal.add(sameLevelReward);
			context.lines.add(RewardLine.arrived(rewardUser, REWARD_TYPE_SAME_LEVEL,
				beforeCoveredRatio, levelRatio, diffRatio, sameLevelReward));
		}
	}

	/**
	 * 按当前业务代码的平级公式拆分平级池。
	 *
	 * <p>一个后续同级：拿完整平级池；两个同级：1/2 + 1/2；三个同级：1/2 + 1/4 + 1/4；
	 * 四个同级：1/2 + 1/4 + 1/8 + 1/8；五个同级：1/2 + 1/4 + 1/8 + 1/16 + 1/16。</p>
	 *
	 * @param pool 当前平级池，单位USDT
	 * @param sameIndex 同级组内第几个，从1开始
	 * @param sameCount 同级组总人数
	 * @return 当前同级用户分得的奖励金额，单位USDT
	 */
	private static BigDecimal calculateSameLevelReward(BigDecimal pool, int sameIndex, int sameCount) {
		if (sameCount <= 0 || pool.compareTo(BigDecimal.ZERO) <= 0) {
			return BigDecimal.ZERO;
		}
		if (sameCount == 1) {
			return pool;
		}
		int power = sameIndex == sameCount ? sameCount - 1 : sameIndex;
		BigDecimal divisor = new BigDecimal(2).pow(power);
		return pool.divide(divisor, SCALE, RoundingMode.HALF_UP);
	}

	/**
	 * 按百分比计算奖励金额。
	 *
	 * @param baseAmount 奖励基数，单位USDT
	 * @param ratioPercent 奖励比例，单位%
	 * @return 奖励金额，单位USDT
	 */
	private static BigDecimal calculateReward(BigDecimal baseAmount, BigDecimal ratioPercent) {
		if (baseAmount == null || ratioPercent == null
			|| baseAmount.compareTo(BigDecimal.ZERO) <= 0 || ratioPercent.compareTo(BigDecimal.ZERO) <= 0) {
			return BigDecimal.ZERO;
		}
		return baseAmount.multiply(ratioPercent).divide(ONE_HUNDRED, SCALE, RoundingMode.HALF_UP);
	}

	/**
	 * 默认F等级团队奖励比例配置。
	 *
	 * @return F等级到团队奖励比例的映射，单位%
	 */
	private static Map<Integer, BigDecimal> defaultLevelRatioMap() {
		Map<Integer, BigDecimal> map = new LinkedHashMap<>();
		map.put(1, new BigDecimal("5"));
		map.put(2, new BigDecimal("10"));
		map.put(3, new BigDecimal("15"));
		map.put(4, new BigDecimal("20"));
		map.put(5, new BigDecimal("25"));
		map.put(6, new BigDecimal("30"));
		map.put(7, new BigDecimal("40"));
		map.put(8, new BigDecimal("50"));
		map.put(9, new BigDecimal("55"));
		return map;
	}

	/**
	 * 快速创建模拟上级用户。
	 *
	 * @param userId 用户ID
	 * @param level F等级
	 * @param hasUnexitedOrder 是否持有未出局托管订单
	 * @return 模拟上级用户
	 */
	private static ParentUser parent(Long userId, Integer level, boolean hasUnexitedOrder) {
		return new ParentUser(userId, level, hasUnexitedOrder);
	}

	private static String parentChain(List<ParentUser> parentUsers) {
		List<String> items = new ArrayList<>();
		for (ParentUser user : parentUsers) {
			items.add(user.userId + "(F" + user.level + ", unexitedOrder=" + user.hasUnexitedOrder + ")");
		}
		return String.join(" -> ", items);
	}

	private static String money(BigDecimal amount) {
		return amount.setScale(6, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
	}

	private static String percent(BigDecimal amount) {
		return amount.setScale(4, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
	}

	/**
	 * 模拟上级用户。
	 */
	private static class ParentUser {
		private final Long userId;
		private final Integer level;
		private final boolean hasUnexitedOrder;

		private ParentUser(Long userId, Integer level, boolean hasUnexitedOrder) {
			this.userId = userId;
			this.level = level;
			this.hasUnexitedOrder = hasUnexitedOrder;
		}
	}

	/**
	 * 穿透式平级组扫描结果。
	 */
	private static class SameLevelGroup {
		private final List<Integer> sameIndexes;
		private final int nextIndex;

		private SameLevelGroup(List<Integer> sameIndexes, int nextIndex) {
			this.sameIndexes = sameIndexes;
			this.nextIndex = nextIndex;
		}

		private List<Integer> rewardSameIndexes() {
			if (sameIndexes.size() <= 1) {
				return new ArrayList<>();
			}
			return new ArrayList<>(sameIndexes.subList(1, sameIndexes.size()));
		}

		private static SameLevelGroup single(int index) {
			List<Integer> indexes = new ArrayList<>();
			indexes.add(index);
			return new SameLevelGroup(indexes, index + 1);
		}
	}

	/**
	 * 单个测试案例的计算上下文。
	 */
	private static class RewardContext {
		private final List<RewardLine> lines = new ArrayList<>();
		private BigDecimal arrivedTotal = BigDecimal.ZERO;
		private BigDecimal coveredRatio = BigDecimal.ZERO;
	}

	/**
	 * 单条模拟奖励结果。
	 */
	private static class RewardLine {
		private final Long userId;
		private final Integer level;
		private final String rewardType;
		private final boolean arrived;
		private final BigDecimal beforeCoveredRatio;
		private final BigDecimal levelRatio;
		private final BigDecimal diffRatio;
		private final BigDecimal rewardAmount;

		private RewardLine(Long userId, Integer level, String rewardType, boolean arrived,
						   BigDecimal beforeCoveredRatio, BigDecimal levelRatio, BigDecimal diffRatio,
						   BigDecimal rewardAmount) {
			this.userId = userId;
			this.level = level;
			this.rewardType = rewardType;
			this.arrived = arrived;
			this.beforeCoveredRatio = beforeCoveredRatio;
			this.levelRatio = levelRatio;
			this.diffRatio = diffRatio;
			this.rewardAmount = rewardAmount;
		}

		private static RewardLine arrived(ParentUser user, String rewardType, BigDecimal beforeCoveredRatio,
										  BigDecimal levelRatio, BigDecimal diffRatio, BigDecimal rewardAmount) {
			return new RewardLine(user.userId, user.level, rewardType, true, beforeCoveredRatio, levelRatio, diffRatio, rewardAmount);
		}

		private static RewardLine noDiff(ParentUser user, BigDecimal beforeCoveredRatio,
										 BigDecimal levelRatio, BigDecimal diffRatio) {
			return new RewardLine(user.userId, user.level, REWARD_TYPE_NO_DIFF, false, beforeCoveredRatio, levelRatio,
				diffRatio, BigDecimal.ZERO);
		}

		@Override
		public String toString() {
			return "userId=" + userId
				+ ", F" + level
				+ ", arrived=" + arrived
				+ ", type=" + rewardType
				+ ", beforeCovered=" + percent(beforeCoveredRatio) + "%"
				+ ", levelRatio=" + percent(levelRatio) + "%"
				+ ", diffRatio=" + percent(diffRatio) + "%"
				+ ", reward=" + money(rewardAmount) + " USDT";
		}
	}
}
