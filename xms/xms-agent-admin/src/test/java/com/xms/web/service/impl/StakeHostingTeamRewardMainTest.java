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
 * 调整 main 中的等级比例、奖励基数、上级链路即可快速验证不同业务场景。</p>
 */
public class StakeHostingTeamRewardMainTest {

	private static final String REWARD_TYPE_DIFF = "DIFF";
	private static final String REWARD_TYPE_SAME_LEVEL = "SAME_LEVEL";
	private static final String REWARD_TYPE_SKIPPED = "SKIPPED";
	private static final String REWARD_TYPE_NO_DIFF = "NO_DIFF";
	private static final String SKIP_NO_UNEXITED_ORDER = "无未出局托管订单";
	private static final String SKIP_NO_DIFF_RATIO = "等级比例未超过已覆盖比例";
	private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");
	private static final int SCALE = 6;

	/**
	 * 本地运行入口。
	 *
	 * <p>默认内置多个案例。你可以直接修改 levelRatioMap、netReward 或 parents 来验证新的极差/平级组合。</p>
	 *
	 * @param args 未使用
	 */
	public static void main(String[] args) {
		Map<Integer, BigDecimal> levelRatioMap = defaultLevelRatioMap();
		BigDecimal netReward = new BigDecimal("100");

		// parentUsers 必须按真实查询顺序配置：直属上级在前，上级的上级在后，即 t_user_relation.distance ASC。
		// 下面示例使用“大ID在前、小ID在后”只是为了看起来更像链路层级，算法本身不按ID判断上下级。
		runCase("案例1",
			levelRatioMap,
			netReward,
			Arrays.asList(
				parent(1004L, 3, true),
				parent(1003L, 4, true),
				parent(1002L, 5, true),
				parent(1001L, 5, false),
				parent(1000L, 5, true),
				parent(999L, 5, true)
			));
/*
		runCase("案例2：F5 -> F5 -> F5 -> F6，F5先完成极差和平级，F6再拿补差",
			levelRatioMap,
			netReward,
			Arrays.asList(
				parent(2004L, 5, true),
				parent(2003L, 5, true),
				parent(2002L, 5, true),
				parent(2001L, 6, true)
			));

		runCase("案例3：F5 -> F6 -> F5，后面的F5不再参与前面F5的平级",
			levelRatioMap,
			netReward,
			Arrays.asList(
				parent(3003L, 5, true),
				parent(3002L, 6, true),
				parent(3001L, 5, true)
			));

		runCase("案例4：五个F5同级，观察平级池按 1/2、1/4、1/8、1/16、1/16 拆分",
			levelRatioMap,
			netReward,
			Arrays.asList(
				parent(4005L, 5, true),
				parent(4004L, 5, true),
				parent(4003L, 5, true),
				parent(4002L, 5, true),
				parent(4001L, 5, true)
			));

		runCase("案例5：第一个F5无未出局托管订单，同级下一个F5拿极差并作为平级池起点",
			levelRatioMap,
			netReward,
			Arrays.asList(
				parent(5003L, 5, false),
				parent(5002L, 5, true),
				parent(5001L, 5, true),
				parent(5000L, 6, true)
			));

		runCase("案例6：前面F5无未出局托管订单，后续F6按完整未覆盖比例拿奖",
			levelRatioMap,
			netReward,
			Arrays.asList(
				parent(6002L, 5, false),
				parent(6001L, 6, true)
			));*/
	}

	/**
	 * 运行一个极差/平级奖励测试案例并打印结果。
	 *
	 * @param caseName 案例名称
	 * @param levelRatioMap F等级团队奖励比例配置，单位%
	 * @param netReward 用户到账静态净收益，单位USDT，也是极差/平级奖励计算基数
	 * @param parentUsers 源用户上级链路，必须按近到远排序，对应真实SQL中的 t_user_relation.distance ASC
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
		System.out.println("skippedTotal = " + money(context.skippedTotal) + " USDT");
		System.out.println("finalCoveredRatio = " + percent(context.coveredRatio) + "%");
	}

	/**
	 * 按上级链路模拟发放极差奖和平级奖。
	 *
	 * <p>该方法复制当前业务核心口径：无未出局托管订单的上级先过滤，等同上级链路里查询不到；
	 * 第一个有效同级用户先拿完整极差，F5及以上再以这笔极差金额作为平级池，让连续同级按平级公式拆分。</p>
	 *
	 * @param levelRatioMap F等级团队奖励比例配置，单位%
	 * @param netReward 用户到账静态净收益，单位USDT
	 * @param parentUsers 源用户上级链路，按近到远排序，对应真实SQL中的 t_user_relation.distance ASC
	 * @return 本案例的计算上下文，包含明细、汇总和最终覆盖比例
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

			// 当前等级比例减去已覆盖比例，就是当前上级段可发放的极差比例。
			BigDecimal diffRatio = levelRatio.subtract(coveredRatio);
			if (diffRatio.compareTo(BigDecimal.ZERO) <= 0) {
				// 没有新增差额时不发奖励；测试工具保留一行输出，方便看清楚为什么后续上级没拿到极差/平级。
				context.lines.add(RewardLine.noDiff(parent, coveredRatio, levelRatio, diffRatio));
				continue;
			}

			// 先识别当前连续同级段。F5以下只会处理当前用户，F5及以上才可能额外触发平级奖。
			int sameCount = parent.level >= 5 ? countSameLevelRun(rewardParentUsers, i, parent.level) : 1;
			BigDecimal beforeCoveredRatio = coveredRatio;
			BigDecimal diffRewardAmount = calculateReward(netReward, diffRatio);
			boolean hasCoveredUser = false;
			int firstCoveredSameIndex = -1;

			for (int sameIndex = 0; sameIndex < sameCount; sameIndex++) {
				ParentUser rewardUser = rewardParentUsers.get(i + sameIndex);

				// 当前同级段中，第一个有效用户拿完整极差；无未出局订单的用户已被过滤，不占平级位置。
				if (!hasCoveredUser) {
					hasCoveredUser = true;
					firstCoveredSameIndex = sameIndex;
					context.arrivedTotal = context.arrivedTotal.add(diffRewardAmount);
					context.lines.add(RewardLine.arrived(rewardUser, REWARD_TYPE_DIFF, beforeCoveredRatio, levelRatio, diffRatio, diffRewardAmount));
				}
			}

			if (hasCoveredUser) {
				// 只有用户实际拿到极差后，当前等级比例才会成为后续上级的已覆盖比例。
				coveredRatio = levelRatio;
				if (parent.level >= 5) {
					// F5及以上：以第一个到账同级用户拿到的极差金额作为平级池，从该用户开始连续同级一起拆分。
					collectSameLevelReward(context, rewardParentUsers, i + firstCoveredSameIndex,
						sameCount - firstCoveredSameIndex, beforeCoveredRatio, levelRatio, diffRatio, diffRewardAmount);
				}
			}

			// 连续同级已经作为一组处理完，外层循环跳过这一组剩余用户。
			i += sameCount - 1;
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
	 * 收集并打印连续同级段的平级奖。
	 *
	 * <p>平级池等于第一个到账同级用户已经拿到的极差金额；从这个用户开始，连续同级按
	 * 1/2、1/4、1/4 等公式拆分。无未出局托管订单的用户已在进入本方法前过滤。</p>
	 *
	 * @param context 当前测试案例上下文
	 * @param parentUsers 源用户上级链路，按近到远排序
	 * @param startSameIndex 第一个到账极差用户在完整上级链路中的下标
	 * @param sameCount 从第一个到账极差用户开始的连续同级人数
	 * @param beforeCoveredRatio 发放本段极差前的已覆盖比例，单位%
	 * @param levelRatio 当前F等级团队奖励比例，单位%
	 * @param diffRatio 本段极差比例，单位%
	 * @param sameLevelPool 平级池金额，单位USDT
	 */
	private static void collectSameLevelReward(RewardContext context, List<ParentUser> parentUsers,
											   int startSameIndex, int sameCount, BigDecimal beforeCoveredRatio,
											   BigDecimal levelRatio, BigDecimal diffRatio, BigDecimal sameLevelPool) {
		if (sameCount <= 1 || sameLevelPool.compareTo(BigDecimal.ZERO) <= 0) {
			return;
		}
		for (int sameIndex = 0; sameIndex < sameCount; sameIndex++) {
			ParentUser rewardUser = parentUsers.get(startSameIndex + sameIndex);
			BigDecimal sameLevelReward = calculateSameLevelReward(sameLevelPool, sameIndex + 1, sameCount);
			context.arrivedTotal = context.arrivedTotal.add(sameLevelReward);
			context.lines.add(RewardLine.arrived(rewardUser, REWARD_TYPE_SAME_LEVEL,
				beforeCoveredRatio, levelRatio, diffRatio, sameLevelReward));
		}
	}

	/**
	 * 统计从指定位置开始连续相同F等级的上级数量。
	 *
	 * @param parentUsers 上级链路
	 * @param startIndex 起始下标
	 * @param level 当前F等级
	 * @return 连续同级数量
	 */
	private static int countSameLevelRun(List<ParentUser> parentUsers, int startIndex, Integer level) {
		int count = 0;
		for (int i = startIndex; i < parentUsers.size(); i++) {
			if (!level.equals(parentUsers.get(i).level)) {
				break;
			}
			count++;
		}
		return count;
	}

	/**
	 * 按当前业务代码的平级公式拆分差额池。
	 *
	 * <p>两个同级：1/2 + 1/2；三个同级：1/2 + 1/4 + 1/4；
	 * 四个同级：1/2 + 1/4 + 1/8 + 1/8；五个同级：1/2 + 1/4 + 1/8 + 1/16 + 1/16。</p>
	 *
	 * @param pool 当前等级差额池，单位USDT
	 * @param sameIndex 同级组内第几个，1开始
	 * @param sameCount 同级组总人数
	 * @return 当前同级用户分得的奖励金额，单位USDT
	 */
	private static BigDecimal calculateSameLevelReward(BigDecimal pool, int sameIndex, int sameCount) {
		if (sameCount <= 1 || pool.compareTo(BigDecimal.ZERO) <= 0) {
			return BigDecimal.ZERO;
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
	 * 判断模拟上级是否具备团队奖励到账资格。
	 *
	 * <p>当前统一口径：持有未出局托管订单才算有效用户，不再单独模拟账号 is_valid。</p>
	 *
	 * @param user 模拟上级用户
	 * @return null表示可到账；非null表示跳过原因
	 */
	private static String getRewardSkipReason(ParentUser user) {
		if (!user.hasUnexitedOrder) {
			return SKIP_NO_UNEXITED_ORDER;
		}
		return null;
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
	 * 单个测试案例的计算上下文。
	 */
	private static class RewardContext {
		private final List<RewardLine> lines = new ArrayList<>();
		private BigDecimal arrivedTotal = BigDecimal.ZERO;
		private BigDecimal skippedTotal = BigDecimal.ZERO;
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
		private final String skipReason;

		private RewardLine(Long userId, Integer level, String rewardType, boolean arrived,
						   BigDecimal beforeCoveredRatio, BigDecimal levelRatio,
						   BigDecimal diffRatio, BigDecimal rewardAmount, String skipReason) {
			this.userId = userId;
			this.level = level;
			this.rewardType = rewardType;
			this.arrived = arrived;
			this.beforeCoveredRatio = beforeCoveredRatio;
			this.levelRatio = levelRatio;
			this.diffRatio = diffRatio;
			this.rewardAmount = rewardAmount;
			this.skipReason = skipReason;
		}

		private static RewardLine arrived(ParentUser user, String rewardType, BigDecimal beforeCoveredRatio,
										  BigDecimal levelRatio, BigDecimal diffRatio, BigDecimal rewardAmount) {
			return new RewardLine(user.userId, user.level, rewardType, true, beforeCoveredRatio, levelRatio, diffRatio, rewardAmount, null);
		}

		private static RewardLine skipped(ParentUser user, BigDecimal beforeCoveredRatio, BigDecimal levelRatio,
										  BigDecimal diffRatio, BigDecimal rewardAmount, String skipReason) {
			return new RewardLine(user.userId, user.level, REWARD_TYPE_SKIPPED, false, beforeCoveredRatio,
				levelRatio, diffRatio, rewardAmount, skipReason);
		}

		private static RewardLine noDiff(ParentUser user, BigDecimal beforeCoveredRatio, BigDecimal levelRatio,
										 BigDecimal diffRatio) {
			return new RewardLine(user.userId, user.level, REWARD_TYPE_NO_DIFF, false, beforeCoveredRatio,
				levelRatio, diffRatio, BigDecimal.ZERO, SKIP_NO_DIFF_RATIO);
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
				+ ", reward=" + money(rewardAmount) + " USDT"
				+ (skipReason == null ? "" : ", skipReason=" + skipReason);
		}
	}
}
