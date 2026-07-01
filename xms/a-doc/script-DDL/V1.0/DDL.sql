-- 全平台托管累计金额汇总表。
-- 本表只维护 id=1 一条记录，所有用户托管金额都累计到 id=1；id=1 不是用户ID。
-- 订单生效时增加托管金额；订单到期、取消托管和后台取消时扣减托管金额，最低扣到0。

CREATE TABLE IF NOT EXISTS `t_stake_hosting_user_amount_summary` (
                                                                     `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                                                     `total_amount` decimal(20,6) NOT NULL DEFAULT '0.000000' COMMENT '全平台托管累计金额',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime DEFAULT NULL COMMENT '更新时间',
    `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci DEFAULT NULL COMMENT '备注',
    `deleted` int NOT NULL DEFAULT '0' COMMENT '删除标识 0正常 1删除',
    PRIMARY KEY (`id`) USING BTREE
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci ROW_FORMAT=DYNAMIC COMMENT='全平台托管累计金额汇总表';

INSERT INTO t_stake_hosting_user_amount_summary
(id, total_amount, create_time, update_time, remark, deleted)
VALUES
    (1, 0.000000, NOW(), NOW(), '全平台托管累计金额汇总记录', 0)
    ON DUPLICATE KEY UPDATE
                         deleted = 0,
                         update_time = NOW();

-- 后台菜单 SQL。
SET @stakeHostingRootParentId := 0;
SET @stakeHostingRootOrderNum := 30;

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT '托管管理', @stakeHostingRootParentId, @stakeHostingRootOrderNum, 'stakeHosting', NULL, 1, 0, 'M', '0', '0', '', 'money', 'admin', sysdate(), '', null, '托管管理目录'
    WHERE NOT EXISTS (
  SELECT 1 FROM sys_menu WHERE menu_name = '托管管理' AND parent_id = @stakeHostingRootParentId AND menu_type = 'M'
);

SELECT @stakeHostingRootId := menu_id
FROM sys_menu
WHERE menu_name = '托管管理' AND parent_id = @stakeHostingRootParentId AND menu_type = 'M'
    LIMIT 1;

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT '全平台托管累计金额', @stakeHostingRootId, 13, 'stakeHostingUserAmountSummary', 'xms/stakeHostingUserAmountSummary/index', 1, 0, 'C', '0', '0', 'xms:stakeHostingUserAmountSummary:list', 'money', 'admin', sysdate(), '', null, '全平台托管累计金额菜单'
    WHERE NOT EXISTS (
  SELECT 1 FROM sys_menu WHERE parent_id = @stakeHostingRootId AND perms = 'xms:stakeHostingUserAmountSummary:list'
);

SELECT @stakeHostingUserAmountSummaryMenuId := menu_id
FROM sys_menu
WHERE parent_id = @stakeHostingRootId AND perms = 'xms:stakeHostingUserAmountSummary:list'
    LIMIT 1;

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT '查询托管累计金额', @stakeHostingUserAmountSummaryMenuId, 1, '#', '', 1, 0, 'F', '0', '0', 'xms:stakeHostingUserAmountSummary:query', '#', 'admin', sysdate(), '', null, ''
    WHERE NOT EXISTS (
  SELECT 1 FROM sys_menu WHERE parent_id = @stakeHostingUserAmountSummaryMenuId AND perms = 'xms:stakeHostingUserAmountSummary:query'
);

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT '增加托管累计金额', @stakeHostingUserAmountSummaryMenuId, 2, '#', '', 1, 0, 'F', '0', '0', 'xms:stakeHostingUserAmountSummary:increase', '#', 'admin', sysdate(), '', null, ''
    WHERE NOT EXISTS (
  SELECT 1 FROM sys_menu WHERE parent_id = @stakeHostingUserAmountSummaryMenuId AND perms = 'xms:stakeHostingUserAmountSummary:increase'
);

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT '扣除托管累计金额', @stakeHostingUserAmountSummaryMenuId, 3, '#', '', 1, 0, 'F', '0', '0', 'xms:stakeHostingUserAmountSummary:decrease', '#', 'admin', sysdate(), '', null, ''
    WHERE NOT EXISTS (
  SELECT 1 FROM sys_menu WHERE parent_id = @stakeHostingUserAmountSummaryMenuId AND perms = 'xms:stakeHostingUserAmountSummary:decrease'
);

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT '修改托管累计金额备注', @stakeHostingUserAmountSummaryMenuId, 4, '#', '', 1, 0, 'F', '0', '0', 'xms:stakeHostingUserAmountSummary:edit', '#', 'admin', sysdate(), '', null, ''
    WHERE NOT EXISTS (
  SELECT 1 FROM sys_menu WHERE parent_id = @stakeHostingUserAmountSummaryMenuId AND perms = 'xms:stakeHostingUserAmountSummary:edit'
);


INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '后台拨付托管收益分配方式', 't_stake_hosting_order_grant_reward_mode', '0', 'admin', sysdate(), '后台拨付托管收益分配方式'
    WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 't_stake_hosting_order_grant_reward_mode');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1, '静态动态进锁定USDT', '1', 't_stake_hosting_order_grant_reward_mode', '', 'info', 'Y', '0', 'admin', sysdate(), ''
    WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 't_stake_hosting_order_grant_reward_mode' AND dict_value = '1');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 2, '静态进锁定USDT，动态进可用USDT', '2', 't_stake_hosting_order_grant_reward_mode', '', 'warning', 'N', '0', 'admin', sysdate(), ''
    WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 't_stake_hosting_order_grant_reward_mode' AND dict_value = '2');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 3, '静态动态进可用USDT', '3', 't_stake_hosting_order_grant_reward_mode', '', 'success', 'N', '0', 'admin', sysdate(), ''
    WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 't_stake_hosting_order_grant_reward_mode' AND dict_value = '3');
