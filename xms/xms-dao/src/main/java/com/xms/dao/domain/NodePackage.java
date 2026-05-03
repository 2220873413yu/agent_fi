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
import com.xms.common.annotation.Excel;
import com.baomidou.mybatisplus.annotation.TableField;

/**
 * 节点套餐对象 t_node_package
 *
 * @author xms
 * @date 2026-04-28
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "t_node_package")
public class NodePackage extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 主键id */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 节点名称 */
    @Excel(name = "节点名称")
    @ApiModelProperty(value = "节点名称")
    private String name;
    /** 销量 */
    @Excel(name = "销量")
    @ApiModelProperty(value = "销量")
    private String sales;
    /** 节点价格 */
    @Excel(name = "节点价格")
    @ApiModelProperty(value = "节点价格")
    private BigDecimal price;
    /** 等级 */
    @Excel(name = "等级")
    @ApiModelProperty(value = "等级")
    private Integer level;
    /** 直推奖励比例(%) */
    @Excel(name = "直推奖励比例(%)")
    @ApiModelProperty(value = "直推奖励比例(%)")
    private BigDecimal directReferralRate;
    /** 间推奖励比例(%)，无则0 */
    @Excel(name = "间推奖励比例(%)，无则0")
    @ApiModelProperty(value = "间推奖励比例(%)，无则0")
    private BigDecimal indirectReferralRate;
    /** 权重系数(倍数) */
    @Excel(name = "权重系数(倍数)")
    @ApiModelProperty(value = "权重系数(倍数)")
    private BigDecimal weightMultiplier;
    /** 预测下单手续费减免比例(%) */
    @Excel(name = "预测下单手续费减免比例(%)")
    @ApiModelProperty(value = "预测下单手续费减免比例(%)")
    private BigDecimal predOrderFeeReliefRate;
    /** 是否上架 0:否,1:是 */
    @Excel(name = "是否上架 0:否,1:是")
    @ApiModelProperty(value = "是否上架 0:否,1:是")
    private Integer status;


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
            .append("name", getName())
            .append("sales", getSales())
            .append("price", getPrice())
            .append("level", getLevel())
            .append("directReferralRate", getDirectReferralRate())
            .append("indirectReferralRate", getIndirectReferralRate())
            .append("weightMultiplier", getWeightMultiplier())
            .append("predOrderFeeReliefRate", getPredOrderFeeReliefRate())
            .append("status", getStatus())
            .append("createTime", getCreateTime())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
        .toString();
    }
}
