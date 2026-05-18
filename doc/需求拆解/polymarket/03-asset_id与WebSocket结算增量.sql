-- Polymarket asset_id/token_id快照与WebSocket结算预留字段增量SQL
-- 说明：
-- 1. 不重建旧表，只补字段和索引。
-- 2. 使用当前数据库 DATABASE() 判断字段/索引是否存在，重复执行不会重复添加。

SET @schema_name := DATABASE();

-- 字典：Polymarket猜中后实际发放AFI，更新旧的“兑付USDT”文案。
UPDATE sys_dict_data
SET dict_label = 'Polymarket猜中兑付AFI',
    update_by = 'admin',
    update_time = sysdate(),
    remark = 'Polymarket猜中后按结算AFI价格发放等值AFI'
WHERE dict_type = 't_user_money_log_source_type'
  AND dict_value = '41';

-- 字典：Polymarket订单业务类型，前端下单时显式传入并落库。
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

-- 订单表：业务类型，1加密、2体育、3Up/Down；历史订单默认加密。
SET @column_exists := (
  SELECT COUNT(1)
  FROM information_schema.columns
  WHERE table_schema = @schema_name
    AND table_name = 't_polymarket_order'
    AND column_name = 'biz_type'
);
SET @sql := IF(
  @column_exists = 0,
  'ALTER TABLE t_polymarket_order ADD COLUMN biz_type int NOT NULL DEFAULT 1 COMMENT ''业务类型 1加密 2体育 3Up/Down'' AFTER market_slug',
  'SELECT ''t_polymarket_order.biz_type already exists'''
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @index_exists := (
  SELECT COUNT(1)
  FROM information_schema.statistics
  WHERE table_schema = @schema_name
    AND table_name = 't_polymarket_order'
    AND index_name = 'idx_biz_type'
);
SET @sql := IF(
  @index_exists = 0,
  'ALTER TABLE t_polymarket_order ADD KEY idx_biz_type (biz_type)',
  'SELECT ''t_polymarket_order.idx_biz_type already exists'''
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

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

-- 订单表：结算时使用的AFI/USDT价格快照，中奖后按该价格把USDT等值换算成AFI发放。
SET @column_exists := (
  SELECT COUNT(1)
  FROM information_schema.columns
  WHERE table_schema = @schema_name
    AND table_name = 't_polymarket_order'
    AND column_name = 'payout_afi_price'
);
SET @sql := IF(
  @column_exists = 0,
  'ALTER TABLE t_polymarket_order ADD COLUMN payout_afi_price decimal(20,6) NOT NULL DEFAULT ''0.000000'' COMMENT ''结算时AFI/USDT价格快照'' AFTER payout_usdt_amount',
  'SELECT ''t_polymarket_order.payout_afi_price already exists'''
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 订单表：中奖后实际发放到用户AFI钱包valid_num2的数量。
SET @column_exists := (
  SELECT COUNT(1)
  FROM information_schema.columns
  WHERE table_schema = @schema_name
    AND table_name = 't_polymarket_order'
    AND column_name = 'payout_afi_amount'
);
SET @sql := IF(
  @column_exists = 0,
  'ALTER TABLE t_polymarket_order ADD COLUMN payout_afi_amount decimal(24,8) NOT NULL DEFAULT ''0.00000000'' COMMENT ''实际发放AFI数量'' AFTER payout_afi_price',
  'SELECT ''t_polymarket_order.payout_afi_amount already exists'''
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 订单表：临时限制同一用户同一Polymarket市场只能下一笔正常订单。
-- 建唯一索引前可先执行以下SQL排查历史重复数据；如有结果，需要先人工处理后再建索引。
-- SELECT user_id, market_slug, deleted, COUNT(*)
-- FROM t_polymarket_order
-- GROUP BY user_id, market_slug, deleted
-- HAVING COUNT(*) > 1;
SET @index_exists := (
  SELECT COUNT(1)
  FROM information_schema.statistics
  WHERE table_schema = @schema_name
    AND table_name = 't_polymarket_order'
    AND index_name = 'uk_user_market_slug'
);
SET @sql := IF(
  @index_exists = 0,
  'ALTER TABLE t_polymarket_order ADD UNIQUE KEY uk_user_market_slug (user_id, market_slug, deleted)',
  'SELECT ''t_polymarket_order.uk_user_market_slug already exists'''
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

-- 市场表：记录市场维度实际总发放AFI数量，USDT等值仍保存在total_payout_usdt_amount用于对账。
SET @column_exists := (
  SELECT COUNT(1)
  FROM information_schema.columns
  WHERE table_schema = @schema_name
    AND table_name = 't_polymarket_market'
    AND column_name = 'total_payout_afi_amount'
);
SET @sql := IF(
  @column_exists = 0,
  'ALTER TABLE t_polymarket_market ADD COLUMN total_payout_afi_amount decimal(24,8) NOT NULL DEFAULT ''0.00000000'' COMMENT ''市场实际总发放AFI数量'' AFTER total_payout_usdt_amount',
  'SELECT ''t_polymarket_market.total_payout_afi_amount already exists'''
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
