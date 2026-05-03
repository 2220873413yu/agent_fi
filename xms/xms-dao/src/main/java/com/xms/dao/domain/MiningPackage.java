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
 * 矿机套餐对象 t_mining_package
 *
 * @author xms
 * @date 2026-02-21
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "t_mining_package")
public class MiningPackage extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 主键id */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 矿机名称 */
    @Excel(name = "矿机名称")
    @ApiModelProperty(value = "矿机名称")
    private String name;
    /** 销量 */
    @Excel(name = "销量")
    private Integer sales;
	/** 可售库存数量 */
	@Excel(name = "可售库存数量")
	private Integer availableStock;
    /** 矿机价格 */
    @Excel(name = "矿机价格")
    @ApiModelProperty(value = "矿机价格")
    private BigDecimal price;
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
    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("name", getName())
            .append("sales", getSales())
            .append("price", getPrice())
            .append("status", getStatus())
            .append("createTime", getCreateTime())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
        .toString();
    }
}
