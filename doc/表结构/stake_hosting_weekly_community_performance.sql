-- 托管每周新增小区业绩增量脚本
-- 本脚本只做“本周新增业绩记录”基础建设，不接入全球分红发放逻辑。

ALTER TABLE `t_stake_hosting_order`
    ADD COLUMN `performance_start_time` bigint DEFAULT NULL COMMENT '业绩开始时间，格式yyyyMMddHHmmss' AFTER `last_reward_day`,
    ADD COLUMN `performance_end_time` bigint DEFAULT NULL COMMENT '业绩结束时间，格式yyyyMMddHHmmss' AFTER `performance_start_time`,
    ADD COLUMN `weekly_performance_status` int NOT NULL DEFAULT '0' COMMENT '周新增业绩处理状态 0:待处理 1:队列中 2:处理中 3:已完成 4:失败' AFTER `performance_end_time`,
    ADD COLUMN `weekly_performance_skip_reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci DEFAULT NULL COMMENT '周新增业绩跳过原因' AFTER `weekly_performance_status`,
    ADD COLUMN `weekly_performance_time` datetime DEFAULT NULL COMMENT '周新增业绩处理完成时间' AFTER `weekly_performance_skip_reason`,
    ADD KEY `idx_weekly_performance_status` (`weekly_performance_status`) USING BTREE,
    ADD KEY `idx_performance_start_time` (`performance_start_time`) USING BTREE;

CREATE TABLE IF NOT EXISTS `t_stake_hosting_weekly_community_performance` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `account` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci DEFAULT NULL COMMENT '钱包地址快照',
  `week_start_time` bigint NOT NULL COMMENT '周开始时间，格式yyyyMMddHHmmss',
  `week_end_time` bigint NOT NULL COMMENT '周结束时间，格式yyyyMMddHHmmss',
  `self_new_performance` decimal(20,6) NOT NULL DEFAULT '0.000000' COMMENT '本周个人新增业绩',
  `team_new_performance` decimal(20,6) NOT NULL DEFAULT '0.000000' COMMENT '本周团队新增业绩',
  `total_line_performance` decimal(20,6) NOT NULL DEFAULT '0.000000' COMMENT '本周所有直推区新增业绩合计',
  `max_line_performance` decimal(20,6) NOT NULL DEFAULT '0.000000' COMMENT '本周最大直推区新增业绩',
  `community_new_performance` decimal(20,6) NOT NULL DEFAULT '0.000000' COMMENT '本周新增小区业绩',
  `settle_status` int NOT NULL DEFAULT '0' COMMENT '状态 0:统计中 1:已锁定 2:已参与分红',
  `batch_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci DEFAULT NULL COMMENT '后续全球分红批次号',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci DEFAULT NULL COMMENT '备注',
  `deleted` int NOT NULL DEFAULT '0' COMMENT '删除标志 0正常 1删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_user_week` (`user_id`,`week_start_time`) USING BTREE,
  KEY `idx_week` (`week_start_time`,`week_end_time`) USING BTREE,
  KEY `idx_settle_status` (`settle_status`) USING BTREE,
  KEY `idx_community_new_performance` (`community_new_performance`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci ROW_FORMAT=DYNAMIC COMMENT='托管每周新增小区业绩表';

-- ----------------------------
-- 字典 SQL
-- ----------------------------
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '托管每周新增小区业绩状态', 't_stake_hosting_weekly_community_performance_settle_status', '0', 'admin', sysdate(), '托管每周新增小区业绩状态'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 't_stake_hosting_weekly_community_performance_settle_status');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 0, '统计中', '0', 't_stake_hosting_weekly_community_performance_settle_status', '', 'info', 'Y', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 't_stake_hosting_weekly_community_performance_settle_status' AND dict_value = '0');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1, '已锁定', '1', 't_stake_hosting_weekly_community_performance_settle_status', '', 'warning', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 't_stake_hosting_weekly_community_performance_settle_status' AND dict_value = '1');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 2, '已参与分红', '2', 't_stake_hosting_weekly_community_performance_settle_status', '', 'success', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 't_stake_hosting_weekly_community_performance_settle_status' AND dict_value = '2');

-- ----------------------------
-- 菜单 SQL
-- 执行前可调整 @stakeHostingRootParentId 和 @stakeHostingRootOrderNum。
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
SELECT '每周新增小区业绩', @stakeHostingRootId, 10, 'stakeHostingWeeklyCommunityPerformance', 'xms/stakeHostingWeeklyCommunityPerformance/index', 1, 0, 'C', '0', '0', 'xms:stakeHostingWeeklyCommunityPerformance:list', 'chart', 'admin', sysdate(), '', null, '托管每周新增小区业绩菜单'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @stakeHostingRootId AND perms = 'xms:stakeHostingWeeklyCommunityPerformance:list');

SELECT @weeklyCommunityPerformanceMenuId := menu_id FROM sys_menu WHERE parent_id = @stakeHostingRootId AND perms = 'xms:stakeHostingWeeklyCommunityPerformance:list' LIMIT 1;

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT '每周新增小区业绩查询', @weeklyCommunityPerformanceMenuId, 1, '#', '', 1, 0, 'F', '0', '0', 'xms:stakeHostingWeeklyCommunityPerformance:query', '#', 'admin', sysdate(), '', null, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @weeklyCommunityPerformanceMenuId AND perms = 'xms:stakeHostingWeeklyCommunityPerformance:query');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT '每周新增小区业绩导出', @weeklyCommunityPerformanceMenuId, 2, '#', '', 1, 0, 'F', '0', '0', 'xms:stakeHostingWeeklyCommunityPerformance:export', '#', 'admin', sysdate(), '', null, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @weeklyCommunityPerformanceMenuId AND perms = 'xms:stakeHostingWeeklyCommunityPerformance:export');
