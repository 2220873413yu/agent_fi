package com.xms.app.entity.dto;

import com.xms.dao.domain.PolymarketOrder;

/**
 * App端Polymarket内部订单返回对象。
 *
 * <p>当前版本直接继承订单实体，便于返回订单快照字段；列表接口会在服务层清理原始JSON快照，详情接口才返回。</p>
 */
public class PolymarketOrderDto extends PolymarketOrder {
}
