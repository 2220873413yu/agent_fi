-- 托管G7每日快照后台菜单、字典增量 SQL
-- 本脚本只新增字典和菜单，不修改历史建表 SQL。
-- 执行前如需调整托管管理菜单位置，可修改 @stakeHostingRootParentId 和 @stakeHostingRootOrderNum。

-- ----------------------------
-- 字典：G7每日快照收益率来源
-- ----------------------------
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT 'G7每日快照收益率来源', 't_stake_hosting_daily_team_performance_rate_source', '0', 'admin', sysdate(), 'G7每日快照收益率来源'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 't_stake_hosting_daily_team_performance_rate_source');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 0, '未计算', '0', 't_stake_hosting_daily_team_performance_rate_source', '', 'info', 'Y', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 't_stake_hosting_daily_team_performance_rate_source' AND dict_value = '0');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1, 'G7区间', '1', 't_stake_hosting_daily_team_performance_rate_source', '', 'success', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 't_stake_hosting_daily_team_performance_rate_source' AND dict_value = '1');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 2, '指定收益率', '2', 't_stake_hosting_daily_team_performance_rate_source', '', 'primary', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 't_stake_hosting_daily_team_performance_rate_source' AND dict_value = '2');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 3, '未推广规则', '3', 't_stake_hosting_daily_team_performance_rate_source', '', 'warning', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 't_stake_hosting_daily_team_performance_rate_source' AND dict_value = '3');

-- ----------------------------
-- 字典：G7每日快照计算状态
-- ----------------------------
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT 'G7每日快照计算状态', 't_stake_hosting_daily_team_performance_calc_status', '0', 'admin', sysdate(), 'G7每日快照计算状态'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 't_stake_hosting_daily_team_performance_calc_status');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 0, '未计算', '0', 't_stake_hosting_daily_team_performance_calc_status', '', 'warning', 'Y', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 't_stake_hosting_daily_team_performance_calc_status' AND dict_value = '0');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1, '已计算', '1', 't_stake_hosting_daily_team_performance_calc_status', '', 'success', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 't_stake_hosting_daily_team_performance_calc_status' AND dict_value = '1');

-- ----------------------------
-- 菜单：G7每日快照
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
SELECT 'G7每日快照', @stakeHostingRootId, 12, 'stakeHostingDailyTeamPerformance', 'xms/stakeHostingDailyTeamPerformance/index', 1, 0, 'C', '0', '0', 'xms:stakeHostingDailyTeamPerformance:list', 'chart', 'admin', sysdate(), '', null, '托管G7每日团队新增业绩与静态收益率快照菜单'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @stakeHostingRootId AND perms = 'xms:stakeHostingDailyTeamPerformance:list');

SELECT @dailyTeamPerformanceMenuId := menu_id FROM sys_menu WHERE parent_id = @stakeHostingRootId AND perms = 'xms:stakeHostingDailyTeamPerformance:list' LIMIT 1;

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT 'G7每日快照查询', @dailyTeamPerformanceMenuId, 1, '#', '', 1, 0, 'F', '0', '0', 'xms:stakeHostingDailyTeamPerformance:query', '#', 'admin', sysdate(), '', null, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @dailyTeamPerformanceMenuId AND perms = 'xms:stakeHostingDailyTeamPerformance:query');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT 'G7每日快照导出', @dailyTeamPerformanceMenuId, 2, '#', '', 1, 0, 'F', '0', '0', 'xms:stakeHostingDailyTeamPerformance:export', '#', 'admin', sysdate(), '', null, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @dailyTeamPerformanceMenuId AND perms = 'xms:stakeHostingDailyTeamPerformance:export');
