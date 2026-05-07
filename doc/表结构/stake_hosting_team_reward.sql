-- 质押托管团队奖励与服务费结算
-- 执行前确认已完成 stake_hosting.sql 和 stake_hosting_level_recalculate.sql。

ALTER TABLE `t_stake_hosting_package`
  ADD COLUMN `service_fee_ratio` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '服务费比例，单位%' AFTER `min_amount`;

ALTER TABLE `t_user_level_config`
  ADD COLUMN `team_reward_ratio` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '团队奖励比例，单位%' AFTER `community_performance`,
  ADD COLUMN `global_fee_dividend_ratio` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '全球手续费分红比例，单位%' AFTER `team_reward_ratio`;

CREATE TABLE IF NOT EXISTS `t_stake_hosting_reward_settlement` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `settlement_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL COMMENT '结算单号',
  `source_order_id` bigint DEFAULT NULL COMMENT '源托管订单ID',
  `source_order_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci DEFAULT NULL COMMENT '源托管订单号',
  `source_user_id` bigint DEFAULT NULL COMMENT '源用户ID',
  `receive_user_id` bigint DEFAULT NULL COMMENT '接收用户ID',
  `reward_type` int NOT NULL DEFAULT 0 COMMENT '奖励类型 1:静态服务费结算 2:直推奖 3:极差奖 4:平级奖 5:平台沉淀',
  `reward_level` int DEFAULT NULL COMMENT '奖励等级',
  `reward_base_amount` decimal(20,6) NOT NULL DEFAULT 0.000000 COMMENT '奖励基数',
  `reward_ratio` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '奖励比例，单位%',
  `reward_amount` decimal(20,6) NOT NULL DEFAULT 0.000000 COMMENT '奖励金额',
  `gross_static_reward` decimal(20,6) NOT NULL DEFAULT 0.000000 COMMENT '静态毛收益',
  `service_fee_ratio` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '服务费比例，单位%',
  `service_fee_amount` decimal(20,6) NOT NULL DEFAULT 0.000000 COMMENT '服务费金额',
  `net_static_reward` decimal(20,6) NOT NULL DEFAULT 0.000000 COMMENT '静态净收益',
  `arrival_status` int NOT NULL DEFAULT 0 COMMENT '到账状态 0:未到账 1:已到账',
  `skip_reason` int DEFAULT NULL COMMENT '未到账原因 1:无上级 2:无有效托管订单 3:后台拨付订单不触发 4:无效用户',
  `settlement_day` int NOT NULL COMMENT '结算日期，格式yyyyMMdd',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci DEFAULT NULL COMMENT '备注',
  `deleted` int NOT NULL DEFAULT 0 COMMENT '删除标志 0正常 1删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_settlement_no` (`settlement_no`) USING BTREE,
  KEY `idx_source_order_no` (`source_order_no`) USING BTREE,
  KEY `idx_source_user_id` (`source_user_id`) USING BTREE,
  KEY `idx_receive_user_id` (`receive_user_id`) USING BTREE,
  KEY `idx_reward_type` (`reward_type`) USING BTREE,
  KEY `idx_settlement_day` (`settlement_day`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci COMMENT='托管奖励结算明细表' ROW_FORMAT=DYNAMIC;

INSERT INTO t_sys_para (para_code, para_value, para_desc, visible, create_time, active_flag, remark)
SELECT 'biz_stake_hosting_direct_reward_ratio', '10', '托管直推奖励比例，例如10表示10%', '0', sysdate(), 1, '托管团队奖励参数'
WHERE NOT EXISTS (SELECT 1 FROM t_sys_para WHERE para_code = 'biz_stake_hosting_direct_reward_ratio');

UPDATE `t_user_level_config` SET `team_reward_ratio` = 5.00, `global_fee_dividend_ratio` = 30.00 WHERE `level` = 1;
UPDATE `t_user_level_config` SET `team_reward_ratio` = 10.00, `global_fee_dividend_ratio` = 20.00 WHERE `level` = 2;
UPDATE `t_user_level_config` SET `team_reward_ratio` = 15.00, `global_fee_dividend_ratio` = 16.00 WHERE `level` = 3;
UPDATE `t_user_level_config` SET `team_reward_ratio` = 20.00, `global_fee_dividend_ratio` = 13.00 WHERE `level` = 4;
UPDATE `t_user_level_config` SET `team_reward_ratio` = 25.00, `global_fee_dividend_ratio` = 8.00 WHERE `level` = 5;
UPDATE `t_user_level_config` SET `team_reward_ratio` = 30.00, `global_fee_dividend_ratio` = 6.00 WHERE `level` = 6;
UPDATE `t_user_level_config` SET `team_reward_ratio` = 40.00, `global_fee_dividend_ratio` = 4.00 WHERE `level` = 7;
UPDATE `t_user_level_config` SET `team_reward_ratio` = 50.00, `global_fee_dividend_ratio` = 2.00 WHERE `level` = 8;
UPDATE `t_user_level_config` SET `team_reward_ratio` = 55.00, `global_fee_dividend_ratio` = 1.00 WHERE `level` = 9;

-- 奖励记录来源类型：托管团队奖励
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 28, '托管直推奖', '28', 'reward_record_source_type', '', 'success', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'reward_record_source_type' AND dict_value = '28');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 29, '托管极差奖', '29', 'reward_record_source_type', '', 'primary', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'reward_record_source_type' AND dict_value = '29');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 30, '托管平级奖', '30', 'reward_record_source_type', '', 'warning', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'reward_record_source_type' AND dict_value = '30');

-- 钱包流水来源类型：托管团队奖励
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 32, '托管直推奖', '32', 't_user_money_log_source_type', '', 'success', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 't_user_money_log_source_type' AND dict_value = '32');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 33, '托管极差奖', '33', 't_user_money_log_source_type', '', 'primary', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 't_user_money_log_source_type' AND dict_value = '33');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 34, '托管平级奖', '34', 't_user_money_log_source_type', '', 'warning', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 't_user_money_log_source_type' AND dict_value = '34');

-- 托管结算明细字典
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '托管结算奖励类型', 't_stake_hosting_reward_settlement_reward_type', '0', 'admin', sysdate(), '托管奖励结算明细奖励类型'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 't_stake_hosting_reward_settlement_reward_type');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1, '静态服务费结算', '1', 't_stake_hosting_reward_settlement_reward_type', '', 'info', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 't_stake_hosting_reward_settlement_reward_type' AND dict_value = '1');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 2, '直推奖', '2', 't_stake_hosting_reward_settlement_reward_type', '', 'success', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 't_stake_hosting_reward_settlement_reward_type' AND dict_value = '2');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 3, '极差奖', '3', 't_stake_hosting_reward_settlement_reward_type', '', 'primary', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 't_stake_hosting_reward_settlement_reward_type' AND dict_value = '3');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 4, '平级奖', '4', 't_stake_hosting_reward_settlement_reward_type', '', 'warning', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 't_stake_hosting_reward_settlement_reward_type' AND dict_value = '4');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 5, '平台沉淀', '5', 't_stake_hosting_reward_settlement_reward_type', '', 'danger', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 't_stake_hosting_reward_settlement_reward_type' AND dict_value = '5');

INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '托管结算到账状态', 't_stake_hosting_reward_settlement_arrival_status', '0', 'admin', sysdate(), '托管奖励结算明细到账状态'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 't_stake_hosting_reward_settlement_arrival_status');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 0, '未到账', '0', 't_stake_hosting_reward_settlement_arrival_status', '', 'info', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 't_stake_hosting_reward_settlement_arrival_status' AND dict_value = '0');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1, '已到账', '1', 't_stake_hosting_reward_settlement_arrival_status', '', 'success', 'Y', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 't_stake_hosting_reward_settlement_arrival_status' AND dict_value = '1');

INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '托管结算未到账原因', 't_stake_hosting_reward_settlement_skip_reason', '0', 'admin', sysdate(), '托管奖励结算明细未到账原因'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 't_stake_hosting_reward_settlement_skip_reason');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1, '无上级', '1', 't_stake_hosting_reward_settlement_skip_reason', '', 'info', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 't_stake_hosting_reward_settlement_skip_reason' AND dict_value = '1');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 2, '无有效托管订单', '2', 't_stake_hosting_reward_settlement_skip_reason', '', 'warning', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 't_stake_hosting_reward_settlement_skip_reason' AND dict_value = '2');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 3, '后台拨付订单不触发', '3', 't_stake_hosting_reward_settlement_skip_reason', '', 'danger', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 't_stake_hosting_reward_settlement_skip_reason' AND dict_value = '3');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 4, '无效用户', '4', 't_stake_hosting_reward_settlement_skip_reason', '', 'danger', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 't_stake_hosting_reward_settlement_skip_reason' AND dict_value = '4');

-- 奖励结算明细菜单 SQL：只读流水页
-- 如需调整目录位置，可修改 @stakeHostingRootParentId 和 @stakeHostingRootOrderNum。
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
