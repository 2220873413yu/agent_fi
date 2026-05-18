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

/**
 * 节点认购AFI线性释放订单，对应表 t_node_package_release_order。
 *
 * <p>每笔已支付成功的节点认购订单生成一条释放订单，记录初始化时的权重、应释放AFI、
 * 已释放AFI和运行天数快照。该表只记录释放计划和进度，实际每日入账由定时任务写入AFI钱包。</p>
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "t_node_package_release_order")
@ApiModel(value = "NodePackageReleaseOrder", description = "节点认购AFI线性释放订单")
public class NodePackageReleaseOrder extends BaseEntity {
	private static final long serialVersionUID = 1L;

	/** 主键ID。 */
	@TableId(type = IdType.AUTO)
	@Excel(name = "主键ID", sort = 1)
	@ApiModelProperty(value = "主键ID")
	private Long id;

	/** 释放订单号。 */
	@Excel(name = "释放订单号", sort = 2, width = 30)
	@ApiModelProperty(value = "释放订单号")
	private String releaseNo;

	/** 来源节点订单ID。 */
	@Excel(name = "来源节点订单ID", sort = 3)
	@ApiModelProperty(value = "来源节点订单ID")
	private Long nodeOrderId;

	/** 来源节点订单号。 */
	@Excel(name = "来源节点订单号", sort = 4, width = 30)
	@ApiModelProperty(value = "来源节点订单号")
	private String nodeOrderNo;

	/** 用户ID。 */
	@Excel(name = "用户ID", sort = 5)
	@ApiModelProperty(value = "用户ID")
	private Long userId;

	/** 钱包地址快照。 */
	@Excel(name = "钱包地址", sort = 6, width = 40)
	@ApiModelProperty(value = "钱包地址快照")
	private String address;

	/** 节点等级快照。 */
	@Excel(name = "节点等级", sort = 7, dictType = "t_node_plan_node_level")
	@ApiModelProperty(value = "节点等级快照")
	private Integer packageLevel;

	/** 来源节点订单金额USDT。 */
	@Excel(name = "订单金额USDT", sort = 8)
	@ApiModelProperty(value = "来源节点订单金额USDT")
	private BigDecimal orderValueUsdt;

	/** 订单权重系数快照。 */
	@Excel(name = "订单权重", sort = 9)
	@ApiModelProperty(value = "订单权重系数快照")
	private BigDecimal weightMultiplier;

	/** 初始化时全网总权重。 */
	@Excel(name = "全网总权重", sort = 10)
	@ApiModelProperty(value = "初始化时全网总权重")
	private BigDecimal totalWeight;

	/** 初始化时每1权重可分AFI。 */
	@Excel(name = "每权重可分AFI", sort = 11)
	@ApiModelProperty(value = "初始化时每1权重可分AFI")
	private BigDecimal amountPerWeight;

	/** 本释放订单总释放AFI。 */
	@Excel(name = "总释放AFI", sort = 12)
	@ApiModelProperty(value = "本释放订单总释放AFI")
	private BigDecimal totalReleaseAmount;

	/** 每日释放AFI。 */
	@Excel(name = "每日释放AFI", sort = 13)
	@ApiModelProperty(value = "每日释放AFI")
	private BigDecimal dailyReleaseAmount;

	/** 已释放AFI。 */
	@Excel(name = "已释放AFI", sort = 14)
	@ApiModelProperty(value = "已释放AFI")
	private BigDecimal releasedAmount;

	/** 剩余待释放AFI。 */
	@Excel(name = "剩余AFI", sort = 15)
	@ApiModelProperty(value = "剩余待释放AFI")
	private BigDecimal remainingAmount;

	/** 总释放天数，固定365。 */
	@Excel(name = "总天数", sort = 16)
	@ApiModelProperty(value = "总释放天数，固定365")
	private Integer totalDays;

	/** 已运行释放天数。 */
	@Excel(name = "已运行天数", sort = 17)
	@ApiModelProperty(value = "已运行释放天数")
	private Integer runDays;

	/** 最后释放日期，格式yyyyMMdd。 */
	@Excel(name = "最后释放日期", sort = 18)
	@ApiModelProperty(value = "最后释放日期，格式yyyyMMdd")
	private Integer lastReleaseDay;

	/** 状态：0待释放 1释放中 2释放完成 3异常。 */
	@Excel(name = "释放状态", sort = 19, dictType = "t_node_package_release_order_status")
	@ApiModelProperty(value = "状态：0待释放 1释放中 2释放完成 3异常")
	private Integer status;

	/** 初始化批次号。 */
	@Excel(name = "初始化批次号", sort = 20, width = 30)
	@ApiModelProperty(value = "初始化批次号")
	private String initBatchNo;

	/** 删除标志：0正常 1删除。 */
	@ApiModelProperty(value = "删除标志：0正常 1删除")
	private Integer deleted;

	@TableField(exist = false)
	private String beginCreateTime;

	@TableField(exist = false)
	private String endCreateTime;
}
