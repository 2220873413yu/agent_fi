package com.xms.dao.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.xms.common.annotation.Excel;
import com.xms.common.core.domain.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Polymarket平台内部订单对象，对应表t_polymarket_order。
 *
 * <p>该订单记录用户用平台AFI购买的内部预测份额，不是Polymarket真实CLOB订单；创建后不可卖出或撤销，只等待结算。</p>
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "PolymarketOrder", description = "Polymarket平台内部订单快照")
@TableName(value = "t_polymarket_order")
public class PolymarketOrder extends BaseEntity {
	private static final long serialVersionUID = 1L;

	/** 主键ID。 */
	@TableId(type = IdType.AUTO)
	private Long id;

	/** 平台内部订单号。 */
	@Excel(name = "订单号", sort = 1, width = 30)
	@ApiModelProperty(value = "订单号")
	private String orderNo;

	/** 用户ID。 */
	@Excel(name = "用户ID", sort = 2)
	@ApiModelProperty(value = "用户ID")
	private Long userId;

	/** 用户钱包地址快照。 */
	@Excel(name = "钱包地址", sort = 3, width = 40)
	@ApiModelProperty(value = "钱包地址快照")
	private String account;

	/** Polymarket事件slug快照。 */
	@Excel(name = "事件slug", sort = 4, width = 40)
	@ApiModelProperty(value = "Polymarket事件slug")
	private String eventSlug;

	/** Polymarket事件标题快照。 */
	@Excel(name = "事件标题", sort = 5, width = 40)
	@ApiModelProperty(value = "Polymarket事件标题")
	private String eventTitle;

	/** Polymarket市场slug。 */
	@Excel(name = "市场slug", sort = 6, width = 40)
	@ApiModelProperty(value = "Polymarket市场slug")
	private String marketSlug;

	/** 业务类型：1加密，2体育，3Up/Down。 */
	@Excel(name = "业务类型", sort = 7, dictType = "t_polymarket_order_biz_type")
	@ApiModelProperty(value = "业务类型 1加密 2体育 3Up/Down")
	private Integer bizType;

	/** Polymarket市场ID快照。 */
	@Excel(name = "市场ID", sort = 8)
	@ApiModelProperty(value = "Polymarket市场ID")
	private String marketId;

	/** Polymarket conditionId快照。 */
	@Excel(name = "条件ID", sort = 9, width = 50)
	@ApiModelProperty(value = "Polymarket conditionId")
	private String conditionId;

	/** Polymarket市场问题快照。 */
	@Excel(name = "市场问题", sort = 10, width = 50)
	@ApiModelProperty(value = "Polymarket市场问题")
	private String marketQuestion;

	/** 用户选择结果在Polymarket outcomes数组中的下标。 */
	@Excel(name = "结果下标", sort = 11)
	@ApiModelProperty(value = "选择结果下标")
	private Integer outcomeIndex;

	/** 用户选择结果名称，例如Yes、No、Up、Down。 */
	@Excel(name = "选择结果", sort = 12)
	@ApiModelProperty(value = "选择结果")
	private String outcomeName;

	/** 用户选择结果对应的Polymarket asset_id/token_id快照。 */
	@Excel(name = "资产ID", sort = 13, width = 50)
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

	/** 下单时市场原始JSON快照。 */
	@ApiModelProperty(value = "下单市场快照JSON")
	private String orderSnapshotJson;

	/** 结算时市场原始JSON快照。 */
	@ApiModelProperty(value = "结算市场快照JSON")
	private String settleSnapshotJson;

	/** 删除标志：0正常，1删除。 */
	@ApiModelProperty(value = "删除标志 0正常 1删除")
	private Integer deleted;

	@TableField(exist = false)
	private String beginCreateTime;

	@TableField(exist = false)
	private String endCreateTime;
}
