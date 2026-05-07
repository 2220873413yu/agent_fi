---
name: xms-wallet-performance
description: 指导 xms 项目中的钱包与业绩操作，包含单个钱包加减、批量钱包发放、直推奖/间推奖定义、质押与节点业绩定义及小区大区重算。用户提到“钱包操作”“批量发放”“直推奖”“间推奖”“rewardRecord”“userMoney”“业绩计算”“小区业绩”“大区业绩”“bachUpdateMoneyValid”时使用。
---

# XMS 钱包与业绩操作规范

## 权威参考

实现前先阅读并对齐这两个文件：

- `xms/xms-agent-admin/src/main/java/com/xms/web/util/MoneyOperationReference.java`
- `xms/xms-common/src/main/java/com/xms/common/constant/ConstantType.java`

禁止脱离参考类自行发明新写法。

## 总原则

涉及发奖或余额变更时，必须同时完成两件事：

1. 写奖励流水：`RewardRecord`
2. 更新钱包余额：`t_user_money` 使用累加 SQL（`field = field + ?`）

钱包字段更新必须是增量，不能覆盖总值。

## 单个钱包操作（低频）

适用：人工调账、提现扣款、单笔奖励。

- 增加余额：调用 `userWalletService.handerUserMoney(amount, ...)`，`amount` 传正数
- 扣减余额：调用 `userWalletService.handerUserMoney(amount.negate(), ...)`
- 返回值必须校验：`rows == 1`，否则抛异常并回滚

关键约束：

- 扣款时必须用 `amount.negate()`，不要手写负数字面量
- `sourceType`、`coinType` 必须按业务确认后再填

## 批量钱包操作（高频）

适用：定时结算、批量发奖。

固定流程：

1. 按 1000 条分批处理
2. `reward <= 0` 直接跳过
3. 先构建 `UserMoney` 列表（本次增量）
4. 构建 `RewardRecord` 列表
5. 每满 1000 条提交一次：批量更新钱包 + `saveBatch` 流水
6. 循环结束后提交尾批

`UserMoney` 必填项：

- `id`：用户 ID（WHERE 条件）
- `gtId`：`IDUtils.getSnowflakeStr()`
- 对应增量字段：`validNum1/2/3/5`
- `sourceCode`、`sourceId`、`sourceType`、`updateTime`

批量更新必须使用 `JdbcTemplate.batchUpdate` + 累加 SQL；若任意行影响 0 行，整批回滚。

## 钱包字段与批量方法映射

- `valid_num1`：USDT
- `valid_num2`：FSN
- `valid_num3`：线性释放余额
- `valid_num5`：DFC 产出

实现时按 `MoneyOperationReference` 的 SQL 常量与 `bachUpdateMoneyByField` 模式落地。

## 直推奖/间推奖定义

以 `ConstantType` 注释定义为准：

- `ConstantType.xms_reward_record_source_type.type_1`：节点直推奖
- `ConstantType.xms_reward_record_source_type.type_2`：节点间推奖
- `ConstantType.user_money_log_source_type.type_1`：节点直推奖
- `ConstantType.user_money_log_source_type.type_2`：节点间推奖

业务类型补充：

- `ConstantType.xms_reward_record_business_type.type_2`：直推奖励（算力）
- `ConstantType.xms_reward_record_business_type.type_3`：间推奖励（算力）

规则：发放直推/间推奖励时，`RewardRecord.sourceType` 与钱包 `sourceType` 要保持语义一致，禁止一边写直推一边写间推。

## 业绩定义（t_user_info）

- `performance`：个人业绩（质押/矿机）
- `umbrella_performance`：伞下团队业绩（质押/矿机）
- `sub_node_performance`：直推节点数（节点业务，仅直推人）
- `node_team_performance`：团队节点数（节点业务，整条上级链路）
- `community_performance`：小区业绩（派生重算）
- `max_leg_performance`：大区业绩（派生重算）
- `sub_performance`：废弃字段，不使用

## 业绩操作前置询问（强制）

在实现任何业绩变更前，必须先问用户：

`本次操作影响的是哪种业绩？（质押/矿机业绩、节点业绩、还是其他）`

未确认业务类型时，不得直接写业绩字段。

## 质押/矿机业绩规则

- 个人业绩：`performance +/- amount`
- 团队业绩：对 `parentIds` 全链路更新 `umbrella_performance +/- amount`
- 扣减使用 `GREATEST(IFNULL(field,0)-amount,0)` 防负数
- 当 `performance` 或 `umbrella_performance` 变化后，必须重算小区/大区

## 节点业绩规则

- 直推节点数：`sub_node_performance +/- 1`（仅 `inviteUserId`）
- 团队节点数：`node_team_performance +/- 1`（`parentIds` 全链路）
- 单位是“个数”而不是金额
- 节点业绩变更不触发小区/大区重算

## 小区/大区业绩定义与重算

仅用于质押/矿机场景。

- 子线贡献值：`child.performance + child.umbrella_performance`
- 大区业绩：所有直推线贡献值中的最大值
- 小区业绩：所有直推线贡献总和 - 大区业绩（最小为 0）

当上级列表为 `parentIds` 时，按父级逐个重算并回写：

- `community_performance`
- `max_leg_performance`

## 金额精度规则

统一使用：

`setScale(ConstantStatic.newScale, ConstantStatic.roundingModeNew)`

禁止：

- `new BigDecimal(double)`
- 硬编码精度或舍入模式

## 常见错误清单

- 把钱包总额写回字段（应只传本次增量）
- 漏填 `gtId`
- `reward <= 0` 仍入库
- 一次性提交超大批次（应 1000 一批）
- 未确认业绩类型就更新字段
- 直推奖与间推奖的 `sourceType` 混用
