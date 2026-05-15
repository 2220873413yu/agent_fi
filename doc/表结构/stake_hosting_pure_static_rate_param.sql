-- 托管纯静态收益率系统参数
-- para_value 使用百分比数值，例如 0.5 表示 0.5%，不是 0.005。

INSERT INTO t_sys_para (para_code, para_value, para_desc, visible, create_time, active_flag, remark)
SELECT 'PURE_STATIC_RATE_BEFORE_RETURN_PERCENT', '0.5', '未触发G7区间时，未回本订单使用的纯静态基础收益率，单位%', '0', sysdate(), 1, '托管静态收益参数'
WHERE NOT EXISTS (SELECT 1 FROM t_sys_para WHERE para_code = 'PURE_STATIC_RATE_BEFORE_RETURN_PERCENT');

INSERT INTO t_sys_para (para_code, para_value, para_desc, visible, create_time, active_flag, remark)
SELECT 'PURE_STATIC_RATE_AFTER_RETURN_PERCENT', '0.2', '未触发G7区间时，已回本订单使用的纯静态基础收益率，单位%', '0', sysdate(), 1, '托管静态收益参数'
WHERE NOT EXISTS (SELECT 1 FROM t_sys_para WHERE para_code = 'PURE_STATIC_RATE_AFTER_RETURN_PERCENT');
