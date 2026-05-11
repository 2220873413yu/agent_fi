-- 托管订单服务费比例快照
-- 口径B：订单创建时写入当时套餐服务费比例，101结算时只读取订单快照，不回查当前套餐配置。

ALTER TABLE `t_stake_hosting_order`
    ADD COLUMN `service_fee_ratio` decimal(10,4) NOT NULL DEFAULT '0.0000' COMMENT '服务费比例快照，单位%' AFTER `stake_usdt_amount`;
