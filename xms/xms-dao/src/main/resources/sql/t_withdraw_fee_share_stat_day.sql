CREATE TABLE `t_withdraw_fee_share_stat_day` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `stat_date` INT NOT NULL COMMENT '统计日期 yyyymmdd',
  `total_fee` DECIMAL(20,6) NOT NULL DEFAULT '0.000000' COMMENT '当日提现手续费总额',
  `distributed_fee` DECIMAL(20,6) NOT NULL DEFAULT '0.000000' COMMENT '实际分红总额',
  `user_count` INT NOT NULL DEFAULT 0 COMMENT '符合条件的V9用户数',
  `per_user_amount` DECIMAL(20,6) NOT NULL DEFAULT '0.000000' COMMENT '人均分得金额',
  `share_user_snapshot` TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci COMMENT '参与分红的V9用户快照 userId#account,用逗号分隔',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '0:待执行 1:已分红 2:未分红/失败',
  `fail_reason` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci DEFAULT NULL COMMENT '未分红原因',
  `remark` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_stat_date` (`stat_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci COMMENT='每日V9节点提现手续费分红汇总';

