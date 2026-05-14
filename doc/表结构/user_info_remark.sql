ALTER TABLE `t_user_info`
  ADD COLUMN `remark` varchar(500) DEFAULT NULL COMMENT '后台用户备注' AFTER `last_login_ip`;
