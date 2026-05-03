# U卡购买与升级子系统需求文档

## 📋 需求概述

本模块负责用户购买算力卡（U卡）和卡片升级功能，包括：
- U卡等级体系
- 购买流程
- 升级机制
- 算力计算
- 数据持久化

---

## 1. U卡等级体系

### 1.1 卡片等级定义

| 卡片等级 | 等级代码 | 价格 (USDT) | 算力值 (T) | 算力/价格比 |
|---------|---------|------------|-----------|-----------|
| 🥉 普通卡 | NORMAL  | 100       | 100       | 1.00      |
| 🥈 白银卡 | SILVER  | 1,000     | 1,050     | 1.05      |
| 🥇 白金卡 | GOLD    | 3,000     | 3,300     | 1.10      |
| 💎 黑金卡 | DIAMOND | 10,000    | 11,500    | 1.15      |

### 1.2 等级权重说明

- 等级越高，算力性价比越高
- 高等级卡片可参与更多分红池
- 高等级卡片影响团队等级（V1-V9）升级条件

---

## 2. 购买流程

### 2.1 业务流程

```
用户选择卡片等级
    ↓
检查 USDT 余额是否足够
    ↓
扣除用户 USDT
    ↓
创建 U卡订单记录
    ↓
分配算力
    ↓
触发推荐奖励（直推/间推）
    ↓
更新全网算力统计
    ↓
返回购买成功
```

### 2.2 购买前置条件

- ✅ 用户已完成注册
- ✅ 用户已完成实名认证（可选，根据业务需要）
- ✅ USDT 余额 ≥ 卡片价格
- ✅ 用户有上级推荐人（注册时绑定）

### 2.3 购买后触发事件

1. **算力分配**
   - 立即分配对应等级的算力
   - 更新用户总算力
   - 更新全网有效算力

2. **推荐奖励**（详见第4节）
   - 直推奖励：上级获得 10% USDT + 10% 算力
   - 间推奖励：上上级获得 5% USDT + 5% 算力

3. **团队算力更新**
   - 更新推荐链上所有上级的小区算力
   - 检查是否触发等级升级（V1-V9）

---

## 3. 升级机制

### 3.1 升级规则

**核心原则**：
- ✅ 支持随时补差价升级
- ✅ 只能从低等级升级到高等级（不可降级）
- ✅ 升级后立即获得新增算力
- ✅ 算力实时生效，无需等待

### 3.2 升级差价计算

| 当前等级 | 目标等级 | 需补差价 (USDT) | 新增算力 (T) |
|---------|---------|----------------|-------------|
| 普通卡   | 白银卡   | 900           | 950         |
| 普通卡   | 白金卡   | 2,900         | 3,200       |
| 普通卡   | 黑金卡   | 9,900         | 11,400      |
| 白银卡   | 白金卡   | 2,000         | 2,250       |
| 白银卡   | 黑金卡   | 9,000         | 10,450      |
| 白金卡   | 黑金卡   | 7,000         | 8,200       |

**计算公式**：
```
补差价 = 目标等级价格 - 当前等级价格
新增算力 = 目标等级算力 - 当前等级算力
```

### 3.3 升级流程

```
用户选择当前卡片
    ↓
选择目标升级等级
    ↓
计算补差价
    ↓
检查 USDT 余额是否足够
    ↓
扣除差价 USDT
    ↓
更新卡片等级
    ↓
增加算力差值
    ↓
触发推荐奖励（针对差价部分）
    ↓
更新全网算力统计
    ↓
返回升级成功
```

### 3.4 升级限制

- ❌ 不支持跨多级升级（需逐级升级）**或** ✅ 支持跨级升级（根据业务决定）
- ❌ 不支持降级
- ❌ 卡片过期后不可升级（如有过期机制）

---

## 4. 推荐奖励机制

### 4.1 直推奖励

**触发条件**：用户购买或升级 U卡

**奖励对象**：直接推荐人（上级）

**奖励内容**：
- 💰 10% 购买金额（USDT）
- 🔨 10% 算力值

**计算示例**（购买白银卡 1,000 U）：
- 直推人获得：100 USDT + 105 T 算力

### 4.2 间推奖励

**触发条件**：用户购买或升级 U卡

**奖励对象**：间接推荐人（上上级）

**奖励内容**：
- 💰 5% 购买金额（USDT）
- 🔨 5% 算力值

**计算示例**（购买白银卡 1,000 U）：
- 间推人获得：50 USDT + 52.5 T 算力

### 4.3 奖励特性

- ✅ **无烧伤机制**：奖励永久有效，不受限制
- ✅ **实时发放**：购买/升级成功后立即发放
- ✅ **算力累加**：奖励算力计入个人总算力

---

## 5. 数据库表设计

### 5.1 U卡订单表（u_card_orders）

```sql
CREATE TABLE u_card_orders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '订单ID',
    order_no VARCHAR(64) NOT NULL UNIQUE COMMENT '订单号',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    card_level ENUM('NORMAL', 'SILVER', 'GOLD', 'DIAMOND') NOT NULL COMMENT '卡片等级',
    card_name VARCHAR(50) NOT NULL COMMENT '卡片名称（普通卡/白银卡/白金卡/黑金卡）',
    price DECIMAL(20, 6) NOT NULL COMMENT '支付价格（USDT）',
    hashrate DECIMAL(20, 2) NOT NULL COMMENT '获得算力（T）',
    order_type ENUM('PURCHASE', 'UPGRADE') NOT NULL COMMENT '订单类型（购买/升级）',
    source_card_id BIGINT NULL COMMENT '升级来源卡片ID（仅升级订单有值）',
    payment_status TINYINT DEFAULT 0 COMMENT '支付状态（0待支付 1已支付 2已退款）',
    order_status TINYINT DEFAULT 0 COMMENT '订单状态（0待处理 1已完成 2已取消）',
    direct_referrer_id BIGINT NULL COMMENT '直推人ID',
    direct_reward_usdt DECIMAL(20, 6) DEFAULT 0 COMMENT '直推奖励USDT',
    direct_reward_hashrate DECIMAL(20, 2) DEFAULT 0 COMMENT '直推奖励算力',
    indirect_referrer_id BIGINT NULL COMMENT '间推人ID',
    indirect_reward_usdt DECIMAL(20, 6) DEFAULT 0 COMMENT '间推奖励USDT',
    indirect_reward_hashrate DECIMAL(20, 2) DEFAULT 0 COMMENT '间推奖励算力',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    paid_at DATETIME NULL COMMENT '支付时间',
    completed_at DATETIME NULL COMMENT '完成时间',
    remark VARCHAR(500) NULL COMMENT '备注',
    
    INDEX idx_user_id (user_id),
    INDEX idx_order_no (order_no),
    INDEX idx_created_at (created_at),
    INDEX idx_direct_referrer (direct_referrer_id),
    INDEX idx_indirect_referrer (indirect_referrer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='U卡订单表';
```

---

### 5.2 用户U卡表（user_cards）

```sql
CREATE TABLE user_cards (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '卡片ID',
    card_no VARCHAR(64) NOT NULL UNIQUE COMMENT '卡片编号',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    card_level ENUM('NORMAL', 'SILVER', 'GOLD', 'DIAMOND') NOT NULL COMMENT '当前卡片等级',
    card_name VARCHAR(50) NOT NULL COMMENT '卡片名称',
    total_paid DECIMAL(20, 6) NOT NULL DEFAULT 0 COMMENT '累计支付金额（含升级）',
    current_hashrate DECIMAL(20, 2) NOT NULL COMMENT '当前算力（T）',
    total_hashrate DECIMAL(20, 2) NOT NULL COMMENT '累计获得算力（包含奖励）',
    purchase_order_id BIGINT NOT NULL COMMENT '购买订单ID',
    last_upgrade_order_id BIGINT NULL COMMENT '最后升级订单ID',
    upgrade_count INT DEFAULT 0 COMMENT '升级次数',
    card_status TINYINT DEFAULT 1 COMMENT '卡片状态（0禁用 1启用 2已过期）',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '购买时间',
    upgraded_at DATETIME NULL COMMENT '最后升级时间',
    expired_at DATETIME NULL COMMENT '过期时间（如有）',
    
    INDEX idx_user_id (user_id),
    INDEX idx_card_no (card_no),
    INDEX idx_card_level (card_level),
    INDEX idx_card_status (card_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户U卡表';
```

---

### 5.3 用户算力统计表（user_hashrate_stats）

```sql
CREATE TABLE user_hashrate_stats (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '统计ID',
    user_id BIGINT NOT NULL UNIQUE COMMENT '用户ID',
    total_hashrate DECIMAL(20, 2) DEFAULT 0 COMMENT '总算力（个人购买+奖励）',
    purchase_hashrate DECIMAL(20, 2) DEFAULT 0 COMMENT '购买算力',
    direct_reward_hashrate DECIMAL(20, 2) DEFAULT 0 COMMENT '直推奖励算力',
    indirect_reward_hashrate DECIMAL(20, 2) DEFAULT 0 COMMENT '间推奖励算力',
    active_card_count INT DEFAULT 0 COMMENT '活跃卡片数量',
    highest_card_level ENUM('NORMAL', 'SILVER', 'GOLD', 'DIAMOND') NULL COMMENT '最高卡片等级',
    total_investment DECIMAL(20, 6) DEFAULT 0 COMMENT '累计投资金额（USDT）',
    last_purchase_at DATETIME NULL COMMENT '最后购买时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    INDEX idx_user_id (user_id),
    INDEX idx_total_hashrate (total_hashrate)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户算力统计表';
```

---

### 5.4 全网算力统计表（global_hashrate_stats）

```sql
CREATE TABLE global_hashrate_stats (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '统计ID',
    stat_date DATE NOT NULL UNIQUE COMMENT '统计日期',
    total_hashrate DECIMAL(30, 2) DEFAULT 0 COMMENT '全网总算力',
    active_hashrate DECIMAL(30, 2) DEFAULT 0 COMMENT '全网有效算力',
    total_users INT DEFAULT 0 COMMENT '总用户数',
    active_users INT DEFAULT 0 COMMENT '活跃用户数',
    normal_card_count INT DEFAULT 0 COMMENT '普通卡数量',
    silver_card_count INT DEFAULT 0 COMMENT '白银卡数量',
    gold_card_count INT DEFAULT 0 COMMENT '白金卡数量',
    diamond_card_count INT DEFAULT 0 COMMENT '黑金卡数量',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    INDEX idx_stat_date (stat_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='全网算力统计表';
```

---

### 5.5 卡片升级记录表（card_upgrade_logs）

```sql
CREATE TABLE card_upgrade_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
    card_id BIGINT NOT NULL COMMENT '卡片ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    order_id BIGINT NOT NULL COMMENT '升级订单ID',
    from_level ENUM('NORMAL', 'SILVER', 'GOLD', 'DIAMOND') NOT NULL COMMENT '升级前等级',
    to_level ENUM('NORMAL', 'SILVER', 'GOLD', 'DIAMOND') NOT NULL COMMENT '升级后等级',
    upgrade_price DECIMAL(20, 6) NOT NULL COMMENT '补差价（USDT）',
    hashrate_increase DECIMAL(20, 2) NOT NULL COMMENT '新增算力（T）',
    before_hashrate DECIMAL(20, 2) NOT NULL COMMENT '升级前算力',
    after_hashrate DECIMAL(20, 2) NOT NULL COMMENT '升级后算力',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '升级时间',
    
    INDEX idx_card_id (card_id),
    INDEX idx_user_id (user_id),
    INDEX idx_order_id (order_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='卡片升级记录表';
```

---

## 6. 接口设计

### 6.1 获取卡片等级列表

**接口路径**：`GET /api/cards/levels`

**响应示例**：
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "level": "NORMAL",
      "name": "普通卡",
      "price": 100,
      "hashrate": 100,
      "ratio": 1.00,
      "icon": "🥉"
    },
    {
      "level": "SILVER",
      "name": "白银卡",
      "price": 1000,
      "hashrate": 1050,
      "ratio": 1.05,
      "icon": "🥈"
    }
  ]
}
```

---

### 6.2 购买U卡

**接口路径**：`POST /api/cards/purchase`

**请求参数**：
```json
{
  "card_level": "SILVER",
  "payment_password": "******"
}
```

**响应示例**：
```json
{
  "code": 200,
  "message": "购买成功",
  "data": {
    "order_no": "UC20251203123456789",
    "card_id": 12345,
    "card_no": "CARD20251203001",
    "card_level": "SILVER",
    "card_name": "白银卡",
    "paid_amount": 1000,
    "hashrate": 1050,
    "direct_reward": {
      "referrer_name": "上级用户",
      "reward_usdt": 100,
      "reward_hashrate": 105
    },
    "indirect_reward": {
      "referrer_name": "上上级用户",
      "reward_usdt": 50,
      "reward_hashrate": 52.5
    }
  }
}
```

---

### 6.3 查询我的卡片列表

**接口路径**：`GET /api/cards/my-cards`

**响应示例**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total_cards": 3,
    "total_hashrate": 4200,
    "cards": [
      {
        "card_id": 12345,
        "card_no": "CARD20251203001",
        "card_level": "SILVER",
        "card_name": "白银卡",
        "current_hashrate": 1050,
        "total_paid": 1000,
        "upgrade_count": 0,
        "can_upgrade": true,
        "next_level": "GOLD",
        "upgrade_price": 2000,
        "created_at": "2025-12-03 10:00:00"
      }
    ]
  }
}
```

---

### 6.4 计算升级差价

**接口路径**：`GET /api/cards/{cardId}/upgrade-price`

**请求参数**：
- `cardId`: 卡片ID（路径参数）
- `target_level`: 目标等级（查询参数，如 GOLD）

**响应示例**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "card_id": 12345,
    "current_level": "SILVER",
    "target_level": "GOLD",
    "upgrade_price": 2000,
    "hashrate_increase": 2250,
    "current_hashrate": 1050,
    "after_hashrate": 3300,
    "user_balance": 5000,
    "can_upgrade": true
  }
}
```

---

### 6.5 升级卡片

**接口路径**：`POST /api/cards/{cardId}/upgrade`

**请求参数**：
```json
{
  "target_level": "GOLD",
  "payment_password": "******"
}
```

**响应示例**：
```json
{
  "code": 200,
  "message": "升级成功",
  "data": {
    "order_no": "UU20251203123456789",
    "card_id": 12345,
    "from_level": "SILVER",
    "to_level": "GOLD",
    "upgrade_price": 2000,
    "hashrate_increase": 2250,
    "before_hashrate": 1050,
    "after_hashrate": 3300,
    "direct_reward": {
      "reward_usdt": 200,
      "reward_hashrate": 225
    }
  }
}
```

---

## 7. 业务规则

### 7.1 购买限制

- ✅ 单用户可购买多张卡片
- ✅ 同一用户可持有不同等级的卡片
- ❓ 是否限制单日购买次数（根据业务决定）
- ❓ 是否限制单个等级最大持有数量（根据业务决定）

### 7.2 升级限制

- ✅ 仅支持逐级升级或跨级升级（待确认）
- ❌ 不支持降级
- ✅ 升级无次数限制
- ✅ 升级后卡片编号保持不变

### 7.3 算力生效规则

- ✅ 购买后算力**立即生效**
- ✅ 升级后新增算力**立即生效**
- ✅ 推荐奖励算力**立即计入**总算力

### 7.4 资金流转

**购买流程**：
```
用户 USDT 账户 → 平台 USDT 资金池
                ↓
        10% → 直推人 USDT 账户
                ↓
        5% → 间推人 USDT 账户
```

**升级流程**（同购买）

---

## 8. 异常处理

### 8.1 购买失败场景

| 场景 | 错误码 | 提示信息 | 处理方式 |
|------|--------|---------|---------|
| 余额不足 | 1001 | USDT余额不足，请先充值 | 提示用户充值 |
| 卡片等级不存在 | 1002 | 卡片等级不存在 | 前端校验 |
| 支付密码错误 | 1003 | 支付密码错误 | 重新输入 |
| 系统繁忙 | 1004 | 系统繁忙，请稍后重试 | 稍后重试 |

### 8.2 升级失败场景

| 场景 | 错误码 | 提示信息 | 处理方式 |
|------|--------|---------|---------|
| 卡片不存在 | 2001 | 卡片不存在或已失效 | 刷新列表 |
| 余额不足 | 2002 | USDT余额不足，请先充值 | 提示用户充值 |
| 已是最高等级 | 2003 | 已是最高等级，无法升级 | 前端禁用升级按钮 |
| 卡片已过期 | 2004 | 卡片已过期，无法升级 | 提示购买新卡 |

---

## 9. 开发建议

### 9.1 事务处理

所有涉及资金和算力变动的操作必须使用**数据库事务**：

```java
@Transactional(rollbackFor = Exception.class)
public void purchaseCard() {
    // 1. 扣除用户USDT
    // 2. 创建订单
    // 3. 创建卡片
    // 4. 更新算力统计
    // 5. 发放推荐奖励
    // 6. 更新全网算力
}
```

### 9.2 幂等性保证

- 使用订单号作为唯一标识
- 添加订单状态机制
- 防止重复提交

### 9.3 性能优化

- 全网算力统计使用**异步更新**
- 推荐奖励发放使用**消息队列**
- 算力排行榜使用**Redis缓存**

### 9.4 数据一致性

- 用户算力 = 购买算力 + 直推奖励算力 + 间推奖励算力
- 定期校验算力数据一致性
- 记录所有算力变动日志

---

## 10. 测试用例

### 10.1 购买测试

| 测试场景 | 预期结果 |
|---------|---------|
| 购买普通卡（余额足够） | 扣款成功，获得100T算力 |
| 购买白银卡（余额不足） | 提示余额不足 |
| 购买后检查上级奖励 | 直推人获得10% USDT + 10%算力 |
| 购买后检查上上级奖励 | 间推人获得5% USDT + 5%算力 |

### 10.2 升级测试

| 测试场景 | 预期结果 |
|---------|---------|
| 普通卡升级白银卡（余额足够） | 扣款900U，算力增加950T |
| 白银卡升级白金卡（余额足够） | 扣款2000U，算力增加2250T |
| 黑金卡再次升级 | 提示已是最高等级 |
| 升级后检查推荐奖励 | 上级获得差价10%的USDT和算力 |

---

## 11. 前后端对接清单

### 11.1 后端需要提供的接口

- [x] 获取卡片等级列表
- [x] 购买U卡
- [x] 查询我的卡片列表
- [x] 查询卡片详情
- [x] 计算升级差价
- [x] 升级卡片
- [x] 查询购买/升级订单列表
- [x] 查询订单详情

### 11.2 前端需要实现的页面

- [ ] U卡商城页面（展示4种卡片）
- [ ] 购买确认弹窗
- [ ] 我的卡片列表页面
- [ ] 卡片详情页面
- [ ] 升级确认弹窗
- [ ] 订单列表页面
- [ ] 订单详情页面

---

**文档版本**：v1.0  
**创建日期**：2025年12月3日  
**负责人**：开发团队  
**协作方**：前端团队、后端团队

