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
 * 广告轮播图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_banner")
@ApiModel(value="Banner对象", description="appBanner图")
public class Banner extends BaseXmsEntity {

	/**
	 * id
	 */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

	/**
	 * 图片路径
	 */
	@ApiModelProperty(value = "图片路径")
    private String image;

	/**
	 * 图片url
	 */
    @ApiModelProperty(value = "图片url")
    private String url;

	/**
	 * 公告id(废弃)
	 */
	@ApiModelProperty(value = "公告id(废弃)")
    private Long noticeId;

	/**
	 * 语言 1:简体中文,2:繁体,3:英文,4:日文,5:韩文
	 */
	private Integer type;
	/**
	 * 图片排序
	 */
    @ApiModelProperty(value = "图片排序")
    private Integer sort;

	/**
	 * 图片说明
	 */
	@ApiModelProperty(value = "图片说明")
    private String content;

	/**
	 * 状态（0正常 1停用）
	 */
    @ApiModelProperty(value = "状态（0正常 1停用）")
    private String enable;


}
