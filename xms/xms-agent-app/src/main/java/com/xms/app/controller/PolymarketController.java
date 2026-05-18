package com.xms.app.controller;

import com.alibaba.fastjson.JSONObject;
import com.xms.app.entity.dto.PolymarketEventListDto;
import com.xms.app.entity.dto.PolymarketMarketDetailDto;
import com.xms.app.entity.dto.PolymarketOrderConfigDto;
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
import io.swagger.annotations.ApiParam;
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
	@ApiOperation(value = "Polymarket普通板块事件", notes = "按板块代理查询 Polymarket Gamma API 事件列表，默认返回精简DTO：事件基础信息和每个事件前8个市场。完整原始市场字段请使用market slug详情接口。")
	@Anonymous
	@GetMapping("/events")
	public ResultPista<PolymarketEventListDto> events(@ApiParam(value = "板块名称：crypto加密货币，sports体育", defaultValue = "crypto")
										  @RequestParam(defaultValue = "crypto") String section,
										  @ApiParam(value = "每页事件数量，后端会限制最大值", defaultValue = "20")
										  @RequestParam(defaultValue = "20") Integer limit,
										  @ApiParam(value = "分页偏移量，从0开始", defaultValue = "0")
										  @RequestParam(defaultValue = "0") Integer offset) {
		return ResultPista.data(polymarketService.listEvents(section, limit, offset));
	}

//	/**
//	 * 查询Polymarket加密货币板块事件。
//	 *
//	 * @param limit 每页数量
//	 * @param offset 分页偏移量
//	 * @return 加密货币板块事件列表
//	 */
//	@ApiOperation(value = "Polymarket加密货币事件", notes = "固定查询 crypto 板块，返回精简事件DTO和每个事件前8个市场。完整原始市场字段请使用market slug详情接口。")
//	@Anonymous
//	@GetMapping("/crypto/events")
//	public ResultPista<PolymarketEventListDto> cryptoEvents(@ApiParam(value = "每页事件数量，后端会限制最大值", defaultValue = "20")
//												@RequestParam(defaultValue = "20") Integer limit,
//												@ApiParam(value = "分页偏移量，从0开始", defaultValue = "0")
//												@RequestParam(defaultValue = "0") Integer offset) {
//		return ResultPista.data(polymarketService.listEvents("crypto", limit, offset));
//	}

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
	@ApiOperation(value = "Polymarket加密货币Up/Down事件", notes = "按币种和当前5分钟窗口组装 Up/Down 市场slug并查询详情，返回 Polymarket 原始市场结构和本地调试字段。")
	@Anonymous
	@GetMapping("/crypto/updown")
	public ResultPista<JSONObject> cryptoUpDown(@ApiParam(value = "逗号分隔币种，支持btc、eth、sol、xrp", defaultValue = "btc,eth,sol")
												@RequestParam(defaultValue = "btc,eth,sol") String coins,
												@ApiParam(value = "当前5分钟窗口之前查询几个窗口", defaultValue = "2")
												@RequestParam(defaultValue = "2") Integer before,
												@ApiParam(value = "当前5分钟窗口之后查询几个窗口", defaultValue = "6")
												@RequestParam(defaultValue = "6") Integer after) {
		return ResultPista.data(polymarketService.listCryptoUpDownEvents(coins, before, after));
	}

//	/**
//	 * 查询Polymarket体育板块事件。
//	 *
//	 * @param limit 每页数量
//	 * @param offset 分页偏移量
//	 * @return 体育板块事件列表
//	 */
//	@ApiOperation(value = "Polymarket体育事件", notes = "固定查询 sports 板块，返回精简事件DTO和每个事件前8个市场。完整原始市场字段请使用market slug详情接口。")
//	@Anonymous
//	@GetMapping("/sports/events")
//	public ResultPista<PolymarketEventListDto> sportsEvents(@ApiParam(value = "每页事件数量，后端会限制最大值", defaultValue = "20")
//												@RequestParam(defaultValue = "20") Integer limit,
//												@ApiParam(value = "分页偏移量，从0开始", defaultValue = "0")
//												@RequestParam(defaultValue = "0") Integer offset) {
//		return ResultPista.data(polymarketService.listEvents("sports", limit, offset));
//	}

	/**
	 * 按market slug查询Polymarket市场详情。
	 *
	 * @param slug Polymarket市场slug
	 * @return 市场原始详情，并带本地调试字段
	 */
	@ApiOperation(value = "Polymarket市场详情", notes = "按 Polymarket market slug 查询市场详情，返回前端展示用精简DTO；完整原始市场字段仅供后端报价、下单和结算内部使用。")
	@Anonymous
	@GetMapping("/markets/slug/{slug}")
	public ResultPista<PolymarketMarketDetailDto> marketBySlug(@ApiParam(value = "Polymarket市场slug", required = true)
												@PathVariable String slug) {
		return ResultPista.data(polymarketService.getMarketBySlug(slug));
	}

	/**
	 * 查询Polymarket内部下单页面配置。
	 *
	 * <p>前端用这里的值对齐后端强校验，例如最低AFI下单数量、结束前最后几秒禁用下单，避免页面能点但接口拒绝。</p>
	 *
	 * @return 最低AFI下单数量、结束前禁用秒数和App端Authorization前缀
	 */
	@ApiOperation(value = "Polymarket内部订单配置", notes = "返回内部下单页面配置，包括最低折算AFI数量、结束前禁止下单秒数和App请求头前缀。")
	@Anonymous
	@GetMapping("/orders/config")
	public ResultPista<PolymarketOrderConfigDto> orderConfig() {
		return ResultPista.data(PolymarketOrderConfigDto.builder()
			.minAfiAmount(polymarketOrderAppService.getMinAfiOrderAmount())
			.minSecondsBeforeEnd(5)
			.authorizationPrefix("App ")
			.build());
	}

	/**
	 * 预览平台内部Polymarket订单报价。
	 *
	 * <p>只实时读取AFI价格、Polymarket outcome价格和用户节点手续费减免，计算等值USDT、购买成本AFI、外扣手续费和最大兑付；不会扣钱包、不会写订单。</p>
	 *
	 * @param req 报价参数：marketSlug、outcomeIndex、shareAmount
	 * @return 报价快照，金额单位包括购买成本AFI、手续费AFI、实际总扣款AFI和USDT
	 */
	@ApiOperation(value = "Polymarket内部订单报价", notes = "请求体字段为marketSlug、outcomeIndex、shareAmount、bizType。bizType为1加密、2体育、3Up/Down；后端实时读取AFI价格、Polymarket outcome价格和用户节点手续费减免，按购买份额反算购买成本AFI、外扣手续费AFI和实际总扣款AFI，不扣钱包、不写订单。")
	@PostMapping("/orders/quote")
	public ResultPista<PolymarketOrderQuoteDto> quote(@ApiParam(value = "报价请求体：marketSlug市场slug，outcomeIndex选择结果下标，shareAmount购买份额，bizType业务类型1加密2体育3Up/Down", required = true)
													  @Valid @RequestBody PolymarketOrderReq req) {
		return ResultPista.data(polymarketOrderAppService.quote(req, SecurityUtils.getLoginAppUser().getUserId()));
	}

	/**
	 * 为当前App用户创建平台内部Polymarket订单。
	 *
	 * <p>创建时会重新拉取实时价格，不信任前端报价；成功后从用户AFI钱包validNum2扣减购买成本和外扣手续费，并保存订单快照。</p>
	 *
	 * @param req 下单参数：marketSlug、outcomeIndex、shareAmount、bizType
	 * @return 已创建的内部订单快照
	 */
	@ApiOperation(value = "创建Polymarket内部订单", notes = "请求体字段为marketSlug、outcomeIndex、shareAmount、bizType。bizType为1加密、2体育、3Up/Down；创建时重新拉实时价格，按购买份额折算购买成本AFI和外扣手续费AFI，合并扣减用户validNum2，生成平台内部订单。")
	@PostMapping("/orders/create")
	@RepeatSubmit
	public ResultPista<PolymarketOrderDto> createOrder(@ApiParam(value = "下单请求体：marketSlug市场slug，outcomeIndex选择结果下标，shareAmount购买份额，bizType业务类型1加密2体育3Up/Down", required = true)
													   @Valid @RequestBody PolymarketOrderReq req) {
		return polymarketOrderAppService.create(req, SecurityUtils.getLoginAppUser().getUserId());
	}

	/**
	 * 查询当前App用户的Polymarket内部订单列表。
	 *
	 * @param lastId 可选游标，只查id更小的记录
	 * @param bizType 可选业务类型
	 * @return 当前用户订单列表
	 */
	@ApiOperation(value = "我的Polymarket内部订单", notes = "查询当前App登录用户的内部订单列表；lastId用于向下翻页，bizType为空时查询全部业务类型。")
	@GetMapping("/orders/my")
	public ResultPista<List<PolymarketOrderDto>> myOrders(@ApiParam(value = "分页游标，只查询id小于该值的订单")
														  Long lastId,
														  @ApiParam(value = "业务类型：1加密，2体育，3Up/Down")
														  Integer bizType) {
		return ResultPista.data(polymarketOrderAppService.myOrders(lastId, bizType, SecurityUtils.getLoginAppUser().getUserId()));
	}

	/**
	 * 查询当前App用户的一笔Polymarket内部订单详情。
	 *
	 * @param orderNo 平台内部订单号
	 * @return 订单详情
	 */
	@ApiOperation(value = "Polymarket内部订单详情", notes = "按平台内部订单号查询当前App登录用户的一笔Polymarket内部订单详情。")
	@GetMapping("/orders/{orderNo}")
	public ResultPista<PolymarketOrderDto> orderDetail(@ApiParam(value = "平台内部订单号", required = true)
													   @PathVariable String orderNo) {
		return ResultPista.data(polymarketOrderAppService.detail(orderNo, SecurityUtils.getLoginAppUser().getUserId()));
	}

}
