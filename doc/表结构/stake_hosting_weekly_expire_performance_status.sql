-- 托管订单周新增/到期小区业绩处理状态拆分增量脚本
-- 说明：
-- 1. weekly_performance_* 继续表示订单生效时的周新增小区业绩处理状态。
-- 2. weekly_expire_performance_* 专门表示订单到期完成后的周小区业绩重算状态，方便排查到期扣减是否已触发重算。
-- 3. 状态值沿用周业绩处理状态：0未处理、1队列中、2处理中、3已处理、4处理失败。

ALTER TABLE `t_stake_hosting_order`
  ADD COLUMN `weekly_expire_performance_status` int NOT NULL DEFAULT '0' COMMENT '周到期小区业绩重算状态 0未处理 1队列中 2处理中 3已处理 4处理失败' AFTER `weekly_performance_time`,
  ADD COLUMN `weekly_expire_performance_skip_reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci DEFAULT NULL COMMENT '周到期小区业绩重算跳过/失败原因' AFTER `weekly_expire_performance_status`,
  ADD COLUMN `weekly_expire_performance_time` datetime DEFAULT NULL COMMENT '周到期小区业绩重算处理时间' AFTER `weekly_expire_performance_skip_reason`;

CREATE INDEX `idx_weekly_expire_performance_status`
  ON `t_stake_hosting_order` (`weekly_expire_performance_status`) USING BTREE;
