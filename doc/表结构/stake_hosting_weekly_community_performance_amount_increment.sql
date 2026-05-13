-- 托管每周新增小区业绩金额字段补充脚本
-- 适用场景：已执行过 stake_hosting_weekly_community_performance_points_increment.sql，
-- 现在只补充 USDT 原始业绩金额口径字段，不重复添加积分字段。

ALTER TABLE `t_stake_hosting_weekly_community_performance`
    ADD COLUMN `self_increase_amount` decimal(20,6) NOT NULL DEFAULT '0.000000' COMMENT '本周个人新增托管业绩，单位USDT' AFTER `community_new_performance`,
    ADD COLUMN `self_expire_amount` decimal(20,6) NOT NULL DEFAULT '0.000000' COMMENT '本周个人到期托管业绩，单位USDT' AFTER `self_increase_amount`,
    ADD COLUMN `team_increase_amount` decimal(20,6) NOT NULL DEFAULT '0.000000' COMMENT '本周团队新增托管业绩，单位USDT' AFTER `self_expire_amount`,
    ADD COLUMN `team_expire_amount` decimal(20,6) NOT NULL DEFAULT '0.000000' COMMENT '本周团队到期托管业绩，单位USDT' AFTER `team_increase_amount`,
    ADD COLUMN `self_net_amount` decimal(20,6) NOT NULL DEFAULT '0.000000' COMMENT '本周个人净新增托管业绩，单位USDT' AFTER `team_expire_amount`,
    ADD COLUMN `team_net_amount` decimal(20,6) NOT NULL DEFAULT '0.000000' COMMENT '本周团队净新增托管业绩，单位USDT' AFTER `self_net_amount`,
    ADD COLUMN `total_line_amount` decimal(20,6) NOT NULL DEFAULT '0.000000' COMMENT '本周末所有直推区有效托管业绩合计，单位USDT' AFTER `team_net_amount`,
    ADD COLUMN `max_line_amount` decimal(20,6) NOT NULL DEFAULT '0.000000' COMMENT '本周末最大直推区有效托管业绩，单位USDT' AFTER `total_line_amount`,
    ADD COLUMN `previous_community_amount` decimal(20,6) NOT NULL DEFAULT '0.000000' COMMENT '上周末小区有效托管业绩快照，单位USDT' AFTER `max_line_amount`,
    ADD COLUMN `current_community_amount` decimal(20,6) NOT NULL DEFAULT '0.000000' COMMENT '本周末小区有效托管业绩快照，单位USDT' AFTER `previous_community_amount`,
    ADD COLUMN `community_new_amount` decimal(20,6) NOT NULL DEFAULT '0.000000' COMMENT '本周小区净新增托管业绩，单位USDT' AFTER `current_community_amount`;
