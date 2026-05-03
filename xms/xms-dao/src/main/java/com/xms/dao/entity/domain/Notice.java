package com.xms.dao.entity.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 通知公告表
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "sys_notice")
public class Notice implements Serializable {


	/**
	 * 公告ID
	 */
    @ApiModelProperty(value = "公告ID")
    @TableId(value = "notice_id", type = IdType.AUTO)
    private Integer noticeId;

	/**
	 * 封面图
	 */
	private String image;

	/**
	 * 内容图(暂时废弃)
	 */
	private String contentImage;

	/**
	 * 公告标题
	 */
	@ApiModelProperty(value = "公告标题")
    private String noticeTitle;

	/**
	 * 通知类型 1:公告,2:咨询中心,3:基金百科
	 */
    private Integer noticeType;

	/**
	 * 公告内容
	 */
	@ApiModelProperty(value = "公告内容")
    private String noticeContent;

	/**
	 * 公告状态（0正常 1关闭）
	 */
    @ApiModelProperty(value = "公告状态（0正常 1关闭）")
    private String status;

	/**
	 * 创建者
	 */
    @ApiModelProperty(value = "创建者")
    private String createBy;

	/**
	 * 创建时间
	 */
	@ApiModelProperty(value = "创建时间")
    private Date createTime;

	/**
	 * 更新者
	 */
    @ApiModelProperty(value = "更新者")
    private String updateBy;

	/**
	 * 更新时间
	 */
	@ApiModelProperty(value = "更新时间")
    private Date updateTime;

	/**
	 * 备注
	 */
	@ApiModelProperty(value = "备注")
    private String remark;

	/** 语言 1:简体中文,2:繁体,3:英文,4:日文,5:韩文 */
	private Integer type;
}
