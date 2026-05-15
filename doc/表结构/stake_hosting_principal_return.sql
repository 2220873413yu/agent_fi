-- 托管订单到期退还USDT本金增量脚本
-- 仅新增钱包流水来源类型：39=质押退还。

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 39, '质押退还', '39', 't_user_money_log_source_type', '', 'success', 'N', '0', 'admin', sysdate(), '托管用户购买订单到期退还USDT本金'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 't_user_money_log_source_type' AND dict_value = '39');
