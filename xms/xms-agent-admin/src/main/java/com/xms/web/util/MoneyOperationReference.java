package com.xms.web.util;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ArrayUtil;
import com.xms.common.constant.ConstantStatic;
import com.xms.common.constant.ConstantType;
import com.xms.common.exception.ServiceException;
import com.xms.common.utils.uuid.IDUtils;
import com.xms.dao.domain.RewardRecord;
import com.xms.dao.entity.domain.UserInfo;
import com.xms.dao.entity.domain.UserMoney;
import com.xms.dao.service.IRewardRecordService;
import com.xms.dao.service.UserInfoService;
import com.xms.dao.service.UserWalletService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 【参考类】余额操作 & 业绩计算标准示例
 *
 * 本类不对外暴露接口，仅作为编写"保存流水 + 加减余额 + 业绩计算"的权威参考。
 * 新增类似功能时，严格按照本类的模式实现，不要自行发明写法。
 *
 * ╔══════════════════════════════════════════════════════════════╗
 * ║  【AI 强制规则】在实现任何业绩加减之前，必须先询问用户：      ║
 * ║  "本次操作影响的是哪种业绩？"                                ║
 * ║  不同业务操作的业绩字段完全不同，绝对不能自行假设。           ║
 * ║  必须等用户确认后，再对照下方"业绩字段速查表"编写代码。       ║
 * ╚══════════════════════════════════════════════════════════════╝
 *
 * ┌──────────────────────────────────────────────────────────────┐
 * │  业绩字段速查表（t_user_info）                                │
 * ├──────────────────┬──────────────────────────────────────────┤
 * │  字段                   │ 含义 & 适用业务                   │
 * ├──────────────────┬──────────────────────────────────────────┤
 * │ performance             │ 个人业绩（质押/矿机业务）          │
 * │ umbrella_performance    │ 伞下团队业绩（质押/矿机，整条链路）│
 * │ sub_node_performance    │ 直推节点数量（节点业务，仅直推人） │
 * │ node_team_performance   │ 团队节点数量（节点业务，整条链路） │
 * │ community_performance   │ 小区业绩（派生，重算而来）         │
 * │ max_leg_performance     │ 大区业绩（派生，重算而来）         │
 * │ sub_performance         │ 直推业绩（已废弃，勿使用）         │
 * └──────────────────┴──────────────────────────────────────────┘
 *
 * 目录：
 *  1. 单个用户余额增加   → {@link #singleUserMoneyAddExample}
 *  2. 单个用户余额扣减   → {@link #singleUserMoneyDeductExample}
 *  3. 批量用户余额变更   → {@link #batchUserMoneyExample}
 *  4. 质押/矿机业务业绩  → {@link #addStakePerformanceExample} / {@link #subtractStakePerformanceExample}
 *  5. 节点业务业绩       → {@link #addNodePerformanceExample} / {@link #subtractNodePerformanceExample}
 *  6. 小区/大区业绩重算  → {@link #calculateCommunityPerformanceExample}
 */
@Slf4j
@Component
@AllArgsConstructor
public class MoneyOperationReference {

    // ─────────────────────────────────────────────
    // 原生 SQL 常量：余额字段均采用累加/累减写法，禁止覆盖
    // ─────────────────────────────────────────────
    private static final String SQL_VALID_NUM1_ADD =
        "UPDATE t_user_money SET update_time=?,gt_id=?,valid_num1=valid_num1+?,source_code=?,source_type=?,source_id=? WHERE id=?";
    private static final String SQL_VALID_NUM2_ADD =
        "UPDATE t_user_money SET update_time=?,gt_id=?,valid_num2=valid_num2+?,source_code=?,source_type=?,source_id=? WHERE id=?";
    private static final String SQL_VALID_NUM3_ADD =
        "UPDATE t_user_money SET update_time=?,gt_id=?,valid_num3=valid_num3+?,source_code=?,source_type=?,source_id=? WHERE id=?";
    private static final String SQL_VALID_NUM5_ADD =
        "UPDATE t_user_money SET update_time=?,gt_id=?,valid_num5=valid_num5+?,source_code=?,source_type=?,source_id=? WHERE id=?";

    private final JdbcTemplate jdbcTemplate;
    private final UserWalletService userWalletService;
    private final IRewardRecordService rewardRecordService;
    private final UserInfoService userInfoService;


    // ═══════════════════════════════════════════════════════
    //  1. 单个用户余额增加
    //     使用场景：单笔奖励发放、人工调账加款等
    //     调用 handerUserMoney，传正数 amount
    // ═══════════════════════════════════════════════════════

    /**
     * 单个用户余额增加示例。
     *
     * @param userId        目标用户ID
     * @param amount        增加金额（必须 > 0）
     * @param sourceOrderNo 来源订单号，用于追溯
     * @param sourceUserId  来源用户ID（系统操作填操作人ID，自动任务填 userId 本身）
     * @param bizType       ConstantType.user_money_log_source_type.typeXX（必须按业务确认）
     * @param coinType      ConstantType.user_money_coin_type.typeXX（必须按业务确认）
     */
    private void singleUserMoneyAddExample(Long userId, BigDecimal amount,
                                           String sourceOrderNo, Long sourceUserId,
                                           int bizType, int coinType) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        int rows = userWalletService.handerUserMoney(amount, sourceOrderNo, userId, sourceUserId, bizType, coinType);
        if (rows != 1) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            throw new ServiceException("更新用户余额失败，userId=" + userId);
        }
    }


    // ═══════════════════════════════════════════════════════
    //  2. 单个用户余额扣减
    //     使用场景：提现扣款、消费扣款等
    //     关键点：amount 先取反（.negate()），再传入 handerUserMoney
    //     内部 SQL 是 field = field + ?，传负数即为减法
    // ═══════════════════════════════════════════════════════

    /**
     * 单个用户余额扣减示例（来自 BizWithdrawalServiceImpl 提现扣款）。
     *
     * 注意：amount 本身传正数，方法内部自动取反传给 handerUserMoney。
     * 如果调用方已经传入负数，去掉 negate() 改为直接传。
     *
     * @param userId        目标用户ID
     * @param amount        扣减金额（传正数，方法内取反）
     * @param sourceOrderNo 来源订单号
     * @param sourceUserId  来源用户ID
     * @param bizType       ConstantType.user_money_log_source_type.typeXX
     * @param coinType      ConstantType.user_money_coin_type.typeXX
     */
    private void singleUserMoneyDeductExample(Long userId, BigDecimal amount,
                                              String sourceOrderNo, Long sourceUserId,
                                              int bizType, int coinType) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        // .negate() 将正数变为负数，handerUserMoney 内部 SQL 执行 field = field + (-amount)
        int rows = userWalletService.handerUserMoney(amount.negate(), sourceOrderNo, userId, sourceUserId, bizType, coinType);
        if (rows != 1) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            throw new ServiceException("扣减用户余额失败，userId=" + userId);
        }
    }


    // ═══════════════════════════════════════════════════════
    //  3. 批量用户余额变更 + 同步写流水记录
    //     使用场景：定时任务批量发放奖励（日结、周结等）
    //     固定 1000 条一批，防止单事务对象过大
    // ═══════════════════════════════════════════════════════

    /**
     * 批量发放奖励示例（valid_num1，即 USDT）。
     *
     * 替换其他字段时只需换 SQL 常量和 amountGetter：
     *   valid_num1 → USDT         → SQL_VALID_NUM1_ADD, e -> e.getValidNum1()
     *   valid_num2 → FSN          → SQL_VALID_NUM2_ADD, e -> e.getValidNum2()
     *   valid_num3 → 线性释放     → SQL_VALID_NUM3_ADD, e -> e.getValidNum3()
     *   valid_num5 → DFC 产出     → SQL_VALID_NUM5_ADD, e -> e.getValidNum1()  (注意：valid5 setter 也叫 validNum1，见实现)
     */
    private void batchUserMoneyExample(List<BatchRewardItem> rewardItems) {
        if (CollectionUtil.isEmpty(rewardItems)) {
            return;
        }

        int batchSize = 1000;
        int count = 0;
        Date now = new Date();
        List<UserMoney> moneyList = new ArrayList<>(batchSize);
        List<RewardRecord> recordList = new ArrayList<>(batchSize);

        for (BatchRewardItem item : rewardItems) {
            if (item.amount == null || item.amount.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            // ① 构建钱包累加对象（只填本次增量，SQL 自动 field = field + delta）
            UserMoney entity = new UserMoney();
            entity.setId(item.userId);                   // WHERE id=? 的条件，即 userId（非自增主键）
            entity.setValidNum1(item.amount);            // 本次增量（按实际字段替换）
            entity.setGtId(IDUtils.getSnowflakeStr());   // 每条流水唯一ID，必须设置
            entity.setSourceCode(item.sourceOrderNo);
            entity.setSourceId(item.sourceUserId);
            entity.setSourceType(ConstantType.user_money_log_source_type.type_8); // 替换为实际类型
            entity.setUpdateTime(now);
            moneyList.add(entity);

            // ② 构建奖励流水记录
            RewardRecord record = new RewardRecord();
            record.setOrderCode(IDUtils.getSnowflakeStr());
            record.setUserId(item.userId);
            record.setAmount(item.amount);
            record.setCoinType(ConstantType.user_money_coin_type.type_1);             // 替换为实际币种
            record.setSourceType(ConstantType.xms_reward_record_source_type.type_6); // 替换为实际来源
            record.setSourceOrderCode(item.sourceOrderNo);
            record.setSourceUserId(item.sourceUserId);
            record.setCreateTime(now);
            recordList.add(record);

            // ③ 达到批次上限，提交一次
            count++;
            if (count >= batchSize) {
                bachUpdateMoneyByField(SQL_VALID_NUM1_ADD, moneyList, e -> e.getValidNum1());
                moneyList.clear();
                rewardRecordService.saveBatch(recordList);
                recordList.clear();
                count = 0;
            }
        }

        // ④ 提交尾部不足一批的数据
        if (CollectionUtil.isNotEmpty(moneyList)) {
            bachUpdateMoneyByField(SQL_VALID_NUM1_ADD, moneyList, e -> e.getValidNum1());
        }
        if (CollectionUtil.isNotEmpty(recordList)) {
            rewardRecordService.saveBatch(recordList);
        }
    }

    /**
     * 通用批量余额更新，适配 valid_num1 ~ valid_num5 等各字段。
     * 任意一行影响行数为 0（用户不存在），整批回滚。
     */
    private void bachUpdateMoneyByField(String sql, List<UserMoney> list,
                                        java.util.function.Function<UserMoney, BigDecimal> amountGetter) {
        int[] rows = jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                UserMoney m = list.get(i);
                ps.setTimestamp(1, new java.sql.Timestamp(m.getUpdateTime().getTime()));
                ps.setString(2, m.getGtId());
                ps.setBigDecimal(3, amountGetter.apply(m));
                ps.setString(4, m.getSourceCode());
                ps.setInt(5, m.getSourceType());
                ps.setLong(6, m.getSourceId());
                ps.setLong(7, m.getId());
            }
            @Override
            public int getBatchSize() { return list.size(); }
        });
        if (ArrayUtil.contains(rows, 0)) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            log.error("批量余额更新：存在用户记录不存在，已回滚");
            throw new ServiceException("批量余额更新失败，请检查用户钱包初始化");
        }
    }


    // ═══════════════════════════════════════════════════════
    //  4. 质押 / 矿机业务 — 个人业绩 & 团队业绩
    //
    //  字段：
    //    performance            → 个人业绩（质押金额），仅影响自己
    //    umbrella_performance   → 伞下团队业绩（质押金额），影响整条 parentIds 链路
    //
    //  触发时机：
    //    增加 → 用户购买矿机/质押订单支付成功回调
    //    减少 → 用户主动退出订单 / 订单出局
    //
    //  【AI 规则】只有在用户明确说"这是质押业绩"或"矿机业绩"时，才能使用这组方法。
    // ═══════════════════════════════════════════════════════

    /** 质押/矿机业务：增加个人业绩（购买支付成功时调用）。 */
    private void addStakePerformanceExample(Long userId, BigDecimal amount) {
        boolean ok = userInfoService.lambdaUpdate()
            .eq(UserInfo::getUserId, userId)
            .setSql("performance = performance + " + amount.toPlainString())
            .update();
        if (!ok) {
            throw new ServiceException("更新个人业绩失败，userId=" + userId);
        }
    }

    /** 质押/矿机业务：扣减个人业绩（退出/出局时调用）。GREATEST 防止变负数。 */
    private void subtractStakePerformanceExample(Long userId, BigDecimal amount) {
        userInfoService.lambdaUpdate()
            .eq(UserInfo::getUserId, userId)
            .setSql("performance = GREATEST(IFNULL(performance, 0) - " + amount.toPlainString() + ", 0)")
            .update();
    }

    /**
     * 质押/矿机业务：增加伞下团队业绩（整条上级链路同时加）。
     *
     * @param parentIds userInfo.getParentIds() 获取的上级链路
     * @param amount    增加的质押金额
     */
    private void addStakeTeamPerformanceExample(List<Long> parentIds, BigDecimal amount) {
        if (CollectionUtil.isEmpty(parentIds)) {
            return;
        }
        boolean ok = userInfoService.lambdaUpdate()
            .in(UserInfo::getUserId, parentIds)
            .setSql("umbrella_performance = umbrella_performance + " + amount.toPlainString())
            .update();
        if (!ok) {
            throw new ServiceException("更新伞下团队业绩失败");
        }
    }

    /** 质押/矿机业务：扣减伞下团队业绩（退出/出局时调用）。 */
    private void subtractStakeTeamPerformanceExample(List<Long> parentIds, BigDecimal amount) {
        if (CollectionUtil.isEmpty(parentIds)) {
            return;
        }
        userInfoService.lambdaUpdate()
            .in(UserInfo::getUserId, parentIds)
            .setSql("umbrella_performance = GREATEST(IFNULL(umbrella_performance, 0) - " + amount.toPlainString() + ", 0)")
            .update();
    }

    // 兼容旧命名，保持向后一致
    private void addPersonalPerformanceExample(Long userId, BigDecimal amount) { addStakePerformanceExample(userId, amount); }
    private void subtractPersonalPerformanceExample(Long userId, BigDecimal amount) { subtractStakePerformanceExample(userId, amount); }
    private void addTeamPerformanceExample(List<Long> parentIds, BigDecimal amount) { addStakeTeamPerformanceExample(parentIds, amount); }
    private void subtractTeamPerformanceExample(List<Long> parentIds, BigDecimal amount) { subtractStakeTeamPerformanceExample(parentIds, amount); }


    // ═══════════════════════════════════════════════════════
    //  5. 节点业务 — 直推节点数 & 团队节点数
    //
    //  字段：
    //    sub_node_performance   → 直推节点数量，仅增加直推人（inviteUserId）自己的记录
    //    node_team_performance  → 团队节点数量，影响整条 parentIds 链路
    //
    //  注意：节点数量单位是"个"，固定 +1 / -1，不是金额
    //
    //  触发时机：
    //    增加 → 用户购买节点订单支付成功回调（见 handleBizType1）
    //    减少 → 节点退出（如有该业务）
    //
    //  【AI 规则】只有在用户明确说"这是节点业绩"时，才能使用这组方法。
    // ═══════════════════════════════════════════════════════

    /**
     * 节点业务：直推人节点数 +1（仅增加购买者的直接邀请人）。
     *
     * @param inviteUserId 购买者的直接邀请人 ID（userInfo.getInviteUserId()）
     */
    private void addNodePerformanceExample(Long inviteUserId) {
        if (inviteUserId == null) {
            return;
        }
        userInfoService.lambdaUpdate()
            .eq(UserInfo::getUserId, inviteUserId)
            .setSql("sub_node_performance = sub_node_performance + 1")
            .update();
    }

    /**
     * 节点业务：团队节点数 +1（整条上级链路）。
     *
     * @param parentIds userInfo.getParentIds()，购买者的所有上级
     */
    private void addNodeTeamPerformanceExample(List<Long> parentIds) {
        if (CollectionUtil.isEmpty(parentIds)) {
            return;
        }
        userInfoService.lambdaUpdate()
            .in(UserInfo::getUserId, parentIds)
            .setSql("node_team_performance = node_team_performance + 1")
            .update();
    }

    /** 节点业务：直推人节点数 -1。GREATEST 防止变负数。 */
    private void subtractNodePerformanceExample(Long inviteUserId) {
        if (inviteUserId == null) {
            return;
        }
        userInfoService.lambdaUpdate()
            .eq(UserInfo::getUserId, inviteUserId)
            .setSql("sub_node_performance = GREATEST(IFNULL(sub_node_performance, 0) - 1, 0)")
            .update();
    }

    /** 节点业务：团队节点数 -1（整条上级链路）。 */
    private void subtractNodeTeamPerformanceExample(List<Long> parentIds) {
        if (CollectionUtil.isEmpty(parentIds)) {
            return;
        }
        userInfoService.lambdaUpdate()
            .in(UserInfo::getUserId, parentIds)
            .setSql("node_team_performance = GREATEST(IFNULL(node_team_performance, 0) - 1, 0)")
            .update();
    }


    // ═══════════════════════════════════════════════════════
    //  6. 小区业绩 & 大区业绩重算（适用于质押/矿机业务）
    //
    //  定义：
    //    大区业绩（max_leg_performance） = 直推线中贡献值最大的一条线
    //    小区业绩（community_performance）= 所有直推线总贡献值 - 大区业绩
    //    子节点贡献值 = 子节点 performance + 子节点 umbrella_performance
    //
    //  触发时机：umbrella_performance 或 performance 变化后立即重算
    //
    //  【AI 规则】节点业务不使用本方法，只有质押/矿机业务才重算小区/大区。
    // ═══════════════════════════════════════════════════════

    /**
     * 重新计算指定上级列表的小区业绩 & 大区业绩。
     *
     * @param parentIds 需要重算的上级用户 ID 列表（userInfo.getParentIds()）
     */
    private void calculateCommunityPerformanceExample(List<Long> parentIds) {
        if (CollectionUtil.isEmpty(parentIds)) {
            return;
        }
        for (Long parentId : parentIds) {
            List<UserInfo> children = userInfoService.lambdaQuery()
                .eq(UserInfo::getInviteUserId, parentId)
                .select(UserInfo::getUserId,
                    UserInfo::getPerformance,
                    UserInfo::getUmbrellaPerformance)
                .list();

            if (CollectionUtil.isEmpty(children) || children.size() <= 1) {
                userInfoService.lambdaUpdate()
                    .eq(UserInfo::getUserId, parentId)
                    .set(UserInfo::getCommunityPerformance, BigDecimal.ZERO)
                    .update();
                continue;
            }

            BigDecimal totalPerformance = BigDecimal.ZERO;
            BigDecimal maxLegPerformance = BigDecimal.ZERO;

            for (UserInfo child : children) {
                BigDecimal legPerformance = child.getPerformance().add(child.getUmbrellaPerformance());
                totalPerformance = totalPerformance.add(legPerformance);
                if (legPerformance.compareTo(maxLegPerformance) > 0) {
                    maxLegPerformance = legPerformance;
                }
            }

            BigDecimal communityPerformance = totalPerformance.subtract(maxLegPerformance);
            if (communityPerformance.compareTo(BigDecimal.ZERO) < 0) {
                communityPerformance = BigDecimal.ZERO;
            }

            userInfoService.lambdaUpdate()
                .eq(UserInfo::getUserId, parentId)
                .set(UserInfo::getCommunityPerformance, communityPerformance)
                .set(UserInfo::getMaxLegPerformance, maxLegPerformance)
                .update();
        }
    }


    // ═══════════════════════════════════════════════════════
    //  内部数据结构（批量奖励场景使用）
    // ═══════════════════════════════════════════════════════

    /**
     * 批量奖励发放时，每个用户对应的奖励项。
     * 调用方自行组装，传入 {@link #batchUserMoneyExample}。
     */
    public static class BatchRewardItem {
        /** 收益人 userId */
        public Long userId;
        /** 本次增量金额（必须 > 0） */
        public BigDecimal amount;
        /** 来源订单号，用于追溯 */
        public String sourceOrderNo;
        /** 来源用户ID */
        public Long sourceUserId;

        public BatchRewardItem(Long userId, BigDecimal amount,
                               String sourceOrderNo, Long sourceUserId) {
            this.userId = userId;
            this.amount = amount;
            this.sourceOrderNo = sourceOrderNo;
            this.sourceUserId = sourceUserId;
        }
    }
}
