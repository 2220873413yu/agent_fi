-- 托管全球分红奖池与奖池流水增量 SQL
-- 本脚本只新增表、初始化数据、字典和菜单，不修改历史建表 SQL。
-- 执行前如需调整托管管理菜单位置，可修改 @stakeHostingRootParentId 和 @stakeHostingRootOrderNum。

CREATE TABLE IF NOT EXISTS `t_stake_hosting_global_dividend_pool` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `pool_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL COMMENT '奖池编码',
  `balance_amount` decimal(20,6) NOT NULL DEFAULT 0.000000 COMMENT '当前奖池余额',
  `total_income_amount` decimal(20,6) NOT NULL DEFAULT 0.000000 COMMENT '累计收入金额',
  `total_expense_amount` decimal(20,6) NOT NULL DEFAULT 0.000000 COMMENT '累计支出金额',
  `last_income_time` datetime NULL DEFAULT NULL COMMENT '最近收入时间',
  `last_expense_time` datetime NULL DEFAULT NULL COMMENT '最近支出时间',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '创建者',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '更新者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '备注',
  `deleted` int NOT NULL DEFAULT 0 COMMENT '删除标志 0正常 1删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_pool_code` (`pool_code`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci COMMENT='托管全球分红奖池表' ROW_FORMAT=DYNAMIC;

CREATE TABLE IF NOT EXISTS `t_stake_hosting_global_dividend_pool_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `log_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL COMMENT '流水单号',
  `pool_id` bigint NOT NULL COMMENT '奖池ID',
  `pool_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL COMMENT '奖池编码',
  `flow_type` int NOT NULL COMMENT '流水类型 1:收入 2:支出',
  `biz_type` int NOT NULL COMMENT '业务类型 1:每日服务费入池 2:后台手动增加 3:每周全球分红扣减 4:后台手动扣减',
  `change_amount` decimal(20,6) NOT NULL DEFAULT 0.000000 COMMENT '变动金额',
  `before_amount` decimal(20,6) NOT NULL DEFAULT 0.000000 COMMENT '变动前余额',
  `after_amount` decimal(20,6) NOT NULL DEFAULT 0.000000 COMMENT '变动后余额',
  `source_order_id` bigint NULL DEFAULT NULL COMMENT '来源托管订单ID',
  `source_order_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '来源托管订单号',
  `source_user_id` bigint NULL DEFAULT NULL COMMENT '来源用户ID',
  `source_settlement_day` int NULL DEFAULT NULL COMMENT '来源结算日，格式yyyyMMdd',
  `source_batch_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '来源批次号',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '创建者',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '更新者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '备注',
  `deleted` int NOT NULL DEFAULT 0 COMMENT '删除标志 0正常 1删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_log_no` (`log_no`) USING BTREE,
  KEY `idx_pool_id` (`pool_id`) USING BTREE,
  KEY `idx_flow_type` (`flow_type`) USING BTREE,
  KEY `idx_biz_type` (`biz_type`) USING BTREE,
  KEY `idx_source_order_no` (`source_order_no`) USING BTREE,
  KEY `idx_source_batch_no` (`source_batch_no`) USING BTREE,
  KEY `idx_source_settlement_day` (`source_settlement_day`) USING BTREE,
  KEY `idx_create_time` (`create_time`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci COMMENT='托管全球分红奖池流水表' ROW_FORMAT=DYNAMIC;

INSERT INTO `t_stake_hosting_global_dividend_pool` (`pool_code`, `balance_amount`, `total_income_amount`, `total_expense_amount`, `create_by`, `create_time`, `remark`, `deleted`)
SELECT 'STAKE_HOSTING_GLOBAL_DIVIDEND', 0.000000, 0.000000, 0.000000, 'admin', sysdate(), '托管全球分红奖池', 0
WHERE NOT EXISTS (
  SELECT 1 FROM `t_stake_hosting_global_dividend_pool` WHERE `pool_code` = 'STAKE_HOSTING_GLOBAL_DIVIDEND' AND `deleted` = 0
);

-- ----------------------------
-- 字典 SQL
-- ----------------------------
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '托管全球分红奖池流水类型', 't_stake_hosting_global_dividend_pool_log_flow_type', '0', 'admin', sysdate(), '托管全球分红奖池流水收入/支出类型'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 't_stake_hosting_global_dividend_pool_log_flow_type');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1, '收入', '1', 't_stake_hosting_global_dividend_pool_log_flow_type', '', 'success', 'Y', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 't_stake_hosting_global_dividend_pool_log_flow_type' AND dict_value = '1');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 2, '支出', '2', 't_stake_hosting_global_dividend_pool_log_flow_type', '', 'danger', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 't_stake_hosting_global_dividend_pool_log_flow_type' AND dict_value = '2');

INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '托管全球分红奖池业务类型', 't_stake_hosting_global_dividend_pool_log_biz_type', '0', 'admin', sysdate(), '托管全球分红奖池流水业务类型'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 't_stake_hosting_global_dividend_pool_log_biz_type');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1, '每日服务费入池', '1', 't_stake_hosting_global_dividend_pool_log_biz_type', '', 'success', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 't_stake_hosting_global_dividend_pool_log_biz_type' AND dict_value = '1');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 2, '后台手动增加', '2', 't_stake_hosting_global_dividend_pool_log_biz_type', '', 'primary', 'Y', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 't_stake_hosting_global_dividend_pool_log_biz_type' AND dict_value = '2');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 3, '每周全球分红扣减', '3', 't_stake_hosting_global_dividend_pool_log_biz_type', '', 'warning', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 't_stake_hosting_global_dividend_pool_log_biz_type' AND dict_value = '3');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 4, '后台手动扣减', '4', 't_stake_hosting_global_dividend_pool_log_biz_type', '', 'danger', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 't_stake_hosting_global_dividend_pool_log_biz_type' AND dict_value = '4');

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
SELECT '全球分红奖池', @stakeHostingRootId, 6, 'stakeHostingGlobalDividendPool', 'xms/stakeHostingGlobalDividendPool/index', 1, 0, 'C', '0', '0', 'xms:stakeHostingGlobalDividendPool:list', 'money', 'admin', sysdate(), '', null, '托管全球分红奖池菜单'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @stakeHostingRootId AND perms = 'xms:stakeHostingGlobalDividendPool:list');

SELECT @globalDividendPoolMenuId := menu_id FROM sys_menu WHERE parent_id = @stakeHostingRootId AND perms = 'xms:stakeHostingGlobalDividendPool:list' LIMIT 1;

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT '全球分红奖池调账', @globalDividendPoolMenuId, 1, '#', '', 1, 0, 'F', '0', '0', 'xms:stakeHostingGlobalDividendPool:adjust', '#', 'admin', sysdate(), '', null, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @globalDividendPoolMenuId AND perms = 'xms:stakeHostingGlobalDividendPool:adjust');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT '奖池流水', @stakeHostingRootId, 7, 'stakeHostingGlobalDividendPoolLog', 'xms/stakeHostingGlobalDividendPoolLog/index', 1, 0, 'C', '0', '0', 'xms:stakeHostingGlobalDividendPoolLog:list', 'log', 'admin', sysdate(), '', null, '托管全球分红奖池流水菜单'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @stakeHostingRootId AND perms = 'xms:stakeHostingGlobalDividendPoolLog:list');

SELECT @globalDividendPoolLogMenuId := menu_id FROM sys_menu WHERE parent_id = @stakeHostingRootId AND perms = 'xms:stakeHostingGlobalDividendPoolLog:list' LIMIT 1;

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT '奖池流水导出', @globalDividendPoolLogMenuId, 1, '#', '', 1, 0, 'F', '0', '0', 'xms:stakeHostingGlobalDividendPoolLog:export', '#', 'admin', sysdate(), '', null, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @globalDividendPoolLogMenuId AND perms = 'xms:stakeHostingGlobalDividendPoolLog:export');
