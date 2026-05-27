-- 示例质押补充：套餐释放、每日收益、独立等级配置
-- 适用于已经执行过 20260526-demo-pledge.sql 的数据库；新库可直接执行更新后的 20260526 脚本。

ALTER TABLE `t_demo_pledge_package`
  ADD COLUMN `release_days` int NOT NULL DEFAULT '1' COMMENT '释放天数' AFTER `pledge_usdt_amount`,
  ADD COLUMN `daily_rate` decimal(10,4) NOT NULL DEFAULT '0.0000' COMMENT '日利率，单位%' AFTER `release_days`;

ALTER TABLE `t_demo_pledge_order`
  ADD COLUMN `release_days` int NOT NULL DEFAULT '1' COMMENT '释放天数快照' AFTER `pledge_usdt_amount`,
  ADD COLUMN `daily_rate` decimal(10,4) NOT NULL DEFAULT '0.0000' COMMENT '日利率快照，单位%' AFTER `release_days`,
  ADD COLUMN `released_days` int NOT NULL DEFAULT '0' COMMENT '已释放天数' AFTER `daily_rate`,
  ADD COLUMN `reward_status` tinyint NOT NULL DEFAULT '0' COMMENT '收益释放状态 0释放中 1已完成' AFTER `released_days`,
  ADD COLUMN `total_reward_usdt_amount` decimal(20,6) NOT NULL DEFAULT '0.000000' COMMENT '累计收益USDT金额' AFTER `reward_status`,
  ADD COLUMN `last_reward_day` int DEFAULT NULL COMMENT '最近释放日期yyyyMMdd' AFTER `total_reward_usdt_amount`,
  ADD COLUMN `last_reward_time` datetime DEFAULT NULL COMMENT '最近释放时间' AFTER `last_reward_day`,
  ADD KEY `idx_reward_status_day` (`reward_status`, `last_reward_day`);

CREATE TABLE IF NOT EXISTS `t_demo_pledge_level_config` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `level` int NOT NULL COMMENT '等级编码 0暂无 1F1 2F2...',
  `performance` decimal(20,6) NOT NULL DEFAULT '0.000000' COMMENT '个人托管业绩门槛',
  `team_performance` decimal(20,6) NOT NULL DEFAULT '0.000000' COMMENT '团队托管业绩门槛',
  `community_performance` decimal(20,6) NOT NULL DEFAULT '0.000000' COMMENT '小区托管业绩门槛',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `deleted` int NOT NULL DEFAULT '0' COMMENT '删除标记 0正常 1删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_level` (`level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='示例质押等级配置表';

INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '示例质押收益释放状态', 't_demo_pledge_reward_status', '0', 'admin', sysdate(), '示例质押收益释放状态'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 't_demo_pledge_reward_status');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 0, '释放中', '0', 't_demo_pledge_reward_status', '', 'warning', 'Y', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 't_demo_pledge_reward_status' AND dict_value = '0');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1, '已完成', '1', 't_demo_pledge_reward_status', '', 'success', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 't_demo_pledge_reward_status' AND dict_value = '1');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 33, '示例质押每日收益', '33', 'reward_record_source_type', '', 'primary', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'reward_record_source_type' AND dict_value = '33');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 46, '示例质押购买扣减USDT', '46', 'user_money_log_source_type', '', 'warning', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'user_money_log_source_type' AND dict_value = '46');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 47, '示例质押每日收益USDT', '47', 'user_money_log_source_type', '', 'primary', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'user_money_log_source_type' AND dict_value = '47');

SELECT @demoRootId := menu_id FROM sys_menu WHERE menu_name = '示例业务' AND menu_type = 'M' LIMIT 1;

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT '示例质押等级配置', @demoRootId, 3, 'demoPledgeLevelConfig', 'xms/demoPledgeLevelConfig/index', 1, 0, 'C', '0', '0', 'xms:demoPledgeLevelConfig:list', 'tree', 'admin', sysdate(), '', null, '示例质押等级配置菜单'
WHERE @demoRootId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @demoRootId AND perms = 'xms:demoPledgeLevelConfig:list');

SELECT @demoLevelMenuId := menu_id FROM sys_menu WHERE parent_id = @demoRootId AND perms = 'xms:demoPledgeLevelConfig:list' LIMIT 1;

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT '示例质押等级配置查询', @demoLevelMenuId, 1, '#', '', 1, 0, 'F', '0', '0', 'xms:demoPledgeLevelConfig:query', '#', 'admin', sysdate(), '', null, ''
WHERE @demoLevelMenuId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @demoLevelMenuId AND perms = 'xms:demoPledgeLevelConfig:query');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT '示例质押等级配置新增', @demoLevelMenuId, 2, '#', '', 1, 0, 'F', '0', '0', 'xms:demoPledgeLevelConfig:add', '#', 'admin', sysdate(), '', null, ''
WHERE @demoLevelMenuId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @demoLevelMenuId AND perms = 'xms:demoPledgeLevelConfig:add');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT '示例质押等级配置修改', @demoLevelMenuId, 3, '#', '', 1, 0, 'F', '0', '0', 'xms:demoPledgeLevelConfig:edit', '#', 'admin', sysdate(), '', null, ''
WHERE @demoLevelMenuId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @demoLevelMenuId AND perms = 'xms:demoPledgeLevelConfig:edit');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT '示例质押等级配置删除', @demoLevelMenuId, 4, '#', '', 1, 0, 'F', '0', '0', 'xms:demoPledgeLevelConfig:remove', '#', 'admin', sysdate(), '', null, ''
WHERE @demoLevelMenuId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @demoLevelMenuId AND perms = 'xms:demoPledgeLevelConfig:remove');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT '示例质押等级配置导出', @demoLevelMenuId, 5, '#', '', 1, 0, 'F', '0', '0', 'xms:demoPledgeLevelConfig:export', '#', 'admin', sysdate(), '', null, ''
WHERE @demoLevelMenuId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @demoLevelMenuId AND perms = 'xms:demoPledgeLevelConfig:export');
