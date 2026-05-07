-- 质押托管等级重算字段与配置
-- 口径：game_level=真实等级，min_game_level=赠送等级，admin_game_level=管理员保底等级。
-- 奖励实际等级取 GREATEST(game_level, min_game_level, admin_game_level)。

ALTER TABLE `t_user_info`
  MODIFY COLUMN `game_level` int NULL DEFAULT 0 COMMENT '真实等级',
  MODIFY COLUMN `min_game_level` int NULL DEFAULT 0 COMMENT '赠送等级',
  ADD COLUMN `admin_game_level` int NULL DEFAULT 0 COMMENT '管理员保底等级' AFTER `min_game_level`;

ALTER TABLE `t_user_level_config`
  MODIFY COLUMN `level` int NOT NULL COMMENT 'F等级编码 0:暂无,1:F1,2:F2,3:F3,4:F4,5:F5,6:F6,7:F7,8:F8,9:F9',
  MODIFY COLUMN `performance` decimal(10,2) NULL DEFAULT 0.00 COMMENT '个人托管业绩',
  MODIFY COLUMN `team_performance` decimal(10,2) NULL DEFAULT 0.00 COMMENT '大区业绩(历史字段，本需求不参与等级考核)',
  MODIFY COLUMN `community_performance` decimal(10,2) NULL DEFAULT 0.00 COMMENT '小区托管业绩',
  MODIFY COLUMN `required_leg_num` int NULL DEFAULT 0 COMMENT '废弃字段：需要满足的线数量，本需求不参与等级考核',
  MODIFY COLUMN `leg_level_min` int NULL DEFAULT 0 COMMENT '废弃字段：线内代理最小等级，本需求不参与等级考核',
  MODIFY COLUMN `leg_level_count` int NULL DEFAULT 0 COMMENT '废弃字段：每条线里需要几个该等级及以上代理，本需求不参与等级考核';

INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '用户F等级', 't_user_info_game_level', '0', 'admin', sysdate(), '用户真实/赠送/管理员保底等级字典'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 't_user_info_game_level');

UPDATE sys_dict_data SET dict_label = '暂无', list_class = 'info'
WHERE dict_type = 't_user_info_game_level' AND dict_value = '0';
UPDATE sys_dict_data SET dict_label = 'F1', list_class = 'success'
WHERE dict_type = 't_user_info_game_level' AND dict_value = '1';
UPDATE sys_dict_data SET dict_label = 'F2', list_class = 'success'
WHERE dict_type = 't_user_info_game_level' AND dict_value = '2';
UPDATE sys_dict_data SET dict_label = 'F3', list_class = 'success'
WHERE dict_type = 't_user_info_game_level' AND dict_value = '3';
UPDATE sys_dict_data SET dict_label = 'F4', list_class = 'primary'
WHERE dict_type = 't_user_info_game_level' AND dict_value = '4';
UPDATE sys_dict_data SET dict_label = 'F5', list_class = 'primary'
WHERE dict_type = 't_user_info_game_level' AND dict_value = '5';
UPDATE sys_dict_data SET dict_label = 'F6', list_class = 'primary'
WHERE dict_type = 't_user_info_game_level' AND dict_value = '6';
UPDATE sys_dict_data SET dict_label = 'F7', list_class = 'warning'
WHERE dict_type = 't_user_info_game_level' AND dict_value = '7';
UPDATE sys_dict_data SET dict_label = 'F8', list_class = 'warning'
WHERE dict_type = 't_user_info_game_level' AND dict_value = '8';
UPDATE sys_dict_data SET dict_label = 'F9', list_class = 'danger'
WHERE dict_type = 't_user_info_game_level' AND dict_value = '9';

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 0, '暂无', '0', 't_user_info_game_level', '', 'info', 'Y', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 't_user_info_game_level' AND dict_value = '0');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1, 'F1', '1', 't_user_info_game_level', '', 'success', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 't_user_info_game_level' AND dict_value = '1');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 2, 'F2', '2', 't_user_info_game_level', '', 'success', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 't_user_info_game_level' AND dict_value = '2');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 3, 'F3', '3', 't_user_info_game_level', '', 'success', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 't_user_info_game_level' AND dict_value = '3');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 4, 'F4', '4', 't_user_info_game_level', '', 'primary', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 't_user_info_game_level' AND dict_value = '4');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 5, 'F5', '5', 't_user_info_game_level', '', 'primary', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 't_user_info_game_level' AND dict_value = '5');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 6, 'F6', '6', 't_user_info_game_level', '', 'primary', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 't_user_info_game_level' AND dict_value = '6');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 7, 'F7', '7', 't_user_info_game_level', '', 'warning', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 't_user_info_game_level' AND dict_value = '7');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 8, 'F8', '8', 't_user_info_game_level', '', 'warning', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 't_user_info_game_level' AND dict_value = '8');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 9, 'F9', '9', 't_user_info_game_level', '', 'danger', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 't_user_info_game_level' AND dict_value = '9');

UPDATE `t_user_level_config`
SET `performance` = 500.00, `team_performance` = 0.00, `community_performance` = 5000.00,
    `required_leg_num` = 0, `leg_level_min` = 0, `leg_level_count` = 0, `remark` = 'F1 托管等级'
WHERE `level` = 1;
UPDATE `t_user_level_config`
SET `performance` = 1000.00, `team_performance` = 0.00, `community_performance` = 20000.00,
    `required_leg_num` = 0, `leg_level_min` = 0, `leg_level_count` = 0, `remark` = 'F2 托管等级'
WHERE `level` = 2;
UPDATE `t_user_level_config`
SET `performance` = 1000.00, `team_performance` = 0.00, `community_performance` = 30000.00,
    `required_leg_num` = 0, `leg_level_min` = 0, `leg_level_count` = 0, `remark` = 'F3 托管等级'
WHERE `level` = 3;
UPDATE `t_user_level_config`
SET `performance` = 2000.00, `team_performance` = 0.00, `community_performance` = 100000.00,
    `required_leg_num` = 0, `leg_level_min` = 0, `leg_level_count` = 0, `remark` = 'F4 托管等级'
WHERE `level` = 4;
UPDATE `t_user_level_config`
SET `performance` = 2000.00, `team_performance` = 0.00, `community_performance` = 350000.00,
    `required_leg_num` = 0, `leg_level_min` = 0, `leg_level_count` = 0, `remark` = 'F5 托管等级'
WHERE `level` = 5;
UPDATE `t_user_level_config`
SET `performance` = 2000.00, `team_performance` = 0.00, `community_performance` = 700000.00,
    `required_leg_num` = 0, `leg_level_min` = 0, `leg_level_count` = 0, `remark` = 'F6 托管等级'
WHERE `level` = 6;
UPDATE `t_user_level_config`
SET `performance` = 3000.00, `team_performance` = 0.00, `community_performance` = 1500000.00,
    `required_leg_num` = 0, `leg_level_min` = 0, `leg_level_count` = 0, `remark` = 'F7 托管等级'
WHERE `level` = 7;
UPDATE `t_user_level_config`
SET `performance` = 5000.00, `team_performance` = 0.00, `community_performance` = 3000000.00,
    `required_leg_num` = 0, `leg_level_min` = 0, `leg_level_count` = 0, `remark` = 'F8 托管等级'
WHERE `level` = 8;
UPDATE `t_user_level_config`
SET `performance` = 5000.00, `team_performance` = 0.00, `community_performance` = 6000000.00,
    `required_leg_num` = 0, `leg_level_min` = 0, `leg_level_count` = 0, `remark` = 'F9 托管等级'
WHERE `level` = 9;

INSERT INTO `t_user_level_config` (`level`, `performance`, `team_performance`, `community_performance`, `required_leg_num`, `leg_level_min`, `leg_level_count`, `remark`)
SELECT 1, 500.00, 0.00, 5000.00, 0, 0, 0, 'F1 托管等级'
WHERE NOT EXISTS (SELECT 1 FROM `t_user_level_config` WHERE `level` = 1);
INSERT INTO `t_user_level_config` (`level`, `performance`, `team_performance`, `community_performance`, `required_leg_num`, `leg_level_min`, `leg_level_count`, `remark`)
SELECT 2, 1000.00, 0.00, 20000.00, 0, 0, 0, 'F2 托管等级'
WHERE NOT EXISTS (SELECT 1 FROM `t_user_level_config` WHERE `level` = 2);
INSERT INTO `t_user_level_config` (`level`, `performance`, `team_performance`, `community_performance`, `required_leg_num`, `leg_level_min`, `leg_level_count`, `remark`)
SELECT 3, 1000.00, 0.00, 30000.00, 0, 0, 0, 'F3 托管等级'
WHERE NOT EXISTS (SELECT 1 FROM `t_user_level_config` WHERE `level` = 3);
INSERT INTO `t_user_level_config` (`level`, `performance`, `team_performance`, `community_performance`, `required_leg_num`, `leg_level_min`, `leg_level_count`, `remark`)
SELECT 4, 2000.00, 0.00, 100000.00, 0, 0, 0, 'F4 托管等级'
WHERE NOT EXISTS (SELECT 1 FROM `t_user_level_config` WHERE `level` = 4);
INSERT INTO `t_user_level_config` (`level`, `performance`, `team_performance`, `community_performance`, `required_leg_num`, `leg_level_min`, `leg_level_count`, `remark`)
SELECT 5, 2000.00, 0.00, 350000.00, 0, 0, 0, 'F5 托管等级'
WHERE NOT EXISTS (SELECT 1 FROM `t_user_level_config` WHERE `level` = 5);
INSERT INTO `t_user_level_config` (`level`, `performance`, `team_performance`, `community_performance`, `required_leg_num`, `leg_level_min`, `leg_level_count`, `remark`)
SELECT 6, 2000.00, 0.00, 700000.00, 0, 0, 0, 'F6 托管等级'
WHERE NOT EXISTS (SELECT 1 FROM `t_user_level_config` WHERE `level` = 6);
INSERT INTO `t_user_level_config` (`level`, `performance`, `team_performance`, `community_performance`, `required_leg_num`, `leg_level_min`, `leg_level_count`, `remark`)
SELECT 7, 3000.00, 0.00, 1500000.00, 0, 0, 0, 'F7 托管等级'
WHERE NOT EXISTS (SELECT 1 FROM `t_user_level_config` WHERE `level` = 7);
INSERT INTO `t_user_level_config` (`level`, `performance`, `team_performance`, `community_performance`, `required_leg_num`, `leg_level_min`, `leg_level_count`, `remark`)
SELECT 8, 5000.00, 0.00, 3000000.00, 0, 0, 0, 'F8 托管等级'
WHERE NOT EXISTS (SELECT 1 FROM `t_user_level_config` WHERE `level` = 8);
INSERT INTO `t_user_level_config` (`level`, `performance`, `team_performance`, `community_performance`, `required_leg_num`, `leg_level_min`, `leg_level_count`, `remark`)
SELECT 9, 5000.00, 0.00, 6000000.00, 0, 0, 0, 'F9 托管等级'
WHERE NOT EXISTS (SELECT 1 FROM `t_user_level_config` WHERE `level` = 9);
