-- OpenAI聊天用户扣费状态增量脚本
-- 说明：只做增量字段新增，不修改历史建表SQL。

ALTER TABLE `t_user_info`
	ADD COLUMN `open_ai_paid_status` tinyint NOT NULL DEFAULT 0 COMMENT 'OpenAI聊天扣费状态 0未扣费 1已扣费' AFTER `stake_hosting_static_rate`;
