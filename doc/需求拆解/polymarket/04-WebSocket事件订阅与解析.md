# Polymarket WebSocket事件订阅与解析

## 范围

- 本文说明 App 服务如何订阅 Polymarket Market Channel，以及收到事件后如何解析。
- 当前版本 WebSocket 只作为“市场已开奖”的实时触发器，不直接发奖、不写钱包。
- 真正发奖仍由统一结算入口 `processSettlingMarket(marketSlug)` 完成，并且会再次查询 Gamma API 复核最终结果。

## 订阅地址

```text
wss://ws-subscriptions-clob.polymarket.com/ws/market
```

官方文档：

```text
https://docs.polymarket.com/cn/market-data/websocket/market-channel
```

## asset_id来源

- 下单时，后端调用 Gamma API 查询市场详情。
- 市场详情中的 `clobTokenIds` 是该市场每个结果选项对应的 `asset_id/token_id`。
- `clobTokenIds` 与 `outcomes` 按下标对应：

```text
outcomes[0]      -> clobTokenIds[0]
outcomes[1]      -> clobTokenIds[1]
outcome_index    -> clobTokenIds[outcome_index]
```

- 下单后：
  - 订单表 `t_polymarket_order.asset_id` 保存用户选择的结果资产ID。
  - 市场表 `t_polymarket_market.asset_ids_json` 保存该市场全部结果资产ID数组。
  - 市场表 `t_polymarket_market.outcomes_json` 保存该市场全部结果名称数组。

## 订阅报文

启动时从 `t_polymarket_market` 查询：

```text
status = 0 待结算
deleted = 0
asset_ids_json 不为空
```

汇总所有 `asset_id` 后发送订阅：

```json
{
  "assets_ids": ["asset_id_1", "asset_id_2"],
  "type": "market",
  "custom_feature_enabled": true
}
```

说明：

- `assets_ids` 是 Polymarket Market Channel 订阅字段。
- `type=market` 表示订阅市场频道。
- `custom_feature_enabled=true` 用于接收扩展事件，例如 `market_resolved`。
- 如果当前没有待结算市场，则保持连接但不发送空订阅。

## 事件处理策略

Polymarket Market Channel 可能返回单个 JSON 对象，也可能返回 JSON 数组。处理时统一转成事件对象逐条解析。

当前只处理：

```text
event_type = market_resolved
```

其他事件，例如价格变化、订单簿变化、best bid/ask、last trade 等，只记录调试日志，不修改数据库。

## market_resolved解析

`market_resolved` 是当前结算系统关注的核心事件。主要字段：

```text
slug                市场slug
asset_ids           市场全部结果asset_id
outcomes            市场全部结果名称
winning_asset_id    赢家asset_id
winning_outcome     赢家名称
```

解析流程：

```text
收到 market_resolved
        ↓
优先读取 slug
        ↓
按 t_polymarket_market.market_slug 匹配待结算市场
        ↓
如果缺少 slug，则用 winning_asset_id 匹配 asset_ids_json
        ↓
匹配到 status=0 的市场后，抢占为 status=1 结算中
        ↓
保留延迟队列 TODO，后续由队列消费者调用 processSettlingMarket
```

## 状态边界

- WebSocket 只处理 `status=0待结算` 的市场。
- 如果市场已经是：
  - `1结算中`
  - `2结算完成`
  - `3待人工复核`

则重复事件不再处理。

## 为什么不能直接按WebSocket发奖

WebSocket 是实时消息通道，可能出现断线、重复消息、字段缺失或消息顺序问题。

所以 WebSocket 只做触发，不做最终资产结算。最终发奖时仍需要：

```text
processSettlingMarket(marketSlug)
        ↓
重新查询 Gamma API
        ↓
确认 umaResolutionStatus = resolved
        ↓
确认 outcomePrices 明确为 0/1
        ↓
批量结算订单和钱包
```

这样 WebSocket 快，Quartz 稳，数据库状态防重复，最终发奖入口只有一套。

## 当前未实现内容

- 暂不实现 Redisson 延迟队列消费者。
- 暂不真实投递延迟队列消息，只保留中文 TODO。
- 暂不订阅实时价格用于页面展示。
- 暂不把 WebSocket 原始事件落库。

## 测试点

- 启动 App 后不再连接数海地址，不再发送 `login` 或 `market=WI,WX,WA`。
- 有待结算市场时，会发送 Polymarket `assets_ids/type/custom_feature_enabled` 订阅。
- 收到 `market_resolved` 且 `slug` 匹配时，市场从 `0待结算` 改为 `1结算中`。
- 收到重复 `market_resolved` 时，市场不会重复派发。
- 收到非结算事件时，不修改市场、不发奖。
