-- 托管全球分红当前权重字段
-- 用途：下单生效/订单到期时实时维护当前权重；每周102全球分红任务再读取当前小区权重生成周快照。

ALTER TABLE `t_user_info`
  ADD COLUMN `global_dividend_weight` decimal(20,6) NOT NULL DEFAULT '0.000000' COMMENT '我的全球分红权重' AFTER `performance_mining`,
  ADD COLUMN `global_dividend_umbrella_weight` decimal(20,6) NOT NULL DEFAULT '0.000000' COMMENT '团队全球分红权重' AFTER `global_dividend_weight`,
  ADD COLUMN `global_dividend_community_weight` decimal(20,6) NOT NULL DEFAULT '0.000000' COMMENT '小区全球分红权重' AFTER `global_dividend_umbrella_weight`;
