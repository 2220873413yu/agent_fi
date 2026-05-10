# UserInfoController 接口维护文档

## 1. 我的团队数据

- 接口路径：`GET /userinfo/myTeamInfo`
- Controller：`UserInfoController#myTeamInfo`
- Service：`BizUserService#myTeamInfo`
- 返回对象：`MyTeamInfoDto`

### 1.1 页面用途

用于 App “我的团队”页面展示用户当前托管等级、下一等级升级进度、团队人数、直推人数、托管收益概览等数据。

### 1.2 等级口径

当前等级取用户表中三个等级的最大值：

- `game_level`：真实等级。
- `min_game_level`：赠送等级。
- `admin_game_level`：后台管理等级。

目标等级配置读取 `t_user_level_config`：

- 如果当前等级小于 `F9`，目标等级为当前等级下一档，例如当前 `F8` 查询 `F9` 配置。
- 如果当前等级已经是 `F9`，仍查询 `F9` 配置。
- 如果用户当前等级为空，按 `0` 处理，目标等级查询 `F1`。

### 1.3 升级条件

目前升级条件只展示两项：

- 个人托管：当前值取 `t_user_info.performance`，目标值取目标等级配置 `t_user_level_config.performance`。
- 团队托管：当前值取 `t_user_info.umbrella_performance`，目标值取目标等级配置 `t_user_level_config.community_performance`。

页面原型里的“推广两条线达标人数”当前不实现。

### 1.4 收益统计口径

- 团队收益：查询 `t_stake_hosting_user_reward_summary`，按 `diff_reward_amount + same_level_reward_amount` 汇总展示。
- 间推收益：当前无间推奖业务，返回 `0`。
- 全球分红：查询 `t_stake_hosting_user_reward_summary.global_dividend_amount`。
- 团队总托管：取 `t_user_info.umbrella_performance`。
- 自身托管：取 `t_user_info.performance`。

### 1.5 进度计算

- 进度 = `当前值 / 目标值 * 100`。
- 目标值为空或小于等于 `0` 时，进度返回 `0`。
- 当前值超过目标值时，进度封顶为 `100`。
- “还需”金额 = `目标值 - 当前值`，小于 `0` 时返回 `0`。

### 1.6 返回字段

- `currentLevel`：当前最大等级编码。
- `targetLevel`：目标等级编码。
- `selfHostingAmount`：当前个人托管金额。
- `targetSelfHostingAmount`：目标个人托管金额。
- `selfHostingNeedAmount`：距离目标个人托管还需金额。
- `selfHostingProgress`：个人托管进度，单位 `%`。
- `teamHostingAmount`：当前团队托管金额。
- `targetTeamHostingAmount`：目标团队托管金额。
- `teamHostingNeedAmount`：距离目标团队托管还需金额。
- `teamHostingProgress`：团队托管进度，单位 `%`。
- `teamUserCount`：团队人数。
- `directUserCount`：直推人数。
- `teamRewardAmount`：团队收益，等于托管极差奖累计 + 托管平级奖累计。
- `indirectRewardAmount`：间推收益，当前固定 `0`。
- `globalDividendAmount`：托管全球分红累计金额。
- `teamTotalHostingAmount`：团队总托管金额。
- `selfTotalHostingAmount`：自身托管金额。

### 1.7 暂不实现

- 推广两条线达标人数。
- 间推奖。
