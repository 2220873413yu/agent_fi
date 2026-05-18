package com.xms.app.entity.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Polymarket普通板块事件列表精简返回对象。
 *
 * <p>用于Crypto/Sports事件列表接口。该对象只承载列表展示和选择市场需要的数据，
 * 不作为下单价格快照；正式报价和下单仍会按marketSlug实时查询Polymarket市场详情。</p>
 */
@Data
@Builder
@ApiModel(value = "PolymarketEventListDto", description = "Polymarket普通板块事件列表精简返回")
public class PolymarketEventListDto {

	/**
	 * 板块名称。
	 *
	 * <p>当前只支持crypto和sports。</p>
	 */
	@ApiModelProperty(value = "板块名称：crypto加密货币，sports体育", example = "crypto")
	private String section;

	/**
	 * Polymarket Gamma API的标签ID。
	 *
	 * <p>crypto当前对应21，sports当前对应1。</p>
	 */
	@ApiModelProperty(value = "Polymarket Gamma tag_id", example = "21")
	private Integer tagId;

	/**
	 * 本次接口返回数据的获取时间。
	 *
	 * <p>格式为服务端LocalDateTime字符串，用于前端展示和排查缓存刷新时间。</p>
	 */
	@ApiModelProperty(value = "本次数据获取时间")
	private String fetchedAt;

	/**
	 * 本次请求Polymarket Gamma的源地址。
	 *
	 * <p>仅用于调试排查，不参与业务判断。</p>
	 */
	@ApiModelProperty(value = "本次请求的Polymarket Gamma源地址，用于调试排查")
	private String sourceUrl;

	/**
	 * 分页每页事件数量。
	 *
	 * <p>该值是后端归一化后的实际请求数量。</p>
	 */
	@ApiModelProperty(value = "分页每页事件数量", example = "20")
	private Integer limit;

	/**
	 * 分页偏移量。
	 *
	 * <p>从0开始，透传给Polymarket Gamma列表接口。</p>
	 */
	@ApiModelProperty(value = "分页偏移量，从0开始", example = "0")
	private Integer offset;

	/**
	 * 本次实际返回的事件数量。
	 */
	@ApiModelProperty(value = "本次返回事件数量", example = "20")
	private Integer count;

	/**
	 * 事件精简列表。
	 *
	 * <p>每个事件下默认最多返回8个市场，完整市场原始字段请使用market slug详情接口查询。</p>
	 */
	@ApiModelProperty(value = "事件精简列表，每个事件默认最多返回8个市场")
	private List<PolymarketEventDto> events;
}
