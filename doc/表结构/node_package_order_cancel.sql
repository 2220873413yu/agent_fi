CREATE TABLE IF NOT EXISTS `t_node_package_order_cancel` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `origin_order_id` bigint NOT NULL COMMENT '原节点订单id',
  `order_no` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci DEFAULT NULL COMMENT '订单号',
  `user_id` bigint NOT NULL COMMENT '用户id',
  `address` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci DEFAULT NULL COMMENT '钱包地址',
  `hash` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci DEFAULT NULL COMMENT '支付hash',
  `package_level` int NOT NULL DEFAULT '0' COMMENT '下单时节点等级快照',
  `direct_referral_rate` decimal(5,2) NOT NULL DEFAULT '0.00' COMMENT '下单时直推奖励比例快照(%)',
  `indirect_referral_rate` decimal(5,2) DEFAULT '0.00' COMMENT '下单时间推奖励比例快照(%)，无则NULL',
  `weight_multiplier` decimal(10,2) NOT NULL DEFAULT '1.00' COMMENT '下单时权重系数快照(倍数)',
  `pred_order_fee_relief_rate` decimal(5,2) NOT NULL DEFAULT '0.00' COMMENT '下单时预测下单手续费减免比例快照(%)',
  `order_value_usdt` decimal(20,6) DEFAULT '0.000000' COMMENT '支付金额',
  `source_type` int DEFAULT '0' COMMENT '订单来源 0:购买,1:后台拨付',
  `status` int DEFAULT '0' COMMENT '原订单状态 0:未支付,1:支付成功',
  `biz_status` int DEFAULT '0' COMMENT '原业务处理状态 0:未处理,1:已处理',
  `create_time` datetime DEFAULT NULL COMMENT '原订单创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '原订单修改时间',
  `pay_time` datetime DEFAULT NULL COMMENT '原订单支付时间',
  `cancel_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '取消时间',
  `cancel_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci DEFAULT NULL COMMENT '取消操作人',
  `cancel_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci DEFAULT NULL COMMENT '取消原因',
  `release_order_id` bigint DEFAULT NULL COMMENT '线性释放订单id',
  `release_status_before` int DEFAULT NULL COMMENT '取消前释放订单状态',
  `released_amount_snapshot` decimal(20,6) DEFAULT NULL COMMENT '取消时已释放AFI快照',
  `remaining_amount_snapshot` decimal(20,6) DEFAULT NULL COMMENT '取消时剩余待释放AFI快照',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_origin_order_id` (`origin_order_id`) USING BTREE,
  KEY `idx_user_id` (`user_id`) USING BTREE,
  KEY `idx_order_no` (`order_no`) USING BTREE,
  KEY `idx_cancel_time` (`cancel_time`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci ROW_FORMAT=DYNAMIC COMMENT='节点套餐取消订单归档';

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 4, '已暂停', '4', 't_node_package_release_order_status', '', 'warning', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 't_node_package_release_order_status' AND dict_value = '4');

-- 节点取消订单归档菜单。默认挂到“节点管理”菜单下；如果你的环境父菜单ID不同，请先修改该变量。
SET @nodeOrderCancelParentId := 2375;

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT '节点取消订单归档', @nodeOrderCancelParentId, 4, 'nodePackageOrderCancel', 'xms/nodePackageOrderCancel/index', 1, 0, 'C', '0', '0', 'xms:nodePackageOrderCancel:list', 'log', 'admin', sysdate(), '', null, '节点取消订单归档菜单'
WHERE NOT EXISTS (
  SELECT 1 FROM sys_menu WHERE parent_id = @nodeOrderCancelParentId AND path = 'nodePackageOrderCancel' AND menu_type = 'C'
);

SELECT @nodeOrderCancelMenuId := menu_id
FROM sys_menu
WHERE parent_id = @nodeOrderCancelParentId AND path = 'nodePackageOrderCancel' AND menu_type = 'C'
LIMIT 1;

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT '节点取消订单归档查询', @nodeOrderCancelMenuId, 1, '#', '', 1, 0, 'F', '0', '0', 'xms:nodePackageOrderCancel:query', '#', 'admin', sysdate(), '', null, ''
WHERE NOT EXISTS (
  SELECT 1 FROM sys_menu WHERE parent_id = @nodeOrderCancelMenuId AND perms = 'xms:nodePackageOrderCancel:query'
);

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT '节点取消订单归档导出', @nodeOrderCancelMenuId, 2, '#', '', 1, 0, 'F', '0', '0', 'xms:nodePackageOrderCancel:export', '#', 'admin', sysdate(), '', null, ''
WHERE NOT EXISTS (
  SELECT 1 FROM sys_menu WHERE parent_id = @nodeOrderCancelMenuId AND perms = 'xms:nodePackageOrderCancel:export'
);
