-- G7 静态日利率口径调整增量 SQL
-- 说明：
-- 1. 本次不新增字段、不改字段类型、不新建表。
-- 2. G7 已改为“今日团队新增业绩 vs 昨日团队新增业绩”口径。
-- 3. 以下 SQL 只修正数据库字段注释和表注释，避免继续误解为“有效托管 TVL / 到期扣减”。

ALTER TABLE `t_stake_hosting_daily_team_performance`
  MODIFY COLUMN `team_expired_amount` decimal(20,6) NOT NULL DEFAULT '0.000000'
    COMMENT '当天伞下团队到期托管USDT金额，当前不参与G7静态日利率',
  MODIFY COLUMN `previous_team_tvl` decimal(20,6) NOT NULL DEFAULT '0.000000'
    COMMENT '昨日伞下团队新增托管USDT金额，字段名沿用previous_team_tvl',
  MODIFY COLUMN `current_team_tvl` decimal(20,6) NOT NULL DEFAULT '0.000000'
    COMMENT '当日伞下团队新增托管USDT金额，字段名沿用current_team_tvl',
  COMMENT = '托管G7每日团队新增业绩与收益率快照表';
