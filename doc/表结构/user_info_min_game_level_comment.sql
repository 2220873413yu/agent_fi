-- 修改 t_user_info.min_game_level 字段注释为“赠送等级”
ALTER TABLE `t_user_info`
  MODIFY COLUMN `min_game_level` int NULL DEFAULT 0 COMMENT '赠送等级';
