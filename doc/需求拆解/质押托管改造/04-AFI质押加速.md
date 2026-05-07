# 第四批：AFI 质押加速

## 目标

实现托管订单的 AFI 质押加速能力。

AFI 加速只作用于单笔托管订单，不作用于用户全局收益。用户可对符合条件的托管订单质押 AFI，质押成功后从次日开始提高该托管订单的静态收益倍率。

## 已确认规则

- 一个托管订单只能绑定一个 AFI 质押加速记录。
- 已绑定 AFI 加速的托管订单，不允许重复质押或追加质押。
- 1 天套餐不参与 AFI 加速。
- 只有 30 天及以上套餐可参与 AFI 加速。
- 只有 `产出中`、未完成、未绑定 AFI 加速的托管订单可以质押 AFI。
- 今天质押 AFI，明天开始加速收益。
- 当天已发放过静态收益的订单，当天不补发加速差额。
- 无论托管期第几天质押 AFI，托管订单到期时，绑定的 AFI 都退还给用户。
- AFI 不做销毁，本批口径改为到期退还。
- AFI 资产使用用户钱包 `valid_num2`，对应 `ConstantType.user_money_coin_type.type_2`。
- 用户质押 AFI 时，立即扣减 AFI 可用余额，并写钱包流水。
- 托管订单到期时，将质押 AFI 退还到用户 `valid_num2`，并写钱包流水。
- AFI 充值来源后续单独实现，本批先记录为前置依赖。
- AFI 价格先使用后台配置价格，后续可替换为接口获取价格。
- 对托管订单进行 AFI 质押加速时，必须记录当时 AFI 币价快照，避免历史记录受后续价格变化影响。
- 加速档位通过新配置表维护，不在代码中写死。

## 加速计算口径

AFI 加速按“AFI 等值 USDT / 托管 USDT 金额”的比例命中后台配置档位。

示例：

```text
托管订单金额 = 1000 USDT
用户质押 AFI 等值 = 10 USDT
质押比例 = 10 / 1000 = 1%
命中配置：1% => 1.1x
最终收益 = 原静态收益 × 1.1
```

每日静态收益计算时：

```text
基础静态收益 = 托管金额 × 当日基础收益率
加速后静态收益 = 基础静态收益 × 加速倍率
```

如果订单当天未到 AFI 生效日期，则不使用加速倍率。

## 表结构影响

### AFI 加速配置表

新增 AFI 加速配置表，用于后台维护质押比例和加速倍率。

建议表名：`t_stake_hosting_afi_accelerate_config`

建议字段：

- `id`
- `pledge_ratio`：AFI 等值 USDT / 托管 USDT 比例，单位 %
- `accelerate_rate`：加速倍率，例如 `1.10`
- `sort`
- `status`：`0 停用`，`1 启用`
- `remark`
- `create_time`
- `update_time`
- `create_by`
- `update_by`
- `deleted`

命中规则：

- 只使用启用状态配置。
- 按 `pledge_ratio <= 当前质押比例` 命中。
- 当命中多个配置时，取满足条件的最高 `pledge_ratio` 档位。
- 质押记录保存命中的 `pledge_ratio` 和 `accelerate_rate` 快照。

### 托管订单 AFI 质押记录表

新增托管订单 AFI 质押记录表。

建议表名：`t_stake_hosting_afi_pledge`

建议字段：

- `id`
- `pledge_no`：质押单号
- `stake_hosting_order_id`
- `stake_hosting_order_no`
- `user_id`
- `account`
- `stake_usdt_amount`：托管订单金额快照
- `afi_amount`：质押 AFI 数量
- `afi_price`：AFI 价格快照
- `afi_usdt_amount`：AFI 等值 USDT
- `pledge_ratio`：命中比例
- `accelerate_rate`：命中倍率
- `pledge_time`
- `effective_day`：生效日期，格式 `yyyyMMdd`
- `return_time`：退还时间
- `status`：`0 未生效`，`1 生效中`，`2 已退还`
- `remark`
- `create_time`
- `update_time`
- `create_by`
- `update_by`
- `deleted`

建议约束：

- `uk_stake_hosting_order_id(stake_hosting_order_id)`，保证一个托管订单只能绑定一个 AFI 加速记录。
- `uk_pledge_no(pledge_no)`
- `idx_user_id(user_id)`
- `idx_status(status)`
- `idx_effective_day(effective_day)`

### 托管订单表

`t_stake_hosting_order` 建议增加字段：

- `afi_accelerated`：是否已绑定 AFI 加速，`0 否`，`1 是`

用于列表快速判断订单是否已经加速。

### AFI 充值记录

AFI 来自用户链上充值，类似买节点链上回调。

本批先记录依赖，后续单独实现：

- AFI 充值记录表。
- AFI 充值回调。
- AFI 入账 `valid_num2`。
- AFI 钱包流水。

## 后端影响

### App 端

新增接口：

- 可加速托管订单列表。
- 托管订单详情展示 AFI 加速状态。
- 提交 AFI 质押加速。

提交 AFI 质押时校验：

- 托管订单属于当前用户。
- 托管订单状态为 `产出中`。
- 套餐天数大于 1 天。
- 订单未完成。
- 订单未绑定 AFI 加速。
- AFI 数量大于 0。
- 用户 `valid_num2` 余额足够。
- 能命中有效加速配置。
- 当前 AFI 后台配置价格大于 0。

质押成功后：

- 扣减用户 AFI 钱包余额。
- 写 AFI 钱包流水。
- 写 AFI 质押记录。
- 更新托管订单 `afi_accelerated = 1`。

### 定时收益任务

每日发放托管静态收益时：

- 先计算基础静态收益。
- 查询该托管订单是否存在已生效 AFI 加速记录。
- 如果存在且 `effective_day <= 当前日期`，则静态收益乘以加速倍率。
- 如果不存在或未到生效日期，则按基础收益发放。

本节只记录与 AFI 加速的关联口径，具体定时收益任务改造后续单独处理，因为收益任务涉及基础收益、服务费、团队奖励、订单完成等多段逻辑。

### 订单完成

托管订单达到套餐天数完成时：

- 正常扣减托管业绩。
- 如果存在 AFI 加速记录且未退还，则将质押 AFI 退还给用户。
- 退还到用户钱包 `valid_num2`。
- 写 AFI 退还钱包流水。
- 更新 AFI 质押记录为已退还。
- 记录退还时间。

## 后台影响

新增后台页面：

- AFI 加速配置管理。
- 托管订单 AFI 加速记录查询。

AFI 加速配置管理支持：

- 查询。
- 新增。
- 修改。
- 启用 / 停用。
- 排序。

AFI 加速记录页面支持：

- 按用户 ID、钱包地址、托管订单号、质押单号、状态查询。
- 查看 AFI 数量、价格快照、等值 USDT、命中比例、加速倍率、生效日期、退还时间。
- 导出。

AFI 充值记录后台页面后续单独实现。

## 待确认问题

- AFI 质押扣减钱包流水 `source_type` 编号待确认，建议新增 `35=AFI质押扣减`，不复用平台拨扣。
- AFI 到期退还钱包流水 `source_type` 编号待确认，建议新增 `36=AFI质押退还`。
- AFI 后台配置价格存放位置待确认：可以使用 `t_sys_para`，也可以新增独立配置表。
- AFI 充值完整业务后续单独设计，包括充值记录表、链上回调、幂等入账和后台查询。
- AFI 加速配置是否需要限制最大倍率或最大质押比例，后续实现前再确认。
- 定时收益任务改造后续单独细化，本批文档先记录 AFI 加速对收益任务的影响点。

## 验收用例

- 1 天套餐提交 AFI 加速失败。
- 非产出中订单提交 AFI 加速失败。
- 已完成订单提交 AFI 加速失败。
- 已绑定 AFI 加速的订单重复提交失败。
- AFI 余额不足提交失败。
- AFI 后台配置价格小于等于 0 时提交失败。
- 未命中有效加速配置提交失败。
- 质押成功后，用户 `valid_num2` 立即扣减。
- 质押成功后写 AFI 钱包流水。
- 质押成功后生成 AFI 质押记录。
- 质押成功后托管订单 `afi_accelerated = 1`。
- 当天质押当天收益不加速。
- 次日发放收益时按加速倍率计算。
- 质押记录保存质押时 AFI 币价快照。
- 托管订单完成时，AFI 质押数量退还到用户 `valid_num2`。
- 到期退还时写 AFI 钱包流水。
- 托管订单完成时，AFI 质押记录更新为已退还。
