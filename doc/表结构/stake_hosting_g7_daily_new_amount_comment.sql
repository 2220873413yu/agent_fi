-- G7 静态日利率每日新增口径注释修正历史 SQL
-- 说明：
-- 1. 本脚本用于旧“每日新增业绩”实现阶段。
-- 2. 总业绩TVL改造后，previous_team_tvl/current_team_tvl 仅保留为新增业绩审计字段。
-- 3. 新规则字段请执行 stake_hosting_g7_total_performance.sql。

ALTER TABLE `t_stake_hosting_daily_team_performance`
  MODIFY COLUMN `team_expired_amount` decimal(20,6) NOT NULL DEFAULT '0.000000'
    COMMENT '当天伞下团队到期托管USDT金额，历史/审计字段，不参与新G7公式',
  MODIFY COLUMN `previous_team_tvl` decimal(20,6) NOT NULL DEFAULT '0.000000'
    COMMENT '昨日伞下团队新增托管USDT金额，旧字段/审计',
  MODIFY COLUMN `current_team_tvl` decimal(20,6) NOT NULL DEFAULT '0.000000'
    COMMENT '当日伞下团队新增托管USDT金额，旧字段/审计',
  COMMENT = '托管G7每日团队业绩与收益率快照表';
