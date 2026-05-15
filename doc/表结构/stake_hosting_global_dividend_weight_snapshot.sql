-- 托管全球分红权重快照与旧周业绩清理
-- 执行前可按实际环境调整 @stakeHostingRootParentId / @stakeHostingRootOrderNum。

CREATE TABLE IF NOT EXISTS `t_stake_hosting_global_dividend_weight_snapshot` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `account` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci DEFAULT NULL COMMENT '钱包地址快照',
  `week_start_time` bigint NOT NULL COMMENT '周开始时间，格式yyyyMMddHHmmss',
  `week_end_time` bigint NOT NULL COMMENT '周结束时间，格式yyyyMMddHHmmss',
  `self_weight` decimal(20,6) NOT NULL DEFAULT '0.000000' COMMENT '本期个人全球分红权重',
  `umbrella_weight` decimal(20,6) NOT NULL DEFAULT '0.000000' COMMENT '本期团队全球分红权重',
  `community_weight` decimal(20,6) NOT NULL DEFAULT '0.000000' COMMENT '本期小区全球分红权重',
  `previous_community_weight` decimal(20,6) NOT NULL DEFAULT '0.000000' COMMENT '上一期小区全球分红权重',
  `dividend_weight` decimal(20,6) NOT NULL DEFAULT '0.000000' COMMENT '本期分红权重=max(本期小区权重-上一期小区权重,0)',
  `settle_status` int NOT NULL DEFAULT '0' COMMENT '状态 0:未参与分红 1:已参与分红',
  `batch_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci DEFAULT NULL COMMENT '全球分红批次号',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci DEFAULT NULL COMMENT '备注',
  `deleted` int NOT NULL DEFAULT '0' COMMENT '删除标识 0正常 1删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_user_week` (`user_id`,`week_start_time`) USING BTREE,
  KEY `idx_week` (`week_start_time`,`week_end_time`) USING BTREE,
  KEY `idx_dividend_weight` (`dividend_weight`) USING BTREE,
  KEY `idx_settle_status` (`settle_status`) USING BTREE,
  KEY `idx_batch_no` (`batch_no`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci ROW_FORMAT=DYNAMIC COMMENT='托管全球分红权重快照表';

INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '托管全球分红权重快照状态', 't_stake_hosting_global_dividend_weight_snapshot_settle_status', '0', 'admin', sysdate(), '托管全球分红权重快照状态'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 't_stake_hosting_global_dividend_weight_snapshot_settle_status');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 0, '未参与分红', '0', 't_stake_hosting_global_dividend_weight_snapshot_settle_status', '', 'info', 'Y', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 't_stake_hosting_global_dividend_weight_snapshot_settle_status' AND dict_value = '0');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1, '已参与分红', '1', 't_stake_hosting_global_dividend_weight_snapshot_settle_status', '', 'success', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 't_stake_hosting_global_dividend_weight_snapshot_settle_status' AND dict_value = '1');

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

-- 删除旧“每周新增小区业绩”菜单和字典。
DELETE FROM sys_menu WHERE perms LIKE 'xms:stakeHostingWeeklyCommunityPerformance:%';
DELETE FROM sys_menu WHERE perms = 'xms:stakeHostingWeeklyCommunityPerformance:list';
DELETE FROM sys_dict_data WHERE dict_type = 't_stake_hosting_weekly_community_performance_settle_status';
DELETE FROM sys_dict_type WHERE dict_type = 't_stake_hosting_weekly_community_performance_settle_status';

-- 删除旧周业绩表和订单旧周业绩状态字段。
DROP TABLE IF EXISTS `t_stake_hosting_weekly_community_performance`;

ALTER TABLE `t_stake_hosting_order`
  DROP COLUMN `performance_start_time`,
  DROP COLUMN `performance_end_time`,
  DROP COLUMN `weekly_performance_status`,
  DROP COLUMN `weekly_performance_skip_reason`,
  DROP COLUMN `weekly_performance_time`,
  DROP COLUMN `weekly_expire_performance_status`,
  DROP COLUMN `weekly_expire_performance_skip_reason`,
  DROP COLUMN `weekly_expire_performance_time`;
