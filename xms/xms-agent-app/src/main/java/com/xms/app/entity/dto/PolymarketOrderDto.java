package com.xms.app.entity.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.xms.common.annotation.Excel;
import com.xms.dao.domain.PolymarketOrder;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.math.BigDecimal;
import java.util.Date;

/**
 * App端Polymarket内部订单返回对象。
 *
 * <p>当前版本直接继承订单实体，便于返回订单快照字段；列表接口会在服务层清理原始JSON快照，详情接口才返回。</p>
 */
@ApiModel(value = "PolymarketOrderDto", description = "Polymarket内部订单返回对象，继承订单快照字段")
public class PolymarketOrderDto{

	/** 业务类型：1加密，2体育，3Up/Down。 */
	@Excel(name = "业务类型", sort = 7, dictType = "t_polymarket_order_biz_type")
	@ApiModelProperty(value = "业务类型 1加密 2体育 3Up/Down")
	private Integer bizType;

	/** 主键ID。 */
	@TableId(type = IdType.AUTO)
	private Long id;

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

	/** 后端按购买份额、outcome价格和AFI价格折算出的实际扣减AFI数量，对应用户valid_num2。 */
	@Excel(name = "下单AFI", sort = 13)
	@ApiModelProperty(value = "实际扣减AFI数量，由购买份额按实时价格折算得到")
	private BigDecimal afiAmount;

	/** 下单时AFI/USDT价格快照。 */
	@Excel(name = "AFI价格", sort = 14)
	@ApiModelProperty(value = "AFI价格快照，单位USDT")
	private BigDecimal afiPrice;

	/** AFI按下单价格折算后的USDT等值金额。 */
	@Excel(name = "等值USDT", sort = 15)
	@ApiModelProperty(value = "AFI等值USDT")
	private BigDecimal afiUsdtAmount;

	/** 下单时Polymarket结果价格快照。 */
	@Excel(name = "成交价格", sort = 16)
	@ApiModelProperty(value = "Polymarket outcome成交价格")
	private BigDecimal outcomePrice;

	/** 用户购买的结果份额，猜中后最大兑付USDT数量等于该份额。 */
	@Excel(name = "份额", sort = 17)
	@ApiModelProperty(value = "购买份额，猜中后最大兑付USDT数量等于该份额")
	private BigDecimal shareAmount;

	/** 用户猜中时最大兑付USDT金额。 */
	@Excel(name = "最大兑付USDT", sort = 18)
	@ApiModelProperty(value = "最大兑付USDT")
	private BigDecimal maxPayoutUsdt;

	/** 市场结束时间快照。 */
	@Excel(name = "结束时间", sort = 19, dateFormat = "yyyy-MM-dd HH:mm:ss")
	@ApiModelProperty(value = "市场结束时间")
	private Date endTime;

	/** 订单状态：0待结算，1已猜中，2未猜中，3待人工复核，4已作废/已退款。 */
	@Excel(name = "订单状态", sort = 20, dictType = "t_polymarket_order_status")
	@ApiModelProperty(value = "订单状态 0待结算 1已猜中 2未猜中 3待人工复核 4已作废/已退款")
	private Integer status;

	/** 结算后赢家结果下标。 */
	@Excel(name = "赢家下标", sort = 21)
	@ApiModelProperty(value = "赢家结果下标")
	private Integer resultOutcomeIndex;

	/** 结算后赢家结果名称。 */
	@Excel(name = "赢家结果", sort = 22)
	@ApiModelProperty(value = "赢家结果")
	private String resultOutcomeName;

	/** 中奖应兑付的USDT等值金额，不是实际入账币种。 */
	@Excel(name = "兑付USDT等值", sort = 23)
	@ApiModelProperty(value = "中奖应兑付的USDT等值金额")
	private BigDecimal payoutUsdtAmount;

	/** 结算时使用的AFI/USDT价格快照。 */
	@Excel(name = "结算AFI价格", sort = 24)
	@ApiModelProperty(value = "结算时AFI/USDT价格快照")
	private BigDecimal payoutAfiPrice;

	/** 实际发放到用户valid_num2的AFI数量。 */
	@Excel(name = "兑付AFI", sort = 25)
	@ApiModelProperty(value = "实际发放到AFI钱包validNum2的数量")
	private BigDecimal payoutAfiAmount;

	/** 结算时间。 */
	@Excel(name = "结算时间", sort = 26, dateFormat = "yyyy-MM-dd HH:mm:ss")
	@ApiModelProperty(value = "结算时间")
	private Date settleTime;
}
