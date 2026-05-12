# OpenAI聊天机器人

## 状态

已完成。

## 实现方式

聊天机器人使用 OpenAI API 实现，当前后端入口为：

- `xms-agent-app/src/main/java/com/xms/app/controller/chat/OpenAiChatController.java`

项目通过 Hutool AI 的 `OpenaiService` 调用 OpenAI，支持普通对话、图片理解和 SSE 流式输出。

## 接口

### 开通 AI 访问凭证

`POST /open/ai/openGptAction`

说明：

- 用户进入聊天前先调用该接口开通 AI 访问凭证。
- 当前实现会通过业务服务写入用户 AI 访问标识。
- 用户首次开通时扣减一次 AFI，扣费状态记录在 `t_user_info.open_ai_paid_status`。
- 已扣费用户后续再次调用该接口，只刷新 Redis 访问凭证，不再扣 AFI。

### 关闭 AI 访问凭证

`GET /open/ai/closeGptAction`

说明：

- 当前关闭接口已注释；如后续恢复，只删除当前登录用户的 AI 访问标识，不影响已扣费状态。

### 文本聊天

`POST /open/ai/chat`

请求体为 Hutool `Message` 列表，由前端传入本轮需要携带的上下文。

示例：

```json
[
  {
    "role": "system",
    "content": "你是一个助手"
  },
  {
    "role": "user",
    "content": "你好"
  }
]
```

### 文本聊天 SSE

`POST /open/ai/chat/sse`

说明：

- 请求体同文本聊天。
- 使用 SSE 流式返回模型输出。

### 图片理解

`POST /open/ai/chat/image`

请求示例：

```json
{
  "prompt": "帮我分析这张图片",
  "images": [
    "https://example.com/image.png"
  ]
}
```

说明：

- `images` 为图片 URL 列表。
- 图片 URL 需要 OpenAI 服务可访问。

### 图片理解 SSE

`POST /open/ai/chat/sse/image`

说明：

- 请求体同图片理解。
- 使用 SSE 流式返回模型输出。

## 权限与访问控制

聊天接口调用前会校验当前登录用户是否已开通 AI 访问凭证。

校验逻辑：

- Redis key：`RedisConstant.DbConstant.USER_AI_AGENT + 当前登录用户ID`
- 未开通时抛出 `ResponseCode.CODE_1075`

扣费逻辑：

- `openGptAction` 使用 `t_user_info.open_ai_paid_status` 做一次性扣费标识。
- 首次开通时原子更新 `open_ai_paid_status=1`，再扣减用户 AFI 钱包 `valid_num2`。
- 如果扣费失败，事务回滚，`open_ai_paid_status` 恢复为 `0`。
- 已扣费用户再次开通只刷新 Redis 凭证。

## 历史消息口径

当前版本不再使用后端 `t_chat_session`、`t_chat_message` 保存聊天历史。

历史上下文由前端按需传入 `List<Message>`，后端只负责转发当前请求内容到 OpenAI。

已删除旧方案：

- `t_chat_session`
- `t_chat_message`
- 旧 `/api/chat` 调试接口
- 旧聊天消息 DAO/Service/Mapper

如数据库已执行过旧聊天表 SQL，可执行：

- `doc/表结构/drop_chat_app.sql`

## 配置

OpenAI 配置位于 App 服务配置中：

```yaml
openai:
  api-key: ${OPENAI_API_KEY:}
  base-url: https://api.openai.com/v1
  model: gpt-4o-mini
```

本地调试可通过环境变量 `OPENAI_API_KEY` 配置 key。生产环境不建议将真实 key 写入 Git 仓库。

## 说明

- 当前聊天能力由 OpenAI API 提供。
- 每个用户只在首次开通聊天时扣除一次 AFI 费用。
- 后端不再保存大量历史消息，避免额外聊天表和数据清理成本。
- 前端如果需要上下文连续对话，应自行控制携带最近若干条消息。
