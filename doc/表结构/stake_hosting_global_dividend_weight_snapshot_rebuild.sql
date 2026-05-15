-- 托管全球分红权重快照重做脚本
-- 开发阶段使用：旧周新增业绩、旧全球分红批次、旧全球分红明细数据直接删除，不做迁移。

DROP TABLE IF EXISTS `t_stake_hosting_weekly_community_performance`;
DROP TABLE IF EXISTS `t_stake_hosting_global_dividend_detail`;
DROP TABLE IF EXISTS `t_stake_hosting_global_dividend_batch`;

ALTER TABLE `t_stake_hosting_order`
  DROP COLUMN `weekly_performance_status`,
  DROP COLUMN `weekly_performance_skip_reason`,
  DROP COLUMN `weekly_performance_time`,
  DROP COLUMN `weekly_expire_performance_status`,
  DROP COLUMN `weekly_expire_performance_skip_reason`,
  DROP COLUMN `weekly_expire_performance_time`;

CREATE TABLE IF NOT EXISTS `t_stake_hosting_global_dividend_weight_snapshot` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `account` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci DEFAULT NULL COMMENT '钱包地址快照',
  `week_start_time` bigint NOT NULL COMMENT '周开始时间，格式yyyyMMddHHmmss',
  `week_end_time` bigint NOT NULL COMMENT '周结束时间，格式yyyyMMddHHmmss',
  `total_line_weight` decimal(20,6) NOT NULL DEFAULT '0.000000' COMMENT '结算时刻所有直推区有效权重合计',
  `max_line_weight` decimal(20,6) NOT NULL DEFAULT '0.000000' COMMENT '结算时刻最大直推区有效权重',
  `community_weight` decimal(20,6) NOT NULL DEFAULT '0.000000' COMMENT '结算时刻小区有效权重=直推区合计权重-最大区权重',
  `previous_community_weight` decimal(20,6) NOT NULL DEFAULT '0.000000' COMMENT '上一期结算小区有效权重',
  `dividend_weight` decimal(20,6) NOT NULL DEFAULT '0.000000' COMMENT '本期参与分红权重=max(小区权重-上一期小区权重,0)',
  `settle_status` int NOT NULL DEFAULT '0' COMMENT '状态 0:已快照 1:已参与分红',
  `batch_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci DEFAULT NULL COMMENT '全球分红批次号',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci DEFAULT NULL COMMENT '备注',
  `deleted` int NOT NULL DEFAULT '0' COMMENT '删除标志 0正常 1删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_user_week` (`user_id`,`week_start_time`) USING BTREE,
  KEY `idx_week` (`week_start_time`,`week_end_time`) USING BTREE,
  KEY `idx_dividend_weight` (`dividend_weight`) USING BTREE,
  KEY `idx_settle_status` (`settle_status`) USING BTREE,
  KEY `idx_batch_no` (`batch_no`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci ROW_FORMAT=DYNAMIC COMMENT='托管全球分红权重快照表';

CREATE TABLE IF NOT EXISTS `t_stake_hosting_global_dividend_batch` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `batch_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL COMMENT '批次号',
  `settlement_day` int NOT NULL COMMENT '结算日，格式yyyyMMdd',
  `period_start_time` datetime NOT NULL COMMENT '分红周期开始时间',
  `period_end_time` datetime NOT NULL COMMENT '分红周期结束时间',
  `plan_amount` decimal(20,6) NOT NULL DEFAULT '0.000000' COMMENT '计划分红金额',
  `actual_amount` decimal(20,6) NOT NULL DEFAULT '0.000000' COMMENT '实际分红金额',
  `user_count` int NOT NULL DEFAULT '0' COMMENT '参与人数',
  `status` int NOT NULL DEFAULT '0' COMMENT '状态 0:处理中 1:已完成 2:失败',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci DEFAULT NULL COMMENT '备注',
  `deleted` int NOT NULL DEFAULT '0' COMMENT '删除标志 0正常 1删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_batch_no` (`batch_no`) USING BTREE,
  KEY `idx_settlement_day` (`settlement_day`) USING BTREE,
  KEY `idx_status` (`status`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci ROW_FORMAT=DYNAMIC COMMENT='托管全球分红批次表';

CREATE TABLE IF NOT EXISTS `t_stake_hosting_global_dividend_detail` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `batch_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL COMMENT '批次号',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `account` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci DEFAULT NULL COMMENT '钱包地址快照',
  `reward_level` int NOT NULL COMMENT '奖励等级',
  `level_dividend_ratio` decimal(10,4) NOT NULL DEFAULT '0.0000' COMMENT '等级分红比例，单位%',
  `level_pool_amount` decimal(20,6) NOT NULL DEFAULT '0.000000' COMMENT '等级奖池金额，单位USDT',
  `previous_community_weight` decimal(20,6) NOT NULL DEFAULT '0.000000' COMMENT '上期小区有效权重',
  `community_weight` decimal(20,6) NOT NULL DEFAULT '0.000000' COMMENT '本期小区有效权重',
  `dividend_weight` decimal(20,6) NOT NULL DEFAULT '0.000000' COMMENT '本期用户分红权重',
  `level_dividend_weight` decimal(20,6) NOT NULL DEFAULT '0.000000' COMMENT '同等级分红权重合计',
  `reward_amount` decimal(20,6) NOT NULL DEFAULT '0.000000' COMMENT '分红金额，单位USDT',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci DEFAULT NULL COMMENT '备注',
  `deleted` int NOT NULL DEFAULT '0' COMMENT '删除标志 0正常 1删除',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_batch_no` (`batch_no`) USING BTREE,
  KEY `idx_user_id` (`user_id`) USING BTREE,
  KEY `idx_reward_level` (`reward_level`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci ROW_FORMAT=DYNAMIC COMMENT='托管全球分红明细表';

INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '托管全球分红批次状态', 't_stake_hosting_global_dividend_batch_status', '0', 'admin', sysdate(), '托管全球分红批次状态'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 't_stake_hosting_global_dividend_batch_status');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 0, '处理中', '0', 't_stake_hosting_global_dividend_batch_status', '', 'info', 'Y', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 't_stake_hosting_global_dividend_batch_status' AND dict_value = '0');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1, '已完成', '1', 't_stake_hosting_global_dividend_batch_status', '', 'success', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 't_stake_hosting_global_dividend_batch_status' AND dict_value = '1');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 2, '失败', '2', 't_stake_hosting_global_dividend_batch_status', '', 'danger', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 't_stake_hosting_global_dividend_batch_status' AND dict_value = '2');

INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '托管全球分红权重快照状态', 't_stake_hosting_global_dividend_weight_snapshot_settle_status', '0', 'admin', sysdate(), '托管全球分红权重快照状态'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 't_stake_hosting_global_dividend_weight_snapshot_settle_status');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 0, '已快照', '0', 't_stake_hosting_global_dividend_weight_snapshot_settle_status', '', 'info', 'Y', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 't_stake_hosting_global_dividend_weight_snapshot_settle_status' AND dict_value = '0');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1, '已参与分红', '1', 't_stake_hosting_global_dividend_weight_snapshot_settle_status', '', 'success', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 't_stake_hosting_global_dividend_weight_snapshot_settle_status' AND dict_value = '1');

DELETE FROM sys_menu WHERE perms LIKE 'xms:stakeHostingWeeklyCommunityPerformance:%' OR perms = 'xms:stakeHostingWeeklyCommunityPerformance:list';

SET @stakeHostingRootParentId := 0;
SET @stakeHostingRootOrderNum := 94;

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT '托管管理', @stakeHostingRootParentId, @stakeHostingRootOrderNum, 'stakeHosting', 'Layout', 1, 0, 'M', '0', '0', '', 'tree', 'admin', sysdate(), '', null, '托管管理目录'
WHERE NOT EXISTS (
  SELECT 1 FROM sys_menu WHERE menu_name = '托管管理' AND parent_id = @stakeHostingRootParentId AND menu_type = 'M'
);

SELECT @stakeHostingRootId := menu_id FROM sys_menu WHERE menu_name = '托管管理' AND parent_id = @stakeHostingRootParentId AND menu_type = 'M' LIMIT 1;

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT '全球分红权重快照', @stakeHostingRootId, 10, 'stakeHostingGlobalDividendWeightSnapshot', 'xms/stakeHostingGlobalDividendWeightSnapshot/index', 1, 0, 'C', '0', '0', 'xms:stakeHostingGlobalDividendWeightSnapshot:list', 'chart', 'admin', sysdate(), '', null, '托管全球分红权重快照菜单'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @stakeHostingRootId AND perms = 'xms:stakeHostingGlobalDividendWeightSnapshot:list');

SELECT @weightSnapshotMenuId := menu_id FROM sys_menu WHERE parent_id = @stakeHostingRootId AND perms = 'xms:stakeHostingGlobalDividendWeightSnapshot:list' LIMIT 1;

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT '全球分红权重快照查询', @weightSnapshotMenuId, 1, '#', '', 1, 0, 'F', '0', '0', 'xms:stakeHostingGlobalDividendWeightSnapshot:query', '#', 'admin', sysdate(), '', null, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @weightSnapshotMenuId AND perms = 'xms:stakeHostingGlobalDividendWeightSnapshot:query');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT '全球分红权重快照导出', @weightSnapshotMenuId, 2, '#', '', 1, 0, 'F', '0', '0', 'xms:stakeHostingGlobalDividendWeightSnapshot:export', '#', 'admin', sysdate(), '', null, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @weightSnapshotMenuId AND perms = 'xms:stakeHostingGlobalDividendWeightSnapshot:export');
