/*
 Navicat Premium Dump SQL

 Source Server         : 本地电脑myql
 Source Server Type    : MySQL
 Source Server Version : 80046 (8.0.46)
 Source Host           : localhost:13306
 Source Schema         : xms_agent

 Target Server Type    : MySQL
 Target Server Version : 80046 (8.0.46)
 File Encoding         : 65001

 Date: 06/05/2026 19:03:10
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for bms_user
-- ----------------------------
DROP TABLE IF EXISTS `bms_user`;
CREATE TABLE `bms_user`  (
                             `user_id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
                             `user_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '昵称',
                             `wallet_address` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '钱包地址',
                             `aleo_address` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'aleo地址',
                             `parent_id` bigint NULL DEFAULT NULL COMMENT '推荐人',
                             `parent_wallet_address` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '推荐人钱包地址',
                             `level` tinyint NOT NULL DEFAULT -1 COMMENT '级别(普通用户：0、区代理：1、县代理：2、市代理：3、省代理：4、全国代：5、全球代：6)',
                             `self_achievement` decimal(20, 2) NOT NULL DEFAULT 0.00 COMMENT '本人业绩',
                             `team_achievement` decimal(20, 2) NOT NULL DEFAULT 0.00 COMMENT '团队业绩',
                             `direct_push_achievement` decimal(20, 2) NULL DEFAULT 0.00 COMMENT '直推业绩',
                             `computility` decimal(20, 2) NOT NULL DEFAULT 0.00 COMMENT '总算力',
                             `aleo_computility` decimal(20, 2) NULL DEFAULT 0.00 COMMENT 'aleo算力',
                             `oort_computility` decimal(20, 2) NULL DEFAULT 0.00 COMMENT 'oort算力',
                             `dfc_computility` decimal(20, 2) NULL DEFAULT 0.00 COMMENT 'dfc算力',
                             `usdt` decimal(20, 6) NOT NULL DEFAULT 0.000000 COMMENT 'usdt',
                             `aleo` decimal(20, 6) NOT NULL DEFAULT 0.000000 COMMENT 'aleo',
                             `oort` decimal(20, 6) NOT NULL DEFAULT 0.000000 COMMENT 'oort',
                             `dfc` decimal(20, 6) NULL DEFAULT 0.000000 COMMENT 'dfc',
                             `df` decimal(20, 6) NULL DEFAULT 0.000000 COMMENT 'df',
                             `ancestors` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '祖级列表',
                             `direct_push_deposit` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '直推入金列表',
                             `has_usdt_withdraw` tinyint NOT NULL DEFAULT 1 COMMENT 'usdt提现权限',
                             `has_aleo_withdraw` tinyint NOT NULL DEFAULT 1 COMMENT 'aleo提现权限',
                             `has_oort_withdraw` tinyint NOT NULL DEFAULT 1 COMMENT 'oort提现权限',
                             `has_dfc_withdraw` tinyint NOT NULL DEFAULT 1 COMMENT 'dfc提现权限',
                             `is_new` tinyint NOT NULL DEFAULT 1 COMMENT '是否新会员',
                             `is_active` tinyint NOT NULL DEFAULT 0 COMMENT '激活状态（1激活 0未激活）',
                             `status` tinyint NOT NULL DEFAULT 1 COMMENT '帐号状态（1正常 0停用）',
                             `signature_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '签名文件',
                             `location` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '地区',
                             `mark` tinyint NULL DEFAULT NULL COMMENT '标注',
                             `del_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
                             `version` int NULL DEFAULT 0 COMMENT '版本',
                             `create_dept` bigint NULL DEFAULT NULL COMMENT '创建部门',
                             `create_by` bigint NULL DEFAULT NULL COMMENT '创建者',
                             `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
                             `update_by` bigint NULL DEFAULT NULL COMMENT '更新者',
                             `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
                             `dfc_mining_days` int NULL DEFAULT 0 COMMENT 'dfc挖矿运行天数',
                             `personal_reduction_ratio` decimal(20, 4) NULL DEFAULT 0.0000 COMMENT '个人减产比例 1就是减产1%',
                             `team_reduction_ratio` decimal(20, 4) NULL DEFAULT 0.0000 COMMENT '团队减产比例 1就是减产1%',
                             `yesterday_done` int NULL DEFAULT 0 COMMENT '昨日任务是否完成 0:否,1:是',
                             `last_month_done` int NULL DEFAULT 0 COMMENT '上个月任务是否完成 0:否,1:是',
                             `team_last_order_date_int` bigint NULL DEFAULT 0 COMMENT '团队最近下单日期(yyyymmdd)',
                             `has_watch` int NULL DEFAULT 0 COMMENT '是否绑定激活码 0:否,1:是',
                             PRIMARY KEY (`user_id`) USING BTREE,
                             UNIQUE INDEX `uni_wallet_address`(`wallet_address` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7696 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户信息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for gen_table
-- ----------------------------
DROP TABLE IF EXISTS `gen_table`;
CREATE TABLE `gen_table`  (
                              `table_id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
                              `table_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT '' COMMENT '表名称',
                              `table_comment` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT '' COMMENT '表描述',
                              `sub_table_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '关联子表的表名',
                              `sub_table_fk_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '子表关联的外键名',
                              `class_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT '' COMMENT '实体类名称',
                              `tpl_category` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT 'crud' COMMENT '使用的模板（crud单表操作 tree树表操作）',
                              `package_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '生成包路径',
                              `module_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '生成模块名',
                              `business_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '生成业务名',
                              `function_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '生成功能名',
                              `function_author` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '生成功能作者',
                              `gen_type` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT '0' COMMENT '生成代码方式（0zip压缩包 1自定义路径）',
                              `gen_path` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT '/' COMMENT '生成路径（不填默认项目路径）',
                              `options` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '其它生成选项',
                              `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT '' COMMENT '创建者',
                              `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
                              `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT '' COMMENT '更新者',
                              `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
                              `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '备注',
                              PRIMARY KEY (`table_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 86 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_520_ci COMMENT = '代码生成业务表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for gen_table_column
-- ----------------------------
DROP TABLE IF EXISTS `gen_table_column`;
CREATE TABLE `gen_table_column`  (
                                     `column_id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
                                     `table_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '归属表编号',
                                     `column_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '列名称',
                                     `column_comment` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '列描述',
                                     `column_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '列类型',
                                     `java_type` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT 'JAVA类型',
                                     `java_field` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT 'JAVA字段名',
                                     `is_pk` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '是否主键（1是）',
                                     `is_increment` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '是否自增（1是）',
                                     `is_required` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '是否必填（1是）',
                                     `is_insert` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '是否为插入字段（1是）',
                                     `is_edit` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '是否编辑字段（1是）',
                                     `is_list` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '是否列表字段（1是）',
                                     `is_query` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '是否查询字段（1是）',
                                     `query_type` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT 'EQ' COMMENT '查询方式（等于、不等于、大于、小于、范围）',
                                     `html_type` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '显示类型（文本框、文本域、下拉框、复选框、单选框、日期控件）',
                                     `dict_type` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT '' COMMENT '字典类型',
                                     `sort` int NULL DEFAULT NULL COMMENT '排序',
                                     `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT '' COMMENT '创建者',
                                     `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
                                     `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT '' COMMENT '更新者',
                                     `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
                                     PRIMARY KEY (`column_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1878 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_520_ci COMMENT = '代码生成业务表字段' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for qrtz_blob_triggers
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_blob_triggers`;
CREATE TABLE `qrtz_blob_triggers`  (
                                       `sched_name` varchar(120) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '调度名称',
                                       `trigger_name` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT 'qrtz_triggers表trigger_name的外键',
                                       `trigger_group` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT 'qrtz_triggers表trigger_group的外键',
                                       `blob_data` blob NULL COMMENT '存放持久化Trigger对象',
                                       PRIMARY KEY (`sched_name`, `trigger_name`, `trigger_group`) USING BTREE,
                                       CONSTRAINT `qrtz_blob_triggers_ibfk_1` FOREIGN KEY (`sched_name`, `trigger_name`, `trigger_group`) REFERENCES `qrtz_triggers` (`sched_name`, `trigger_name`, `trigger_group`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci COMMENT = 'Blob类型的触发器表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for qrtz_calendars
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_calendars`;
CREATE TABLE `qrtz_calendars`  (
                                   `sched_name` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL COMMENT '调度名称',
                                   `calendar_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL COMMENT '日历名称',
                                   `calendar` blob NOT NULL COMMENT '存放持久化calendar对象',
                                   PRIMARY KEY (`sched_name`, `calendar_name`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci COMMENT = '日历信息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for qrtz_cron_triggers
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_cron_triggers`;
CREATE TABLE `qrtz_cron_triggers`  (
                                       `sched_name` varchar(120) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '调度名称',
                                       `trigger_name` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT 'qrtz_triggers表trigger_name的外键',
                                       `trigger_group` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT 'qrtz_triggers表trigger_group的外键',
                                       `cron_expression` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT 'cron表达式',
                                       `time_zone_id` varchar(80) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '时区',
                                       PRIMARY KEY (`sched_name`, `trigger_name`, `trigger_group`) USING BTREE,
                                       CONSTRAINT `qrtz_cron_triggers_ibfk_1` FOREIGN KEY (`sched_name`, `trigger_name`, `trigger_group`) REFERENCES `qrtz_triggers` (`sched_name`, `trigger_name`, `trigger_group`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci COMMENT = 'Cron类型的触发器表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for qrtz_fired_triggers
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_fired_triggers`;
CREATE TABLE `qrtz_fired_triggers`  (
                                        `sched_name` varchar(120) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '调度名称',
                                        `entry_id` varchar(95) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '调度器实例id',
                                        `trigger_name` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT 'qrtz_triggers表trigger_name的外键',
                                        `trigger_group` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT 'qrtz_triggers表trigger_group的外键',
                                        `instance_name` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '调度器实例名',
                                        `fired_time` bigint NOT NULL COMMENT '触发的时间',
                                        `sched_time` bigint NOT NULL COMMENT '定时器制定的时间',
                                        `priority` int NOT NULL COMMENT '优先级',
                                        `state` varchar(16) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '状态',
                                        `job_name` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '任务名称',
                                        `job_group` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '任务组名',
                                        `is_nonconcurrent` varchar(1) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '是否并发',
                                        `requests_recovery` varchar(1) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '是否接受恢复执行',
                                        PRIMARY KEY (`sched_name`, `entry_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci COMMENT = '已触发的触发器表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for qrtz_job_details
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_job_details`;
CREATE TABLE `qrtz_job_details`  (
                                     `sched_name` varchar(120) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '调度名称',
                                     `job_name` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '任务名称',
                                     `job_group` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '任务组名',
                                     `description` varchar(250) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '相关介绍',
                                     `job_class_name` varchar(250) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '执行任务类名称',
                                     `is_durable` varchar(1) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '是否持久化',
                                     `is_nonconcurrent` varchar(1) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '是否并发',
                                     `is_update_data` varchar(1) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '是否更新数据',
                                     `requests_recovery` varchar(1) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '是否接受恢复执行',
                                     `job_data` blob NULL COMMENT '存放持久化job对象',
                                     PRIMARY KEY (`sched_name`, `job_name`, `job_group`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci COMMENT = '任务详细信息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for qrtz_locks
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_locks`;
CREATE TABLE `qrtz_locks`  (
                               `sched_name` varchar(120) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '调度名称',
                               `lock_name` varchar(40) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '悲观锁名称',
                               PRIMARY KEY (`sched_name`, `lock_name`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci COMMENT = '存储的悲观锁信息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for qrtz_paused_trigger_grps
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_paused_trigger_grps`;
CREATE TABLE `qrtz_paused_trigger_grps`  (
                                             `sched_name` varchar(120) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '调度名称',
                                             `trigger_group` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT 'qrtz_triggers表trigger_group的外键',
                                             PRIMARY KEY (`sched_name`, `trigger_group`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci COMMENT = '暂停的触发器表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for qrtz_scheduler_state
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_scheduler_state`;
CREATE TABLE `qrtz_scheduler_state`  (
                                         `sched_name` varchar(120) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '调度名称',
                                         `instance_name` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '实例名称',
                                         `last_checkin_time` bigint NOT NULL COMMENT '上次检查时间',
                                         `checkin_interval` bigint NOT NULL COMMENT '检查间隔时间',
                                         PRIMARY KEY (`sched_name`, `instance_name`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci COMMENT = '调度器状态表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for qrtz_simple_triggers
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_simple_triggers`;
CREATE TABLE `qrtz_simple_triggers`  (
                                         `sched_name` varchar(120) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '调度名称',
                                         `trigger_name` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT 'qrtz_triggers表trigger_name的外键',
                                         `trigger_group` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT 'qrtz_triggers表trigger_group的外键',
                                         `repeat_count` bigint NOT NULL COMMENT '重复的次数统计',
                                         `repeat_interval` bigint NOT NULL COMMENT '重复的间隔时间',
                                         `times_triggered` bigint NOT NULL COMMENT '已经触发的次数',
                                         PRIMARY KEY (`sched_name`, `trigger_name`, `trigger_group`) USING BTREE,
                                         CONSTRAINT `qrtz_simple_triggers_ibfk_1` FOREIGN KEY (`sched_name`, `trigger_name`, `trigger_group`) REFERENCES `qrtz_triggers` (`sched_name`, `trigger_name`, `trigger_group`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci COMMENT = '简单触发器的信息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for qrtz_simprop_triggers
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_simprop_triggers`;
CREATE TABLE `qrtz_simprop_triggers`  (
                                          `sched_name` varchar(120) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '调度名称',
                                          `trigger_name` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT 'qrtz_triggers表trigger_name的外键',
                                          `trigger_group` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT 'qrtz_triggers表trigger_group的外键',
                                          `str_prop_1` varchar(512) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT 'String类型的trigger的第一个参数',
                                          `str_prop_2` varchar(512) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT 'String类型的trigger的第二个参数',
                                          `str_prop_3` varchar(512) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT 'String类型的trigger的第三个参数',
                                          `int_prop_1` int NULL DEFAULT NULL COMMENT 'int类型的trigger的第一个参数',
                                          `int_prop_2` int NULL DEFAULT NULL COMMENT 'int类型的trigger的第二个参数',
                                          `long_prop_1` bigint NULL DEFAULT NULL COMMENT 'long类型的trigger的第一个参数',
                                          `long_prop_2` bigint NULL DEFAULT NULL COMMENT 'long类型的trigger的第二个参数',
                                          `dec_prop_1` decimal(13, 4) NULL DEFAULT NULL COMMENT 'decimal类型的trigger的第一个参数',
                                          `dec_prop_2` decimal(13, 4) NULL DEFAULT NULL COMMENT 'decimal类型的trigger的第二个参数',
                                          `bool_prop_1` varchar(1) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT 'Boolean类型的trigger的第一个参数',
                                          `bool_prop_2` varchar(1) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT 'Boolean类型的trigger的第二个参数',
                                          PRIMARY KEY (`sched_name`, `trigger_name`, `trigger_group`) USING BTREE,
                                          CONSTRAINT `qrtz_simprop_triggers_ibfk_1` FOREIGN KEY (`sched_name`, `trigger_name`, `trigger_group`) REFERENCES `qrtz_triggers` (`sched_name`, `trigger_name`, `trigger_group`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci COMMENT = '同步机制的行锁表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for qrtz_triggers
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_triggers`;
CREATE TABLE `qrtz_triggers`  (
                                  `sched_name` varchar(120) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '调度名称',
                                  `trigger_name` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '触发器的名字',
                                  `trigger_group` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '触发器所属组的名字',
                                  `job_name` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT 'qrtz_job_details表job_name的外键',
                                  `job_group` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT 'qrtz_job_details表job_group的外键',
                                  `description` varchar(250) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '相关介绍',
                                  `next_fire_time` bigint NULL DEFAULT NULL COMMENT '上一次触发时间（毫秒）',
                                  `prev_fire_time` bigint NULL DEFAULT NULL COMMENT '下一次触发时间（默认为-1表示不触发）',
                                  `priority` int NULL DEFAULT NULL COMMENT '优先级',
                                  `trigger_state` varchar(16) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '触发器状态',
                                  `trigger_type` varchar(8) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '触发器的类型',
                                  `start_time` bigint NOT NULL COMMENT '开始时间',
                                  `end_time` bigint NULL DEFAULT NULL COMMENT '结束时间',
                                  `calendar_name` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '日程表名称',
                                  `misfire_instr` smallint NULL DEFAULT NULL COMMENT '补偿执行的策略',
                                  `job_data` blob NULL COMMENT '存放持久化job对象',
                                  PRIMARY KEY (`sched_name`, `trigger_name`, `trigger_group`) USING BTREE,
                                  INDEX `sched_name`(`sched_name` ASC, `job_name` ASC, `job_group` ASC) USING BTREE,
                                  CONSTRAINT `qrtz_triggers_ibfk_1` FOREIGN KEY (`sched_name`, `job_name`, `job_group`) REFERENCES `qrtz_job_details` (`sched_name`, `job_name`, `job_group`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci COMMENT = '触发器详细信息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sys_config
-- ----------------------------
DROP TABLE IF EXISTS `sys_config`;
CREATE TABLE `sys_config`  (
                               `config_id` int NOT NULL AUTO_INCREMENT COMMENT '参数主键',
                               `config_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT '' COMMENT '参数名称',
                               `config_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT '' COMMENT '参数键名',
                               `config_value` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT '' COMMENT '参数键值',
                               `config_type` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT 'N' COMMENT '系统内置（Y是 N否）',
                               `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT '' COMMENT '创建者',
                               `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
                               `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT '' COMMENT '更新者',
                               `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
                               `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '备注',
                               PRIMARY KEY (`config_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_520_ci COMMENT = '参数配置表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sys_dept
-- ----------------------------
DROP TABLE IF EXISTS `sys_dept`;
CREATE TABLE `sys_dept`  (
                             `dept_id` bigint NOT NULL AUTO_INCREMENT COMMENT '部门id',
                             `parent_id` bigint NULL DEFAULT 0 COMMENT '父部门id',
                             `ancestors` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT '' COMMENT '祖级列表',
                             `dept_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT '' COMMENT '部门名称',
                             `order_num` int NULL DEFAULT 0 COMMENT '显示顺序',
                             `leader` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '负责人',
                             `phone` varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '联系电话',
                             `email` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '邮箱',
                             `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT '0' COMMENT '部门状态（0正常 1停用）',
                             `del_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
                             `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT '' COMMENT '创建者',
                             `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
                             `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT '' COMMENT '更新者',
                             `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
                             PRIMARY KEY (`dept_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 104 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_520_ci COMMENT = '部门表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sys_dict_data
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict_data`;
CREATE TABLE `sys_dict_data`  (
                                  `dict_code` bigint NOT NULL AUTO_INCREMENT COMMENT '字典编码',
                                  `dict_sort` int NULL DEFAULT 0 COMMENT '字典排序',
                                  `dict_label` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT '' COMMENT '字典标签',
                                  `dict_value` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT '' COMMENT '字典键值',
                                  `dict_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT '' COMMENT '字典类型',
                                  `css_class` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '样式属性（其他样式扩展）',
                                  `list_class` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '表格回显样式',
                                  `is_default` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT 'N' COMMENT '是否默认（Y是 N否）',
                                  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',
                                  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT '' COMMENT '创建者',
                                  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
                                  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT '' COMMENT '更新者',
                                  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
                                  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '备注',
                                  PRIMARY KEY (`dict_code`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 768 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_520_ci COMMENT = '字典数据表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sys_dict_type
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict_type`;
CREATE TABLE `sys_dict_type`  (
                                  `dict_id` bigint NOT NULL AUTO_INCREMENT COMMENT '字典主键',
                                  `dict_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT '' COMMENT '字典名称',
                                  `dict_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT '' COMMENT '字典类型',
                                  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',
                                  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT '' COMMENT '创建者',
                                  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
                                  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT '' COMMENT '更新者',
                                  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
                                  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '备注',
                                  PRIMARY KEY (`dict_id`) USING BTREE,
                                  UNIQUE INDEX `dict_type`(`dict_type` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 234 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_520_ci COMMENT = '字典类型表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sys_job
-- ----------------------------
DROP TABLE IF EXISTS `sys_job`;
CREATE TABLE `sys_job`  (
                            `job_id` bigint NOT NULL AUTO_INCREMENT COMMENT '任务ID',
                            `job_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL DEFAULT '' COMMENT '任务名称',
                            `job_group` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL DEFAULT 'DEFAULT' COMMENT '任务组名',
                            `invoke_target` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL COMMENT '调用目标字符串',
                            `cron_expression` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT '' COMMENT 'cron执行表达式',
                            `misfire_policy` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT '3' COMMENT '计划执行错误策略（1立即执行 2执行一次 3放弃执行）',
                            `concurrent` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT '1' COMMENT '是否并发执行（0允许 1禁止）',
                            `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT '0' COMMENT '状态（0正常 1暂停）',
                            `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT '' COMMENT '创建者',
                            `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
                            `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT '' COMMENT '更新者',
                            `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
                            `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT '' COMMENT '备注信息',
                            PRIMARY KEY (`job_id`, `job_name`, `job_group`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 34 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_520_ci COMMENT = '定时任务调度表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sys_job_log
-- ----------------------------
DROP TABLE IF EXISTS `sys_job_log`;
CREATE TABLE `sys_job_log`  (
                                `job_log_id` bigint NOT NULL AUTO_INCREMENT COMMENT '任务日志ID',
                                `job_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL COMMENT '任务名称',
                                `job_group` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL COMMENT '任务组名',
                                `invoke_target` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL COMMENT '调用目标字符串',
                                `job_message` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '日志信息',
                                `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT '0' COMMENT '执行状态（0正常 1失败）',
                                `exception_info` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT '' COMMENT '异常信息',
                                `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
                                PRIMARY KEY (`job_log_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 692 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_520_ci COMMENT = '定时任务调度日志表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sys_logininfor
-- ----------------------------
DROP TABLE IF EXISTS `sys_logininfor`;
CREATE TABLE `sys_logininfor`  (
                                   `info_id` bigint NOT NULL AUTO_INCREMENT COMMENT '访问ID',
                                   `user_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT '' COMMENT '用户账号',
                                   `ipaddr` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT '' COMMENT '登录IP地址',
                                   `login_location` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT '' COMMENT '登录地点',
                                   `browser` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT '' COMMENT '浏览器类型',
                                   `os` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT '' COMMENT '操作系统',
                                   `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT '0' COMMENT '登录状态（0成功 1失败）',
                                   `msg` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT '' COMMENT '提示消息',
                                   `login_time` datetime NULL DEFAULT NULL COMMENT '访问时间',
                                   PRIMARY KEY (`info_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 76 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_520_ci COMMENT = '系统访问记录' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sys_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu`  (
                             `menu_id` bigint NOT NULL AUTO_INCREMENT COMMENT '菜单ID',
                             `menu_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL COMMENT '菜单名称',
                             `parent_id` bigint NULL DEFAULT 0 COMMENT '父菜单ID',
                             `order_num` int NULL DEFAULT 0 COMMENT '显示顺序',
                             `path` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT '' COMMENT '路由地址',
                             `component` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '组件路径',
                             `query` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '路由参数',
                             `is_frame` int NULL DEFAULT 1 COMMENT '是否为外链（0是 1否）',
                             `is_cache` int NULL DEFAULT 0 COMMENT '是否缓存（0缓存 1不缓存）',
                             `menu_type` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT '' COMMENT '菜单类型（M目录 C菜单 F按钮）',
                             `visible` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT '0' COMMENT '菜单状态（0显示 1隐藏）',
                             `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT '0' COMMENT '菜单状态（0正常 1停用）',
                             `perms` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '权限标识',
                             `icon` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT '#' COMMENT '菜单图标',
                             `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT '' COMMENT '创建者',
                             `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
                             `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT '' COMMENT '更新者',
                             `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
                             `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT '' COMMENT '备注',
                             PRIMARY KEY (`menu_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3025 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_520_ci COMMENT = '菜单权限表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sys_notice
-- ----------------------------
DROP TABLE IF EXISTS `sys_notice`;
CREATE TABLE `sys_notice`  (
                               `notice_id` int NOT NULL AUTO_INCREMENT COMMENT '公告ID',
                               `notice_title` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL COMMENT '公告标题',
                               `notice_type` int NOT NULL COMMENT '公告类型 1:公告,2:咨询中心,3:基金百科',
                               `notice_content` longblob NULL COMMENT '公告内容',
                               `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT '0' COMMENT '公告状态（0正常 1关闭）',
                               `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT '' COMMENT '创建者',
                               `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
                               `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT '' COMMENT '更新者',
                               `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                               `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '备注',
                               `type` int NULL DEFAULT 0 COMMENT '语言类型 1:繁体,2:英文,3:韩文,4:日文',
                               `image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '封面图',
                               `content_image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '内容图(基金百科的时候才会有内容图)',
                               PRIMARY KEY (`notice_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 44 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_520_ci COMMENT = '通知公告表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sys_oper_log
-- ----------------------------
DROP TABLE IF EXISTS `sys_oper_log`;
CREATE TABLE `sys_oper_log`  (
                                 `oper_id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志主键',
                                 `title` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT '' COMMENT '模块标题',
                                 `business_type` int NULL DEFAULT 0 COMMENT '业务类型（0其它 1新增 2修改 3删除）',
                                 `method` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT '' COMMENT '方法名称',
                                 `request_method` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT '' COMMENT '请求方式',
                                 `operator_type` int NULL DEFAULT 0 COMMENT '操作类别（0其它 1后台用户 2手机端用户）',
                                 `oper_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT '' COMMENT '操作人员',
                                 `dept_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT '' COMMENT '部门名称',
                                 `oper_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT '' COMMENT '请求URL',
                                 `oper_ip` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT '' COMMENT '主机地址',
                                 `oper_location` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT '' COMMENT '操作地点',
                                 `oper_param` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT '' COMMENT '请求参数',
                                 `json_result` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT '' COMMENT '返回参数',
                                 `status` int NULL DEFAULT 0 COMMENT '操作状态（0正常 1异常）',
                                 `error_msg` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT '' COMMENT '错误消息',
                                 `oper_time` datetime NULL DEFAULT NULL COMMENT '操作时间',
                                 PRIMARY KEY (`oper_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 359 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_520_ci COMMENT = '操作日志记录' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sys_post
-- ----------------------------
DROP TABLE IF EXISTS `sys_post`;
CREATE TABLE `sys_post`  (
                             `post_id` bigint NOT NULL AUTO_INCREMENT COMMENT '岗位ID',
                             `post_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL COMMENT '岗位编码',
                             `post_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL COMMENT '岗位名称',
                             `post_sort` int NOT NULL COMMENT '显示顺序',
                             `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL COMMENT '状态（0正常 1停用）',
                             `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT '' COMMENT '创建者',
                             `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
                             `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT '' COMMENT '更新者',
                             `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
                             `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '备注',
                             PRIMARY KEY (`post_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_520_ci COMMENT = '岗位信息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role`  (
                             `role_id` bigint NOT NULL AUTO_INCREMENT COMMENT '角色ID',
                             `role_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL COMMENT '角色名称',
                             `role_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL COMMENT '角色权限字符串',
                             `role_sort` int NOT NULL COMMENT '显示顺序',
                             `data_scope` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT '1' COMMENT '数据范围（1：全部数据权限 2：自定数据权限 3：本部门数据权限 4：本部门及以下数据权限）',
                             `menu_check_strictly` tinyint(1) NULL DEFAULT 1 COMMENT '菜单树选择项是否关联显示',
                             `dept_check_strictly` tinyint(1) NULL DEFAULT 1 COMMENT '部门树选择项是否关联显示',
                             `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL COMMENT '角色状态（0正常 1停用）',
                             `del_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
                             `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT '' COMMENT '创建者',
                             `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
                             `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT '' COMMENT '更新者',
                             `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
                             `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '备注',
                             PRIMARY KEY (`role_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_520_ci COMMENT = '角色信息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sys_role_dept
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_dept`;
CREATE TABLE `sys_role_dept`  (
                                  `role_id` bigint NOT NULL COMMENT '角色ID',
                                  `dept_id` bigint NOT NULL COMMENT '部门ID',
                                  PRIMARY KEY (`role_id`, `dept_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_520_ci COMMENT = '角色和部门关联表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sys_role_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_menu`;
CREATE TABLE `sys_role_menu`  (
                                  `role_id` bigint NOT NULL COMMENT '角色ID',
                                  `menu_id` bigint NOT NULL COMMENT '菜单ID',
                                  PRIMARY KEY (`role_id`, `menu_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_520_ci COMMENT = '角色和菜单关联表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
                             `user_id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
                             `dept_id` bigint NULL DEFAULT NULL COMMENT '部门ID',
                             `user_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL COMMENT '用户账号',
                             `nick_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL COMMENT '用户昵称',
                             `user_type` varchar(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT '00' COMMENT '用户类型（00系统用户）',
                             `email` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT '' COMMENT '用户邮箱',
                             `phonenumber` varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT '' COMMENT '手机号码',
                             `sex` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT '0' COMMENT '用户性别（0男 1女 2未知）',
                             `avatar` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT '' COMMENT '头像地址',
                             `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT '' COMMENT '密码',
                             `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT '0' COMMENT '帐号状态（0正常 1停用）',
                             `del_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
                             `token` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '最近一次登录token',
                             `login_ip` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT '' COMMENT '最后登录IP',
                             `login_date` datetime NULL DEFAULT NULL COMMENT '最后登录时间',
                             `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT '' COMMENT '创建者',
                             `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
                             `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT '' COMMENT '更新者',
                             `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
                             `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '备注',
                             `google_key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '谷歌验证器的key，绑定之后保存',
                             `google_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '谷歌验证器code，用于绑定',
                             `login_duration` int NULL DEFAULT 0 COMMENT '可登陆时长（小时） -1无限制',
                             `binding` bigint NULL DEFAULT 0 COMMENT '是否绑定google 0否  1是',
                             PRIMARY KEY (`user_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 12 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_520_ci COMMENT = '用户信息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sys_user_post
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_post`;
CREATE TABLE `sys_user_post`  (
                                  `user_id` bigint NOT NULL COMMENT '用户ID',
                                  `post_id` bigint NOT NULL COMMENT '岗位ID',
                                  PRIMARY KEY (`user_id`, `post_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_520_ci COMMENT = '用户与岗位关联表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sys_user_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role`  (
                                  `user_id` bigint NOT NULL COMMENT '用户ID',
                                  `role_id` bigint NOT NULL COMMENT '角色ID',
                                  PRIMARY KEY (`user_id`, `role_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_520_ci COMMENT = '用户和角色关联表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for t_asset_transfer_record
-- ----------------------------
DROP TABLE IF EXISTS `t_asset_transfer_record`;
CREATE TABLE `t_asset_transfer_record`  (
                                            `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键id',
                                            `user_id` bigint NOT NULL COMMENT '用户ID',
                                            `from_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '划出地址',
                                            `to_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '划入地址',
                                            `source_order_no` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL COMMENT '第三方来源订单号',
                                            `coin_type` int NULL DEFAULT 4 COMMENT '币种 1:USDT,2:DFC,3:OORT,4:锁定USDT',
                                            `order_no` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '订单号',
                                            `recharge_amount` decimal(20, 6) NOT NULL DEFAULT 0.000000 COMMENT '划转金额',
                                            `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '备注',
                                            `arrival_amount` decimal(20, 6) NULL DEFAULT NULL COMMENT '到账金额',
                                            `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                            `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
                                            PRIMARY KEY (`id`) USING BTREE,
                                            UNIQUE INDEX `uk_order_no`(`source_order_no` ASC) USING BTREE,
                                            INDEX `idx_user_id`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 223 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'DF划转记录' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for t_banner
-- ----------------------------
DROP TABLE IF EXISTS `t_banner`;
CREATE TABLE `t_banner`  (
                             `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
                             `image` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '图片路径',
                             `url` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '图片url',
                             `notice_id` bigint NULL DEFAULT NULL COMMENT '公告id(废弃)',
                             `sort` int UNSIGNED NULL DEFAULT 1 COMMENT '图片排序',
                             `content` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '图片说明',
                             `enable` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',
                             `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
                             `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
                             `type` int NULL DEFAULT 0 COMMENT '语言类型 1:繁体,2:英文,3:韩文,4:日文',
                             PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 34 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_520_ci COMMENT = 'appBanner图' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for t_contract
-- ----------------------------
DROP TABLE IF EXISTS `t_contract`;
CREATE TABLE `t_contract`  (
                               `id` int NOT NULL AUTO_INCREMENT COMMENT '主键id',
                               `type` tinyint NULL DEFAULT 1 COMMENT '类型(1.用户协议 2.合同)',
                               `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '内容',
                               `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                               `update_time` timestamp NULL DEFAULT NULL COMMENT '修改时间',
                               `active_flag` tinyint NOT NULL DEFAULT 1 COMMENT '是否删除（1:否,2:是）',
                               `biz_type` int NULL DEFAULT 0 COMMENT '语言类型 0:繁体中文,1:英文',
                               `status` int NULL DEFAULT 1 COMMENT '状态 1: 上架 2:下架',
                               `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '创建人',
                               `update_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '更新人',
                               `deleted` int NOT NULL DEFAULT 0 COMMENT '是否删除 否 0  1 是',
                               `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '备注',
                               PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '合同协议表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for t_email_config
-- ----------------------------
DROP TABLE IF EXISTS `t_email_config`;
CREATE TABLE `t_email_config`  (
                                   `id` bigint NOT NULL AUTO_INCREMENT COMMENT '自增主键',
                                   `email` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL,
                                   `status` int NULL DEFAULT 0 COMMENT '状态 0:正常,1:禁用',
                                   `enable` int NULL DEFAULT 0 COMMENT '是否启用 0:否,1:是',
                                   `app_auth_password` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '应用专式密码',
                                   `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                   `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
                                   `deleted` int NOT NULL DEFAULT 0 COMMENT '是否删除（0:否,1:是）',
                                   PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_520_ci COMMENT = '谷歌邮箱配置' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for t_mining_mgmt_fee_config
-- ----------------------------
DROP TABLE IF EXISTS `t_mining_mgmt_fee_config`;
CREATE TABLE `t_mining_mgmt_fee_config`  (
                                             `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
                                             `fee_pool_ratio` decimal(10, 4) NULL DEFAULT 20.0000 COMMENT '管理费池比例(单位%)，默认20=20%',
                                             `agent_diff_county_ratio` decimal(10, 4) NOT NULL DEFAULT 1.0000 COMMENT '县代理总比例(单位%)',
                                             `agent_diff_area_ratio` decimal(10, 4) NOT NULL DEFAULT 2.0000 COMMENT '区代理总比例(单位%)',
                                             `agent_diff_city_ratio` decimal(10, 4) NOT NULL DEFAULT 3.0000 COMMENT '市代理总比例(单位%)',
                                             `agent_diff_province_ratio` decimal(10, 4) NOT NULL DEFAULT 4.0000 COMMENT '省代理总比例(单位%)',
                                             `agent_diff_national_ratio` decimal(10, 4) NOT NULL DEFAULT 5.0000 COMMENT '全国代理总比例(单位%)',
                                             `national_same_level_ratio` decimal(10, 4) NOT NULL DEFAULT 10.0000 COMMENT '全国代理平级奖比例(单位%)：取全国级差奖励的X%',
                                             `platform_fee_ratio` decimal(10, 4) NOT NULL DEFAULT 8.0000 COMMENT '平台管理费比例(单位%)',
                                             `direct_push_ratio` decimal(10, 4) NOT NULL DEFAULT 3.0000 COMMENT '直推奖励比例(单位%)',
                                             `indirect_push_ratio` decimal(10, 4) NOT NULL DEFAULT 1.0000 COMMENT '间推奖励比例(单位%)',
                                             `service_center_ratio` decimal(10, 4) NOT NULL DEFAULT 2.0000 COMMENT '服务中心比例(单位%)',
                                             `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '备注',
                                             `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                             `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
                                             `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '删除标记 0:未删,1:已删',
                                             PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_520_ci COMMENT = '矿机每日产出管理费(20%)配置表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for t_mining_package
-- ----------------------------
DROP TABLE IF EXISTS `t_mining_package`;
CREATE TABLE `t_mining_package`  (
                                     `id` int NOT NULL AUTO_INCREMENT COMMENT '主键id',
                                     `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL DEFAULT '1' COMMENT '矿机名称',
                                     `sales` int UNSIGNED NOT NULL DEFAULT 0 COMMENT '销量',
                                     `available_stock` int NOT NULL DEFAULT 0 COMMENT '可售库存数量',
                                     `price` decimal(20, 6) NOT NULL DEFAULT 0.000000 COMMENT '矿机价格',
                                     `status` int NOT NULL DEFAULT 0 COMMENT '是否上架 0:否,1:是',
                                     `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                     `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
                                     `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '算力',
                                     PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_520_ci COMMENT = '矿机套餐' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for t_mining_package_order
-- ----------------------------
DROP TABLE IF EXISTS `t_mining_package_order`;
CREATE TABLE `t_mining_package_order`  (
                                           `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
                                           `mining_no` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '矿机编号',
                                           `order_no` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '订单号',
                                           `master_order_no` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '主订单号 业务场景:一次性可以买多个矿机标识哪几个订单是同一个时间点买的',
                                           `user_id` bigint NOT NULL DEFAULT 1 COMMENT '用户id',
                                           `order_value_usdt` decimal(20, 6) NULL DEFAULT 0.000000 COMMENT '订单价值多少usdt',
                                           `pay_valid_num1` decimal(20, 6) NULL DEFAULT 0.000000 COMMENT '支付usdt金额',
                                           `pay_valid_num2` decimal(20, 6) NULL DEFAULT 0.000000 COMMENT '支付dfc金额',
                                           `pay_valid_num4` decimal(20, 6) NULL DEFAULT 0.000000 COMMENT '支付锁定usdt金额',
                                           `pay_type` int NULL DEFAULT 1 COMMENT '支付方式 1:USDT,2:DFC',
                                           `run_days` int NULL DEFAULT 0 COMMENT '运行天数',
                                           `source_type` int NULL DEFAULT 0 COMMENT '订单来源 0:购买,1:后台拨付',
                                           `day_reward` decimal(20, 6) NULL DEFAULT 0.000000 COMMENT '每日收益',
                                           `stake_dfc_amount` decimal(20, 6) NULL DEFAULT 0.000000 COMMENT '质押dfc数量',
                                           `total_reward` decimal(30, 8) NULL DEFAULT 0.00000000 COMMENT '累计收益',
                                           `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                           `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
                                           `status` int NULL DEFAULT 0 COMMENT '状态 0:未质押,1:已质押',
                                           `biz_status` int NULL DEFAULT 0 COMMENT '购买矿机业务状态 0:未处理,1:已处理',
                                           `biz_status1` int NULL DEFAULT 0 COMMENT '质押矿机业务状态 0:未处理,1:已处理',
                                           `remark` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL COMMENT '备注(物流收货信息)',
                                           `dfc_price` decimal(20, 6) NULL DEFAULT 0.000000 COMMENT 'dfc的价格',
                                           `stake_type` int NULL DEFAULT 0 COMMENT '质押类型 1:托管,2:自提',
                                           `stake_startup_remaining_days` int NOT NULL DEFAULT 0 COMMENT '质押启动期剩余天数',
                                           `computing_power` decimal(20, 6) NULL DEFAULT 0.000000 COMMENT '算力',
                                           `stake_date` datetime NULL DEFAULT NULL COMMENT '质押时间',
                                           `shipping_status` tinyint NOT NULL DEFAULT 0 COMMENT '发货状态 0:未发货,1:已发货',
                                           `shipping_company` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '物流公司名称',
                                           `tracking_no` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '物流单号',
                                           PRIMARY KEY (`id`) USING BTREE,
                                           INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
                                           INDEX `idx_order_no`(`order_no` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 78 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_520_ci COMMENT = '矿机订单表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for t_mining_package_tier
-- ----------------------------
DROP TABLE IF EXISTS `t_mining_package_tier`;
CREATE TABLE `t_mining_package_tier`  (
                                          `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
                                          `start_index` int NOT NULL COMMENT '区间起始(含)',
                                          `end_index` int NULL DEFAULT NULL COMMENT '区间结束(含)',
                                          `stake_amount` decimal(20, 6) NOT NULL DEFAULT 0.000000 COMMENT '质押金额(DFC)',
                                          `day_reward` decimal(20, 6) NOT NULL DEFAULT 0.000000 COMMENT '日产出(DFC)',
                                          `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                          `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
                                          `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '备注',
                                          PRIMARY KEY (`id`) USING BTREE,
                                          INDEX `idx_range`(`start_index` ASC, `end_index` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_520_ci COMMENT = '矿机质押区间配置表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for t_mining_reward_config
-- ----------------------------
DROP TABLE IF EXISTS `t_mining_reward_config`;
CREATE TABLE `t_mining_reward_config`  (
                                           `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                           `reward_level` int NOT NULL COMMENT '奖励角色 1:直推,2:间推,3:市代理,4:省代理,5:全国代理',
                                           `reward_ratio` decimal(10, 4) NOT NULL DEFAULT 0.0000 COMMENT '奖励比例 例如:1代表1%',
                                           `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                           `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
                                           `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT '' COMMENT '创建者',
                                           `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT '' COMMENT '更新者',
                                           `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'remark',
                                           PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '矿机奖励分配配置表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for t_mq_transaction_log
-- ----------------------------
DROP TABLE IF EXISTS `t_mq_transaction_log`;
CREATE TABLE `t_mq_transaction_log`  (
                                         `id` int NOT NULL AUTO_INCREMENT COMMENT '主键，目前版本除非断电，否则不重置',
                                         `transaction_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '事务id，唯一消息ID，发送完删除,事务消息需要',
                                         `log` mediumblob NULL COMMENT '日志body内容,最大16M',
                                         `create_time` bigint NULL DEFAULT NULL COMMENT '时间戳,毫秒级',
                                         PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'mq可靠投递日志表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for t_node_package
-- ----------------------------
DROP TABLE IF EXISTS `t_node_package`;
CREATE TABLE `t_node_package`  (
                                   `id` int NOT NULL AUTO_INCREMENT COMMENT '主键id',
                                   `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL DEFAULT '1' COMMENT '节点名称',
                                   `sales` int UNSIGNED NULL DEFAULT 0 COMMENT '销量',
                                   `price` decimal(20, 6) NOT NULL DEFAULT 0.000000 COMMENT '节点价格',
                                   `level` int NULL DEFAULT 0 COMMENT '等级',
                                   `direct_referral_rate` decimal(5, 2) NOT NULL DEFAULT 0.00 COMMENT '直推奖励比例(%)',
                                   `indirect_referral_rate` decimal(5, 2) NULL DEFAULT 0.00 COMMENT '间推奖励比例(%)，无则0',
                                   `weight_multiplier` decimal(10, 2) NOT NULL DEFAULT 1.00 COMMENT '权重系数(倍数)',
                                   `pred_order_fee_relief_rate` decimal(5, 2) NOT NULL DEFAULT 0.00 COMMENT '预测下单手续费减免比例(%)',
                                   `status` int NOT NULL DEFAULT 0 COMMENT '是否上架 0:否,1:是',
                                   `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                   `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
                                   `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '备注',
                                   PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_520_ci COMMENT = '节点套餐' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for t_node_package_order
-- ----------------------------
DROP TABLE IF EXISTS `t_node_package_order`;
CREATE TABLE `t_node_package_order`  (
                                         `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
                                         `order_no` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '订单号',
                                         `user_id` bigint NOT NULL COMMENT '用户id',
                                         `address` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '钱包地址',
                                         `hash` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '支付hash',
                                         `package_level` int NOT NULL DEFAULT 0 COMMENT '下单时节点等级快照',
                                         `direct_referral_rate` decimal(5, 2) NOT NULL DEFAULT 0.00 COMMENT '下单时直推奖励比例快照(%)',
                                         `indirect_referral_rate` decimal(5, 2) NULL DEFAULT 0.00 COMMENT '下单时间推奖励比例快照(%)，无则NULL',
                                         `weight_multiplier` decimal(10, 2) NOT NULL DEFAULT 1.00 COMMENT '下单时权重系数快照(倍数)',
                                         `pred_order_fee_relief_rate` decimal(5, 2) NOT NULL DEFAULT 0.00 COMMENT '下单时预测下单手续费减免比例快照(%)',
                                         `order_value_usdt` decimal(20, 6) NULL DEFAULT 0.000000 COMMENT '支付金额',
                                         `source_type` int NULL DEFAULT 0 COMMENT '订单来源 0:购买,1:后台拨付',
                                         `status` int NULL DEFAULT 0 COMMENT '订单状态 0:未支付,1:支付成功',
                                         `biz_status` int NULL DEFAULT 0 COMMENT '业务处理状态 0:未处理,1:已处理',
                                         `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                         `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
                                         `pay_time` datetime NULL DEFAULT NULL COMMENT '支付时间',
                                         PRIMARY KEY (`id`) USING BTREE,
                                         UNIQUE INDEX `uniqe_hash`(`hash` ASC) USING BTREE,
                                         INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
                                         INDEX `idx_order_no`(`order_no` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 14 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_520_ci COMMENT = '节点套餐购买订单(参数快照)' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for t_recharge_record
-- ----------------------------
DROP TABLE IF EXISTS `t_recharge_record`;
CREATE TABLE `t_recharge_record`  (
                                      `id` int UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键id',
                                      `user_id` int NOT NULL COMMENT '用户ID',
                                      `order_no` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL COMMENT '充值订单号',
                                      `coin_type` int NULL DEFAULT 1 COMMENT '币种 1:USDT,2:DFC,3:OORT',
                                      `recharge_amount` decimal(20, 6) NOT NULL DEFAULT 0.000000 COMMENT '充值金额',
                                      `status` int NOT NULL DEFAULT 0 COMMENT '状态(0:等待充值,1:充值成功 2:已过期)',
                                      `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '充值地址',
                                      `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                      `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
                                      `tx_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'hash',
                                      PRIMARY KEY (`id`) USING BTREE,
                                      UNIQUE INDEX `uk_order_no`(`order_no` ASC) USING BTREE,
                                      INDEX `idx_user_id`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 39 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '充值记录表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for t_stake_hosting_order
-- ----------------------------
DROP TABLE IF EXISTS `t_stake_hosting_order`;
CREATE TABLE `t_stake_hosting_order`  (
                                          `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                          `order_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL COMMENT '托管订单号',
                                          `user_id` bigint NOT NULL COMMENT '用户ID',
                                          `account` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL COMMENT '用户钱包地址',
                                          `package_id` bigint NOT NULL COMMENT '套餐ID',
                                          `package_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL COMMENT '套餐名称快照',
                                          `package_days` int NOT NULL COMMENT '套餐天数快照',
                                          `stake_usdt_amount` decimal(20, 6) NOT NULL DEFAULT 0.000000 COMMENT '托管USDT金额',
                                          `source_type` int NOT NULL DEFAULT 0 COMMENT '订单来源 0:用户购买 1:后台拨付',
                                          `pay_status` int NOT NULL DEFAULT 0 COMMENT '支付状态 0:待支付 1:支付成功',
                                          `status` int NOT NULL DEFAULT 0 COMMENT '业务状态 0:未开始 1:产出中 2:已完成 3:已暂停',
                                          `pay_hash` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '支付hash',
                                          `pay_amount` decimal(20, 6) NULL DEFAULT 0.000000 COMMENT '链上支付金额',
                                          `pay_time` datetime NULL DEFAULT NULL COMMENT '支付时间',
                                          `effective_time` datetime NULL DEFAULT NULL COMMENT '生效时间',
                                          `finish_time` datetime NULL DEFAULT NULL COMMENT '完成时间',
                                          `run_days` int NOT NULL DEFAULT 0 COMMENT '已运行天数，即已发放静态收益次数',
                                          `today_reward` decimal(20, 6) NOT NULL DEFAULT 0.000000 COMMENT '今日静态收益',
                                          `total_static_reward` decimal(20, 6) NOT NULL DEFAULT 0.000000 COMMENT '累计已发静态收益',
                                          `is_return_principal` int NOT NULL DEFAULT 0 COMMENT '是否回本 0:否 1:是',
                                          `last_reward_day` int NULL DEFAULT NULL COMMENT '最近一次发放日期，格式yyyyMMdd',
                                          `create_day` int NULL DEFAULT NULL COMMENT '创建日期，格式yyyyMMdd',
                                          `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '创建者',
                                          `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                          `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '更新者',
                                          `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
                                          `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '备注',
                                          `deleted` int NOT NULL DEFAULT 0 COMMENT '删除标志 0正常 1删除',
                                          PRIMARY KEY (`id`) USING BTREE,
                                          UNIQUE INDEX `uk_order_no`(`order_no` ASC) USING BTREE,
                                          UNIQUE INDEX `uk_pay_hash`(`pay_hash` ASC) USING BTREE,
                                          INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
                                          INDEX `idx_status`(`status` ASC) USING BTREE,
                                          INDEX `idx_pay_status`(`pay_status` ASC) USING BTREE,
                                          INDEX `idx_source_type`(`source_type` ASC) USING BTREE,
                                          INDEX `idx_create_time`(`create_time` ASC) USING BTREE,
                                          INDEX `idx_last_reward_day`(`last_reward_day` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_520_ci COMMENT = '托管订单表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for t_stake_hosting_package
-- ----------------------------
DROP TABLE IF EXISTS `t_stake_hosting_package`;
CREATE TABLE `t_stake_hosting_package`  (
                                            `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                            `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL COMMENT '套餐名称',
                                            `days` int NOT NULL COMMENT '托管天数，固定为1/30/90/180/360',
                                            `min_amount` decimal(20, 6) NOT NULL DEFAULT 10.000000 COMMENT '最低起购USDT金额',
                                            `service_fee_ratio` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '服务费比例，单位%',
                                            `sort` int NOT NULL DEFAULT 0 COMMENT '排序',
                                            `status` int NOT NULL DEFAULT 0 COMMENT '状态 0:下架 1:上架',
                                            `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '创建者',
                                            `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                            `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '更新者',
                                            `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
                                            `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '备注',
                                            `deleted` int NOT NULL DEFAULT 0 COMMENT '删除标志 0正常 1删除',
                                            PRIMARY KEY (`id`) USING BTREE,
                                            UNIQUE INDEX `uk_days`(`days` ASC) USING BTREE,
                                            INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_520_ci COMMENT = '托管套餐表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for t_stake_hosting_reward_settlement
-- ----------------------------
DROP TABLE IF EXISTS `t_stake_hosting_reward_settlement`;
CREATE TABLE `t_stake_hosting_reward_settlement`  (
                                                     `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                                     `settlement_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL COMMENT '结算单号',
                                                     `source_order_id` bigint NULL DEFAULT NULL COMMENT '源托管订单ID',
                                                     `source_order_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '源托管订单号',
                                                     `source_user_id` bigint NULL DEFAULT NULL COMMENT '源用户ID',
                                                     `receive_user_id` bigint NULL DEFAULT NULL COMMENT '接收用户ID',
                                                     `reward_type` int NOT NULL DEFAULT 0 COMMENT '奖励类型 1:静态服务费结算 2:直推奖 3:极差奖 4:平级奖 5:平台沉淀',
                                                     `reward_level` int NULL DEFAULT NULL COMMENT '奖励等级',
                                                     `reward_base_amount` decimal(20, 6) NOT NULL DEFAULT 0.000000 COMMENT '奖励基数',
                                                     `reward_ratio` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '奖励比例，单位%',
                                                     `reward_amount` decimal(20, 6) NOT NULL DEFAULT 0.000000 COMMENT '奖励金额',
                                                     `gross_static_reward` decimal(20, 6) NOT NULL DEFAULT 0.000000 COMMENT '静态毛收益',
                                                     `service_fee_ratio` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '服务费比例，单位%',
                                                     `service_fee_amount` decimal(20, 6) NOT NULL DEFAULT 0.000000 COMMENT '服务费金额',
                                                     `net_static_reward` decimal(20, 6) NOT NULL DEFAULT 0.000000 COMMENT '静态净收益',
                                                     `arrival_status` int NOT NULL DEFAULT 0 COMMENT '到账状态 0:未到账 1:已到账',
                                                     `skip_reason` int NULL DEFAULT NULL COMMENT '未到账原因 1:无上级 2:无有效托管订单 3:后台拨付订单不触发 4:无效用户',
                                                     `settlement_day` int NOT NULL COMMENT '结算日期，格式yyyyMMdd',
                                                     `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '创建者',
                                                     `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                                     `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '更新者',
                                                     `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
                                                     `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '备注',
                                                     `deleted` int NOT NULL DEFAULT 0 COMMENT '删除标志 0正常 1删除',
                                                     PRIMARY KEY (`id`) USING BTREE,
                                                     UNIQUE INDEX `uk_settlement_no`(`settlement_no` ASC) USING BTREE,
                                                     INDEX `idx_source_order_no`(`source_order_no` ASC) USING BTREE,
                                                     INDEX `idx_source_user_id`(`source_user_id` ASC) USING BTREE,
                                                     INDEX `idx_receive_user_id`(`receive_user_id` ASC) USING BTREE,
                                                     INDEX `idx_reward_type`(`reward_type` ASC) USING BTREE,
                                                     INDEX `idx_settlement_day`(`settlement_day` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_520_ci COMMENT = '托管奖励结算明细表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for t_stake_order
-- ----------------------------
DROP TABLE IF EXISTS `t_stake_order`;
CREATE TABLE `t_stake_order`  (
                                  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
                                  `user_id` bigint NOT NULL COMMENT '用户id',
                                  `order_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL COMMENT '质押订单号',
                                  `stake_usdt_amount` decimal(20, 6) NOT NULL DEFAULT 0.000000 COMMENT '质押金额/USDT单位',
                                  `status` int NOT NULL DEFAULT 0 COMMENT '状态 0:待链上支付确认,1:产出中,2:已出局(已完成),3:已过期未支付,4:已暂停产出',
                                  `remaining_out_amount` decimal(20, 6) NULL DEFAULT 0.000000 COMMENT '剩余可产出',
                                  `all_out_amount` decimal(20, 6) NULL DEFAULT NULL COMMENT '出局目标产出总额',
                                  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
                                  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '备注',
                                  `pay_time` datetime NULL DEFAULT NULL COMMENT '支付时间',
                                  `pay_hash` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '支付hash',
                                  `today_reward` decimal(20, 6) NULL DEFAULT 0.000000 COMMENT '今日收益',
                                  `biz_status1` int NULL DEFAULT 0 COMMENT '业务状态是否处理 0:否,1:是',
                                  `belong_user_id` bigint NULL DEFAULT 0 COMMENT '业绩归属上级用户id',
                                  `create_day` int NULL DEFAULT NULL COMMENT '创建日期 格式为20260101',
                                  `run_days` int NULL DEFAULT 0 COMMENT '运行天数',
                                  `hash_first_ship_order` int NULL DEFAULT 0 COMMENT '本订单是否命中首单发货 0否1是',
                                  `ship_status` int NULL DEFAULT 0 COMMENT '发货状态 0待发货 1已发货',
                                  `ship_time` datetime NULL DEFAULT NULL COMMENT '发货时间',
                                  `receiver_info` json NULL COMMENT '收货信息JSON快照',
                                  `ship_company` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '物流公司',
                                  `ship_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '物流单号',
                                  `product_snapshot` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL COMMENT '商品快照信息',
                                  `num` int NULL DEFAULT 1 COMMENT '商品数量',
                                  PRIMARY KEY (`id`) USING BTREE,
                                  UNIQUE INDEX `uk_order_no`(`order_no` ASC) USING BTREE,
                                  UNIQUE INDEX `uk_pay_hash`(`pay_hash` ASC) USING BTREE,
                                  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
                                  INDEX `idx_create_day`(`create_day` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 49 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_520_ci COMMENT = '质押订单表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for t_stake_product
-- ----------------------------
DROP TABLE IF EXISTS `t_stake_product`;
CREATE TABLE `t_stake_product`  (
                                    `id` int NOT NULL AUTO_INCREMENT COMMENT '主键id',
                                    `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL DEFAULT '1' COMMENT '质押名称',
                                    `sales` int UNSIGNED NOT NULL DEFAULT 0 COMMENT '销量',
                                    `stake_unit_amount_min` decimal(20, 6) NOT NULL DEFAULT 0.000000 COMMENT '最低质押',
                                    `max_stake_amount` decimal(20, 6) NULL DEFAULT 0.000000 COMMENT '最高质押',
                                    `static_ratio` decimal(20, 6) NOT NULL DEFAULT 0.000000 COMMENT '静态日利率 例如: 1就是1%',
                                    `exit_multiplier` decimal(20, 6) NULL DEFAULT NULL COMMENT '出局倍数',
                                    `is_enabled` int NOT NULL DEFAULT 0 COMMENT '是否上架 0:否,1:是',
                                    `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                    `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
                                    `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '备注',
                                    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_520_ci COMMENT = '质押套餐' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for t_stake_release_bucket
-- ----------------------------
DROP TABLE IF EXISTS `t_stake_release_bucket`;
CREATE TABLE `t_stake_release_bucket`  (
                                           `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
                                           `order_no` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '订单号',
                                           `user_id` bigint NOT NULL COMMENT '用户id',
                                           `linear_days` int NOT NULL DEFAULT 270 COMMENT '线性释放天数(如270)',
                                           `have_days` int NULL DEFAULT 0 COMMENT '剩余释放天数',
                                           `total_amount` decimal(30, 6) NOT NULL DEFAULT 0.000000 COMMENT '桶内累计应线性释放总量',
                                           `remaining_amount` decimal(20, 6) NOT NULL DEFAULT 0.000000 COMMENT '桶内剩余待释放量',
                                           `daily_release_amount` decimal(20, 6) NULL DEFAULT 0.000000 COMMENT '每日释放的数量',
                                           `status` int NULL DEFAULT 0 COMMENT '状态 0:产出中,1:已到期,2:暂停',
                                           `start_time` int NULL DEFAULT NULL COMMENT '时间格式为:yyyymmdd,例如:20260101',
                                           `last_release_time` int NULL DEFAULT NULL COMMENT '上次释放时间 格式为:yyyymmdd,例如:20260101',
                                           `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                           `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
                                           `source_snapshot` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL COMMENT '来源快照(json)：记录桶由哪些订单/天数/金额',
                                           `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '备注',
                                           PRIMARY KEY (`id`) USING BTREE,
                                           INDEX `idx_user_id`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_520_ci COMMENT = '质押收益线性释放汇总桶' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for t_swap_order
-- ----------------------------
DROP TABLE IF EXISTS `t_swap_order`;
CREATE TABLE `t_swap_order`  (
                                 `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                 `user_id` bigint NULL DEFAULT NULL COMMENT '用户id 如果没有存在用户系统为0',
                                 `address` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '购买的钱包地址',
                                 `swap_amount` decimal(20, 6) NULL DEFAULT 0.000000 COMMENT 'swap数量',
                                 `available_amount` decimal(20, 6) NULL DEFAULT 0.000000 COMMENT '可用的额度',
                                 `tx_hash` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '交易hash',
                                 `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                 `expire_time` datetime NULL DEFAULT NULL COMMENT '过期时间(创建后24小时，到期未完成则处理)',
                                 `biz_status` int NULL DEFAULT 0 COMMENT '业务状态 1:待处理,2:已处理,3:未注册丢弃',
                                 `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
                                 `biz_status1` int NULL DEFAULT 0 COMMENT '是否处理提现额度 0:否,1:是',
                                 `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '备注',
                                 PRIMARY KEY (`id`) USING BTREE,
                                 UNIQUE INDEX `uniqe`(`tx_hash` ASC) USING BTREE,
                                 INDEX `idx_user_id`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_520_ci COMMENT = 'swap订单 合约通知回调' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for t_sys_para
-- ----------------------------
DROP TABLE IF EXISTS `t_sys_para`;
CREATE TABLE `t_sys_para`  (
                               `sys_para_id` int NOT NULL AUTO_INCREMENT COMMENT '主键id',
                               `para_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '参数内码',
                               `para_value` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '参数值',
                               `para_desc` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '参数描述',
                               `visible` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT '0' COMMENT '状态（0显示 1隐藏）',
                               `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                               `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
                               `active_flag` tinyint NOT NULL DEFAULT 1 COMMENT '是否删除（1:否,2:是）',
                               `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '备注',
                               PRIMARY KEY (`sys_para_id`) USING BTREE,
                               INDEX `idx_para_code`(`para_code` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_520_ci COMMENT = '系统参数表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for t_sys_version_info
-- ----------------------------
DROP TABLE IF EXISTS `t_sys_version_info`;
CREATE TABLE `t_sys_version_info`  (
                                       `id` int NOT NULL AUTO_INCREMENT,
                                       `version_no` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '版本号',
                                       `version_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '版本下载路径',
                                       `status` varchar(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '是否强制更新（0：否，1：是）',
                                       `device_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '设备类型：ios android',
                                       `note` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL COMMENT '更新内容备注',
                                       `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '热更新链接',
                                       `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                       `update_time` timestamp NULL DEFAULT NULL COMMENT '修改时间',
                                       `active_flag` tinyint NOT NULL DEFAULT 1 COMMENT '是否删除（1:否,2:是）',
                                       PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 31 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_520_ci COMMENT = '版本表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for t_user_info
-- ----------------------------
DROP TABLE IF EXISTS `t_user_info`;
CREATE TABLE `t_user_info`  (
                                `user_id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户id',
                                `account` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '钱包地址',
                                `user_code` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '用户编码',
                                `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '头像',
                                `email` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '邮箱',
                                `node_level` int NULL DEFAULT 0 COMMENT '节点等级 0:无,1:白银节点,2:黄金节点,3:翡翠节点,4:钻石节点',
                                `game_level` int NULL DEFAULT 0 COMMENT '真实等级',
                                `min_game_level` int NULL DEFAULT 0 COMMENT '赠送等级',
                                `admin_game_level` int NULL DEFAULT 0 COMMENT '管理员保底等级',
                                `invite_user_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '邀请用户编码',
                                `invite_user_id` bigint NULL DEFAULT NULL COMMENT '邀请用户id',
                                `status` tinyint NULL DEFAULT 1 COMMENT '状态(1.正常 2.冻结)',
                                `is_valid` tinyint NULL DEFAULT 0 COMMENT '是否有效用户(0.否 1.是)',
                                `sub_num` int NULL DEFAULT 0 COMMENT '直推用户数',
                                `valid_sub_num` int NULL DEFAULT 0 COMMENT '直推有效用户数(废弃)',
                                `umbrella_num` int NULL DEFAULT 0 COMMENT '团队用户数',
                                `valid_umbrella_num` int NULL DEFAULT 0 COMMENT '团队有效用户数(废弃)',
                                `performance` decimal(20, 6) NULL DEFAULT 0.000000 COMMENT '个人托管业绩',
                                `sub_performance` decimal(20, 6) NULL DEFAULT 0.000000 COMMENT '直推托管业绩',
                                `performance_mining` decimal(20, 6) NULL DEFAULT 0.000000 COMMENT '团队托管业绩兼容字段',
                                `umbrella_performance` decimal(20, 6) NULL DEFAULT 0.000000 COMMENT '伞下团队托管业绩',
                                `community_performance` decimal(20, 6) NULL DEFAULT 0.000000 COMMENT '小区托管业绩',
                                `sub_node_performance` decimal(20, 4) NULL DEFAULT 0.0000 COMMENT '直推节点数量',
                                `node_team_performance` decimal(20, 4) NULL DEFAULT 0.0000 COMMENT '团队节点数量',
                                `parent_chain` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '父级链',
                                `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
                                `withdrawal_open_or_close` int NULL DEFAULT 2 COMMENT 'USDT 提现开关(1.关 2.开)',
                                `last_login_ip` varchar(600) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '最近登录的ip地址',
                                `deleted` int NULL DEFAULT 0 COMMENT '删除标记,默认0,1:已删除',
                                PRIMARY KEY (`user_id`) USING BTREE,
                                UNIQUE INDEX `uk_user_code`(`user_code` ASC) USING BTREE,
                                UNIQUE INDEX `uk_account`(`account` ASC) USING BTREE,
                                INDEX `idx_invite_user_code`(`invite_user_code` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8056 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_520_ci COMMENT = '用户信息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for t_user_level_config
-- ----------------------------
DROP TABLE IF EXISTS `t_user_level_config`;
CREATE TABLE `t_user_level_config`  (
                                        `id` int NOT NULL AUTO_INCREMENT COMMENT '主键id',
                                        `level` int NOT NULL COMMENT 'F等级编码 0:暂无,1:F1,2:F2,3:F3,4:F4,5:F5,6:F6,7:F7,8:F8,9:F9',
                                        `performance` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '个人托管业绩',
                                        `team_performance` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '大区业绩(历史字段，本需求不参与等级考核)',
                                        `community_performance` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '小区托管业绩',
                                        `team_reward_ratio` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '团队奖励比例，单位%',
                                        `global_fee_dividend_ratio` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '全球手续费分红比例，单位%',
                                        `required_leg_num` int NULL DEFAULT 0 COMMENT '废弃字段：需要满足的线数量，本需求不参与等级考核',
                                        `leg_level_min` int NULL DEFAULT 0 COMMENT '废弃字段：线内代理最小等级，本需求不参与等级考核',
                                        `leg_level_count` int NULL DEFAULT 0 COMMENT '废弃字段：每条线里需要几个该等级及以上代理，本需求不参与等级考核',
                                        `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                        `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
                                        `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
                                        `deleted` int NULL DEFAULT 0 COMMENT '删除标记,默认0,1已删除',
                                        `create_by` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
                                        `update_by` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
                                        PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 31 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_520_ci COMMENT = '用户等级考核配置表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for t_user_money
-- ----------------------------
DROP TABLE IF EXISTS `t_user_money`;
CREATE TABLE `t_user_money`  (
                                 `id` int NOT NULL AUTO_INCREMENT COMMENT '主键id',
                                 `valid_num1` decimal(40, 8) NULL DEFAULT 0.00000000 COMMENT 'USDT',
                                 `valid_num2` decimal(40, 8) NULL DEFAULT 0.00000000 COMMENT '可用余额数',
                                 `valid_num3` decimal(40, 8) NULL DEFAULT 0.00000000 COMMENT '可用余额数',
                                 `valid_num4` decimal(40, 8) NULL DEFAULT 0.00000000 COMMENT '可用余额数',
                                 `valid_num5` decimal(30, 8) NULL DEFAULT 0.00000000 COMMENT '可用余额数',
                                 `valid_num6` decimal(30, 8) NULL DEFAULT 0.00000000 COMMENT '可用余额数',
                                 `valid_num7` decimal(30, 8) NULL DEFAULT 0.00000000 COMMENT '可用余额数',
                                 `valid_num8` decimal(30, 8) NULL DEFAULT 0.00000000 COMMENT '可用余额数',
                                 `valid_num9` decimal(30, 8) NULL DEFAULT 0.00000000 COMMENT '可用余额数',
                                 `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
                                 `gt_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL DEFAULT '0' COMMENT '每次更新的唯一序号，后续可用来修正数据,',
                                 `source_code` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL DEFAULT '0' COMMENT '来源订单',
                                 `source_type` int NOT NULL DEFAULT 0 COMMENT '来源类型(1.充值 2.提现 3.推荐奖 4.级差奖 5.平级奖 6.购买套餐 7.平台扣拨)',
                                 `source_id` bigint NOT NULL DEFAULT 0 COMMENT '来源用户ID',
                                 `deleted` int NULL DEFAULT 0 COMMENT '删除标记,默认0,1:已删除',
                                 PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8056 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_520_ci COMMENT = '用户钱包表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for t_user_money_log
-- ----------------------------
DROP TABLE IF EXISTS `t_user_money_log`;
CREATE TABLE `t_user_money_log`  (
                                     `id` int NOT NULL AUTO_INCREMENT COMMENT '主键id',
                                     `user_id` bigint NOT NULL COMMENT '用户id',
                                     `coin_type` tinyint NULL DEFAULT NULL COMMENT '币种(对应钱包)',
                                     `change_balance` decimal(40, 8) NULL DEFAULT NULL COMMENT '变动额度',
                                     `before_balance` decimal(40, 8) NULL DEFAULT NULL COMMENT '变动前余额',
                                     `after_balance` decimal(40, 8) NULL DEFAULT NULL COMMENT '变动后余额',
                                     `serial_code` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '流水号',
                                     `gt_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '映射修改记录对应的序号',
                                     `source_code` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '来源订单',
                                     `source_type` int NULL DEFAULT NULL COMMENT '来源类型(1.充值 2.提现 3.推荐奖 4.级差奖 5.平级奖 6.购买套餐 7.平台扣拨)',
                                     `source_id` bigint NULL DEFAULT 0 COMMENT '来源用户ID',
                                     `remark` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '备注',
                                     `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                     `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
                                     `active_flag` tinyint NOT NULL DEFAULT 1 COMMENT '是否删除（1:否,2:是）',
                                     PRIMARY KEY (`id`) USING BTREE,
                                     INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
                                     INDEX `idx_serial_code`(`serial_code` ASC) USING BTREE,
                                     INDEX `idx_source_code`(`source_code` ASC) USING BTREE,
                                     INDEX `gt_id`(`gt_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 144 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_520_ci COMMENT = '钱包流水表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for t_user_relation
-- ----------------------------
DROP TABLE IF EXISTS `t_user_relation`;
CREATE TABLE `t_user_relation`  (
                                    `id` int UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '自增ID',
                                    `par_user_id` bigint UNSIGNED NOT NULL DEFAULT 0 COMMENT '祖先节点',
                                    `pos_user_id` bigint UNSIGNED NOT NULL DEFAULT 0 COMMENT '后代节点',
                                    `distance` int UNSIGNED NOT NULL DEFAULT 0 COMMENT '相隔层级，0表示引用当前节点，1以上的值表示到祖先节点的距离',
                                    `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                    `update_time` timestamp NULL DEFAULT NULL COMMENT '修改时间',
                                    `active_flag` tinyint NOT NULL DEFAULT 1 COMMENT '是否删除（1:否,2:是）',
                                    PRIMARY KEY (`id`) USING BTREE,
                                    INDEX `idx_par_user_id`(`par_user_id` ASC) USING BTREE,
                                    INDEX `idx_pos_user_id`(`pos_user_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 196095 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_520_ci COMMENT = '节点关系表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for t_user_transfer
-- ----------------------------
DROP TABLE IF EXISTS `t_user_transfer`;
CREATE TABLE `t_user_transfer`  (
                                    `id` int NOT NULL AUTO_INCREMENT COMMENT '主键id',
                                    `from_user_id` bigint NOT NULL COMMENT '转账用户ID',
                                    `from_account` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '转账用户账号',
                                    `to_user_id` bigint NULL DEFAULT NULL COMMENT '接收用户ID',
                                    `to_account` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '接收用户账号',
                                    `code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '转账订单号',
                                    `change_balance` decimal(20, 8) UNSIGNED NULL DEFAULT 0.00000000 COMMENT '转账额度',
                                    `fee_balance` decimal(20, 8) UNSIGNED NULL DEFAULT 0.00000000 COMMENT '手续费',
                                    `fee_ratio` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT '0' COMMENT '手续费率',
                                    `actual_amount` decimal(20, 6) NULL DEFAULT NULL COMMENT '到账金额，用户实际收到的金额（扣除手续费后的金额）',
                                    `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                    `coin_type` int NULL DEFAULT 1 COMMENT '转账币种 1:USDT,2:DFC,3:OORT,4:锁定USDT,5:产出DFC',
                                    PRIMARY KEY (`id`) USING BTREE,
                                    INDEX `idx_from_user_id`(`from_user_id` ASC) USING BTREE,
                                    INDEX `idx_to_user_id`(`to_user_id` ASC) USING BTREE,
                                    INDEX `idx_code`(`code` ASC) USING BTREE,
                                    INDEX `idx_coin_type`(`coin_type` ASC) USING BTREE,
                                    INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_520_ci COMMENT = '用户转账记录表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for t_withdrawal
-- ----------------------------
DROP TABLE IF EXISTS `t_withdrawal`;
CREATE TABLE `t_withdrawal`  (
                                 `id` int NOT NULL AUTO_INCREMENT COMMENT '主键id',
                                 `user_id` bigint NOT NULL COMMENT '用户id',
                                 `code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '提现单号',
                                 `change_balance` decimal(20, 8) UNSIGNED NULL DEFAULT 0.00000000 COMMENT '变动额度',
                                 `fee_balance` decimal(20, 8) UNSIGNED NULL DEFAULT 0.00000000 COMMENT '手续费额度',
                                 `fee_ratio` decimal(10, 4) NULL DEFAULT 0.0000 COMMENT '手续费率',
                                 `status` tinyint NULL DEFAULT 0 COMMENT '状态(0.待审核,1.审核成功,2.审核驳回,3:提现成功,4:打款失败)',
                                 `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '备注(提现hash)',
                                 `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                 `update_time` timestamp NULL DEFAULT NULL COMMENT '修改时间',
                                 `account_no` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '账号(银行卡号/USDT地址)暂时废弃',
                                 `coin_type` int NULL DEFAULT 1 COMMENT '提现币种 1:USDT,2:DFC,3OORT',
                                 `biz_status` int NULL DEFAULT 0 COMMENT '业务状态是否处理 0:否,1:是',
                                 `credited_time` datetime NULL DEFAULT NULL COMMENT '提现到账日期',
                                 `credited_amount` decimal(20, 8) NULL DEFAULT 0.00000000 COMMENT '实际到账金额（扣除手续费及限制后的最终到账金额）',
                                 PRIMARY KEY (`id`) USING BTREE,
                                 UNIQUE INDEX `uk_code`(`code` ASC) USING BTREE,
                                 INDEX `idx_user_id`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 28 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_520_ci COMMENT = '提现表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for t_withdrawal_config
-- ----------------------------
DROP TABLE IF EXISTS `t_withdrawal_config`;
CREATE TABLE `t_withdrawal_config`  (
                                        `id` int NOT NULL AUTO_INCREMENT COMMENT '主键id',
                                        `coin_type` int NOT NULL COMMENT '币种 1:USDT',
                                        `withdraw_open` tinyint NOT NULL DEFAULT 1 COMMENT '提现开关(1:开,2:关)',
                                        `min_withdraw_amount` decimal(20, 8) UNSIGNED NOT NULL DEFAULT 0.00000000 COMMENT '最小提现金额',
                                        `fee_ratio` decimal(10, 4) NOT NULL DEFAULT 0.0000 COMMENT '提现手续费率(例如:10表示10%)',
                                        `withdraw_limit` decimal(20, 8) UNSIGNED NOT NULL DEFAULT 0.00000000 COMMENT '提现额度',
                                        `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                        `update_time` timestamp NULL DEFAULT NULL COMMENT '修改时间',
                                        `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '备注',
                                        `daily_free_audit_count` int NOT NULL DEFAULT 0 COMMENT '单日免审核次数',
                                        PRIMARY KEY (`id`) USING BTREE,
                                        UNIQUE INDEX `uk_coin_type`(`coin_type` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_520_ci COMMENT = '提现配置表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for third_part_requset_log
-- ----------------------------
DROP TABLE IF EXISTS `third_part_requset_log`;
CREATE TABLE `third_part_requset_log`  (
                                           `third_party_id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
                                           `business_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '业务id',
                                           `business_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '业务类型',
                                           `request_no` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '请求流水号',
                                           `request_method` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '请求方法名',
                                           `outer_status` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '第三方返回状态',
                                           `request_param` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL COMMENT '请求方法参数',
                                           `response_param` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL COMMENT '返回数据',
                                           `local_status` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '本地处理状态；0-未处理，1-处理成功，2-处理失败',
                                           `batch_no` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '批次号',
                                           `user_id` bigint NULL DEFAULT NULL COMMENT '用户ID',
                                           `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间，调用时间',
                                           `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
                                           PRIMARY KEY (`third_party_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_520_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for xms_diy_store_product
-- ----------------------------
DROP TABLE IF EXISTS `xms_diy_store_product`;
CREATE TABLE `xms_diy_store_product`  (
                                          `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键id',
                                          `product_name` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL COMMENT '商品名称',
                                          `product_name_en` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '商品名称(英文)',
                                          `product_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '商品编码',
                                          `cover_image` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '封面图',
                                          `cover_image_en` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '封面图(英文)',
                                          `slider_image` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL COMMENT '轮播图(JSON数组)',
                                          `slider_image_en` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL COMMENT '轮播图(JSON数组,英文)',
                                          `detail_image` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL COMMENT '详情图(JSON数组)',
                                          `detail_image_en` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL COMMENT '详情图(JSON数组,英文)',
                                          `price` decimal(20, 6) NULL DEFAULT NULL COMMENT '价格',
                                          `sales` int UNSIGNED NOT NULL DEFAULT 0 COMMENT '总销量',
                                          `stock` int NOT NULL DEFAULT -1 COMMENT '总库存冗余 -1不限',
                                          `is_enabled` tinyint NOT NULL DEFAULT 0 COMMENT '是否上架 0否1是',
                                          `sort` int NOT NULL DEFAULT 0 COMMENT '排序',
                                          `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '备注',
                                          `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                          `create_by` int NULL DEFAULT NULL COMMENT '创建人',
                                          `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                          `update_by` int NULL DEFAULT NULL COMMENT '更新人',
                                          `deleted` int NOT NULL DEFAULT 0 COMMENT '是否删除 0否1是',
                                          `spec_type` tinyint(1) NULL DEFAULT 0 COMMENT '规格 0单 1多',
                                          PRIMARY KEY (`id`) USING BTREE,
                                          UNIQUE INDEX `uk_product_code`(`product_code` ASC) USING BTREE,
                                          INDEX `idx_enabled_sort`(`is_enabled` ASC, `sort` ASC) USING BTREE,
                                          INDEX `idx_deleted`(`deleted` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_520_ci COMMENT = '商品主表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for xms_diy_store_product_attr
-- ----------------------------
DROP TABLE IF EXISTS `xms_diy_store_product_attr`;
CREATE TABLE `xms_diy_store_product_attr`  (
                                               `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
                                               `product_id` bigint UNSIGNED NOT NULL DEFAULT 0 COMMENT '商品ID',
                                               `attr_name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL COMMENT '属性名',
                                               `attr_values` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL COMMENT '属性值(逗号分隔)',
                                               `remark` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '备注',
                                               `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                               `create_by` int NULL DEFAULT NULL,
                                               `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                               `update_by` int NULL DEFAULT NULL,
                                               `deleted` int NOT NULL DEFAULT 0 COMMENT '是否删除 0否1是',
                                               PRIMARY KEY (`id`) USING BTREE,
                                               INDEX `idx_product_id`(`product_id` ASC) USING BTREE,
                                               INDEX `idx_product_attr_name`(`product_id` ASC, `attr_name` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 16 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_520_ci COMMENT = '商品属性表，总览' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for xms_diy_store_product_attr_value
-- ----------------------------
DROP TABLE IF EXISTS `xms_diy_store_product_attr_value`;
CREATE TABLE `xms_diy_store_product_attr_value`  (
                                                     `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
                                                     `product_id` bigint UNSIGNED NOT NULL COMMENT '商品ID',
                                                     `sku` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL COMMENT '商品属性索引值(attr_value|attr_value[|...])',
                                                     `stock` int UNSIGNED NOT NULL COMMENT '属性对应库存',
                                                     `sales` int UNSIGNED NULL DEFAULT 0 COMMENT '销量',
                                                     `price` decimal(20, 6) UNSIGNED NOT NULL COMMENT '销售价',
                                                     `image` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '图片',
                                                     `image_en` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '图片(英文)',
                                                     `code_unique` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL DEFAULT '' COMMENT '唯一值(可放规则签名)',
                                                     `cost` decimal(20, 6) UNSIGNED NOT NULL DEFAULT 0.000000 COMMENT '成本价',
                                                     `bar_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '商品条码',
                                                     `weight` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '重量(可选)',
                                                     `volume` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '体积(可选)',
                                                     `brokerage` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '扩展字段1(保留)',
                                                     `brokerage_two` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '扩展字段2(保留)',
                                                     `pink_price` decimal(20, 6) NOT NULL DEFAULT 0.000000 COMMENT '拼团价(保留)',
                                                     `pink_stock` int NOT NULL DEFAULT 0 COMMENT '拼团库存(保留)',
                                                     `seckill_price` decimal(20, 6) NOT NULL DEFAULT 0.000000 COMMENT '秒杀价(保留)',
                                                     `seckill_stock` int NOT NULL DEFAULT 0 COMMENT '秒杀库存(保留)',
                                                     `integral` int(10) UNSIGNED ZEROFILL NULL DEFAULT 0000000000 COMMENT '积分(保留)',
                                                     `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                                     `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                                     `deleted` int NOT NULL DEFAULT 0 COMMENT '是否删除 0否1是',
                                                     PRIMARY KEY (`id`) USING BTREE,
                                                     UNIQUE INDEX `uk_code_unique_sku`(`code_unique` ASC, `sku` ASC) USING BTREE,
                                                     INDEX `idx_product_sku`(`product_id` ASC, `sku` ASC) USING BTREE,
                                                     INDEX `idx_product_enabled`(`product_id` ASC, `deleted` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 22 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_520_ci COMMENT = '商品属性值表(SKU)' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for xms_diy_store_product_rule
-- ----------------------------
DROP TABLE IF EXISTS `xms_diy_store_product_rule`;
CREATE TABLE `xms_diy_store_product_rule`  (
                                               `id` int NOT NULL AUTO_INCREMENT,
                                               `rule_name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL COMMENT '规格模板名称',
                                               `rule_name_en` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '规格模板名称(英文)',
                                               `rule_value` json NOT NULL COMMENT '规格值JSON',
                                               `rule_value_en` json NULL COMMENT '规格值JSON(英文)',
                                               `remark` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '备注',
                                               `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                               `create_by` int NULL DEFAULT NULL,
                                               `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                               `update_by` int NULL DEFAULT NULL,
                                               `deleted` int NOT NULL DEFAULT 0 COMMENT '是否删除 0否1是',
                                               PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_520_ci COMMENT = '商品规则值(规格模板)表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for xms_reward_record
-- ----------------------------
DROP TABLE IF EXISTS `xms_reward_record`;
CREATE TABLE `xms_reward_record`  (
                                      `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
                                      `coin_type` int NULL DEFAULT 1 COMMENT '币种类型 1:BOOMAI,2:MAI',
                                      `order_code` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '订单号',
                                      `user_id` bigint NULL DEFAULT NULL COMMENT '用户id',
                                      `amount` decimal(60, 6) NULL DEFAULT NULL COMMENT '数量',
                                      `business_type` int NULL DEFAULT NULL COMMENT '业务类型：例如 级差、平级等等(枚举类型有多少个还不确定)1:矿机静态释放',
                                      `source_type` int NULL DEFAULT 1 COMMENT '来源类型: 1:每日静态产出',
                                      `source_order_code` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '来源订单号',
                                      `source_user_id` bigint NULL DEFAULT NULL COMMENT '来源用户',
                                      `gt_id` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'gtId',
                                      `settlement_status` int NULL DEFAULT 2 COMMENT '废弃',
                                      `real_time_price` decimal(60, 6) NULL DEFAULT 0.000000 COMMENT '结算时price',
                                      `remark` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
                                      `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                      `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
                                      `issue_type` int NULL DEFAULT 0 COMMENT '业务类型 0:数据库记账,1:链上合约',
                                      `issue_status` int NULL DEFAULT 0 COMMENT '执行状态 0:待发,1:处理中,2:已完成,3:失败',
                                      PRIMARY KEY (`id`) USING BTREE,
                                      INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
                                      INDEX `idx_order_code`(`order_code` ASC) USING BTREE,
                                      INDEX `source_order_code`(`source_order_code` ASC) USING BTREE,
                                      INDEX `source_user_id`(`source_user_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 122 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '奖金记录' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for xms_reward_stat_day
-- ----------------------------
DROP TABLE IF EXISTS `xms_reward_stat_day`;
CREATE TABLE `xms_reward_stat_day`  (
                                        `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
                                        `user_id` bigint NOT NULL COMMENT '用户id',
                                        `stat_date` bigint NOT NULL COMMENT '日期 类似 yyyymmdd',
                                        `static_amount` decimal(20, 6) NULL DEFAULT 0.000000 COMMENT '静态奖励',
                                        `dynamic_amount` decimal(20, 6) NULL DEFAULT 0.000000 COMMENT '动态奖励(互动奖励)',
                                        `team_amount` decimal(20, 6) NULL DEFAULT 0.000000 COMMENT '团队收益',
                                        `total_amount` decimal(20, 6) NULL DEFAULT 0.000000 COMMENT '总收益',
                                        `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                        PRIMARY KEY (`id`) USING BTREE,
                                        INDEX `idx_user_date`(`user_id` ASC, `stat_date` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '每日奖励汇总' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for xms_task
-- ----------------------------
DROP TABLE IF EXISTS `xms_task`;
CREATE TABLE `xms_task`  (
                             `type` varchar(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL,
                             `date` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL COMMENT '格式例如:2023-09-16',
                             PRIMARY KEY (`type`, `date`) USING BTREE,
                             UNIQUE INDEX `uniqe_type_date`(`type` ASC, `date` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_520_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for xms_user_address
-- ----------------------------
DROP TABLE IF EXISTS `xms_user_address`;
CREATE TABLE `xms_user_address`  (
                                     `id` int NOT NULL AUTO_INCREMENT COMMENT 'id',
                                     `province` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL DEFAULT '0' COMMENT '省/洲',
                                     `city` varchar(80) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL COMMENT '市',
                                     `area` varchar(80) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '区/县/街道地址',
                                     `detailed` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '详细地址/街道地址2',
                                     `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL DEFAULT '0' COMMENT '手机号',
                                     `user_name` varchar(80) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL DEFAULT '0' COMMENT '收货人姓名',
                                     `user_id` bigint NULL DEFAULT NULL COMMENT '用户ID',
                                     `address_state` int NOT NULL DEFAULT 0 COMMENT '是否默认0:否,1:是',
                                     `remark` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '备注',
                                     `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                     `create_by` int NULL DEFAULT NULL,
                                     `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                     `update_by` int NULL DEFAULT NULL,
                                     `deleted` int NOT NULL DEFAULT 0 COMMENT '是否删除 否 0  1 是',
                                     PRIMARY KEY (`id`) USING BTREE,
                                     INDEX `user_id`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_520_ci COMMENT = '收货地址' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for xms_user_grade
-- ----------------------------
DROP TABLE IF EXISTS `xms_user_grade`;
CREATE TABLE `xms_user_grade`  (
                                   `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
                                   `user_id` bigint NOT NULL COMMENT '用户id',
                                   `grade` int NULL DEFAULT NULL COMMENT '等级',
                                   `source_id` bigint NULL DEFAULT NULL COMMENT '来源用户',
                                   PRIMARY KEY (`id`) USING BTREE,
                                   INDEX `user_id`(`source_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_520_ci COMMENT = '用户伞下线区等级情况' ROW_FORMAT = DYNAMIC;

SET FOREIGN_KEY_CHECKS = 1;
