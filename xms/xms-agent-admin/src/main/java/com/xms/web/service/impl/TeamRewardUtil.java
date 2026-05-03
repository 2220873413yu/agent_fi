//package com.xms.web.service.impl;
//
//import com.xms.common.constant.ConstantStatic;
//import com.xms.common.constant.ConstantType;
//import com.xms.common.utils.uuid.IDUtils;
//import com.xms.dao.domain.RewardRecord;
//import com.xms.dao.entity.vo.ParentUserTaskVo;
//import lombok.extern.slf4j.Slf4j;
//
//import java.math.BigDecimal;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
///**
// * 团队奖励（极差 + 平级 + 超越）计算工具
// *
// * 说明：
// * - 只做“金额计算”，不做钱包入账或记录落库
// * - 调用方负责根据返回结果发放资产 / 生成奖金记录
// */
//@Slf4j
//public class TeamRewardUtil {
//
//	/**
//	 * 计算团队奖励（极差 + 平级 + 超越）
//	 *
//	 * @param userStaticRewardMap 每个用户的静态收益 userId -> 静态收益
//	 * @param parentUserTaskVoMap 每个用户的上级链路（近到远，distance 升序）
//	 * @param levelConfigMap      等级配置 level -> 配置（包含 rewardRatio, peerRewardRatio，均已除以100）
//	 * @param exceedAwardRatio    超越奖比例（已除以100，例如 0.1 = 10%）
//	 * @return 每个用户的团队奖励总额（包含团队奖 + 平级奖 + 超越奖）
//	 */
//	public static Map<Long, BigDecimal> calcTeamRewards(
//		Map<Long, BigDecimal> userStaticRewardMap,
//		Map<Long, List<ParentUserTaskVo>> parentUserTaskVoMap,
//		Map<Integer, W3UserLevelConfig> levelConfigMap,
//		BigDecimal exceedAwardRatio,
//		List<RewardRecord> teamRewardRecordList,
//		BigDecimal boomaiPrice
//	) {
//		Map<Long, BigDecimal> result = new HashMap<>();
//		if (userStaticRewardMap == null || userStaticRewardMap.isEmpty()) {
//			return result;
//		}
//		RewardRecord rewardRecordEntity = null;
//		for (Map.Entry<Long, BigDecimal> entry : userStaticRewardMap.entrySet()) {
//			Long userId = entry.getKey();
//			BigDecimal staticReward = entry.getValue();
//			if (staticReward == null || staticReward.compareTo(BigDecimal.ZERO) <= 0) {
//				continue;
//			}
//
//			List<ParentUserTaskVo> parentList = parentUserTaskVoMap.get(userId);
//			if (parentList == null || parentList.isEmpty()) {
//				continue;
//			}
//
//			// 上一个拿到级差奖的用户
//			ParentUserTaskVo lastRewardUser = null;
//			// 上一个级差奖金额
//			BigDecimal lastRewardAmount = BigDecimal.ZERO;
//			// 上一个拿奖用户的等级
//			Integer lastRewardLevel = null;
//			// 已发放的累积比例
//			BigDecimal initRewardRatio = BigDecimal.ZERO;
//			// 上一次发奖等级 & 金额（用于超越奖）
//			Integer beforeLevel = 0;
//			BigDecimal beforeReward = BigDecimal.ZERO;
//			// 是否已触发超越奖
//			boolean exceedAwardFlag = true;
//			// 本条链路是否已发放平级奖
//			boolean peerRewardSent = false;
//
//			for (ParentUserTaskVo p : parentList) {
//				// 无效用户不参与
//				if (p.getIsValid() == null || p.getIsValid() == 0) {
//					continue;
//				}
//				rewardRecordEntity = new  RewardRecord();
//				// 真实等级 = max(gameLevel, minGameLevel)
//				int gameLevel =p.getGameLevel();
//
//				W3UserLevelConfig cfg = levelConfigMap.get(gameLevel);
//				if (cfg == null || cfg.getRewardRatio() == null) {
//					continue;
//				}
//
//				BigDecimal finalRewardRatio = cfg.getRewardRatio().subtract(initRewardRatio);
//
//				// 1) 极差奖
//				if (finalRewardRatio.compareTo(BigDecimal.ZERO) > 0) {
//					BigDecimal teamReward = staticReward.multiply(finalRewardRatio)
//						.setScale(ConstantStatic.newScale, ConstantStatic.roundingModeNew);
//					beforeReward = teamReward;
//					if (teamReward.compareTo(BigDecimal.ZERO) > 0) {
//						result.merge(p.getUserId(), teamReward, BigDecimal::add);
//
//						//插入奖金明细
//						rewardRecordEntity.setOrderCode(IDUtils.getSnowflakeStr());
//						rewardRecordEntity.setUserId(p.getUserId());
//						rewardRecordEntity.setAmount(teamReward);
//						rewardRecordEntity.setBusinessType(ConstantType.xms_reward_record_business_type.type_3);
//						rewardRecordEntity.setSourceType(ConstantType.xms_reward_record_source_type.type_10);
//						rewardRecordEntity.setSourceUserId(userId);
//						rewardRecordEntity.setRealTimePrice(boomaiPrice);
//						rewardRecordEntity.setSourceOrderCode(p.getUserId()+"");
//						rewardRecordEntity.setGtId(IDUtils.getSnowflakeStr());
//						teamRewardRecordList.add(rewardRecordEntity);
//
//
//						// 更新极差状态
//						initRewardRatio = cfg.getRewardRatio();
//						lastRewardUser = p;
//						lastRewardAmount = teamReward;
//						lastRewardLevel = gameLevel;
//						if (gameLevel > 0) {
//							beforeLevel = gameLevel;
//						}
//					}
//					continue;
//				}
//
//				// 2) 平级奖（finalRewardRatio == 0）
//				if (finalRewardRatio.compareTo(BigDecimal.ZERO) == 0) {
//					if (!peerRewardSent
//						&& lastRewardUser != null
//						&& lastRewardLevel != null
//						&& lastRewardLevel.equals(gameLevel)
//						&& cfg.getPeerRewardRatio() != null) {
//
//						BigDecimal peerReward = lastRewardAmount.multiply(cfg.getPeerRewardRatio())
//							.setScale(ConstantStatic.newScale, ConstantStatic.roundingModeNew);
//						if (peerReward.compareTo(BigDecimal.ZERO) > 0) {
//							result.merge(p.getUserId(), peerReward, BigDecimal::add);
//							//奖金明细
//							rewardRecordEntity.setOrderCode(IDUtils.getSnowflakeStr());
//							rewardRecordEntity.setUserId(p.getUserId());
//							rewardRecordEntity.setAmount(peerReward);
//							rewardRecordEntity.setBusinessType(ConstantType.xms_reward_record_business_type.type_3);
//							rewardRecordEntity.setSourceType(ConstantType.xms_reward_record_source_type.type_11);
//							rewardRecordEntity.setSourceUserId(userId);
//							rewardRecordEntity.setRealTimePrice(boomaiPrice);
//							rewardRecordEntity.setSourceOrderCode(p.getUserId()+"");
//							rewardRecordEntity.setGtId(IDUtils.getSnowflakeStr());
//							teamRewardRecordList.add(rewardRecordEntity);
//							peerRewardSent = true;
//						}
//					}
//					continue;
//				}
//
//				// 3) 超越奖（finalRewardRatio < 0）
//				if (finalRewardRatio.compareTo(BigDecimal.ZERO) < 0 && exceedAwardFlag) {
//					if (gameLevel < beforeLevel && beforeReward.compareTo(BigDecimal.ZERO) > 0
//						&& exceedAwardRatio != null && exceedAwardRatio.compareTo(BigDecimal.ZERO) > 0) {
//
//						exceedAwardFlag = false;
//						BigDecimal exceedReward = beforeReward.multiply(exceedAwardRatio)
//							.setScale(ConstantStatic.newScale, ConstantStatic.roundingModeNew);
//						if (exceedReward.compareTo(BigDecimal.ZERO) > 0) {
//							result.merge(p.getUserId(), exceedReward, BigDecimal::add);
//
//							//奖金明细
//							rewardRecordEntity.setOrderCode(IDUtils.getSnowflakeStr());
//							rewardRecordEntity.setUserId(p.getUserId());
//							rewardRecordEntity.setAmount(exceedReward);
//							rewardRecordEntity.setBusinessType(ConstantType.xms_reward_record_business_type.type_3);
//							rewardRecordEntity.setSourceType(ConstantType.xms_reward_record_source_type.type_12);
//							rewardRecordEntity.setSourceUserId(userId);
//							rewardRecordEntity.setRealTimePrice(boomaiPrice);
//							rewardRecordEntity.setSourceOrderCode(p.getUserId()+"");
//							rewardRecordEntity.setGtId(IDUtils.getSnowflakeStr());
//							teamRewardRecordList.add(rewardRecordEntity);
//						}
//					}
//				}
//			}
//		}
//
//		return result;
//	}
//
//	/**
//	 * 简单 main 方法：本地快速运行测试
//	 */
//	public static void main(String[] args) {
//		// 被结算用户：100，静态收益 1000
//		Map<Long, BigDecimal> staticMap = new HashMap<>();
//		staticMap.put(100L, new BigDecimal("1000"));
//
//		// 他的上级链：近到远 A(V3)、B(V3)、C(V5)
//		ParentUserTaskVo a = new ParentUserTaskVo();
//		a.setUserId(201L);
//		a.setIsValid(1);
//		a.setGameLevel(3);
//
//		ParentUserTaskVo b = new ParentUserTaskVo();
//		b.setUserId(202L);
//		b.setIsValid(1);
//		b.setGameLevel(3);
//
//		ParentUserTaskVo c = new ParentUserTaskVo();
//		c.setUserId(203L);
//		c.setIsValid(1);
//		c.setGameLevel(5);
//
//		Map<Long, List<ParentUserTaskVo>> parentMap = new HashMap<>();
//		parentMap.put(100L, java.util.Arrays.asList(a, b, c));
//
//		// V3: 团队30%，平级10%；V5: 团队50%，平级10%
//		Map<Integer, W3UserLevelConfig> levelCfg = new HashMap<>();
//		levelCfg.put(3, buildCfg(3, "0.30", "0.10"));
//		levelCfg.put(5, buildCfg(5, "0.50", "0.10"));
//
//		// 超越奖 10%
//		BigDecimal exceedRatio = new BigDecimal("0.10");
//
//		Map<Long, BigDecimal> teamReward = calcTeamRewards(staticMap, parentMap, levelCfg, exceedRatio, new ArrayList<>(),
//			BigDecimal.ONE);
//		System.out.println("团队奖励结果：");
//		for (Map.Entry<Long, BigDecimal> e : teamReward.entrySet()) {
//			System.out.println("userId=" + e.getKey() + " reward=" + e.getValue());
//		}
//	}
//
//	private static W3UserLevelConfig buildCfg(int level, String rewardRatio, String peerRatio) {
//		W3UserLevelConfig cfg = new W3UserLevelConfig();
//		cfg.setLevel(level);
//		cfg.setRewardRatio(new BigDecimal(rewardRatio));
//		cfg.setPeerRewardRatio(new BigDecimal(peerRatio));
//		return cfg;
//	}
//}
//
//
