-- 托管每周新增小区业绩 V2 增量脚本
-- 只做增量变更，不修改历史建表 SQL。
-- 口径：订单金额按套餐积分系数换算为托管积分；周小区业绩按小区有效积分快照差额计算，允许负数。

ALTER TABLE `t_stake_hosting_package`
    ADD COLUMN `performance_coefficient` decimal(10,4) NOT NULL DEFAULT '1.0000' COMMENT '业绩积分系数，用于计算新增小区业绩积分' AFTER `service_fee_ratio`;

UPDATE `t_stake_hosting_package`
SET `performance_coefficient` = CASE `days`
    WHEN 30 THEN 1.0000
    WHEN 90 THEN 3.0000
    WHEN 180 THEN 6.0000
    WHEN 360 THEN 12.0000
    ELSE 1.0000
END
WHERE `deleted` = 0;

ALTER TABLE `t_stake_hosting_order`
    ADD COLUMN `performance_coefficient` decimal(10,4) NOT NULL DEFAULT '1.0000' COMMENT '业绩积分系数快照' AFTER `stake_usdt_amount`,
    ADD COLUMN `performance_points` decimal(20,6) NOT NULL DEFAULT '0.000000' COMMENT '业绩积分快照，托管金额*业绩积分系数' AFTER `performance_coefficient`,
    ADD KEY `idx_performance_end_time` (`performance_end_time`) USING BTREE;

UPDATE `t_stake_hosting_order` o
LEFT JOIN `t_stake_hosting_package` p ON p.id = o.package_id
SET o.performance_coefficient = IFNULL(p.performance_coefficient, 1.0000),
    o.performance_points = IFNULL(o.stake_usdt_amount, 0) * IFNULL(p.performance_coefficient, 1.0000)
WHERE o.deleted = 0
  AND (o.performance_points IS NULL OR o.performance_points = 0);

ALTER TABLE `t_stake_hosting_weekly_community_performance`
    ADD COLUMN `previous_community_performance` decimal(20,6) NOT NULL DEFAULT '0.000000' COMMENT '上周末小区有效积分快照' AFTER `max_line_performance`,
    ADD COLUMN `current_community_performance` decimal(20,6) NOT NULL DEFAULT '0.000000' COMMENT '本周末小区有效积分快照' AFTER `previous_community_performance`;

