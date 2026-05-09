-- 托管全球分红每周发放增量 SQL
-- 本脚本只新增批次表、明细表和相关字典，不修改历史建表 SQL。

CREATE TABLE IF NOT EXISTS `t_stake_hosting_global_dividend_batch` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `batch_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL COMMENT '批次号',
  `settlement_day` int NOT NULL COMMENT '结算日，格式yyyyMMdd',
  `period_start_time` datetime NULL DEFAULT NULL COMMENT '周期开始时间',
  `period_end_time` datetime NULL DEFAULT NULL COMMENT '周期结束时间',
  `plan_amount` decimal(20,6) NOT NULL DEFAULT 0.000000 COMMENT '计划分红金额',
  `actual_amount` decimal(20,6) NOT NULL DEFAULT 0.000000 COMMENT '实际分红金额',
  `user_count` int NOT NULL DEFAULT 0 COMMENT '参与人数',
  `status` int NOT NULL DEFAULT 0 COMMENT '状态 0:处理中 1:已完成 2:失败',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '创建者',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '更新者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '备注',
  `deleted` int NOT NULL DEFAULT 0 COMMENT '删除标志 0正常 1删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_batch_no` (`batch_no`) USING BTREE,
  KEY `idx_settlement_day` (`settlement_day`) USING BTREE,
  KEY `idx_status` (`status`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci COMMENT='托管全球分红批次表' ROW_FORMAT=DYNAMIC;

CREATE TABLE IF NOT EXISTS `t_stake_hosting_global_dividend_detail` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `batch_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL COMMENT '批次号',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `account` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '钱包地址',
  `reward_level` int NOT NULL COMMENT '奖励等级',
  `level_dividend_ratio` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '等级分红比例，单位%',
  `level_pool_amount` decimal(20,6) NOT NULL DEFAULT 0.000000 COMMENT '等级奖池金额',
  `user_community_performance` decimal(20,6) NOT NULL DEFAULT 0.000000 COMMENT '用户小区业绩',
  `level_community_performance` decimal(20,6) NOT NULL DEFAULT 0.000000 COMMENT '等级小区业绩总和',
  `reward_amount` decimal(20,6) NOT NULL DEFAULT 0.000000 COMMENT '分红金额',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '创建者',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '更新者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '备注',
  `deleted` int NOT NULL DEFAULT 0 COMMENT '删除标志 0正常 1删除',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_batch_no` (`batch_no`) USING BTREE,
  KEY `idx_user_id` (`user_id`) USING BTREE,
  KEY `idx_reward_level` (`reward_level`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci COMMENT='托管全球分红明细表' ROW_FORMAT=DYNAMIC;

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

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 31, '托管全球分红', '31', 'reward_record_source_type', '', 'success', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'reward_record_source_type' AND dict_value = '31');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 37, '托管全球分红', '37', 't_user_money_log_source_type', '', 'success', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 't_user_money_log_source_type' AND dict_value = '37');

-- ----------------------------
-- 菜单 SQL
-- ----------------------------
SET @stakeHostingRootParentId := 0;
SET @stakeHostingRootOrderNum := 94;

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT '托管管理', @stakeHostingRootParentId, @stakeHostingRootOrderNum, 'stakeHosting', 'Layout', 1, 0, 'M', '0', '0', '', 'tree', 'admin', sysdate(), '', null, '托管管理目录'
WHERE NOT EXISTS (
  SELECT 1 FROM sys_menu WHERE menu_name = '托管管理' AND parent_id = @stakeHostingRootParentId AND menu_type = 'M'
);

SELECT @stakeHostingRootId := menu_id FROM sys_menu WHERE menu_name = '托管管理' AND parent_id = @stakeHostingRootParentId AND menu_type = 'M' LIMIT 1;

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT '全球分红批次', @stakeHostingRootId, 8, 'stakeHostingGlobalDividendBatch', 'xms/stakeHostingGlobalDividendBatch/index', 1, 0, 'C', '0', '0', 'xms:stakeHostingGlobalDividendBatch:list', 'date', 'admin', sysdate(), '', null, '托管全球分红批次菜单'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @stakeHostingRootId AND perms = 'xms:stakeHostingGlobalDividendBatch:list');

SELECT @globalDividendBatchMenuId := menu_id FROM sys_menu WHERE parent_id = @stakeHostingRootId AND perms = 'xms:stakeHostingGlobalDividendBatch:list' LIMIT 1;

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT '全球分红批次导出', @globalDividendBatchMenuId, 1, '#', '', 1, 0, 'F', '0', '0', 'xms:stakeHostingGlobalDividendBatch:export', '#', 'admin', sysdate(), '', null, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @globalDividendBatchMenuId AND perms = 'xms:stakeHostingGlobalDividendBatch:export');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT '全球分红明细', @stakeHostingRootId, 9, 'stakeHostingGlobalDividendDetail', 'xms/stakeHostingGlobalDividendDetail/index', 1, 0, 'C', '0', '0', 'xms:stakeHostingGlobalDividendDetail:list', 'list', 'admin', sysdate(), '', null, '托管全球分红明细菜单'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @stakeHostingRootId AND perms = 'xms:stakeHostingGlobalDividendDetail:list');

SELECT @globalDividendDetailMenuId := menu_id FROM sys_menu WHERE parent_id = @stakeHostingRootId AND perms = 'xms:stakeHostingGlobalDividendDetail:list' LIMIT 1;

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT '全球分红明细导出', @globalDividendDetailMenuId, 1, '#', '', 1, 0, 'F', '0', '0', 'xms:stakeHostingGlobalDividendDetail:export', '#', 'admin', sysdate(), '', null, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @globalDividendDetailMenuId AND perms = 'xms:stakeHostingGlobalDividendDetail:export');
