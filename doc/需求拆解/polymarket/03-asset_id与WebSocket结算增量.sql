-- Polymarket asset_id/token_id快照与WebSocket结算预留字段增量SQL
-- 说明：
-- 1. 不重建旧表，只补字段和索引。
-- 2. 使用当前数据库 DATABASE() 判断字段/索引是否存在，重复执行不会重复添加。

SET @schema_name := DATABASE();

-- 订单表：保存用户选择结果对应的asset_id/token_id，后续可用于按WebSocket结果定位用户选择。
SET @column_exists := (
  SELECT COUNT(1)
  FROM information_schema.columns
  WHERE table_schema = @schema_name
    AND table_name = 't_polymarket_order'
    AND column_name = 'asset_id'
);
SET @sql := IF(
  @column_exists = 0,
  'ALTER TABLE t_polymarket_order ADD COLUMN asset_id varchar(100) DEFAULT NULL COMMENT ''用户选择结果对应的Polymarket asset_id/token_id快照'' AFTER outcome_name',
  'SELECT ''t_polymarket_order.asset_id already exists'''
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 订单表：按市场批量结算时，需要快速查询某个市场下仍待结算的订单。
SET @index_exists := (
  SELECT COUNT(1)
  FROM information_schema.statistics
  WHERE table_schema = @schema_name
    AND table_name = 't_polymarket_order'
    AND index_name = 'idx_market_status'
);
SET @sql := IF(
  @index_exists = 0,
  'ALTER TABLE t_polymarket_order ADD KEY idx_market_status (market_slug, status)',
  'SELECT ''t_polymarket_order.idx_market_status already exists'''
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 市场表：market_slug必须唯一，保证同一个Polymarket市场只维护一条聚合记录。
SET @index_exists := (
  SELECT COUNT(1)
  FROM information_schema.statistics
  WHERE table_schema = @schema_name
    AND table_name = 't_polymarket_market'
    AND index_name = 'uk_market_slug'
);
SET @sql := IF(
  @index_exists = 0,
  'ALTER TABLE t_polymarket_market ADD UNIQUE KEY uk_market_slug (market_slug)',
  'SELECT ''t_polymarket_market.uk_market_slug already exists'''
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 市场表：Quartz兜底扫描待结算且已到期市场时使用。
SET @index_exists := (
  SELECT COUNT(1)
  FROM information_schema.statistics
  WHERE table_schema = @schema_name
    AND table_name = 't_polymarket_market'
    AND index_name = 'idx_status_end_time'
);
SET @sql := IF(
  @index_exists = 0,
  'ALTER TABLE t_polymarket_market ADD KEY idx_status_end_time (status, end_time)',
  'SELECT ''t_polymarket_market.idx_status_end_time already exists'''
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @index_exists := (
  SELECT COUNT(1)
  FROM information_schema.statistics
  WHERE table_schema = @schema_name
    AND table_name = 't_polymarket_order'
    AND index_name = 'idx_asset_id'
);
SET @sql := IF(
  @index_exists = 0,
  'ALTER TABLE t_polymarket_order ADD KEY idx_asset_id (asset_id)',
  'SELECT ''t_polymarket_order.idx_asset_id already exists'''
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 市场表：保存市场所有结果的asset_id数组快照。
SET @column_exists := (
  SELECT COUNT(1)
  FROM information_schema.columns
  WHERE table_schema = @schema_name
    AND table_name = 't_polymarket_market'
    AND column_name = 'asset_ids_json'
);
SET @sql := IF(
  @column_exists = 0,
  'ALTER TABLE t_polymarket_market ADD COLUMN asset_ids_json longtext COMMENT ''Polymarket市场所有结果asset_id数组快照'' AFTER market_question',
  'SELECT ''t_polymarket_market.asset_ids_json already exists'''
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 市场表：保存市场所有结果名称数组快照，便于后台排查asset_id和结果名称的对应关系。
SET @column_exists := (
  SELECT COUNT(1)
  FROM information_schema.columns
  WHERE table_schema = @schema_name
    AND table_name = 't_polymarket_market'
    AND column_name = 'outcomes_json'
);
SET @sql := IF(
  @column_exists = 0,
  'ALTER TABLE t_polymarket_market ADD COLUMN outcomes_json longtext COMMENT ''Polymarket市场所有结果名称数组快照'' AFTER asset_ids_json',
  'SELECT ''t_polymarket_market.outcomes_json already exists'''
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 市场表：预留WebSocket或最终结算返回的赢家asset_id/token_id。
SET @column_exists := (
  SELECT COUNT(1)
  FROM information_schema.columns
  WHERE table_schema = @schema_name
    AND table_name = 't_polymarket_market'
    AND column_name = 'winning_asset_id'
);
SET @sql := IF(
  @column_exists = 0,
  'ALTER TABLE t_polymarket_market ADD COLUMN winning_asset_id varchar(100) DEFAULT NULL COMMENT ''WebSocket或结算结果返回的赢家asset_id'' AFTER result_outcome_name',
  'SELECT ''t_polymarket_market.winning_asset_id already exists'''
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @index_exists := (
  SELECT COUNT(1)
  FROM information_schema.statistics
  WHERE table_schema = @schema_name
    AND table_name = 't_polymarket_market'
    AND index_name = 'idx_winning_asset_id'
);
SET @sql := IF(
  @index_exists = 0,
  'ALTER TABLE t_polymarket_market ADD KEY idx_winning_asset_id (winning_asset_id)',
  'SELECT ''t_polymarket_market.idx_winning_asset_id already exists'''
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
