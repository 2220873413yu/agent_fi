-- 托管套餐全球分红权重增量修正脚本
-- 说明：performance_coefficient 已存在时执行本脚本；不修改历史建表 SQL。
-- 新口径：1天=0，30天=1，90天=2，180天=3，360天=6。
-- 注意：历史订单按下单时 t_stake_hosting_order.performance_coefficient / performance_points 快照结算，本脚本不重算历史订单。

UPDATE `t_stake_hosting_package`
SET `performance_coefficient` = CASE `days`
    WHEN 1 THEN 0.0000
    WHEN 30 THEN 1.0000
    WHEN 90 THEN 2.0000
    WHEN 180 THEN 3.0000
    WHEN 360 THEN 6.0000
    ELSE `performance_coefficient`
END
WHERE `days` IN (1, 30, 90, 180, 360);
