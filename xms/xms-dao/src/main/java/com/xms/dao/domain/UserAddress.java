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
 * 收货地址对象 xms_user_address
 *
 * @author xms
 * @date 2026-02-26
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "xms_user_address")
public class UserAddress extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** id */
    @TableId(type = IdType.AUTO)
    private Long id;
	/**
	 * 钱包地址
	 */
	@TableField(exist = false)
	@Excel(name = "钱包地址",sort = 2,width = 40)
	private String userAccount;
    /** 省/洲 */
    @Excel(name = "省",sort = 3)
    @ApiModelProperty(value = "省/洲")
    private String province;
    /** 市 */
    @Excel(name = "市",sort = 4)
    @ApiModelProperty(value = "市")
    private String city;
    /** 区/县/街道地址 */
    @Excel(name = "区",sort = 5)
    @ApiModelProperty(value = "区/县/街道地址")
    private String area;
    /** 详细地址/街道地址2 */
    @Excel(name = "详细地址",sort = 6)
    @ApiModelProperty(value = "详细地址/街道地址2")
    private String detailed;
    /** 手机号 */
    @Excel(name = "手机号",sort = 7)
    @ApiModelProperty(value = "手机号")
    private String phone;
    /** 收货人姓名 */
    @Excel(name = "收货人姓名",sort = 8)
    @ApiModelProperty(value = "收货人姓名")
    private String userName;
    /** 用户ID */
    @Excel(name = "用户ID",sort = 1)
    @ApiModelProperty(value = "用户ID")
    private Long userId;
    /** 是否默认0:否,1:是 */
    @Excel(name = "是否默认",sort = 10,readConverterExp = "0=否,1=是")
    @ApiModelProperty(value = "是否默认0:否,1:是")
    private Integer addressState;


    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("province", getProvince())
            .append("city", getCity())
            .append("area", getArea())
            .append("detailed", getDetailed())
            .append("phone", getPhone())
            .append("userName", getUserName())
            .append("userId", getUserId())
            .append("addressState", getAddressState())
            .append("remark", getRemark())
            .append("createTime", getCreateTime())
            .append("createBy", getCreateBy())
            .append("updateTime", getUpdateTime())
            .append("updateBy", getUpdateBy())
            .append("deleted", getDeleted())
        .toString();
    }
}
