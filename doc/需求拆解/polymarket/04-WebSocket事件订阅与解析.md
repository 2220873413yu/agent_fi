# Polymarket WebSocket事件订阅与解析

## 当前状态

- 当前阶段暂时关闭 WebSocket 作为 Polymarket 结算触发源。
- 结算主路径改为 Quartz 定时扫描到期市场，再由统一结算入口 `processSettlingMarket(marketSlug)` 请求 Gamma API 复核并发奖。
- 本文保留为后续重新启用 WebSocket 的技术备忘，不代表当前生产/测试环境必须启动订阅。

## 暂停原因

Polymarket Market Channel 按 `asset_id/token_id` 订阅，不是按 `market_slug` 订阅。订阅后并不会只推送 `market_resolved`，还会推送盘口和价格类事件：

```text
book
price_change
best_bid_ask
last_trade_price
tick_size_change
new_market
market_resolved
```

其中 `book` 事件包含 `bids/asks` 订单簿快照。订阅市场较多时，一个二选一市场通常有 2 个 asset，多个市场会带来大量 `book` 和价格变化推送，可能导致：

- WebSocket 流量持续偏大。
- 初始盘口快照消息过大。
- Netty WebSocket frame 超限，例如超过默认 64KB。
- 业务只关心开奖，但仍需要接收并解码大量无关行情消息。

因此当前阶段不再依赖 WebSocket 结果推送，避免因行情流量影响结算稳定性。

## 当前结算主路径

```text
Quartz 定时任务
        ↓
扫描 t_polymarket_market
条件：status=0、deleted=0、end_time <= now
        ↓
抢占市场为 status=1 结算中
        ↓
投递 marketSlug 到统一结算队列/消费者
        ↓
processSettlingMarket(marketSlug)
        ↓
重新请求 Polymarket Gamma API
        ↓
复核 umaResolutionStatus / outcomePrices / winner
        ↓
批量更新订单、写钱包、更新市场状态
```

Quartz 和消费者职责保持分离：

- Quartz 只发现到期市场并抢占状态，不直接发奖。
- 消费者统一调用 `processSettlingMarket(marketSlug)`，不信任任何单一触发来源。
- 如果 Polymarket 到期后尚未开奖，消费者会按现有逻辑回待结算或进入复核兜底。

## WebSocket订阅备忘

如果后续重新启用 WebSocket，订阅地址仍为：

```text
wss://ws-subscriptions-clob.polymarket.com/ws/market
```

官方文档：

```text
https://docs.polymarket.com/cn/market-data/websocket/market-channel
```

订阅报文示例：

```json
{
  "assets_ids": ["asset_id_1", "asset_id_2"],
  "type": "market",
  "custom_feature_enabled": true
}
```

说明：

- `assets_ids` 是 Polymarket Market Channel 的订阅字段。
- `custom_feature_enabled=true` 可接收扩展事件，例如 `market_resolved`。
- 一个二选一市场通常对应 2 个 asset，订阅数量会随着市场数量成倍增长。

## 重新启用建议

如确实需要 WebSocket 提前触发开奖，建议只作为辅助路径：

- 只订阅临近到期市场，例如 `end_time` 接近当前时间的少量市场。
- 按市场或小批量拆分订阅 asset，避免一次订阅过多 token。
- `maxFramePayloadLength` 可以适当调大作为兜底，但不能作为长期承载大量 `book` 推送的主要方案。
- 业务只处理 `market_resolved`，忽略 `book/price_change/best_bid_ask/last_trade_price`。
- 收到 `market_resolved` 后也只抢占市场并投递 `marketSlug`，不能直接发奖。
- 最终仍必须由 `processSettlingMarket(marketSlug)` 重新请求 Gamma API 复核结果。

## asset_id关系

Polymarket Market Channel 的订阅对象是结果 token，也就是 `asset_id/token_id`。

```text
outcomes[0]      -> clobTokenIds[0]
outcomes[1]      -> clobTokenIds[1]
用户选择结果       -> t_polymarket_order.asset_id
市场全部结果token  -> t_polymarket_market.asset_ids_json
```

如果 WebSocket 返回 `market_resolved`：

```text
market_resolved.slug              -> t_polymarket_market.market_slug
market_resolved.winning_asset_id  -> t_polymarket_market.asset_ids_json
```

`winning_asset_id` 可作为缺少 slug 时的辅助定位字段，但最终赢家仍以 Gamma API 复核结果为准。

## 测试点

- 当前阶段启动 App 后，不应把 WebSocket 作为结算主路径。
- Quartz 到期扫描可派发 `end_time <= now` 且 `status=0` 的市场。
- WebSocket 如后续重新启用，非结算事件不得写库、不得发奖。
- 即使收到 `market_resolved`，也必须通过 `processSettlingMarket(marketSlug)` 复核后再结算。
