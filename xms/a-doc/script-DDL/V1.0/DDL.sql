INSERT INTO t_stake_hosting_user_reward_summary (
    user_id,
    diff_reward_amount,
    same_level_reward_amount,
    global_dividend_amount
)
SELECT
    user_id,
    0.000000,
    0.000000,
    0.000000
FROM t_user_info
WHERE deleted = 0
  AND user_id IS NOT NULL
    ON DUPLICATE KEY UPDATE
                         user_id = VALUES(user_id);
