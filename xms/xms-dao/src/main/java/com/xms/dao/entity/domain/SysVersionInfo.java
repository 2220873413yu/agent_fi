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

import java.util.Date;

/**
 * 版本表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_sys_version_info")
@ApiModel(value="SysVersionInfo对象", description="版本表")
public class SysVersionInfo extends BaseXmsEntity {

	/**
	 * 主键id
	 */
	@ApiModelProperty(value = "主键id")
	@TableId(value = "id", type = IdType.AUTO)
    private Integer id;

	/**
	 * 版本号
	 */
    @ApiModelProperty(value = "版本号")
    private String versionNo;

	/**
	 * 版本下载路径
	 */
	@ApiModelProperty(value = "版本下载路径")
    private String versionUrl;

	/**
	 * 是否强制更新（0：否，1：是）
	 */
    @ApiModelProperty(value = "是否强制更新（0：否，1：是）")
    private String status;

	/**
	 * 设备类型：ios android
	 */
	@ApiModelProperty(value = "设备类型：ios android")
    private String deviceType;

	/**
	 * 更新内容备注
	 */
    @ApiModelProperty(value = "更新内容备注")
    private String note;

	/**
	 * 热更新链接
	 */
	@ApiModelProperty(value = "热更新链接")
    private String remark;

	/**
	 * 是否删除（1:否,2:是）
	 */
    @ApiModelProperty(value = "是否删除（1:否,2:是）")
    private Integer activeFlag;


}
