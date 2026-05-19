package com.xms.app.entity.dto;

import com.xms.common.annotation.Excel;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * App端Polymarket内部订单返回对象。
 *
 * <p>该对象只返回订单展示和对账需要的结构化快照字段；后台订单实体会保留下单市场原始JSON用于核对。</p>
 */
@Data
@ApiModel(value = "PolymarketOrderDto", description = "Polymarket内部订单返回对象")
public class PolymarketOrderDto {

	/** 主键ID。 */
	@ApiModelProperty(value = "主键ID")
	private Long id;

	/** 业务类型：1加密，2体育，3Up/Down。 */
	@Excel(name = "业务类型", sort = 7, dictType = "t_polymarket_order_biz_type")
	@ApiModelProperty(value = "业务类型 1加密 2体育 3Up/Down")
	private Integer bizType;

	/** 平台内部订单号。 */
	@Excel(name = "订单号", sort = 1, width = 30)
	@ApiModelProperty(value = "订单号")
	private String orderNo;

	/** Polymarket事件标题快照。 */
	@Excel(name = "事件标题", sort = 5, width = 40)
	@ApiModelProperty(value = "Polymarket事件标题")
	private String eventTitle;

	/** Polymarket市场slug。 */
	@Excel(name = "市场slug", sort = 6, width = 40)
	@ApiModelProperty(value = "Polymarket市场slug")
	private String marketSlug;

	/** 用户选择结果在Polymarket outcomes数组中的下标。 */
	@Excel(name = "结果下标", sort = 10)
	@ApiModelProperty(value = "选择结果下标")
	private Integer outcomeIndex;

	/** 用户选择结果名称，例如Yes、No、Up、Down。 */
	@Excel(name = "选择结果", sort = 11)
	@ApiModelProperty(value = "选择结果")
	private String outcomeName;

	/** 用户选择结果对应的Polymarket asset_id/token_id快照。 */
	@Excel(name = "资产ID", sort = 12, width = 50)
	@ApiModelProperty(value = "用户选择结果对应的Polymarket asset_id/token_id快照")
	private String assetId;

	/** 购买份额成本折算出的AFI数量，不包含外扣手续费。 */
	@Excel(name = "购买成本AFI", sort = 13)
	@ApiModelProperty(value = "购买份额成本折算出的AFI数量，不包含外扣手续费")
	private BigDecimal afiAmount;

	/** 系统基础交易手续费比例快照，单位%。 */
	@Excel(name = "基础手续费比例", sort = 14)
	@ApiModelProperty(value = "系统基础交易手续费比例快照，单位%")
	private BigDecimal feeRatio;

	/** 节点订单带来的手续费减免比例快照，单位%。 */
	@Excel(name = "手续费减免比例", sort = 15)
	@ApiModelProperty(value = "节点订单带来的手续费减免比例快照，单位%")
	private BigDecimal feeReliefRatio;

	/** 实际外扣手续费比例快照，单位%。 */
	@Excel(name = "实际手续费比例", sort = 16)
	@ApiModelProperty(value = "实际外扣手续费比例快照，单位%")
	private BigDecimal actualFeeRatio;

	/** 本单额外扣减的手续费AFI数量。 */
	@Excel(name = "手续费AFI", sort = 17)
	@ApiModelProperty(value = "本单外扣手续费AFI数量")
	private BigDecimal feeAfiAmount;

	/** 实际总扣款AFI数量，等于购买成本AFI加手续费AFI。 */
	@Excel(name = "总扣款AFI", sort = 18)
	@ApiModelProperty(value = "实际总扣款AFI数量，等于购买成本AFI加手续费AFI")
	private BigDecimal totalPayAfiAmount;

	/** 下单时AFI/USDT价格快照。 */
	@Excel(name = "AFI价格", sort = 19)
	@ApiModelProperty(value = "AFI价格快照，单位USDT")
	private BigDecimal afiPrice;

	/** 购买份额成本的USDT等值金额。 */
	@Excel(name = "等值USDT", sort = 20)
	@ApiModelProperty(value = "购买份额成本的USDT等值金额")
	private BigDecimal afiUsdtAmount;

	/** 下单时Polymarket结果价格快照。 */
	@Excel(name = "成交价格", sort = 21)
	@ApiModelProperty(value = "Polymarket outcome成交价格")
	private BigDecimal outcomePrice;

	/** 用户购买的结果份额。 */
	@Excel(name = "份额", sort = 22)
	@ApiModelProperty(value = "购买份额，猜中后最大兑付USDT数量等于该份额")
	private BigDecimal shareAmount;

	/** 用户猜中时最大兑付USDT金额。 */
	@Excel(name = "最大兑付USDT", sort = 23)
	@ApiModelProperty(value = "最大兑付USDT")
	private BigDecimal maxPayoutUsdt;

	/** 市场结束时间快照。 */
	@Excel(name = "结束时间", sort = 24, dateFormat = "yyyy-MM-dd HH:mm:ss")
	@ApiModelProperty(value = "市场结束时间")
	private Date endTime;

	/** 订单处理状态：0待结算，1已结算，2待人工复核，3已作废/已退款。 */
	@Excel(name = "订单状态", sort = 25, dictType = "t_polymarket_order_status")
	@ApiModelProperty(value = "订单状态 0待结算 1已结算 2待人工复核 3已作废/已退款")
	private Integer status;

	/** 判定结果：0未开奖，1赢，2输。 */
	@Excel(name = "判定结果", sort = 26, dictType = "t_polymarket_order_resolved_status")
	@ApiModelProperty(value = "判定结果 0未开奖 1赢 2输")
	private Integer resolvedStatus;

	/** 结算后赢家结果下标。 */
	@Excel(name = "赢家下标", sort = 26)
	@ApiModelProperty(value = "赢家结果下标")
	private Integer resultOutcomeIndex;

	/** 结算后赢家结果名称。 */
	@Excel(name = "赢家结果", sort = 27)
	@ApiModelProperty(value = "赢家结果")
	private String resultOutcomeName;

	/** 中奖应兑付的USDT等值金额，不是实际入账币种。 */
	@Excel(name = "兑付USDT等值", sort = 28)
	@ApiModelProperty(value = "中奖应兑付的USDT等值金额")
	private BigDecimal payoutUsdtAmount;

	/** 结算时使用的AFI/USDT价格快照。 */
	@Excel(name = "结算AFI价格", sort = 29)
	@ApiModelProperty(value = "结算时AFI/USDT价格快照")
	private BigDecimal payoutAfiPrice;

	/** 实际发放到用户valid_num2的AFI数量。 */
	@Excel(name = "兑付AFI", sort = 30)
	@ApiModelProperty(value = "实际发放到AFI钱包validNum2的数量")
	private BigDecimal payoutAfiAmount;

	/** 结算时间。 */
	@Excel(name = "结算时间", sort = 31, dateFormat = "yyyy-MM-dd HH:mm:ss")
	@ApiModelProperty(value = "结算时间")
	private Date settleTime;
}
