CREATE TABLE IF NOT EXISTS `t_stake_hosting_user_reward_summary` (
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `diff_reward_amount` decimal(20,6) NOT NULL DEFAULT '0.000000' COMMENT '托管极差奖累计',
  `same_level_reward_amount` decimal(20,6) NOT NULL DEFAULT '0.000000' COMMENT '托管平级奖累计',
  `global_dividend_amount` decimal(20,6) NOT NULL DEFAULT '0.000000' COMMENT '托管全球分红累计，可后续使用',
  PRIMARY KEY (`user_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci ROW_FORMAT=DYNAMIC COMMENT='托管用户奖励累计汇总表';

INSERT IGNORE INTO `t_stake_hosting_user_reward_summary`
  (`user_id`, `diff_reward_amount`, `same_level_reward_amount`, `global_dividend_amount`)
SELECT
  `user_id`, 0.000000, 0.000000, 0.000000
FROM `t_user_info`
WHERE `deleted` = 0;
