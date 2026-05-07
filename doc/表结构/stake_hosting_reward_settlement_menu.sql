-- 奖励结算明细菜单 SQL：只读流水页
-- 如果托管管理目录不在主类目下，请执行前修改 @stakeHostingRootParentId。
SET @stakeHostingRootParentId := 0;
SET @stakeHostingRootOrderNum := 94;

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT '托管管理', @stakeHostingRootParentId, @stakeHostingRootOrderNum, 'stakeHosting', 'Layout', 1, 0, 'M', '0', '0', '', 'tree', 'admin', sysdate(), '', null, '托管管理目录'
WHERE NOT EXISTS (
  SELECT 1 FROM sys_menu WHERE menu_name = '托管管理' AND parent_id = @stakeHostingRootParentId AND menu_type = 'M'
);

SELECT @stakeHostingRootId := menu_id FROM sys_menu WHERE menu_name = '托管管理' AND parent_id = @stakeHostingRootParentId AND menu_type = 'M' LIMIT 1;

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT '奖励结算明细', @stakeHostingRootId, 3, 'stakeHostingRewardSettlement', 'xms/stakeHostingRewardSettlement/index', 1, 0, 'C', '0', '0', 'xms:stakeHostingRewardSettlement:list', 'documentation', 'admin', sysdate(), '', null, '托管奖励结算明细菜单'
WHERE NOT EXISTS (
  SELECT 1 FROM sys_menu WHERE parent_id = @stakeHostingRootId AND perms = 'xms:stakeHostingRewardSettlement:list'
);

SELECT @stakeHostingRewardSettlementParentId := menu_id FROM sys_menu WHERE parent_id = @stakeHostingRootId AND perms = 'xms:stakeHostingRewardSettlement:list' LIMIT 1;

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT '奖励结算明细查询', @stakeHostingRewardSettlementParentId, 1, '#', '', 1, 0, 'F', '0', '0', 'xms:stakeHostingRewardSettlement:query', '#', 'admin', sysdate(), '', null, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @stakeHostingRewardSettlementParentId AND perms = 'xms:stakeHostingRewardSettlement:query');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT '奖励结算明细导出', @stakeHostingRewardSettlementParentId, 2, '#', '', 1, 0, 'F', '0', '0', 'xms:stakeHostingRewardSettlement:export', '#', 'admin', sysdate(), '', null, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @stakeHostingRewardSettlementParentId AND perms = 'xms:stakeHostingRewardSettlement:export');
