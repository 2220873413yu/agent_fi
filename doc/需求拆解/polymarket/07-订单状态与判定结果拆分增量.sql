-- Polymarket订单状态与判定结果拆分增量SQL
-- status：订单处理状态 0待结算 1已结算 2待人工复核 3已作废/已退款
-- resolved_status：判定结果 0未开奖 1赢 2输

SET @schema_name := DATABASE();

-- 1. 迁移旧数据：旧status=1/2表示赢/输，新口径迁移到resolved_status。
--    该脚本按“resolved_status为空”判断未迁移，执行前请确认历史数据尚未手动迁移。
UPDATE t_polymarket_order
SET
    resolved_status = CASE
        WHEN status = 1 THEN 1
        WHEN status = 2 THEN 2
        ELSE 0
    END,
    status = CASE
        WHEN status = 1 THEN 1
        WHEN status = 2 THEN 1
        WHEN status = 3 THEN 2
        WHEN status = 4 THEN 3
        ELSE status
    END
WHERE resolved_status IS NULL;

-- 2. 修改字段默认值和注释。
ALTER TABLE t_polymarket_order
    MODIFY COLUMN status int NOT NULL DEFAULT 0 COMMENT '订单状态 0待结算 1已结算 2待人工复核 3已作废/已退款';

ALTER TABLE t_polymarket_order
    MODIFY COLUMN resolved_status int NOT NULL DEFAULT 0 COMMENT '判定结果 0未开奖 1赢 2输';

-- 3. 补判定结果索引。
SET @sql := (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE t_polymarket_order ADD INDEX idx_resolved_status (resolved_status)',
        'SELECT 1'
    )
    FROM information_schema.statistics
    WHERE table_schema = @schema_name
      AND table_name = 't_polymarket_order'
      AND index_name = 'idx_resolved_status'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 4. 重建订单处理状态字典。
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT 'Polymarket订单状态', 't_polymarket_order_status', '0', 'admin', sysdate(), 'Polymarket内部订单处理状态'
WHERE NOT EXISTS (
    SELECT 1 FROM sys_dict_type WHERE dict_type = 't_polymarket_order_status'
);

DELETE FROM sys_dict_data WHERE dict_type = 't_polymarket_order_status';

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
VALUES
(0, '待结算', '0', 't_polymarket_order_status', '', 'info', 'Y', '0', 'admin', sysdate(), ''),
(1, '已结算', '1', 't_polymarket_order_status', '', 'success', 'N', '0', 'admin', sysdate(), ''),
(2, '待人工复核', '2', 't_polymarket_order_status', '', 'warning', 'N', '0', 'admin', sysdate(), ''),
(3, '已作废/已退款', '3', 't_polymarket_order_status', '', 'default', 'N', '0', 'admin', sysdate(), '');

-- 5. 新增判定结果字典。
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT 'Polymarket判定结果', 't_polymarket_order_resolved_status', '0', 'admin', sysdate(), 'Polymarket订单判定结果'
WHERE NOT EXISTS (
    SELECT 1 FROM sys_dict_type WHERE dict_type = 't_polymarket_order_resolved_status'
);

DELETE FROM sys_dict_data WHERE dict_type = 't_polymarket_order_resolved_status';

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
VALUES
(0, '未开奖', '0', 't_polymarket_order_resolved_status', '', 'info', 'Y', '0', 'admin', sysdate(), ''),
(1, '赢', '1', 't_polymarket_order_resolved_status', '', 'success', 'N', '0', 'admin', sysdate(), ''),
(2, '输', '2', 't_polymarket_order_resolved_status', '', 'danger', 'N', '0', 'admin', sysdate(), '');
