-- Polymarket 下单外扣手续费增量 SQL
-- 说明：
-- 1. biz_polymarket_trade_fee_ratio 的值按百分比保存，1 表示 1%。
-- 2. t_polymarket_order.afi_amount 继续表示购买份额成本 AFI，不含手续费。
-- 3. t_polymarket_order.total_pay_afi_amount 表示实际从用户 AFI 钱包扣减的总数量。

SET @schema_name := DATABASE();

INSERT INTO t_sys_para (para_code, para_value, para_desc, visible, create_time, active_flag, remark)
SELECT 'biz_polymarket_trade_fee_ratio', '1', 'Polymarket内部交易手续费比例，单位%', '0', sysdate(), 1, 'Polymarket参数'
WHERE NOT EXISTS (
    SELECT 1 FROM t_sys_para WHERE para_code = 'biz_polymarket_trade_fee_ratio'
);

SET @sql := (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE t_polymarket_order ADD COLUMN fee_ratio decimal(10,6) NOT NULL DEFAULT ''0.000000'' COMMENT ''系统基础交易手续费比例快照，单位%'' AFTER afi_amount',
        'SELECT 1'
    )
    FROM information_schema.columns
    WHERE table_schema = @schema_name
      AND table_name = 't_polymarket_order'
      AND column_name = 'fee_ratio'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql := (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE t_polymarket_order ADD COLUMN fee_relief_ratio decimal(10,6) NOT NULL DEFAULT ''0.000000'' COMMENT ''节点订单手续费减免比例快照，单位%'' AFTER fee_ratio',
        'SELECT 1'
    )
    FROM information_schema.columns
    WHERE table_schema = @schema_name
      AND table_name = 't_polymarket_order'
      AND column_name = 'fee_relief_ratio'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql := (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE t_polymarket_order ADD COLUMN actual_fee_ratio decimal(10,6) NOT NULL DEFAULT ''0.000000'' COMMENT ''实际外扣手续费比例快照，单位%'' AFTER fee_relief_ratio',
        'SELECT 1'
    )
    FROM information_schema.columns
    WHERE table_schema = @schema_name
      AND table_name = 't_polymarket_order'
      AND column_name = 'actual_fee_ratio'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql := (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE t_polymarket_order ADD COLUMN fee_afi_amount decimal(20,6) NOT NULL DEFAULT ''0.000000'' COMMENT ''本单外扣手续费AFI数量'' AFTER actual_fee_ratio',
        'SELECT 1'
    )
    FROM information_schema.columns
    WHERE table_schema = @schema_name
      AND table_name = 't_polymarket_order'
      AND column_name = 'fee_afi_amount'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql := (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE t_polymarket_order ADD COLUMN total_pay_afi_amount decimal(20,6) NOT NULL DEFAULT ''0.000000'' COMMENT ''实际总扣款AFI数量，购买成本AFI加手续费AFI'' AFTER fee_afi_amount',
        'SELECT 1'
    )
    FROM information_schema.columns
    WHERE table_schema = @schema_name
      AND table_name = 't_polymarket_order'
      AND column_name = 'total_pay_afi_amount'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql := (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE t_polymarket_market ADD COLUMN total_fee_afi_amount decimal(20,6) NOT NULL DEFAULT ''0.000000'' COMMENT ''市场实际总手续费AFI数量'' AFTER total_afi_amount',
        'SELECT 1'
    )
    FROM information_schema.columns
    WHERE table_schema = @schema_name
      AND table_name = 't_polymarket_market'
      AND column_name = 'total_fee_afi_amount'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
