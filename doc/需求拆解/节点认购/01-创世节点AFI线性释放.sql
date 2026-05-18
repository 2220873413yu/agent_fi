CREATE TABLE IF NOT EXISTS `t_node_package_release_order` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `release_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL COMMENT '释放订单号',
  `node_order_id` bigint NOT NULL COMMENT '来源节点认购订单ID',
  `node_order_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci DEFAULT NULL COMMENT '来源节点认购订单号',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `address` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci DEFAULT NULL COMMENT '钱包地址快照',
  `package_level` int NOT NULL DEFAULT 0 COMMENT '节点等级快照',
  `order_value_usdt` decimal(20,6) NOT NULL DEFAULT '0.000000' COMMENT '来源节点订单金额USDT',
  `weight_multiplier` decimal(20,6) NOT NULL DEFAULT '0.000000' COMMENT '订单权重快照',
  `total_weight` decimal(20,6) NOT NULL DEFAULT '0.000000' COMMENT '初始化时全网总权重',
  `amount_per_weight` decimal(20,6) NOT NULL DEFAULT '0.000000' COMMENT '初始化时每1权重可分AFI',
  `total_release_amount` decimal(20,6) NOT NULL DEFAULT '0.000000' COMMENT '总释放AFI',
  `daily_release_amount` decimal(20,6) NOT NULL DEFAULT '0.000000' COMMENT '每日释放AFI',
  `released_amount` decimal(20,6) NOT NULL DEFAULT '0.000000' COMMENT '已释放AFI',
  `remaining_amount` decimal(20,6) NOT NULL DEFAULT '0.000000' COMMENT '剩余待释放AFI',
  `total_days` int NOT NULL DEFAULT 365 COMMENT '总释放天数',
  `run_days` int NOT NULL DEFAULT 0 COMMENT '已释放天数',
  `last_release_day` int DEFAULT NULL COMMENT '最后释放日期，格式yyyyMMdd',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态 0待释放 1释放中 2释放完成 3异常',
  `init_batch_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci DEFAULT NULL COMMENT '初始化批次号',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci DEFAULT NULL COMMENT '备注',
  `deleted` int NOT NULL DEFAULT 0 COMMENT '删除标志 0正常 1删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_node_order_id` (`node_order_id`) USING BTREE,
  UNIQUE KEY `uk_release_no` (`release_no`) USING BTREE,
  KEY `idx_user_id` (`user_id`) USING BTREE,
  KEY `idx_node_order_no` (`node_order_no`) USING BTREE,
  KEY `idx_status_last_day` (`status`, `last_release_day`) USING BTREE,
  KEY `idx_init_batch_no` (`init_batch_no`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci ROW_FORMAT=DYNAMIC COMMENT='节点认购AFI线性释放订单';

INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '节点认购AFI释放订单状态', 't_node_package_release_order_status', '0', 'admin', sysdate(), '状态 0待释放 1释放中 2释放完成 3异常'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 't_node_package_release_order_status');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 0, '待释放', '0', 't_node_package_release_order_status', '', 'info', 'Y', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 't_node_package_release_order_status' AND dict_value = '0');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1, '释放中', '1', 't_node_package_release_order_status', '', 'primary', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 't_node_package_release_order_status' AND dict_value = '1');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 2, '释放完成', '2', 't_node_package_release_order_status', '', 'success', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 't_node_package_release_order_status' AND dict_value = '2');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 3, '异常', '3', 't_node_package_release_order_status', '', 'danger', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 't_node_package_release_order_status' AND dict_value = '3');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 45, '节点认购AFI线性释放', '45', 't_user_money_log_source_type', '', 'success', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 't_user_money_log_source_type' AND dict_value = '45');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 32, '节点认购AFI线性释放', '32', 'reward_record_source_type', '', 'success', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'reward_record_source_type' AND dict_value = '32');

INSERT INTO sys_job (job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status, create_by, create_time, remark)
SELECT '初始化节点认购AFI线性释放订单', 'DEFAULT', 'xmsTask.initNodePackageReleaseOrders', '0 0 3 1 1 ?', '3', '1', '1', 'admin', sysdate(), '一次性初始化任务，建议人工确认后手动执行；重复执行按node_order_id幂等跳过'
WHERE NOT EXISTS (SELECT 1 FROM sys_job WHERE invoke_target = 'xmsTask.initNodePackageReleaseOrders');

INSERT INTO sys_job (job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status, create_by, create_time, remark)
SELECT '每日释放节点认购AFI', 'DEFAULT', 'xmsTask.releaseNodePackageAfiDaily', '0 5 0 * * ?', '3', '1', '0', 'admin', sysdate(), '每天释放节点认购AFI到用户AFI钱包valid_num2'
WHERE NOT EXISTS (SELECT 1 FROM sys_job WHERE invoke_target = 'xmsTask.releaseNodePackageAfiDaily');

SET @nodeReleaseParentId := 2375;

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT '节点线性释放订单', @nodeReleaseParentId, 3, 'nodePackageReleaseOrder', 'xms/nodePackageReleaseOrder/index', 1, 0, 'C', '0', '0', 'xms:nodePackageReleaseOrder:list', 'log', 'admin', sysdate(), '', null, '节点线性释放订单菜单'
WHERE NOT EXISTS (
  SELECT 1 FROM sys_menu WHERE parent_id = @nodeReleaseParentId AND path = 'nodePackageReleaseOrder' AND menu_type = 'C'
);

SELECT @nodeReleaseMenuId := menu_id
FROM sys_menu
WHERE parent_id = @nodeReleaseParentId AND path = 'nodePackageReleaseOrder' AND menu_type = 'C'
LIMIT 1;

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT '节点线性释放订单查询', @nodeReleaseMenuId, 1, '#', '', 1, 0, 'F', '0', '0', 'xms:nodePackageReleaseOrder:query', '#', 'admin', sysdate(), '', null, ''
WHERE NOT EXISTS (
  SELECT 1 FROM sys_menu WHERE parent_id = @nodeReleaseMenuId AND perms = 'xms:nodePackageReleaseOrder:query'
);

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT '节点线性释放订单导出', @nodeReleaseMenuId, 2, '#', '', 1, 0, 'F', '0', '0', 'xms:nodePackageReleaseOrder:export', '#', 'admin', sysdate(), '', null, ''
WHERE NOT EXISTS (
  SELECT 1 FROM sys_menu WHERE parent_id = @nodeReleaseMenuId AND perms = 'xms:nodePackageReleaseOrder:export'
);
