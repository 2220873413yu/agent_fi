-- 删除旧聊天会话/消息表增量SQL
-- 说明：当前聊天机器人已改用 OpenAiChatController，不再使用 t_chat_session / t_chat_message。
-- 执行前请确认旧聊天历史数据不再需要保留。

DROP TABLE IF EXISTS `t_chat_message`;
DROP TABLE IF EXISTS `t_chat_session`;
