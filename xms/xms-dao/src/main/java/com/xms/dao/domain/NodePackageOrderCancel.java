package com.xms.dao.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
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
 * 节点套餐取消订单归档对象 t_node_package_order_cancel。
 *
 * <p>取消节点时先把主表订单完整归档到本表，再移出主表，避免取消订单继续参与节点权益、
 * 手续费减免、AFI释放初始化和看板统计等业务口径。</p>
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "t_node_package_order_cancel")
@ApiModel(value = "NodePackageOrderCancel", description = "节点套餐取消订单归档")
public class NodePackageOrderCancel extends BaseEntity {
	private static final long serialVersionUID = 1L;

	/** 主键id。 */
	@TableId(type = IdType.AUTO)
	@ApiModelProperty(value = "主键id")
	private Long id;

	/** 原节点订单id。 */
	@Excel(name = "原节点订单id", sort = 1)
	@ApiModelProperty(value = "原节点订单id")
	private Long originOrderId;

	/** 订单号。 */
	@Excel(name = "订单号", sort = 2)
	@ApiModelProperty(value = "订单号")
	private String orderNo;

	/** 用户id。 */
	@Excel(name = "用户id", sort = 3)
	@ApiModelProperty(value = "用户id")
	private Long userId;

	/** 钱包地址。 */
	@Excel(name = "钱包地址", sort = 4, width = 40)
	@ApiModelProperty(value = "钱包地址")
	private String address;

	/** 支付hash。 */
	@Excel(name = "支付hash", sort = 5, width = 40)
	@ApiModelProperty(value = "支付hash")
	private String hash;

	/** 下单时节点等级快照。 */
	@Excel(name = "节点等级", sort = 6, dictType = "t_node_plan_node_level")
	@ApiModelProperty(value = "下单时节点等级快照")
	private Integer packageLevel;

	/** 下单时直推奖励比例快照，单位%。 */
	@ApiModelProperty(value = "下单时直推奖励比例快照(%)")
	private BigDecimal directReferralRate;

	/** 下单时间推奖励比例快照，单位%。 */
	@ApiModelProperty(value = "下单时间推奖励比例快照(%)，无则NULL")
	private BigDecimal indirectReferralRate;

	/** 下单时权重系数快照，单位倍。 */
	@Excel(name = "权重系数", sort = 7)
	@ApiModelProperty(value = "下单时权重系数快照(倍数)")
	private BigDecimal weightMultiplier;

	/** 下单时预测下单手续费减免比例快照，单位%。 */
	@Excel(name = "手续费减免比例", sort = 8)
	@ApiModelProperty(value = "下单时预测下单手续费减免比例快照(%)")
	private BigDecimal predOrderFeeReliefRate;

	/** 支付金额，单位USDT。 */
	@Excel(name = "支付金额", sort = 9)
	@ApiModelProperty(value = "支付金额")
	private BigDecimal orderValueUsdt;

	/** 订单来源：0购买，1后台拨付。 */
	@Excel(name = "订单来源", sort = 10, dictType = "t_node_package_order_source_type")
	@ApiModelProperty(value = "订单来源 0:购买,1:后台拨付")
	private Integer sourceType;

	/** 原订单状态：0未支付，1支付成功。 */
	@Excel(name = "原订单状态", sort = 11, dictType = "t_node_package_order_status")
	@ApiModelProperty(value = "原订单状态 0:未支付,1:支付成功")
	private Integer status;

	/** 原业务处理状态：0未处理，1已处理。 */
	@ApiModelProperty(value = "原业务处理状态 0:未处理,1:已处理")
	private Integer bizStatus;

	/** 原订单支付时间。 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@Excel(name = "支付时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss", sort = 12)
	private Date payTime;

	/** 取消时间。 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@Excel(name = "取消时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss", sort = 13)
	@ApiModelProperty(value = "取消时间")
	private Date cancelTime;

	/** 取消操作人。 */
	@Excel(name = "取消操作人", sort = 14)
	@ApiModelProperty(value = "取消操作人")
	private String cancelBy;

	/** 取消原因。 */
	@Excel(name = "取消原因", sort = 15)
	@ApiModelProperty(value = "取消原因")
	private String cancelReason;

	/** 线性释放订单id。 */
	@ApiModelProperty(value = "线性释放订单id")
	private Long releaseOrderId;

	/** 取消前释放订单状态。 */
	@ApiModelProperty(value = "取消前释放订单状态")
	private Integer releaseStatusBefore;

	/** 取消时已释放AFI快照。 */
	@ApiModelProperty(value = "取消时已释放AFI快照")
	private BigDecimal releasedAmountSnapshot;

	/** 取消时剩余待释放AFI快照。 */
	@ApiModelProperty(value = "取消时剩余待释放AFI快照")
	private BigDecimal remainingAmountSnapshot;

	/** 取消开始时间查询条件，格式yyyy-MM-dd HH:mm:ss。 */
	@TableField(exist = false)
	private String beginCancelTime;

	/** 取消结束时间查询条件，格式yyyy-MM-dd HH:mm:ss。 */
	@TableField(exist = false)
	private String endCancelTime;
}
