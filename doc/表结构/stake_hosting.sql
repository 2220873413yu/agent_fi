-- 质押托管第一批主流程表结构
-- 执行前确认当前库已存在 t_user_info、t_user_money、xms_reward_record 等基础表。

CREATE TABLE IF NOT EXISTS `t_stake_hosting_package` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL COMMENT '套餐名称',
  `days` int NOT NULL COMMENT '托管天数，固定为1/30/90/180/360',
  `min_amount` decimal(20,6) NOT NULL DEFAULT 10.000000 COMMENT '最低起购USDT金额',
  `service_fee_ratio` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '服务费比例，单位%',
  `sort` int NOT NULL DEFAULT 0 COMMENT '排序',
  `status` int NOT NULL DEFAULT 0 COMMENT '状态 0:下架 1:上架',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '创建者',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '更新者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '备注',
  `deleted` int NOT NULL DEFAULT 0 COMMENT '删除标志 0正常 1删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_days` (`days`) USING BTREE,
  KEY `idx_status` (`status`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci COMMENT='托管套餐表' ROW_FORMAT=DYNAMIC;

CREATE TABLE IF NOT EXISTS `t_stake_hosting_order` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL COMMENT '托管订单号',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `account` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL COMMENT '用户钱包地址',
  `package_id` bigint NOT NULL COMMENT '套餐ID',
  `package_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL COMMENT '套餐名称快照',
  `package_days` int NOT NULL COMMENT '套餐天数快照',
  `stake_usdt_amount` decimal(20,6) NOT NULL DEFAULT 0.000000 COMMENT '托管USDT金额',
  `source_type` int NOT NULL DEFAULT 0 COMMENT '订单来源 0:用户购买 1:后台拨付',
  `pay_status` int NOT NULL DEFAULT 0 COMMENT '支付状态 0:待支付 1:支付成功 2:后台拨付无需支付',
  `status` int NOT NULL DEFAULT 0 COMMENT '业务状态 0:未开始 1:产出中 2:已完成 3:已暂停',
  `pay_hash` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '支付hash',
  `pay_amount` decimal(20,6) NULL DEFAULT 0.000000 COMMENT '链上支付金额',
  `pay_time` datetime NULL DEFAULT NULL COMMENT '支付时间',
  `effective_time` datetime NULL DEFAULT NULL COMMENT '生效时间',
  `finish_time` datetime NULL DEFAULT NULL COMMENT '完成时间',
  `run_days` int NOT NULL DEFAULT 0 COMMENT '已运行天数，即已发放静态收益次数',
  `today_reward` decimal(20,6) NOT NULL DEFAULT 0.000000 COMMENT '今日静态收益',
  `total_static_reward` decimal(20,6) NOT NULL DEFAULT 0.000000 COMMENT '累计已发静态收益',
  `is_return_principal` int NOT NULL DEFAULT 0 COMMENT '是否回本 0:否 1:是',
  `last_reward_day` int NULL DEFAULT NULL COMMENT '最近一次发放日期，格式yyyyMMdd',
  `create_day` int NULL DEFAULT NULL COMMENT '创建日期，格式yyyyMMdd',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '创建者',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '更新者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '备注',
  `deleted` int NOT NULL DEFAULT 0 COMMENT '删除标志 0正常 1删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_order_no` (`order_no`) USING BTREE,
  UNIQUE KEY `uk_pay_hash` (`pay_hash`) USING BTREE,
  KEY `idx_user_id` (`user_id`) USING BTREE,
  KEY `idx_status` (`status`) USING BTREE,
  KEY `idx_pay_status` (`pay_status`) USING BTREE,
  KEY `idx_source_type` (`source_type`) USING BTREE,
  KEY `idx_create_time` (`create_time`) USING BTREE,
  KEY `idx_last_reward_day` (`last_reward_day`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci COMMENT='托管订单表' ROW_FORMAT=DYNAMIC;

INSERT IGNORE INTO `t_stake_hosting_package` (`name`, `days`, `min_amount`, `service_fee_ratio`, `sort`, `status`, `remark`) VALUES
('1天托管', 1, 10.000000, 0.00, 1, 1, '系统固定托管套餐'),
('30天托管', 30, 10.000000, 0.00, 30, 1, '系统固定托管套餐'),
('90天托管', 90, 10.000000, 0.00, 90, 1, '系统固定托管套餐'),
('180天托管', 180, 10.000000, 0.00, 180, 1, '系统固定托管套餐'),
('360天托管', 360, 10.000000, 0.00, 360, 1, '系统固定托管套餐');

ALTER TABLE `t_user_info`
  MODIFY COLUMN `performance` decimal(20,6) NULL DEFAULT 0.000000 COMMENT '个人托管业绩',
  MODIFY COLUMN `sub_performance` decimal(20,6) NULL DEFAULT 0.000000 COMMENT '直推托管业绩',
  MODIFY COLUMN `performance_mining` decimal(20,6) NULL DEFAULT 0.000000 COMMENT '团队托管业绩兼容字段',
  MODIFY COLUMN `umbrella_performance` decimal(20,6) NULL DEFAULT 0.000000 COMMENT '伞下团队托管业绩',
  MODIFY COLUMN `community_performance` decimal(20,6) NULL DEFAULT 0.000000 COMMENT '小区托管业绩';

-- ----------------------------
-- 字典 SQL：托管管理枚举
-- ----------------------------
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '托管套餐天数', 't_stake_hosting_package_days', '0', 'admin', sysdate(), '托管套餐固定天数字典'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 't_stake_hosting_package_days');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1, '1天', '1', 't_stake_hosting_package_days', '', 'default', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 't_stake_hosting_package_days' AND dict_value = '1');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 30, '30天', '30', 't_stake_hosting_package_days', '', 'default', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 't_stake_hosting_package_days' AND dict_value = '30');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 90, '90天', '90', 't_stake_hosting_package_days', '', 'default', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 't_stake_hosting_package_days' AND dict_value = '90');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 180, '180天', '180', 't_stake_hosting_package_days', '', 'default', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 't_stake_hosting_package_days' AND dict_value = '180');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 360, '360天', '360', 't_stake_hosting_package_days', '', 'default', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 't_stake_hosting_package_days' AND dict_value = '360');

INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '托管套餐状态', 't_stake_hosting_package_status', '0', 'admin', sysdate(), '托管套餐上下架状态'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 't_stake_hosting_package_status');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 0, '下架', '0', 't_stake_hosting_package_status', '', 'info', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 't_stake_hosting_package_status' AND dict_value = '0');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1, '上架', '1', 't_stake_hosting_package_status', '', 'success', 'Y', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 't_stake_hosting_package_status' AND dict_value = '1');

INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '托管订单来源', 't_stake_hosting_order_source_type', '0', 'admin', sysdate(), '托管订单来源'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 't_stake_hosting_order_source_type');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 0, '用户购买', '0', 't_stake_hosting_order_source_type', '', 'success', 'Y', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 't_stake_hosting_order_source_type' AND dict_value = '0');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1, '后台拨付', '1', 't_stake_hosting_order_source_type', '', 'warning', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 't_stake_hosting_order_source_type' AND dict_value = '1');

INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '托管订单支付状态', 't_stake_hosting_order_pay_status', '0', 'admin', sysdate(), '托管订单支付状态'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 't_stake_hosting_order_pay_status');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 0, '待支付', '0', 't_stake_hosting_order_pay_status', '', 'info', 'Y', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 't_stake_hosting_order_pay_status' AND dict_value = '0');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1, '支付成功', '1', 't_stake_hosting_order_pay_status', '', 'success', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 't_stake_hosting_order_pay_status' AND dict_value = '1');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 2, '后台拨付', '2', 't_stake_hosting_order_pay_status', '', 'warning', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 't_stake_hosting_order_pay_status' AND dict_value = '2');

INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '托管订单业务状态', 't_stake_hosting_order_status', '0', 'admin', sysdate(), '托管订单业务状态'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 't_stake_hosting_order_status');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 0, '未开始', '0', 't_stake_hosting_order_status', '', 'info', 'Y', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 't_stake_hosting_order_status' AND dict_value = '0');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1, '产出中', '1', 't_stake_hosting_order_status', '', 'success', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 't_stake_hosting_order_status' AND dict_value = '1');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 2, '已完成', '2', 't_stake_hosting_order_status', '', 'primary', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 't_stake_hosting_order_status' AND dict_value = '2');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 3, '已暂停', '3', 't_stake_hosting_order_status', '', 'warning', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 't_stake_hosting_order_status' AND dict_value = '3');

INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '托管订单是否回本', 't_stake_hosting_order_return_principal', '0', 'admin', sysdate(), '托管订单是否回本'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 't_stake_hosting_order_return_principal');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 0, '否', '0', 't_stake_hosting_order_return_principal', '', 'info', 'Y', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 't_stake_hosting_order_return_principal' AND dict_value = '0');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1, '是', '1', 't_stake_hosting_order_return_principal', '', 'success', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 't_stake_hosting_order_return_principal' AND dict_value = '1');

-- ----------------------------
-- 菜单 SQL：托管管理
-- 执行前如需调整位置，可修改 @stakeHostingRootParentId 和 @stakeHostingRootOrderNum。
-- ----------------------------
SET @stakeHostingRootParentId := 0;
SET @stakeHostingRootOrderNum := 94;

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT '托管管理', @stakeHostingRootParentId, @stakeHostingRootOrderNum, 'stakeHosting', 'Layout', 1, 0, 'M', '0', '0', '', 'tree', 'admin', sysdate(), '', null, '托管管理目录'
WHERE NOT EXISTS (
  SELECT 1 FROM sys_menu WHERE menu_name = '托管管理' AND parent_id = @stakeHostingRootParentId AND menu_type = 'M'
);

SELECT @stakeHostingRootId := menu_id FROM sys_menu WHERE menu_name = '托管管理' AND parent_id = @stakeHostingRootParentId AND menu_type = 'M' LIMIT 1;

-- ----------------------------
-- 菜单 SQL：托管套餐
-- ----------------------------
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT '托管套餐', @stakeHostingRootId, 1, 'stakeHostingPackage', 'xms/stakeHostingPackage/index', 1, 0, 'C', '0', '0', 'xms:stakeHostingPackage:list', 'list', 'admin', sysdate(), '', null, '托管套餐菜单'
WHERE NOT EXISTS (
  SELECT 1 FROM sys_menu WHERE parent_id = @stakeHostingRootId AND perms = 'xms:stakeHostingPackage:list'
);

SELECT @stakeHostingPackageParentId := menu_id FROM sys_menu WHERE parent_id = @stakeHostingRootId AND perms = 'xms:stakeHostingPackage:list' LIMIT 1;

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT '托管套餐查询', @stakeHostingPackageParentId, 1, '#', '', 1, 0, 'F', '0', '0', 'xms:stakeHostingPackage:query', '#', 'admin', sysdate(), '', null, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @stakeHostingPackageParentId AND perms = 'xms:stakeHostingPackage:query');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT '托管套餐新增', @stakeHostingPackageParentId, 2, '#', '', 1, 0, 'F', '0', '0', 'xms:stakeHostingPackage:add', '#', 'admin', sysdate(), '', null, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @stakeHostingPackageParentId AND perms = 'xms:stakeHostingPackage:add');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT '托管套餐修改', @stakeHostingPackageParentId, 3, '#', '', 1, 0, 'F', '0', '0', 'xms:stakeHostingPackage:edit', '#', 'admin', sysdate(), '', null, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @stakeHostingPackageParentId AND perms = 'xms:stakeHostingPackage:edit');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT '托管套餐删除', @stakeHostingPackageParentId, 4, '#', '', 1, 0, 'F', '0', '0', 'xms:stakeHostingPackage:remove', '#', 'admin', sysdate(), '', null, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @stakeHostingPackageParentId AND perms = 'xms:stakeHostingPackage:remove');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT '托管套餐导出', @stakeHostingPackageParentId, 5, '#', '', 1, 0, 'F', '0', '0', 'xms:stakeHostingPackage:export', '#', 'admin', sysdate(), '', null, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @stakeHostingPackageParentId AND perms = 'xms:stakeHostingPackage:export');

-- ----------------------------
-- 菜单 SQL：托管订单
-- ----------------------------
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT '托管订单', @stakeHostingRootId, 2, 'stakeHostingOrder', 'xms/stakeHostingOrder/index', 1, 0, 'C', '0', '0', 'xms:stakeHostingOrder:list', 'log', 'admin', sysdate(), '', null, '托管订单菜单'
WHERE NOT EXISTS (
  SELECT 1 FROM sys_menu WHERE parent_id = @stakeHostingRootId AND perms = 'xms:stakeHostingOrder:list'
);

SELECT @stakeHostingOrderParentId := menu_id FROM sys_menu WHERE parent_id = @stakeHostingRootId AND perms = 'xms:stakeHostingOrder:list' LIMIT 1;

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT '托管订单查询', @stakeHostingOrderParentId, 1, '#', '', 1, 0, 'F', '0', '0', 'xms:stakeHostingOrder:query', '#', 'admin', sysdate(), '', null, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @stakeHostingOrderParentId AND perms = 'xms:stakeHostingOrder:query');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT '托管订单新增', @stakeHostingOrderParentId, 2, '#', '', 1, 0, 'F', '0', '0', 'xms:stakeHostingOrder:add', '#', 'admin', sysdate(), '', null, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @stakeHostingOrderParentId AND perms = 'xms:stakeHostingOrder:add');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT '托管订单修改', @stakeHostingOrderParentId, 3, '#', '', 1, 0, 'F', '0', '0', 'xms:stakeHostingOrder:edit', '#', 'admin', sysdate(), '', null, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @stakeHostingOrderParentId AND perms = 'xms:stakeHostingOrder:edit');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT '托管订单删除', @stakeHostingOrderParentId, 4, '#', '', 1, 0, 'F', '0', '0', 'xms:stakeHostingOrder:remove', '#', 'admin', sysdate(), '', null, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @stakeHostingOrderParentId AND perms = 'xms:stakeHostingOrder:remove');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT '托管订单导出', @stakeHostingOrderParentId, 5, '#', '', 1, 0, 'F', '0', '0', 'xms:stakeHostingOrder:export', '#', 'admin', sysdate(), '', null, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @stakeHostingOrderParentId AND perms = 'xms:stakeHostingOrder:export');
