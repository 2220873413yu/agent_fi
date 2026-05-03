package com.xms.dao.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.xms.common.annotation.Excel;
import com.xms.common.core.domain.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 合同协议对象 t_contract
 *
 * @author xms
 * @date 2023-12-22
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "t_contract")
public class Contract extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 主键id */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 协议类型  4:手表认购协议 */
    @Excel(name = "协议类型  4:手表认购协议")
    @ApiModelProperty(value = "协议类型  4:手表认购协议")
    private Integer type;

	/**
	 * 语言类型  1:简体中文,2:英文
	 */
	private Integer bizType;
    /** 内容 */
    @Excel(name = "内容")
    @ApiModelProperty(value = "内容")
    private String content;
    /** 状态 1: 上架 2:下架 */
    @Excel(name = "状态 1: 上架 2:下架")
    @ApiModelProperty(value = "状态 1: 上架 2:下架")
    private Integer status;


    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("type", getType())
            .append("content", getContent())
            .append("status", getStatus())
            .append("createTime", getCreateTime())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .append("deleted", getDeleted())
            .append("createBy", getCreateBy())
            .append("updateBy", getUpdateBy())
        .toString();
    }
}
