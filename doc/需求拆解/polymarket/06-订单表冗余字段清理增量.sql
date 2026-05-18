-- Polymarket 订单表冗余字段清理增量 SQL
-- 说明：
-- 1. 订单表保留 order_snapshot_json，记录用户下单时的市场快照。
-- 2. 结算时市场快照统一保存在 t_polymarket_market.market_snapshot_json。
-- 3. t_polymarket_order 不再保存 settle_snapshot_json/create_by/update_by/deleted。
-- 4. 删除 deleted 后，同一用户同一市场唯一索引调整为 (user_id, market_slug)。
-- 5. 如果历史数据存在同一用户同一 market_slug 多笔订单，需要先人工处理重复数据，再执行唯一索引重建。

SET @schema_name := DATABASE();

-- 执行前可用这个 SQL 检查重复订单：
-- SELECT user_id, market_slug, COUNT(*)
-- FROM t_polymarket_order
-- GROUP BY user_id, market_slug
-- HAVING COUNT(*) > 1;

SET @sql := (
    SELECT IF(
        COUNT(*) > 0,
        'ALTER TABLE t_polymarket_order DROP INDEX uk_user_market_slug',
        'SELECT 1'
    )
    FROM information_schema.statistics
    WHERE table_schema = @schema_name
      AND table_name = 't_polymarket_order'
      AND index_name = 'uk_user_market_slug'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql := (
    SELECT IF(
        COUNT(*) > 0,
        'ALTER TABLE t_polymarket_order DROP COLUMN settle_snapshot_json',
        'SELECT 1'
    )
    FROM information_schema.columns
    WHERE table_schema = @schema_name
      AND table_name = 't_polymarket_order'
      AND column_name = 'settle_snapshot_json'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql := (
    SELECT IF(
        COUNT(*) > 0,
        'ALTER TABLE t_polymarket_order DROP COLUMN create_by',
        'SELECT 1'
    )
    FROM information_schema.columns
    WHERE table_schema = @schema_name
      AND table_name = 't_polymarket_order'
      AND column_name = 'create_by'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql := (
    SELECT IF(
        COUNT(*) > 0,
        'ALTER TABLE t_polymarket_order DROP COLUMN update_by',
        'SELECT 1'
    )
    FROM information_schema.columns
    WHERE table_schema = @schema_name
      AND table_name = 't_polymarket_order'
      AND column_name = 'update_by'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql := (
    SELECT IF(
        COUNT(*) > 0,
        'ALTER TABLE t_polymarket_order DROP COLUMN deleted',
        'SELECT 1'
    )
    FROM information_schema.columns
    WHERE table_schema = @schema_name
      AND table_name = 't_polymarket_order'
      AND column_name = 'deleted'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql := (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE t_polymarket_order ADD UNIQUE KEY uk_user_market_slug (user_id, market_slug)',
        'SELECT 1'
    )
    FROM information_schema.statistics
    WHERE table_schema = @schema_name
      AND table_name = 't_polymarket_order'
      AND index_name = 'uk_user_market_slug'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
