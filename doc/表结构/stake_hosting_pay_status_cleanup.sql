ALTER TABLE `t_stake_hosting_order`
  MODIFY COLUMN `pay_status` int NOT NULL DEFAULT 0 COMMENT '支付状态 0:待支付 1:支付成功';

DELETE FROM `sys_dict_data`
WHERE `dict_type` = 't_stake_hosting_order_pay_status'
  AND `dict_value` = '2';
