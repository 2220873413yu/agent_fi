CREATE TABLE IF NOT EXISTS `t_polymarket_order` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_no` varchar(64) NOT NULL COMMENT '内部订单号',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `account` varchar(100) DEFAULT NULL COMMENT '钱包地址快照',
  `event_slug` varchar(255) DEFAULT NULL COMMENT 'Polymarket事件slug快照',
  `event_title` varchar(500) DEFAULT NULL COMMENT 'Polymarket事件标题快照',
  `market_slug` varchar(255) NOT NULL COMMENT 'Polymarket市场slug',
  `biz_type` int NOT NULL DEFAULT 1 COMMENT '业务类型 1加密 2体育 3Up/Down',
  `market_id` varchar(64) DEFAULT NULL COMMENT 'Polymarket市场ID快照',
  `condition_id` varchar(100) DEFAULT NULL COMMENT 'Polymarket conditionId快照',
  `market_question` varchar(500) DEFAULT NULL COMMENT 'Polymarket市场问题快照',
  `outcome_index` int NOT NULL COMMENT '选择结果下标',
  `outcome_name` varchar(100) NOT NULL COMMENT '选择结果名称',
  `afi_amount` decimal(20,6) NOT NULL DEFAULT '0.000000' COMMENT '下单AFI数量',
  `afi_price` decimal(20,6) NOT NULL DEFAULT '0.000000' COMMENT 'AFI价格快照，单位USDT',
  `afi_usdt_amount` decimal(20,6) NOT NULL DEFAULT '0.000000' COMMENT 'AFI等值USDT',
  `outcome_price` decimal(20,8) NOT NULL DEFAULT '0.00000000' COMMENT 'Polymarket outcome成交价格',
  `share_amount` decimal(24,8) NOT NULL DEFAULT '0.00000000' COMMENT '购买份额',
  `max_payout_usdt` decimal(24,8) NOT NULL DEFAULT '0.00000000' COMMENT '最大兑付USDT',
  `end_time` datetime DEFAULT NULL COMMENT '市场结束时间快照',
  `status` int NOT NULL DEFAULT 0 COMMENT '订单状态 0待结算 1已猜中 2未猜中 3待人工复核 4已作废/已退款',
  `result_outcome_index` int DEFAULT NULL COMMENT '赢家结果下标',
  `result_outcome_name` varchar(100) DEFAULT NULL COMMENT '赢家结果名称',
  `payout_usdt_amount` decimal(24,8) NOT NULL DEFAULT '0.00000000' COMMENT '中奖应兑付USDT等值',
  `payout_afi_price` decimal(20,6) NOT NULL DEFAULT '0.000000' COMMENT '结算时AFI/USDT价格快照',
  `payout_afi_amount` decimal(24,8) NOT NULL DEFAULT '0.00000000' COMMENT '实际发放AFI数量',
  `settle_time` datetime DEFAULT NULL COMMENT '结算时间',
  `order_snapshot_json` longtext COMMENT '下单时Polymarket市场快照JSON',
  `settle_snapshot_json` longtext COMMENT '结算时Polymarket市场快照JSON',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `deleted` int NOT NULL DEFAULT 0 COMMENT '删除标志 0正常 1删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_order_no` (`order_no`) USING BTREE,
  KEY `idx_user_id` (`user_id`) USING BTREE,
  KEY `idx_market_slug` (`market_slug`) USING BTREE,
  KEY `idx_biz_type` (`biz_type`) USING BTREE,
  KEY `idx_status_end_time` (`status`,`end_time`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci ROW_FORMAT=DYNAMIC COMMENT='Polymarket内部订单表';

CREATE TABLE IF NOT EXISTS `t_polymarket_market` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `market_slug` varchar(255) NOT NULL COMMENT 'Polymarket市场slug，市场级结算唯一键',
  `market_id` varchar(64) DEFAULT NULL COMMENT 'Polymarket市场ID快照',
  `condition_id` varchar(100) DEFAULT NULL COMMENT 'Polymarket conditionId快照',
  `event_slug` varchar(255) DEFAULT NULL COMMENT 'Polymarket事件slug快照',
  `event_title` varchar(500) DEFAULT NULL COMMENT 'Polymarket事件标题快照',
  `market_question` varchar(500) DEFAULT NULL COMMENT 'Polymarket市场问题快照',
  `end_time` datetime DEFAULT NULL COMMENT '市场结束时间',
  `status` int NOT NULL DEFAULT 0 COMMENT '市场状态 0待结算 1结算中 2结算完成 3待人工复核',
  `uma_resolution_status` varchar(50) DEFAULT NULL COMMENT 'Polymarket UMA结算状态快照',
  `result_outcome_index` int DEFAULT NULL COMMENT '赢家结果下标',
  `result_outcome_name` varchar(100) DEFAULT NULL COMMENT '赢家结果名称',
  `order_count` int NOT NULL DEFAULT 0 COMMENT '市场总下单次数',
  `total_afi_amount` decimal(20,6) NOT NULL DEFAULT '0.000000' COMMENT '市场总下单AFI',
  `total_usdt_amount` decimal(20,6) NOT NULL DEFAULT '0.000000' COMMENT '市场总下单等值USDT',
  `total_share_amount` decimal(24,8) NOT NULL DEFAULT '0.00000000' COMMENT '市场总购买份额',
  `total_payout_usdt_amount` decimal(24,8) NOT NULL DEFAULT '0.00000000' COMMENT '市场中奖应兑付USDT等值',
  `total_payout_afi_amount` decimal(24,8) NOT NULL DEFAULT '0.00000000' COMMENT '市场实际总发放AFI数量',
  `last_check_time` datetime DEFAULT NULL COMMENT '上次查询Polymarket结果时间',
  `settle_time` datetime DEFAULT NULL COMMENT '市场结算完成时间',
  `market_snapshot_json` longtext COMMENT '最新Polymarket市场快照JSON',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `deleted` int NOT NULL DEFAULT 0 COMMENT '删除标志 0正常 1删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_market_slug` (`market_slug`) USING BTREE,
  KEY `idx_status_end_time` (`status`,`end_time`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci ROW_FORMAT=DYNAMIC COMMENT='Polymarket市场聚合结算表';

INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT 'Polymarket订单状态', 't_polymarket_order_status', '0', 'admin', sysdate(), 'Polymarket内部订单状态'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 't_polymarket_order_status');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 0, '待结算', '0', 't_polymarket_order_status', '', 'info', 'Y', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 't_polymarket_order_status' AND dict_value = '0');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1, '已猜中', '1', 't_polymarket_order_status', '', 'success', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 't_polymarket_order_status' AND dict_value = '1');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 2, '未猜中', '2', 't_polymarket_order_status', '', 'danger', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 't_polymarket_order_status' AND dict_value = '2');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 3, '待人工复核', '3', 't_polymarket_order_status', '', 'warning', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 't_polymarket_order_status' AND dict_value = '3');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 4, '已作废/已退款', '4', 't_polymarket_order_status', '', 'default', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 't_polymarket_order_status' AND dict_value = '4');

INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT 'Polymarket订单业务类型', 't_polymarket_order_biz_type', '0', 'admin', sysdate(), 'Polymarket订单业务类型 1加密 2体育 3Up/Down'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 't_polymarket_order_biz_type');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1, '加密', '1', 't_polymarket_order_biz_type', '', 'primary', 'Y', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 't_polymarket_order_biz_type' AND dict_value = '1');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 2, '体育', '2', 't_polymarket_order_biz_type', '', 'success', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 't_polymarket_order_biz_type' AND dict_value = '2');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 3, 'Up/Down', '3', 't_polymarket_order_biz_type', '', 'warning', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 't_polymarket_order_biz_type' AND dict_value = '3');

INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT 'Polymarket市场状态', 't_polymarket_market_status', '0', 'admin', sysdate(), 'Polymarket市场聚合结算状态'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 't_polymarket_market_status');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 0, '待结算', '0', 't_polymarket_market_status', '', 'info', 'Y', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 't_polymarket_market_status' AND dict_value = '0');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1, '结算中', '1', 't_polymarket_market_status', '', 'warning', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 't_polymarket_market_status' AND dict_value = '1');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 2, '结算完成', '2', 't_polymarket_market_status', '', 'success', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 't_polymarket_market_status' AND dict_value = '2');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 3, '待人工复核', '3', 't_polymarket_market_status', '', 'danger', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 't_polymarket_market_status' AND dict_value = '3');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 40, 'Polymarket下单扣减AFI', '40', 't_user_money_log_source_type', '', 'warning', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 't_user_money_log_source_type' AND dict_value = '40');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 41, 'Polymarket猜中兑付AFI', '41', 't_user_money_log_source_type', '', 'success', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 't_user_money_log_source_type' AND dict_value = '41');

INSERT INTO t_sys_para (para_code, para_value, para_desc, visible, create_time, active_flag, remark)
SELECT 'POLYMARKET_MIN_ORDER_AFI_AMOUNT', '10', 'Polymarket内部下单最低AFI数量', '0', sysdate(), 1, 'Polymarket内部订单参数'
WHERE NOT EXISTS (SELECT 1 FROM t_sys_para WHERE para_code = 'POLYMARKET_MIN_ORDER_AFI_AMOUNT');

INSERT INTO sys_job (job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status, create_by, create_time, remark)
SELECT 'Polymarket内部市场派发', 'DEFAULT', 'xmsTask.settlePolymarketOrders', '0/10 * * * * ?', '3', '1', '0', 'admin', sysdate(), '每10秒扫描已结束的Polymarket市场并派发为结算中'
WHERE NOT EXISTS (SELECT 1 FROM sys_job WHERE invoke_target = 'xmsTask.settlePolymarketOrders');

SET @polymarketParentId := 0;

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT 'Polymarket管理', @polymarketParentId, 95, 'polymarket', 'Layout', 1, 0, 'M', '0', '0', '', 'chart', 'admin', sysdate(), '', null, 'Polymarket管理目录'
WHERE NOT EXISTS (
  SELECT 1 FROM sys_menu WHERE menu_name = 'Polymarket管理' AND parent_id = @polymarketParentId AND menu_type = 'M'
);

SELECT @polymarketMenuRootId := menu_id FROM sys_menu WHERE menu_name = 'Polymarket管理' AND parent_id = @polymarketParentId AND menu_type = 'M' LIMIT 1;

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT 'Polymarket订单', @polymarketMenuRootId, 1, 'polymarketOrder', 'xms/polymarketOrder/index', 1, 0, 'C', '0', '0', 'xms:polymarketOrder:list', 'log', 'admin', sysdate(), '', null, 'Polymarket订单菜单'
WHERE NOT EXISTS (
  SELECT 1 FROM sys_menu WHERE parent_id = @polymarketMenuRootId AND perms = 'xms:polymarketOrder:list'
);

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT 'Polymarket市场', @polymarketMenuRootId, 2, 'polymarketMarket', 'xms/polymarketMarket/index', 1, 0, 'C', '0', '0', 'xms:polymarketMarket:list', 'chart', 'admin', sysdate(), '', null, 'Polymarket市场聚合菜单'
WHERE NOT EXISTS (
  SELECT 1 FROM sys_menu WHERE parent_id = @polymarketMenuRootId AND perms = 'xms:polymarketMarket:list'
);

SELECT @polymarketOrderMenuId := menu_id FROM sys_menu WHERE parent_id = @polymarketMenuRootId AND perms = 'xms:polymarketOrder:list' LIMIT 1;

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT 'Polymarket订单查询', @polymarketOrderMenuId, 1, '#', '', 1, 0, 'F', '0', '0', 'xms:polymarketOrder:query', '#', 'admin', sysdate(), '', null, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @polymarketOrderMenuId AND perms = 'xms:polymarketOrder:query');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT 'Polymarket订单复核', @polymarketOrderMenuId, 2, '#', '', 1, 0, 'F', '0', '0', 'xms:polymarketOrder:edit', '#', 'admin', sysdate(), '', null, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @polymarketOrderMenuId AND perms = 'xms:polymarketOrder:edit');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT 'Polymarket订单导出', @polymarketOrderMenuId, 3, '#', '', 1, 0, 'F', '0', '0', 'xms:polymarketOrder:export', '#', 'admin', sysdate(), '', null, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @polymarketOrderMenuId AND perms = 'xms:polymarketOrder:export');

SELECT @polymarketMarketMenuId := menu_id FROM sys_menu WHERE parent_id = @polymarketMenuRootId AND perms = 'xms:polymarketMarket:list' LIMIT 1;

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT 'Polymarket市场查询', @polymarketMarketMenuId, 1, '#', '', 1, 0, 'F', '0', '0', 'xms:polymarketMarket:query', '#', 'admin', sysdate(), '', null, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @polymarketMarketMenuId AND perms = 'xms:polymarketMarket:query');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT 'Polymarket市场结算', @polymarketMarketMenuId, 2, '#', '', 1, 0, 'F', '0', '0', 'xms:polymarketMarket:edit', '#', 'admin', sysdate(), '', null, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @polymarketMarketMenuId AND perms = 'xms:polymarketMarket:edit');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT 'Polymarket市场导出', @polymarketMarketMenuId, 3, '#', '', 1, 0, 'F', '0', '0', 'xms:polymarketMarket:export', '#', 'admin', sysdate(), '', null, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @polymarketMarketMenuId AND perms = 'xms:polymarketMarket:export');
