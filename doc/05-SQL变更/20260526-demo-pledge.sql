-- 示例质押模块：表结构、字典和菜单
-- 执行前说明：
-- 1. @demoRootParentId 默认放在根目录下；如果要挂到已有业务目录，请改成对应 sys_menu.menu_id。
-- 2. 本模块是初始化项目样板，不代表真实生产质押规则。

CREATE TABLE IF NOT EXISTS `t_demo_pledge_package` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `package_name` varchar(100) NOT NULL COMMENT '套餐名称',
  `pledge_usdt_amount` decimal(20,6) NOT NULL DEFAULT '0.000000' COMMENT '质押USDT金额',
  `release_days` int NOT NULL DEFAULT '1' COMMENT '释放天数',
  `daily_rate` decimal(10,4) NOT NULL DEFAULT '0.0000' COMMENT '日利率，单位%',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态 0停用 1启用',
  `sort` int NOT NULL DEFAULT '0' COMMENT '排序',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `deleted` int NOT NULL DEFAULT '0' COMMENT '删除标记 0正常 1删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='示例质押套餐表';

CREATE TABLE IF NOT EXISTS `t_demo_pledge_order` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_no` varchar(64) NOT NULL COMMENT '订单号',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `package_id` bigint NOT NULL COMMENT '套餐ID',
  `package_name` varchar(100) NOT NULL COMMENT '套餐名称快照',
  `pledge_usdt_amount` decimal(20,6) NOT NULL DEFAULT '0.000000' COMMENT '质押USDT金额',
  `release_days` int NOT NULL DEFAULT '1' COMMENT '释放天数快照',
  `daily_rate` decimal(10,4) NOT NULL DEFAULT '0.0000' COMMENT '日利率快照，单位%',
  `released_days` int NOT NULL DEFAULT '0' COMMENT '已释放天数',
  `reward_status` tinyint NOT NULL DEFAULT '0' COMMENT '收益释放状态 0释放中 1已完成',
  `total_reward_usdt_amount` decimal(20,6) NOT NULL DEFAULT '0.000000' COMMENT '累计收益USDT金额',
  `last_reward_day` int DEFAULT NULL COMMENT '最近释放日期yyyyMMdd',
  `last_reward_time` datetime DEFAULT NULL COMMENT '最近释放时间',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态 0待支付 1已支付 2处理中 3已完成 4失败',
  `pay_time` datetime DEFAULT NULL COMMENT '支付时间',
  `process_time` datetime DEFAULT NULL COMMENT '开始处理时间',
  `finish_time` datetime DEFAULT NULL COMMENT '完成时间',
  `fail_reason` varchar(500) DEFAULT NULL COMMENT '失败原因',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `deleted` int NOT NULL DEFAULT '0' COMMENT '删除标记 0正常 1删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`),
  KEY `idx_reward_status_day` (`reward_status`, `last_reward_day`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='示例质押订单表';

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
SELECT '示例质押套餐状态', 't_demo_pledge_package_status', '0', 'admin', sysdate(), '示例质押套餐状态'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 't_demo_pledge_package_status');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 0, '停用', '0', 't_demo_pledge_package_status', '', 'danger', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 't_demo_pledge_package_status' AND dict_value = '0');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1, '启用', '1', 't_demo_pledge_package_status', '', 'success', 'Y', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 't_demo_pledge_package_status' AND dict_value = '1');

INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '示例质押订单状态', 't_demo_pledge_order_status', '0', 'admin', sysdate(), '示例质押订单状态'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 't_demo_pledge_order_status');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 0, '待支付', '0', 't_demo_pledge_order_status', '', 'info', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 't_demo_pledge_order_status' AND dict_value = '0');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1, '已支付', '1', 't_demo_pledge_order_status', '', 'primary', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 't_demo_pledge_order_status' AND dict_value = '1');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 2, '处理中', '2', 't_demo_pledge_order_status', '', 'warning', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 't_demo_pledge_order_status' AND dict_value = '2');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 3, '已完成', '3', 't_demo_pledge_order_status', '', 'success', 'Y', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 't_demo_pledge_order_status' AND dict_value = '3');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 4, '失败', '4', 't_demo_pledge_order_status', '', 'danger', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 't_demo_pledge_order_status' AND dict_value = '4');

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

SET @demoRootParentId := 0;
SET @demoRootOrderNum := 95;

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT '示例业务', @demoRootParentId, @demoRootOrderNum, 'demoBusiness', 'Layout', 1, 0, 'M', '0', '0', '', 'example', 'admin', sysdate(), '', null, '示例业务目录'
WHERE NOT EXISTS (
  SELECT 1 FROM sys_menu WHERE menu_name = '示例业务' AND parent_id = @demoRootParentId AND menu_type = 'M'
);

SELECT @demoRootId := menu_id FROM sys_menu WHERE menu_name = '示例业务' AND parent_id = @demoRootParentId AND menu_type = 'M' LIMIT 1;

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT '示例质押套餐', @demoRootId, 1, 'demoPledgePackage', 'xms/demoPledgePackage/index', 1, 0, 'C', '0', '0', 'xms:demoPledgePackage:list', 'form', 'admin', sysdate(), '', null, '示例质押套餐菜单'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @demoRootId AND perms = 'xms:demoPledgePackage:list');

SELECT @demoPackageMenuId := menu_id FROM sys_menu WHERE parent_id = @demoRootId AND perms = 'xms:demoPledgePackage:list' LIMIT 1;

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT '示例质押套餐查询', @demoPackageMenuId, 1, '#', '', 1, 0, 'F', '0', '0', 'xms:demoPledgePackage:query', '#', 'admin', sysdate(), '', null, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @demoPackageMenuId AND perms = 'xms:demoPledgePackage:query');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT '示例质押套餐新增', @demoPackageMenuId, 2, '#', '', 1, 0, 'F', '0', '0', 'xms:demoPledgePackage:add', '#', 'admin', sysdate(), '', null, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @demoPackageMenuId AND perms = 'xms:demoPledgePackage:add');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT '示例质押套餐修改', @demoPackageMenuId, 3, '#', '', 1, 0, 'F', '0', '0', 'xms:demoPledgePackage:edit', '#', 'admin', sysdate(), '', null, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @demoPackageMenuId AND perms = 'xms:demoPledgePackage:edit');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT '示例质押套餐删除', @demoPackageMenuId, 4, '#', '', 1, 0, 'F', '0', '0', 'xms:demoPledgePackage:remove', '#', 'admin', sysdate(), '', null, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @demoPackageMenuId AND perms = 'xms:demoPledgePackage:remove');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT '示例质押套餐导出', @demoPackageMenuId, 5, '#', '', 1, 0, 'F', '0', '0', 'xms:demoPledgePackage:export', '#', 'admin', sysdate(), '', null, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @demoPackageMenuId AND perms = 'xms:demoPledgePackage:export');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT '示例质押订单', @demoRootId, 2, 'demoPledgeOrder', 'xms/demoPledgeOrder/index', 1, 0, 'C', '0', '0', 'xms:demoPledgeOrder:list', 'log', 'admin', sysdate(), '', null, '示例质押订单菜单'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @demoRootId AND perms = 'xms:demoPledgeOrder:list');

SELECT @demoOrderMenuId := menu_id FROM sys_menu WHERE parent_id = @demoRootId AND perms = 'xms:demoPledgeOrder:list' LIMIT 1;

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT '示例质押订单查询', @demoOrderMenuId, 1, '#', '', 1, 0, 'F', '0', '0', 'xms:demoPledgeOrder:query', '#', 'admin', sysdate(), '', null, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @demoOrderMenuId AND perms = 'xms:demoPledgeOrder:query');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT '示例质押订单购买', @demoOrderMenuId, 2, '#', '', 1, 0, 'F', '0', '0', 'xms:demoPledgeOrder:buy', '#', 'admin', sysdate(), '', null, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @demoOrderMenuId AND perms = 'xms:demoPledgeOrder:buy');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT '示例质押订单导出', @demoOrderMenuId, 3, '#', '', 1, 0, 'F', '0', '0', 'xms:demoPledgeOrder:export', '#', 'admin', sysdate(), '', null, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @demoOrderMenuId AND perms = 'xms:demoPledgeOrder:export');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT '示例质押等级配置', @demoRootId, 3, 'demoPledgeLevelConfig', 'xms/demoPledgeLevelConfig/index', 1, 0, 'C', '0', '0', 'xms:demoPledgeLevelConfig:list', 'tree', 'admin', sysdate(), '', null, '示例质押等级配置菜单'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @demoRootId AND perms = 'xms:demoPledgeLevelConfig:list');

SELECT @demoLevelMenuId := menu_id FROM sys_menu WHERE parent_id = @demoRootId AND perms = 'xms:demoPledgeLevelConfig:list' LIMIT 1;

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT '示例质押等级配置查询', @demoLevelMenuId, 1, '#', '', 1, 0, 'F', '0', '0', 'xms:demoPledgeLevelConfig:query', '#', 'admin', sysdate(), '', null, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @demoLevelMenuId AND perms = 'xms:demoPledgeLevelConfig:query');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT '示例质押等级配置新增', @demoLevelMenuId, 2, '#', '', 1, 0, 'F', '0', '0', 'xms:demoPledgeLevelConfig:add', '#', 'admin', sysdate(), '', null, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @demoLevelMenuId AND perms = 'xms:demoPledgeLevelConfig:add');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT '示例质押等级配置修改', @demoLevelMenuId, 3, '#', '', 1, 0, 'F', '0', '0', 'xms:demoPledgeLevelConfig:edit', '#', 'admin', sysdate(), '', null, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @demoLevelMenuId AND perms = 'xms:demoPledgeLevelConfig:edit');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT '示例质押等级配置删除', @demoLevelMenuId, 4, '#', '', 1, 0, 'F', '0', '0', 'xms:demoPledgeLevelConfig:remove', '#', 'admin', sysdate(), '', null, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @demoLevelMenuId AND perms = 'xms:demoPledgeLevelConfig:remove');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT '示例质押等级配置导出', @demoLevelMenuId, 5, '#', '', 1, 0, 'F', '0', '0', 'xms:demoPledgeLevelConfig:export', '#', 'admin', sysdate(), '', null, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @demoLevelMenuId AND perms = 'xms:demoPledgeLevelConfig:export');
