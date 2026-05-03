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
import com.xms.common.annotation.Excel;
import com.baomidou.mybatisplus.annotation.TableField;

/**
 * 谷歌邮箱配置对象 t_email_config
 *
 * @author xms
 * @date 2025-09-18
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "t_email_config")
public class EmailConfig extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 自增主键 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 邮箱 */
    @Excel(name = "邮箱")
    @ApiModelProperty(value = "邮箱")
    private String email;
    /** 状态(废弃) 0:正常,1:禁用 */
    @Excel(name = "状态 0:正常,1:禁用")
    @ApiModelProperty(value = "状态 0:正常,1:禁用")
    private Long status;
    /** 是否启用 0:否,1:是 */
    @Excel(name = "是否启用 0:否,1:是")
    @ApiModelProperty(value = "是否启用 0:否,1:是")
    private Long enable;
    /** 应用专式密码 */
    @Excel(name = "应用专式密码")
    @ApiModelProperty(value = "应用专式密码")
    private String appAuthPassword;

	/** 创建者 */
	@TableField(exist = false)
	private String createBy;
	/** 更新者 */
	@TableField(exist = false)
	private String updateBy;
	@TableField(exist = false)
	private Integer deleted;
	@TableField(exist = false)
	private String remark;
    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("email", getEmail())
            .append("status", getStatus())
            .append("enable", getEnable())
            .append("appAuthPassword", getAppAuthPassword())
            .append("createTime", getCreateTime())
            .append("updateTime", getUpdateTime())
            .append("deleted", getDeleted())
        .toString();
    }
}
