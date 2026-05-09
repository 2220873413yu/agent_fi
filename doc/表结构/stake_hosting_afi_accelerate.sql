-- AFI质押加速表结构、字典、菜单
-- 执行前如需调整托管管理菜单位置，可修改 @stakeHostingRootParentId 和 @stakeHostingRootOrderNum。

CREATE TABLE IF NOT EXISTS `t_stake_hosting_afi_accelerate_config` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `pledge_ratio` decimal(10,4) NOT NULL DEFAULT 0.0000 COMMENT 'AFI等值USDT/托管USDT比例，单位%',
  `accelerate_rate` decimal(10,4) NOT NULL DEFAULT 1.0000 COMMENT '加速倍率',
  `sort` int NOT NULL DEFAULT 0 COMMENT '排序',
  `status` int NOT NULL DEFAULT 1 COMMENT '状态 0:停用 1:启用',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '创建者',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '更新者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '备注',
  `deleted` int NOT NULL DEFAULT 0 COMMENT '删除标志 0正常 1删除',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_status` (`status`) USING BTREE,
  KEY `idx_pledge_ratio` (`pledge_ratio`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci COMMENT='AFI质押加速配置表' ROW_FORMAT=DYNAMIC;

CREATE TABLE IF NOT EXISTS `t_stake_hosting_afi_pledge` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `pledge_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL COMMENT '质押单号',
  `stake_hosting_order_id` bigint NOT NULL COMMENT '托管订单ID',
  `stake_hosting_order_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL COMMENT '托管订单号',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `account` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL COMMENT '钱包地址',
  `stake_usdt_amount` decimal(20,6) NOT NULL DEFAULT 0.000000 COMMENT '托管USDT金额快照',
  `afi_amount` decimal(20,6) NOT NULL DEFAULT 0.000000 COMMENT '质押AFI数量',
  `afi_price` decimal(20,6) NOT NULL DEFAULT 0.000000 COMMENT 'AFI价格快照',
  `afi_usdt_amount` decimal(20,6) NOT NULL DEFAULT 0.000000 COMMENT 'AFI等值USDT',
  `pledge_ratio` decimal(10,4) NOT NULL DEFAULT 0.0000 COMMENT '命中质押比例，单位%',
  `accelerate_rate` decimal(10,4) NOT NULL DEFAULT 1.0000 COMMENT '命中加速倍率',
  `pledge_time` datetime NOT NULL COMMENT '质押时间',
  `effective_day` int NOT NULL COMMENT '生效日期，格式yyyyMMdd',
  `return_time` datetime NULL DEFAULT NULL COMMENT '退还时间',
  `status` int NOT NULL DEFAULT 0 COMMENT '状态 0:未生效 1:生效中 2:已退还',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '创建者',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '更新者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '备注',
  `deleted` int NOT NULL DEFAULT 0 COMMENT '删除标志 0正常 1删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_pledge_no` (`pledge_no`) USING BTREE,
  UNIQUE KEY `uk_stake_hosting_order_id` (`stake_hosting_order_id`) USING BTREE,
  KEY `idx_user_id` (`user_id`) USING BTREE,
  KEY `idx_status` (`status`) USING BTREE,
  KEY `idx_effective_day` (`effective_day`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci COMMENT='托管订单AFI质押记录表' ROW_FORMAT=DYNAMIC;

SET @afiAcceleratedColumnExists := (
  SELECT COUNT(1)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 't_stake_hosting_order'
    AND column_name = 'afi_accelerated'
);
SET @afiAcceleratedSql := IF(@afiAcceleratedColumnExists = 0,
  'ALTER TABLE `t_stake_hosting_order` ADD COLUMN `afi_accelerated` int NOT NULL DEFAULT 0 COMMENT ''是否已绑定AFI加速 0:否 1:是'' AFTER `is_return_principal`, ADD KEY `idx_afi_accelerated` (`afi_accelerated`) USING BTREE',
  'SELECT 1'
);
PREPARE stmt FROM @afiAcceleratedSql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

INSERT INTO `t_sys_para` (`para_code`, `para_value`, `para_desc`, `visible`, `create_time`, `active_flag`, `remark`)
SELECT 'stake_hosting_afi_price', '0', 'AFI当前后台配置价格，单位USDT', '0', sysdate(), 1, 'AFI质押加速使用，质押记录保存价格快照'
WHERE NOT EXISTS (SELECT 1 FROM `t_sys_para` WHERE `para_code` = 'stake_hosting_afi_price');

-- ----------------------------
-- 字典 SQL
-- ----------------------------
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT 'AFI加速配置状态', 't_stake_hosting_afi_config_status', '0', 'admin', sysdate(), 'AFI加速配置启停状态'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 't_stake_hosting_afi_config_status');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 0, '停用', '0', 't_stake_hosting_afi_config_status', '', 'info', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 't_stake_hosting_afi_config_status' AND dict_value = '0');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1, '启用', '1', 't_stake_hosting_afi_config_status', '', 'success', 'Y', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 't_stake_hosting_afi_config_status' AND dict_value = '1');

INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT 'AFI质押记录状态', 't_stake_hosting_afi_pledge_status', '0', 'admin', sysdate(), 'AFI质押记录状态'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 't_stake_hosting_afi_pledge_status');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 0, '未生效', '0', 't_stake_hosting_afi_pledge_status', '', 'info', 'Y', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 't_stake_hosting_afi_pledge_status' AND dict_value = '0');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1, '生效中', '1', 't_stake_hosting_afi_pledge_status', '', 'success', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 't_stake_hosting_afi_pledge_status' AND dict_value = '1');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 2, '已退还', '2', 't_stake_hosting_afi_pledge_status', '', 'primary', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 't_stake_hosting_afi_pledge_status' AND dict_value = '2');

INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '托管订单AFI加速状态', 't_stake_hosting_order_afi_accelerated', '0', 'admin', sysdate(), '托管订单是否已绑定AFI加速'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 't_stake_hosting_order_afi_accelerated');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 0, '未加速', '0', 't_stake_hosting_order_afi_accelerated', '', 'info', 'Y', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 't_stake_hosting_order_afi_accelerated' AND dict_value = '0');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1, '已加速', '1', 't_stake_hosting_order_afi_accelerated', '', 'success', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 't_stake_hosting_order_afi_accelerated' AND dict_value = '1');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 35, 'AFI质押扣减', '35', 't_user_money_log_source_type', '', 'warning', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 't_user_money_log_source_type' AND dict_value = '35');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 36, 'AFI质押退还', '36', 't_user_money_log_source_type', '', 'success', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 't_user_money_log_source_type' AND dict_value = '36');

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
SELECT 'AFI加速配置', @stakeHostingRootId, 4, 'stakeHostingAfiAccelerateConfig', 'xms/stakeHostingAfiAccelerateConfig/index', 1, 0, 'C', '0', '0', 'xms:stakeHostingAfiAccelerateConfig:list', 'edit', 'admin', sysdate(), '', null, 'AFI加速配置菜单'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @stakeHostingRootId AND perms = 'xms:stakeHostingAfiAccelerateConfig:list');

SELECT @afiConfigMenuId := menu_id FROM sys_menu WHERE parent_id = @stakeHostingRootId AND perms = 'xms:stakeHostingAfiAccelerateConfig:list' LIMIT 1;

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT 'AFI加速配置查询', @afiConfigMenuId, 1, '#', '', 1, 0, 'F', '0', '0', 'xms:stakeHostingAfiAccelerateConfig:query', '#', 'admin', sysdate(), '', null, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @afiConfigMenuId AND perms = 'xms:stakeHostingAfiAccelerateConfig:query');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT 'AFI加速配置新增', @afiConfigMenuId, 2, '#', '', 1, 0, 'F', '0', '0', 'xms:stakeHostingAfiAccelerateConfig:add', '#', 'admin', sysdate(), '', null, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @afiConfigMenuId AND perms = 'xms:stakeHostingAfiAccelerateConfig:add');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT 'AFI加速配置修改', @afiConfigMenuId, 3, '#', '', 1, 0, 'F', '0', '0', 'xms:stakeHostingAfiAccelerateConfig:edit', '#', 'admin', sysdate(), '', null, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @afiConfigMenuId AND perms = 'xms:stakeHostingAfiAccelerateConfig:edit');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT 'AFI加速配置删除', @afiConfigMenuId, 4, '#', '', 1, 0, 'F', '0', '0', 'xms:stakeHostingAfiAccelerateConfig:remove', '#', 'admin', sysdate(), '', null, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @afiConfigMenuId AND perms = 'xms:stakeHostingAfiAccelerateConfig:remove');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT 'AFI加速配置导出', @afiConfigMenuId, 5, '#', '', 1, 0, 'F', '0', '0', 'xms:stakeHostingAfiAccelerateConfig:export', '#', 'admin', sysdate(), '', null, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @afiConfigMenuId AND perms = 'xms:stakeHostingAfiAccelerateConfig:export');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT 'AFI质押记录', @stakeHostingRootId, 5, 'stakeHostingAfiPledge', 'xms/stakeHostingAfiPledge/index', 1, 0, 'C', '0', '0', 'xms:stakeHostingAfiPledge:list', 'log', 'admin', sysdate(), '', null, 'AFI质押记录菜单'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @stakeHostingRootId AND perms = 'xms:stakeHostingAfiPledge:list');

SELECT @afiPledgeMenuId := menu_id FROM sys_menu WHERE parent_id = @stakeHostingRootId AND perms = 'xms:stakeHostingAfiPledge:list' LIMIT 1;

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT 'AFI质押记录查询', @afiPledgeMenuId, 1, '#', '', 1, 0, 'F', '0', '0', 'xms:stakeHostingAfiPledge:query', '#', 'admin', sysdate(), '', null, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @afiPledgeMenuId AND perms = 'xms:stakeHostingAfiPledge:query');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT 'AFI质押记录导出', @afiPledgeMenuId, 2, '#', '', 1, 0, 'F', '0', '0', 'xms:stakeHostingAfiPledge:export', '#', 'admin', sysdate(), '', null, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @afiPledgeMenuId AND perms = 'xms:stakeHostingAfiPledge:export');
