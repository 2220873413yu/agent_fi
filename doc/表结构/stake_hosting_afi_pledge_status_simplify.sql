-- AFI质押记录状态简化：只保留 1=生效中、2=已退还。
-- 是否参与当天静态收益加速由 effective_day <= rewardDay 判断，不再使用 0=未生效。

UPDATE `t_stake_hosting_afi_pledge`
SET `status` = 1
WHERE `status` = 0;

ALTER TABLE `t_stake_hosting_afi_pledge`
  MODIFY COLUMN `status` int NOT NULL DEFAULT '1' COMMENT '状态 1:生效中 2:已退还';

DELETE FROM `sys_dict_data`
WHERE `dict_type` = 't_stake_hosting_afi_pledge_status'
  AND `dict_value` = '0';

UPDATE `sys_dict_data`
SET `dict_sort` = 1,
    `dict_label` = '生效中',
    `list_class` = 'success',
    `is_default` = 'Y',
    `status` = '0',
    `update_time` = sysdate()
WHERE `dict_type` = 't_stake_hosting_afi_pledge_status'
  AND `dict_value` = '1';

UPDATE `sys_dict_data`
SET `dict_sort` = 2,
    `dict_label` = '已退还',
    `list_class` = 'primary',
    `is_default` = 'N',
    `status` = '0',
    `update_time` = sysdate()
WHERE `dict_type` = 't_stake_hosting_afi_pledge_status'
  AND `dict_value` = '2';
