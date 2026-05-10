-- 托管静态收益率 G7 增量脚本
-- 只做增量变更，不修改历史建表 SQL。
-- 口径：G7 使用伞下团队托管 USDT 金额，不使用套餐积分系数。

ALTER TABLE `t_stake_hosting_order`
    ADD COLUMN `g7_new_performance_status` int NOT NULL DEFAULT '0' COMMENT 'G7团队新增统计状态 0未处理 1已处理' AFTER `weekly_performance_time`,
    ADD COLUMN `g7_new_performance_time` datetime DEFAULT NULL COMMENT 'G7团队新增统计处理时间' AFTER `g7_new_performance_status`,
    ADD COLUMN `g7_expire_performance_status` int NOT NULL DEFAULT '0' COMMENT 'G7团队到期统计状态 0未处理 1已处理' AFTER `g7_new_performance_time`,
    ADD COLUMN `g7_expire_performance_time` datetime DEFAULT NULL COMMENT 'G7团队到期统计处理时间' AFTER `g7_expire_performance_status`;

ALTER TABLE `t_user_info`
    ADD COLUMN `stake_hosting_static_rate` decimal(10,4) NOT NULL DEFAULT '0.0000' COMMENT '托管指定静态收益率，单位%，0表示按G7规则' AFTER `performance_mining`;

CREATE TABLE `t_stake_hosting_daily_team_performance` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `account` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci DEFAULT NULL COMMENT '钱包地址快照',
  `stat_day` int NOT NULL COMMENT '统计日期，格式yyyyMMdd',
  `team_new_amount` decimal(20,6) NOT NULL DEFAULT '0.000000' COMMENT '当天伞下团队新增托管USDT金额',
  `team_expired_amount` decimal(20,6) NOT NULL DEFAULT '0.000000' COMMENT '当天伞下团队到期托管USDT金额',
  `previous_team_tvl` decimal(20,6) NOT NULL DEFAULT '0.000000' COMMENT '昨日伞下团队有效托管USDT TVL',
  `current_team_tvl` decimal(20,6) NOT NULL DEFAULT '0.000000' COMMENT '当日伞下团队有效托管USDT TVL',
  `g_day` decimal(10,4) NOT NULL DEFAULT '0.0000' COMMENT '单日增长率，单位%',
  `g_smooth` decimal(10,4) NOT NULL DEFAULT '0.0000' COMMENT '最近最多7天滚动平均增长率，单位%',
  `base_static_rate` decimal(10,4) NOT NULL DEFAULT '0.5000' COMMENT '命中基础静态收益率，单位%',
  `rate_source` int NOT NULL DEFAULT '0' COMMENT '收益率来源 0未计算 1G7区间 2指定收益率 3未推广特殊规则',
  `calc_status` int NOT NULL DEFAULT '0' COMMENT '计算状态 0未计算 1已计算',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci DEFAULT NULL COMMENT '备注',
  `deleted` int NOT NULL DEFAULT '0' COMMENT '删除标志 0正常 1删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_user_day` (`user_id`, `stat_day`) USING BTREE,
  KEY `idx_stat_day` (`stat_day`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci ROW_FORMAT=DYNAMIC COMMENT='托管G7每日团队TVL与收益率快照表';

CREATE TABLE `t_stake_hosting_static_rate_config` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `min_g` decimal(10,4) NOT NULL COMMENT 'Gsmooth下限，单位%，左闭',
  `max_g` decimal(10,4) DEFAULT NULL COMMENT 'Gsmooth上限，单位%，右开，NULL表示无上限',
  `static_rate` decimal(10,4) NOT NULL COMMENT '日化静态收益率，单位%',
  `sort` int NOT NULL DEFAULT '0' COMMENT '排序',
  `status` int NOT NULL DEFAULT '1' COMMENT '状态 1启用 0停用',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci DEFAULT NULL COMMENT '备注',
  `deleted` int NOT NULL DEFAULT '0' COMMENT '删除标志 0正常 1删除',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_status_sort` (`status`, `sort`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci ROW_FORMAT=DYNAMIC COMMENT='托管G7静态收益率区间配置表';

INSERT INTO `t_stake_hosting_static_rate_config` (`min_g`, `max_g`, `static_rate`, `sort`, `status`, `remark`)
SELECT -999999.0000, -20.0000, 0.3000, 1, 1, 'G < -20%'
WHERE NOT EXISTS (SELECT 1 FROM `t_stake_hosting_static_rate_config` WHERE `sort` = 1 AND `deleted` = 0);
INSERT INTO `t_stake_hosting_static_rate_config` (`min_g`, `max_g`, `static_rate`, `sort`, `status`, `remark`)
SELECT -20.0000, -10.0000, 0.3500, 2, 1, '-20% <= G < -10%'
WHERE NOT EXISTS (SELECT 1 FROM `t_stake_hosting_static_rate_config` WHERE `sort` = 2 AND `deleted` = 0);
INSERT INTO `t_stake_hosting_static_rate_config` (`min_g`, `max_g`, `static_rate`, `sort`, `status`, `remark`)
SELECT -10.0000, 0.0000, 0.4000, 3, 1, '-10% <= G < 0%'
WHERE NOT EXISTS (SELECT 1 FROM `t_stake_hosting_static_rate_config` WHERE `sort` = 3 AND `deleted` = 0);
INSERT INTO `t_stake_hosting_static_rate_config` (`min_g`, `max_g`, `static_rate`, `sort`, `status`, `remark`)
SELECT 0.0000, 5.0000, 0.5000, 4, 1, '0% <= G < 5%'
WHERE NOT EXISTS (SELECT 1 FROM `t_stake_hosting_static_rate_config` WHERE `sort` = 4 AND `deleted` = 0);
INSERT INTO `t_stake_hosting_static_rate_config` (`min_g`, `max_g`, `static_rate`, `sort`, `status`, `remark`)
SELECT 5.0000, 10.0000, 0.6000, 5, 1, '5% <= G < 10%'
WHERE NOT EXISTS (SELECT 1 FROM `t_stake_hosting_static_rate_config` WHERE `sort` = 5 AND `deleted` = 0);
INSERT INTO `t_stake_hosting_static_rate_config` (`min_g`, `max_g`, `static_rate`, `sort`, `status`, `remark`)
SELECT 10.0000, 15.0000, 0.7500, 6, 1, '10% <= G < 15%'
WHERE NOT EXISTS (SELECT 1 FROM `t_stake_hosting_static_rate_config` WHERE `sort` = 6 AND `deleted` = 0);
INSERT INTO `t_stake_hosting_static_rate_config` (`min_g`, `max_g`, `static_rate`, `sort`, `status`, `remark`)
SELECT 15.0000, 20.0000, 0.9000, 7, 1, '15% <= G < 20%'
WHERE NOT EXISTS (SELECT 1 FROM `t_stake_hosting_static_rate_config` WHERE `sort` = 7 AND `deleted` = 0);
INSERT INTO `t_stake_hosting_static_rate_config` (`min_g`, `max_g`, `static_rate`, `sort`, `status`, `remark`)
SELECT 20.0000, 25.0000, 1.1000, 8, 1, '20% <= G < 25%'
WHERE NOT EXISTS (SELECT 1 FROM `t_stake_hosting_static_rate_config` WHERE `sort` = 8 AND `deleted` = 0);
INSERT INTO `t_stake_hosting_static_rate_config` (`min_g`, `max_g`, `static_rate`, `sort`, `status`, `remark`)
SELECT 25.0000, 30.0000, 1.3000, 9, 1, '25% <= G < 30%'
WHERE NOT EXISTS (SELECT 1 FROM `t_stake_hosting_static_rate_config` WHERE `sort` = 9 AND `deleted` = 0);
INSERT INTO `t_stake_hosting_static_rate_config` (`min_g`, `max_g`, `static_rate`, `sort`, `status`, `remark`)
SELECT 30.0000, NULL, 1.5000, 10, 1, 'G >= 30%'
WHERE NOT EXISTS (SELECT 1 FROM `t_stake_hosting_static_rate_config` WHERE `sort` = 10 AND `deleted` = 0);

-- 字典：G7静态收益率配置状态
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT 'G7静态收益率配置状态', 't_stake_hosting_static_rate_config_status', '0', 'admin', sysdate(), 'G7静态收益率配置状态'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 't_stake_hosting_static_rate_config_status');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 0, '停用', '0', 't_stake_hosting_static_rate_config_status', '', 'danger', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 't_stake_hosting_static_rate_config_status' AND dict_value = '0');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1, '启用', '1', 't_stake_hosting_static_rate_config_status', '', 'success', 'Y', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 't_stake_hosting_static_rate_config_status' AND dict_value = '1');

-- 菜单：G7静态收益率配置
-- 执行前如需调整托管管理菜单位置，可修改 @stakeHostingRootParentId 和 @stakeHostingRootOrderNum。
SET @stakeHostingRootParentId := 0;
SET @stakeHostingRootOrderNum := 94;

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT '托管管理', @stakeHostingRootParentId, @stakeHostingRootOrderNum, 'stakeHosting', 'Layout', 1, 0, 'M', '0', '0', '', 'tree', 'admin', sysdate(), '', null, '托管管理目录'
WHERE NOT EXISTS (
  SELECT 1 FROM sys_menu WHERE menu_name = '托管管理' AND parent_id = @stakeHostingRootParentId AND menu_type = 'M'
);

SELECT @stakeHostingRootId := menu_id FROM sys_menu WHERE menu_name = '托管管理' AND parent_id = @stakeHostingRootParentId AND menu_type = 'M' LIMIT 1;

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT 'G7收益率配置', @stakeHostingRootId, 11, 'stakeHostingStaticRateConfig', 'xms/stakeHostingStaticRateConfig/index', 1, 0, 'C', '0', '0', 'xms:stakeHostingStaticRateConfig:list', 'chart', 'admin', sysdate(), '', null, 'G7静态收益率配置菜单'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @stakeHostingRootId AND perms = 'xms:stakeHostingStaticRateConfig:list');

SELECT @staticRateConfigMenuId := menu_id FROM sys_menu WHERE parent_id = @stakeHostingRootId AND perms = 'xms:stakeHostingStaticRateConfig:list' LIMIT 1;

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT 'G7收益率配置查询', @staticRateConfigMenuId, 1, '#', '', 1, 0, 'F', '0', '0', 'xms:stakeHostingStaticRateConfig:query', '#', 'admin', sysdate(), '', null, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @staticRateConfigMenuId AND perms = 'xms:stakeHostingStaticRateConfig:query');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT 'G7收益率配置新增', @staticRateConfigMenuId, 2, '#', '', 1, 0, 'F', '0', '0', 'xms:stakeHostingStaticRateConfig:add', '#', 'admin', sysdate(), '', null, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @staticRateConfigMenuId AND perms = 'xms:stakeHostingStaticRateConfig:add');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT 'G7收益率配置修改', @staticRateConfigMenuId, 3, '#', '', 1, 0, 'F', '0', '0', 'xms:stakeHostingStaticRateConfig:edit', '#', 'admin', sysdate(), '', null, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @staticRateConfigMenuId AND perms = 'xms:stakeHostingStaticRateConfig:edit');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT 'G7收益率配置删除', @staticRateConfigMenuId, 4, '#', '', 1, 0, 'F', '0', '0', 'xms:stakeHostingStaticRateConfig:remove', '#', 'admin', sysdate(), '', null, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @staticRateConfigMenuId AND perms = 'xms:stakeHostingStaticRateConfig:remove');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT 'G7收益率配置导出', @staticRateConfigMenuId, 5, '#', '', 1, 0, 'F', '0', '0', 'xms:stakeHostingStaticRateConfig:export', '#', 'admin', sysdate(), '', null, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @staticRateConfigMenuId AND perms = 'xms:stakeHostingStaticRateConfig:export');
