ALTER TABLE `t_stake_hosting_reward_settlement`
  ADD COLUMN `base_static_rate` decimal(10,4) NOT NULL DEFAULT '0.0000' COMMENT '基础静态收益率，单位%' AFTER `gross_static_reward`,
  ADD COLUMN `afi_accelerate_rate` decimal(10,4) NOT NULL DEFAULT '1.0000' COMMENT 'AFI加速倍率' AFTER `base_static_rate`,
  ADD COLUMN `actual_static_rate` decimal(10,4) NOT NULL DEFAULT '0.0000' COMMENT '实际静态收益率，单位%' AFTER `afi_accelerate_rate`;
