package com.xms.dao.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.xms.common.annotation.Excel;
import com.xms.common.core.domain.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

/**
 * Polymarket市场聚合对象，对应表t_polymarket_market。
 *
 * <p>该表按marketSlug维护市场级结算状态和平台内部下单总额；它不保存每个结果选项的下单分布，具体用户选择仍以订单表为准。</p>
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "t_polymarket_market")
public class PolymarketMarket extends BaseEntity {
	private static final long serialVersionUID = 1L;

	/** 主键ID。 */
	@TableId(type = IdType.AUTO)
	private Long id;

	/** Polymarket市场slug，市场级结算和聚合的唯一键。 */
	@Excel(name = "市场slug", sort = 1, width = 40)
	@ApiModelProperty(value = "Polymarket市场slug")
	private String marketSlug;

	/** Polymarket市场ID快照。 */
	@Excel(name = "市场ID", sort = 2)
	@ApiModelProperty(value = "Polymarket市场ID")
	private String marketId;

	/** Polymarket conditionId快照。 */
	@Excel(name = "条件ID", sort = 3, width = 50)
	@ApiModelProperty(value = "Polymarket conditionId")
	private String conditionId;

	/** Polymarket事件slug快照。 */
	@Excel(name = "事件slug", sort = 4, width = 40)
	@ApiModelProperty(value = "Polymarket事件slug")
	private String eventSlug;

	/** Polymarket事件标题快照。 */
	@Excel(name = "事件标题", sort = 5, width = 40)
	@ApiModelProperty(value = "Polymarket事件标题")
	private String eventTitle;

	/** Polymarket市场问题快照。 */
	@Excel(name = "市场问题", sort = 6, width = 50)
	@ApiModelProperty(value = "Polymarket市场问题")
	private String marketQuestion;

	/** Polymarket市场所有结果asset_id/token_id数组快照。 */
	@ApiModelProperty(value = "Polymarket市场所有结果asset_id数组快照")
	private String assetIdsJson;

	/** Polymarket市场所有结果名称数组快照。 */
	@ApiModelProperty(value = "Polymarket市场所有结果名称数组快照")
	private String outcomesJson;

	/** 市场结束时间。 */
	@Excel(name = "结束时间", sort = 7, dateFormat = "yyyy-MM-dd HH:mm:ss")
	@ApiModelProperty(value = "市场结束时间")
	private Date endTime;

	/** 市场结算状态：0待结算，1结算中，2结算完成，3待人工复核。 */
	@Excel(name = "市场状态", sort = 8, dictType = "t_polymarket_market_status")
	@ApiModelProperty(value = "市场结算状态 0待结算 1结算中 2结算完成 3待人工复核")
	private Integer status;

	/** Polymarket UMA结算状态快照，例如resolved。 */
	@Excel(name = "UMA状态", sort = 9)
	@ApiModelProperty(value = "Polymarket UMA结算状态")
	private String umaResolutionStatus;

	/** 开奖后赢家结果下标。 */
	@Excel(name = "赢家下标", sort = 10)
	@ApiModelProperty(value = "赢家结果下标")
	private Integer resultOutcomeIndex;

	/** 开奖后赢家结果名称。 */
	@Excel(name = "赢家结果", sort = 11)
	@ApiModelProperty(value = "赢家结果名称")
	private String resultOutcomeName;

	/** WebSocket或结算结果返回的赢家asset_id/token_id。 */
	@Excel(name = "赢家资产ID", sort = 12, width = 50)
	@ApiModelProperty(value = "WebSocket或结算结果返回的赢家asset_id/token_id")
	private String winningAssetId;

	/** 市场总下单次数。 */
	@Excel(name = "下单次数", sort = 13)
	@ApiModelProperty(value = "市场总下单次数")
	private Integer orderCount;

	/** 市场总下单AFI数量。 */
	@Excel(name = "总AFI", sort = 14)
	@ApiModelProperty(value = "市场总下单AFI数量")
	private BigDecimal totalAfiAmount;

	/** 市场外扣手续费AFI合计。 */
	@Excel(name = "总手续费AFI", sort = 15)
	@ApiModelProperty(value = "市场外扣手续费AFI合计")
	private BigDecimal totalFeeAfiAmount;

	/** 市场总下单等值USDT。 */
	@Excel(name = "总USDT", sort = 15)
	@ApiModelProperty(value = "市场总下单等值USDT")
	private BigDecimal totalUsdtAmount;

	/** 市场总购买份额。 */
	@Excel(name = "总份额", sort = 16)
	@ApiModelProperty(value = "市场总购买份额")
	private BigDecimal totalShareAmount;

	/** 市场总兑付USDT。 */
	@Excel(name = "总兑付USDT等值", sort = 17)
	@ApiModelProperty(value = "市场中奖应兑付USDT等值合计")
	private BigDecimal totalPayoutUsdtAmount;

	/** 市场实际总发放AFI数量。 */
	@Excel(name = "总兑付AFI", sort = 18)
	@ApiModelProperty(value = "市场实际总发放AFI数量")
	private BigDecimal totalPayoutAfiAmount;

	/** 上次检查Polymarket结果的时间。 */
	@Excel(name = "上次检查时间", sort = 19, dateFormat = "yyyy-MM-dd HH:mm:ss")
	@ApiModelProperty(value = "上次检查时间")
	private Date lastCheckTime;

	/** 市场结算完成时间。 */
	@Excel(name = "结算时间", sort = 20, dateFormat = "yyyy-MM-dd HH:mm:ss")
	@ApiModelProperty(value = "结算完成时间")
	private Date settleTime;

	/** 最新Polymarket市场快照JSON。 */
	@ApiModelProperty(value = "最新Polymarket市场快照JSON")
	private String marketSnapshotJson;

	/** 删除标志：0正常，1删除。 */
	@ApiModelProperty(value = "删除标志 0正常 1删除")
	private Integer deleted;

	@TableField(exist = false)
	private String beginCreateTime;

	@TableField(exist = false)
	private String endCreateTime;

	@TableField(exist = false)
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private Map<String, Object> params;
}
