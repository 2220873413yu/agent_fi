package com.xms.dao.entity.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 节点关系表
 * </p>
 *
 *
 * @since 2023-07-25
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_user_relation")
@ApiModel(value="UserRelation对象", description="节点关系表")
public class UserRelation implements Serializable {
	private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "自增ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "祖先节点")
    private Long parUserId;

    @ApiModelProperty(value = "后代节点")
    private Long posUserId;

    @ApiModelProperty(value = "相隔层级，0表示引用当前节点，1以上的值表示到祖先节点的距离")
    private Integer distance;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "修改时间")
    private Date updateTime;

    @ApiModelProperty(value = "是否删除（1:否,2:是）")
    private Integer activeFlag;


}
