package com.xms.app.service;

import com.alibaba.fastjson.JSONObject;

/**
 * Polymarket公开行情代理服务。
 *
 * <p>当前阶段只读取Gamma公开接口，服务调研页和内部下单报价；这里不做真实下单、不接钱包签名、不落库行情。</p>
 */
public interface PolymarketService {

	/**
	 * 查询普通板块的活跃事件列表。
	 *
	 * @param section 板块名称，目前支持crypto和sports
	 * @param limit 每页数量，服务层会做上限保护
	 * @param offset 分页偏移量
	 * @return 原始事件列表，并带本地调试字段
	 */
	JSONObject listEvents(String section, Integer limit, Integer offset);

	/**
	 * 查询加密货币5分钟Up/Down短周期事件。
	 *
	 * <p>Polymarket这类市场的slug按“币种-updown-5m-五分钟对齐时间戳”生成，例如
	 * {@code btc-updown-5m-1778915700}。本接口会围绕当前时间向前/向后生成一组slug并实时查询Gamma API。</p>
	 *
	 * @param coins 逗号分隔的币种，支持btc、eth、sol、xrp
	 * @param before 当前5分钟窗口之前要查询的窗口数量
	 * @param after 当前5分钟窗口之后要查询的窗口数量
	 * @return Up/Down事件列表，保留Polymarket原始字段并补充本地调试字段
	 */
	JSONObject listCryptoUpDownEvents(String coins, Integer before, Integer after);

	/**
	 * 按公开slug查询一个Polymarket市场详情。
	 *
	 * @param slug Polymarket市场slug
	 * @return 原始市场详情，并带本地调试字段
	 */
	JSONObject getMarketBySlug(String slug);
}
