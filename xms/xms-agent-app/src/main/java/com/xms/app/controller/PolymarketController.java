package com.xms.app.controller;

import com.alibaba.fastjson.JSONObject;
import com.xms.app.entity.dto.PolymarketOrderDto;
import com.xms.app.entity.dto.PolymarketOrderQuoteDto;
import com.xms.app.entity.req.PolymarketOrderReq;
import com.xms.app.service.PolymarketOrderAppService;
import com.xms.app.service.PolymarketService;
import com.xms.common.annotation.Anonymous;
import com.xms.common.annotation.RepeatSubmit;
import com.xms.common.core.domain.api.ResultPista;
import com.xms.common.utils.SecurityUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Polymarket行情调研与平台内部订单接口。
 *
 * <p>行情接口只做公开Gamma API代理，允许匿名访问；下单接口必须是App登录用户，只创建平台内部AFI订单，
 * 不会向Polymarket真实CLOB订单簿下单。</p>
 */
@Api(tags = "Polymarket")
@RestController
@AllArgsConstructor
@RequestMapping("/api/polymarket")
public class PolymarketController {

	private final PolymarketService polymarketService;
	private final PolymarketOrderAppService polymarketOrderAppService;

	/**
	 * 查询Polymarket普通板块事件列表。
	 *
	 * @param section 板块名称，只支持crypto或sports
	 * @param limit 每页数量，服务层会限制最大值
	 * @param offset 分页偏移量
	 * @return Polymarket原始事件列表，并带本地调试字段
	 */
	@ApiOperation(value = "Polymarket普通板块事件")
	@Anonymous
	@GetMapping("/events")
	public ResultPista<JSONObject> events(@RequestParam(defaultValue = "crypto") String section,
										  @RequestParam(defaultValue = "20") Integer limit,
										  @RequestParam(defaultValue = "0") Integer offset) {
		return ResultPista.data(polymarketService.listEvents(section, limit, offset));
	}

	/**
	 * 查询Polymarket加密货币板块事件。
	 *
	 * @param limit 每页数量
	 * @param offset 分页偏移量
	 * @return 加密货币板块事件列表
	 */
	@ApiOperation(value = "Polymarket加密货币事件")
	@Anonymous
	@GetMapping("/crypto/events")
	public ResultPista<JSONObject> cryptoEvents(@RequestParam(defaultValue = "20") Integer limit,
												@RequestParam(defaultValue = "0") Integer offset) {
		return ResultPista.data(polymarketService.listEvents("crypto", limit, offset));
	}

	/**
	 * 查询加密货币5分钟Up/Down短周期事件。
	 *
	 * <p>该接口服务独立调研页的“Crypto Up/Down”模块。它不是普通Crypto事件列表，而是按固定slug规则围绕当前时间查询短周期市场。</p>
	 *
	 * @param coins 逗号分隔币种，支持btc、eth、sol、xrp
	 * @param before 当前5分钟窗口之前的窗口数量
	 * @param after 当前5分钟窗口之后的窗口数量
	 * @return 当前时间附近的Up/Down事件列表
	 */
	@ApiOperation(value = "Polymarket加密货币Up/Down事件")
	@Anonymous
	@GetMapping("/crypto/updown")
	public ResultPista<JSONObject> cryptoUpDown(@RequestParam(defaultValue = "btc,eth,sol") String coins,
												@RequestParam(defaultValue = "2") Integer before,
												@RequestParam(defaultValue = "6") Integer after) {
		return ResultPista.data(polymarketService.listCryptoUpDownEvents(coins, before, after));
	}

	/**
	 * 查询Polymarket体育板块事件。
	 *
	 * @param limit 每页数量
	 * @param offset 分页偏移量
	 * @return 体育板块事件列表
	 */
	@ApiOperation(value = "Polymarket体育事件")
	@Anonymous
	@GetMapping("/sports/events")
	public ResultPista<JSONObject> sportsEvents(@RequestParam(defaultValue = "20") Integer limit,
												@RequestParam(defaultValue = "0") Integer offset) {
		return ResultPista.data(polymarketService.listEvents("sports", limit, offset));
	}

	/**
	 * 按market slug查询Polymarket市场详情。
	 *
	 * @param slug Polymarket市场slug
	 * @return 市场原始详情，并带本地调试字段
	 */
	@ApiOperation(value = "Polymarket市场详情")
	@Anonymous
	@GetMapping("/markets/slug/{slug}")
	public ResultPista<JSONObject> marketBySlug(@PathVariable String slug) {
		return ResultPista.data(polymarketService.getMarketBySlug(slug));
	}

	/**
	 * 查询Polymarket内部下单页面配置。
	 *
	 * <p>前端用这里的值对齐后端强校验，例如最低AFI下单数量、结束前最后几秒禁用下单，避免页面能点但接口拒绝。</p>
	 *
	 * @return 最低AFI下单数量、结束前禁用秒数和App端Authorization前缀
	 */
	@ApiOperation(value = "Polymarket内部订单配置")
	@Anonymous
	@GetMapping("/orders/config")
	public ResultPista<JSONObject> orderConfig() {
		JSONObject config = new JSONObject();
		config.put("minAfiAmount", polymarketOrderAppService.getMinAfiOrderAmount());
		config.put("minSecondsBeforeEnd", 5);
		config.put("authorizationPrefix", "App ");
		return ResultPista.data(config);
	}

	/**
	 * 预览平台内部Polymarket订单报价。
	 *
	 * <p>只实时读取AFI价格和Polymarket outcome价格，计算等值USDT、份额和最大兑付；不会扣钱包、不会写订单。</p>
	 *
	 * @param req 报价参数：marketSlug、outcomeIndex、afiAmount
	 * @return 报价快照，金额单位包括AFI和USDT
	 */
	@ApiOperation(value = "Polymarket内部订单报价")
	@PostMapping("/orders/quote")
	public ResultPista<PolymarketOrderQuoteDto> quote(@Valid @RequestBody PolymarketOrderReq req) {
		return ResultPista.data(polymarketOrderAppService.quote(req));
	}

	/**
	 * 为当前App用户创建平台内部Polymarket订单。
	 *
	 * <p>创建时会重新拉取实时价格，不信任前端报价；成功后扣减用户AFI钱包validNum2并保存订单快照。</p>
	 *
	 * @param req 下单参数：marketSlug、outcomeIndex、afiAmount
	 * @return 已创建的内部订单快照
	 */
	@ApiOperation(value = "创建Polymarket内部订单")
	@PostMapping("/orders/create")
	@RepeatSubmit
	public ResultPista<PolymarketOrderDto> createOrder(@Valid @RequestBody PolymarketOrderReq req) {
		return polymarketOrderAppService.create(req, SecurityUtils.getLoginAppUser().getUserId());
	}

	/**
	 * 查询当前App用户的Polymarket内部订单列表。
	 *
	 * @param lastId 可选游标，只查id更小的记录
	 * @param status 可选订单状态
	 * @return 当前用户订单列表
	 */
	@ApiOperation(value = "我的Polymarket内部订单")
	@GetMapping("/orders/my")
	public ResultPista<List<PolymarketOrderDto>> myOrders(Long lastId, Integer status) {
		return ResultPista.data(polymarketOrderAppService.myOrders(lastId, status, SecurityUtils.getLoginAppUser().getUserId()));
	}

	/**
	 * 查询当前App用户的一笔Polymarket内部订单详情。
	 *
	 * @param orderNo 平台内部订单号
	 * @return 订单详情
	 */
	@ApiOperation(value = "Polymarket内部订单详情")
	@GetMapping("/orders/{orderNo}")
	public ResultPista<PolymarketOrderDto> orderDetail(@PathVariable String orderNo) {
		return ResultPista.data(polymarketOrderAppService.detail(orderNo, SecurityUtils.getLoginAppUser().getUserId()));
	}

}
