# 创世节点AFI线性释放

## 目标

节点认购结束后，把创世节点释放池 `1000万 AFI` 按节点订单权重一次性初始化成释放订单。每笔节点订单生成一条线性释放订单，释放周期固定 `365天`，每日释放到用户 AFI 钱包 `valid_num2`。

后台只提供查询和导出，不允许手动修改释放金额。

## 分配公式

```text
genesis_pool_amount = 10000000 AFI
order_weight = t_node_package_order.weight_multiplier
total_weight = sum(order_weight) where t_node_package_order.status = 1
amount_per_weight = genesis_pool_amount / total_weight
total_release_amount = order_weight * amount_per_weight
daily_release_amount = total_release_amount / 365
```

示例：

```text
A1权重=1，A2权重=3，A3权重=10

如果：
A1买X个
A2买Y个
A3买Z个

total_weight = X*1 + Y*3 + Z*10
每个A1订单可分 = 10000000 / total_weight
每个A2订单可分 = 3 * 10000000 / total_weight
每个A3订单可分 = 10 * 10000000 / total_weight
```

## 表结构

新增 `t_node_package_release_order`，每笔支付成功的节点订单最多对应一条释放订单。

核心字段：

- `node_order_id`：来源节点订单ID，唯一索引用于保证初始化任务幂等。
- `node_order_no`：来源节点订单号。
- `user_id/address`：用户和钱包地址快照。
- `package_level/order_value_usdt/weight_multiplier`：下单时节点等级、金额、权重快照。
- `total_weight`：初始化时全网总权重。
- `amount_per_weight`：初始化时每1份权重可分AFI。
- `total_release_amount`：本释放订单总释放AFI。
- `daily_release_amount`：每日释放AFI。
- `released_amount/remaining_amount`：已释放和剩余AFI。
- `total_days/run_days/last_release_day`：释放周期、已运行天数、最后释放日期。
- `status`：`0待释放 1释放中 2释放完成 3异常`。

## 初始化任务

任务入口：

```text
xmsTask.initNodePackageReleaseOrders
```

流程：

1. 查询 `t_node_package_order.status = 1` 的支付成功订单。
2. 过滤 `weight_multiplier <= 0` 或为空的订单。
3. 按所有有效订单计算 `total_weight` 和 `amount_per_weight`。
4. 查询已存在释放订单的 `node_order_id`。
5. 只为缺失释放订单的节点订单插入 `t_node_package_release_order`。

幂等规则：

```text
uk_node_order_id(node_order_id)
```

重复执行时，同一来源节点订单不会重复生成释放订单。

## 每日释放任务

任务入口：

```text
xmsTask.releaseNodePackageAfiDaily
```

流程：

1. 每天先检查内部任务记录 `SysConstant.TSK_TYPE_200 + yyyyMMdd`，当天已执行则跳过。
2. 查询 `status in (0,1)`、`remaining_amount > 0`、`run_days < 365` 的释放订单。
3. 如果该订单 `last_release_day >= 今天`，跳过，避免重复释放。
4. 普通日期释放 `daily_release_amount`。
5. 第365天直接释放 `remaining_amount`，避免小数截断留下尾差。
6. 增加用户 AFI 钱包 `t_user_money.valid_num2`。
7. 写钱包流水来源类型 `45 节点认购AFI线性释放`。
8. 写奖励记录来源类型 `32 节点认购AFI线性释放`。
9. 更新释放订单 `released_amount/remaining_amount/run_days/last_release_day/status`。

完成规则：

```text
remaining_amount <= 0 或 run_days >= 365
```

满足任一条件后，释放订单状态改为 `2释放完成`。

## 后台菜单

菜单挂载：

```text
父菜单：节点管理
parent_id = 2375
页面：节点线性释放订单
组件：xms/nodePackageReleaseOrder/index
权限前缀：xms:nodePackageReleaseOrder:*
```

页面能力：

- 查询
- 导出

页面不提供：

- 新增
- 编辑
- 删除
- 手动修改释放金额

## 测试口径

1. 初始化公式：用 A1/A2/A3 权重 `1/3/10` 验证 `amount_per_weight` 和每笔订单总释放。
2. 多笔同用户节点订单：应分别生成多条释放订单。
3. 幂等：重复执行 `initNodePackageReleaseOrders`，同一个 `node_order_id` 不重复插入。
4. 每日释放：第1天释放 `daily_release_amount`。
5. 最后一天：释放剩余全部金额，状态变为释放完成。
6. 钱包：每日释放增加用户 AFI 钱包 `valid_num2`。
7. 流水：钱包流水和奖励记录都能追溯到释放订单号。
8. 后台：菜单出现在节点管理下，支持按用户ID、钱包地址、节点等级、状态、来源订单号查询并导出。
