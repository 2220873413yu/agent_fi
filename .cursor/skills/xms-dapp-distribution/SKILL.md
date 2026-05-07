---
name: xms-dapp-distribution
description: Guide Codex when working on the XMS DApp distribution system. Use for this repository when requests involve DApp/app API/admin API, wallet balances, recharge, withdrawal, transfer, staking, mining, node packages, order callbacks, reward settlement, direct/indirect referral rewards, team trees, user relation, user performance, level calculation, Redis stream settlement, or xms-ui admin pages.
---

# XMS DApp Distribution System

## Project Map

This repository is a DApp-style distribution/business system based on RuoYi conventions.

- Backend root: `xms`
- Admin backend entry: `xms/xms-agent-admin/src/main/java/com/xms/web/XmsApplication.java`
- App API backend entry: `xms/xms-agent-app/src/main/java/com/xms/app/AppApplication.java`
- Shared domain/service module: `xms/xms-dao`
- Shared utilities/constants/security/Redis/MQ: `xms/xms-common`
- Admin UI: `xms-ui` using Vue 2 + Element UI + RuoYi layout
- Table reference: `doc/表结构/xms_agent.sql`

When implementing business logic, read the local code first. Prefer existing services, constants, locks, and mapper patterns over new abstractions.

## Backend Modules

- `xms-agent-app`: user-facing DApp APIs. Core packages: `com.xms.app.controller`, `com.xms.app.service.impl`, `com.xms.app.entity.req/resp/dto/vo`.
- `xms-agent-admin`: admin APIs, scheduled tasks, Redis stream receivers, admin-only operations. Core packages: `com.xms.web.controller.xms`, `com.xms.web.service.impl`, `com.xms.web.task`.
- `xms-dao`: MyBatis-Plus domain objects, mapper interfaces, service interfaces/impls.
- `xms-common`: constants, Redis lock/stream, security helpers, result wrappers, ID utils, wallet/reward settlement messaging.
- `xms-system`, `xms-framework`, `xms-quartz`, `xms-generator`: RuoYi platform modules.

## Core Business Areas

- User and invitation tree: `UserInfo`, `UserRelation`, `BizUserServiceImpl`.
- Wallet balances and logs: `UserMoney`, `UserMoneyLog`, `UserWalletService`, `MoneyOperationReference`.
- Recharge/withdrawal/transfer: `BizRechargeServiceImpl`, `BizWithdrawalServiceImpl`, `BizTransferServiceImpl`, admin controllers under `controller/xms`.
- Staking/product purchase: `BizStakeServiceImpl`, `StakeOrder`, `StakeProduct`, DIY product tables.
- Mining/miner staking: `BizMiningServiceImpl`, `MiningPackage`, `MiningPackageOrder`, `MiningPackageTier`, `MiningMgmtFeeConfig`.
- Node packages: `BizNodeServiceImpl`, `NodePackage`, `NodePackageOrder`.
- Rewards/statistics: `RewardRecord`, `RewardStatDay`, `AsyncTaskServiceImpl`, Redis stream settlement.

## Mandatory References

Before changing wallet, reward, or performance logic, read:

- `xms/xms-agent-admin/src/main/java/com/xms/web/util/MoneyOperationReference.java`
- `xms/xms-common/src/main/java/com/xms/common/constant/ConstantType.java`
- The relevant service implementation in `xms-agent-app/src/main/java/com/xms/app/service/impl`
- The relevant domain object in `xms-dao/src/main/java/com/xms/dao/domain` or `xms-dao/src/main/java/com/xms/dao/entity/domain`
- The matching table definition in `doc/表结构/xms_agent.sql` when columns or status values are unclear

Do not invent new status/source/coin semantics without checking constants, table comments, and existing usage.

## User Tree Rules

Registration/login creates the distribution tree:

- `UserInfo.inviteUserId` is the direct inviter.
- `UserInfo.parentChain` stores the ancestor chain as comma-separated user IDs.
- `UserInfo.getParentIds()` parses `parentChain`.
- `UserRelation` is the closure table:
  - `parUserId`: ancestor
  - `posUserId`: descendant
  - `distance`: `0` means self, `1+` means ancestor distance
- New users must create:
  - `UserInfo`
  - `UserMoney` with `id = userId`
  - self `UserRelation`
  - ancestor `UserRelation` rows
  - inviter `sub_num + 1`
  - ancestor `umbrella_num + 1`

Keep registration/tree writes inside a transaction. Preserve the existing 200-level guard.

## Wallet Rules

Wallet updates are high-risk.

- Use `UserWalletService.handerUserMoney(...)` for single-user low-frequency balance changes.
- For deductions, pass `amount.negate()`; do not hand-code negative literals.
- Always check the returned row count. If it is not `1`, throw and roll back.
- For batch rewards, use `JdbcTemplate.batchUpdate` with SQL in the style `field = field + ?`.
- Batch at 1000 rows where possible.
- For every reward/balance earning operation, save `RewardRecord` and update `UserMoney`.
- `UserMoney.gtId`, `sourceCode`, `sourceType`, `sourceId`, and `updateTime` are audit fields; fill them when doing raw/batch updates.
- Never overwrite wallet totals with a computed final amount. Only apply the delta.

Common wallet fields:

- `valid_num1`: USDT
- `valid_num2`: usually DFC/FSN depending on local usage; verify with the caller and existing code
- `valid_num3`: release/locked release balance
- `valid_num4`: locked USDT used by older mining payment paths
- `valid_num5`: DFC output

## Reward Rules

Use `ConstantType` for all source and coin types.

- `RewardRecord` is the reward/audit record.
- `UserMoneyLog` source type must match the same business meaning as `RewardRecord.sourceType`.
- Direct and indirect referral rewards must not be mixed.
- Reward amounts must be `> 0`; skip zero/negative rewards.
- Use `IDUtils.getSnowflakeStr()` for reward/order/gt IDs where existing code does.
- Use `ConstantStatic.newScale` and `ConstantStatic.roundingModeNew` for BigDecimal scaling.
- Avoid `new BigDecimal(double)`.

When adding a new reward path, identify:

- receiver user ID
- source user ID
- source order code
- coin type
- user money log source type
- reward record source type
- whether the receiver must be valid/active

## Performance Rules

Ask or infer only from explicit business context before changing performance fields.

- `performance`: personal staking/mining performance.
- `umbrella_performance`: ancestor team staking/mining performance.
- `sub_node_performance`: direct node count, only direct inviter.
- `node_team_performance`: team node count, ancestor chain.
- `community_performance`: small-region performance, derived.
- `max_leg_performance`: large-region/max-leg performance, derived where present.
- `sub_performance`: deprecated; do not use for new logic.

Staking/mining performance:

- Increase personal `performance` on paid/confirmed order.
- Increase ancestors' `umbrella_performance` using `parentIds`.
- On subtraction, use `GREATEST(IFNULL(field,0)-amount,0)`.
- Recalculate small/large region after `performance` or `umbrella_performance` changes.

Node performance:

- Direct inviter gets `sub_node_performance + 1`.
- Ancestor chain gets `node_team_performance + 1`.
- Unit is count, not money.
- Node performance does not trigger small/large-region recalculation.

## Order And Callback Rules

Most DApp actions are two-phase: create an order, then a signed payment callback confirms it.

- Creation APIs usually validate wallet signature via `BizUserServiceImpl.checkWallet(...)`.
- Callback APIs usually verify MD5 signature with `SignUtil.getSign(...)` and `lq.md5Key`.
- Windows/dev environment often bypasses signature checks via `SystemUtil.getOsInfo()`. Do not remove this behavior casually.
- Callback handlers must be idempotent:
  - If order not found, existing patterns may return `"success"` to avoid repeated upstream callbacks.
  - If order is already successful, return `"success"`.
  - Update with current status in the `WHERE` condition.
- Use `@RedisLock` on user/order sensitive actions.
- Use `@Transactional(rollbackFor = Exception.class)` around multi-table writes.
- Send async settlement only after commit via `TransactionSynchronizationManager.registerSynchronization(... afterCommit ...)`.

## Async Settlement

Dynamic settlement uses Redis Stream:

- Producer: `AsyncDynamicOrderSettlementServiceImpl`
- Message object: `OrderMsgDO`
- Admin-side task logic: `AsyncTaskServiceImpl`
- Stream constants: `RedisConstant.StreamMsgConstant`

When adding settlement work, do not perform irreversible reward work before the source order transaction commits. Prefer after-commit enqueue and idempotent consumers.

## Admin UI Rules

The admin frontend is RuoYi Vue 2 + Element UI.

- API files live in `xms-ui/src/api/xms`.
- Pages live in `xms-ui/src/views/xms/<feature>/index.vue`.
- Follow existing CRUD page shape: query form, table, dialog, export/import if already used.
- Backend admin controllers live in `xms-agent-admin/src/main/java/com/xms/web/controller/xms`.
- Keep route/menu/permission naming aligned with existing `xms:<feature>:<action>` patterns.

## Response And Error Patterns

- App APIs usually return `ResultPista`.
- Admin APIs usually extend `BaseController` and return `AjaxResult` / `TableDataInfo`.
- Throw `ServiceException` with `ResponseCode` where existing code does.
- Use `SecurityUtils.getFrontUserId()` or `SecurityUtils.getLoginAppUser().getUserId()` for app user context.
- Use `SecurityUtils`/RuoYi permissions for admin context.

## Implementation Checklist

Before editing:

- Locate the domain object, mapper/service, controller, and table.
- Identify whether the operation changes money, rewards, performance, order status, user tree, or async settlement.
- Check `ConstantType`, `ConstantStatic`, `RedisConstant`, and `SysConstant` for existing meanings.

While editing:

- Keep writes transactional.
- Add `@RedisLock` for duplicate-submit or same-user critical paths.
- Use delta wallet updates.
- Save reward records for rewards.
- Check row counts for financial writes.
- Use after-commit for Redis stream settlement.
- Keep source types and coin types semantically consistent.

Before finishing:

- Run a focused Maven compile/test for the touched backend module when feasible.
- Run `npm run lint` or targeted frontend checks when editing `xms-ui`.
- Manually reason through duplicate callbacks, insufficient balance, zero reward, missing wallet row, and rollback behavior.
