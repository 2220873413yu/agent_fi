package com.xms.app.service;

import com.xms.app.entity.dto.PolymarketOrderDto;
import com.xms.app.entity.dto.PolymarketOrderQuoteDto;
import com.xms.app.entity.req.PolymarketOrderReq;
import com.xms.common.core.domain.api.ResultPista;

import java.math.BigDecimal;
import java.util.List;

/**
 * App端Polymarket内部订单服务。
 *
 * <p>用户用平台AFI余额创建内部预测订单，订单不进入Polymarket真实订单簿，也不支持卖出或撤销。</p>
 */
public interface PolymarketOrderAppService {

	/**
	 * 预览内部订单报价。
	 *
	 * @param req 报价参数，包含市场slug、选择结果下标和购买份额数量
	 * @param userId 当前App用户ID，用于读取节点手续费减免比例
	 * @return 报价结果，包含购买成本AFI、外扣手续费AFI、实际总扣款AFI、USDT等值和Polymarket结果价格
	 */
	PolymarketOrderQuoteDto quote(PolymarketOrderReq req, Long userId);

	/**
	 * 读取Polymarket内部订单最低购买份额/token数量。
	 *
	 * @return 系统参数中的最低购买份额；参数缺失或非法时返回服务兜底值
	 */
	BigDecimal getMinOrderShareAmount();

	/**
	 * 创建平台内部Polymarket订单。
	 *
	 * @param req 下单参数，包含市场slug、选择结果下标和购买份额数量
	 * @param userId 当前App用户ID
	 * @return 已创建的订单快照
	 */
	ResultPista<PolymarketOrderDto> create(PolymarketOrderReq req, Long userId);

	/**
	 * 查询当前用户的内部订单列表。
	 *
	 * @param lastId 可选游标，只返回ID小于该值的记录
	 * @param bizType 可选业务类型，1加密、2体育、3Up/Down
	 * @param userId 当前App用户ID
	 * @return 用户订单列表
	 */
	List<PolymarketOrderDto> myOrders(Long lastId, Integer bizType, Long userId);

	/**
	 * 查询当前用户的一笔内部订单详情。
	 *
	 * @param orderNo 平台内部订单号
	 * @param userId 当前App用户ID
	 * @return 订单详情
	 */
	PolymarketOrderDto detail(String orderNo, Long userId);

	/**
	 * 派发已到结束时间的待结算市场。
	 *
	 * @param limit 本次最多派发的市场数
	 * @return 成功改为结算中的市场数
	 */
	int settlePendingOrders(Integer limit);
}
