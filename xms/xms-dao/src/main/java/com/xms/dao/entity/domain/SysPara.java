package com.xms.dao.entity.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.xms.common.core.domain.BaseXmsEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <p>
 * 系统参数表
 * </p>
 *
 *
 * @since 2023-03-15
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_sys_para")
@ApiModel(value="SysPara对象", description="系统参数表")
public class SysPara extends BaseXmsEntity {

    @ApiModelProperty(value = "主键id")
	@TableId(value = "sys_para_id", type = IdType.AUTO)
    private Integer sysParaId;

    @ApiModelProperty(value = "参数内码")
    private String paraCode;

    @ApiModelProperty(value = "参数值")
    private String paraValue;

    @ApiModelProperty(value = "参数描述")
    private String paraDesc;

	@ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty(value = "是否删除（1:否,2:是）")
    private Integer activeFlag;
}
