-- 托管每周新增小区业绩积分字段增量脚本
-- 本脚本只调整周业绩表字段口径，不做历史数据迁移；测试环境可先清空相关周业绩数据后执行。

ALTER TABLE `t_stake_hosting_weekly_community_performance`
    ADD COLUMN `self_increase_points` decimal(20,6) NOT NULL DEFAULT '0.000000' COMMENT '本周个人新增积分' AFTER `week_end_time`,
    ADD COLUMN `self_expire_points` decimal(20,6) NOT NULL DEFAULT '0.000000' COMMENT '本周个人到期积分' AFTER `self_increase_points`,
    ADD COLUMN `team_increase_points` decimal(20,6) NOT NULL DEFAULT '0.000000' COMMENT '本周团队新增积分' AFTER `self_expire_points`,
    ADD COLUMN `team_expire_points` decimal(20,6) NOT NULL DEFAULT '0.000000' COMMENT '本周团队到期积分' AFTER `team_increase_points`,
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

ALTER TABLE `t_stake_hosting_weekly_community_performance`
    MODIFY COLUMN `self_new_performance` decimal(20,6) NOT NULL DEFAULT '0.000000' COMMENT '本周个人净新增积分，个人新增积分-个人到期积分',
    MODIFY COLUMN `team_new_performance` decimal(20,6) NOT NULL DEFAULT '0.000000' COMMENT '本周团队净新增积分，团队新增积分-团队到期积分',
    MODIFY COLUMN `total_line_performance` decimal(20,6) NOT NULL DEFAULT '0.000000' COMMENT '本周末所有直推区有效积分合计',
    MODIFY COLUMN `max_line_performance` decimal(20,6) NOT NULL DEFAULT '0.000000' COMMENT '本周末最大直推区有效积分',
    MODIFY COLUMN `previous_community_performance` decimal(20,6) NOT NULL DEFAULT '0.000000' COMMENT '上周末小区有效积分快照',
    MODIFY COLUMN `current_community_performance` decimal(20,6) NOT NULL DEFAULT '0.000000' COMMENT '本周末小区有效积分快照',
    MODIFY COLUMN `community_new_performance` decimal(20,6) NOT NULL DEFAULT '0.000000' COMMENT '本周小区净新增积分/全球分红权重';
