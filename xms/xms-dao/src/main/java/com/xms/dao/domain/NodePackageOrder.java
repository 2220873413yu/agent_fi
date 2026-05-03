package com.xms.dao.domain;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.xms.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.xms.common.annotation.Excel;
import com.baomidou.mybatisplus.annotation.TableField;

/**
 * 节点购买记录对象 t_node_package_order
 *
 * @author xms
 * @date 2026-04-28
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "t_node_package_order")
public class NodePackageOrder extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 主键id */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 订单号 */
    @Excel(name = "订单号",sort = 1)
    @ApiModelProperty(value = "订单号")
    private String orderNo;
    /** 用户id */
    @Excel(name = "用户id",sort=2)
    @ApiModelProperty(value = "用户id")
    private Long userId;
    /** 钱包地址 */
    @Excel(name = "钱包地址",sort = 3,width = 40)
    @ApiModelProperty(value = "钱包地址")
    private String address;
    /** 支付hash */
    @Excel(name = "支付hash",sort = 4,width = 40)
    @ApiModelProperty(value = "支付hash")
    private String hash;
    /** 下单时节点等级快照 */
    @Excel(name = "节点等级",sort = 5, dictType = "t_node_plan_node_level")
    @ApiModelProperty(value = "下单时节点等级快照")
    private Integer packageLevel;
    /** 下单时直推奖励比例快照(%) */
    //@Excel(name = "下单时直推奖励比例快照(%)")
    @ApiModelProperty(value = "下单时直推奖励比例快照(%)")
    private BigDecimal directReferralRate;
    /** 下单时间推奖励比例快照(%)，无则NULL */
	//@Excel(name = "下单时间推奖励比例快照(%)，无则NULL")
    @ApiModelProperty(value = "下单时间推奖励比例快照(%)，无则NULL")
    private BigDecimal indirectReferralRate;
    /** 下单时权重系数快照(倍数) */
    @Excel(name = "权重系数",sort = 6)
    @ApiModelProperty(value = "下单时权重系数快照(倍数)")
    private BigDecimal weightMultiplier;
    /** 下单时预测下单手续费减免比例快照(%) */
    @Excel(name = "手续费减免比例",sort = 7)
    @ApiModelProperty(value = "下单时预测下单手续费减免比例快照(%)")
    private BigDecimal predOrderFeeReliefRate;
    /** 支付金额 */
    @Excel(name = "支付金额",sort = 8)
    @ApiModelProperty(value = "支付金额")
    private BigDecimal orderValueUsdt;
    /** 订单来源 0:购买,1:后台拨付 */
    @Excel(name = "订单来源",sort = 9,dictType = "t_node_package_order_source_type")
    @ApiModelProperty(value = "订单来源 0:购买,1:后台拨付")
    private Integer sourceType;
    /** 订单状态 0:未支付,1:支付成功 */
    @Excel(name = "订单状态",sort = 10,dictType = "t_node_package_order_status")
    @ApiModelProperty(value = "订单状态 0:未支付,1:支付成功")
    private Integer status;
    /** 业务处理状态 0:未处理,1:已处理 */
    //@Excel(name = "业务处理状态 0:未处理,1:已处理")
    @ApiModelProperty(value = "业务处理状态 0:未处理,1:已处理")
    private Integer bizStatus;
    /** 支付时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "支付时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss",sort = 11)
    private Date payTime;


	@TableField(exist = false)
	private String updateBy;
	@TableField(exist = false)
	private String createBy;
	@TableField(exist = false)
	private Integer deleted;
	@TableField(exist = false)
	private String remark;
    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("orderNo", getOrderNo())
            .append("userId", getUserId())
            .append("address", getAddress())
            .append("hash", getHash())
            .append("packageLevel", getPackageLevel())
            .append("directReferralRate", getDirectReferralRate())
            .append("indirectReferralRate", getIndirectReferralRate())
            .append("weightMultiplier", getWeightMultiplier())
            .append("predOrderFeeReliefRate", getPredOrderFeeReliefRate())
            .append("orderValueUsdt", getOrderValueUsdt())
            .append("sourceType", getSourceType())
            .append("status", getStatus())
            .append("bizStatus", getBizStatus())
            .append("payTime", getPayTime())
        .toString();
    }
}
