package com.xms.dao.entity.bo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.xms.common.core.domain.BaseXmsEntity;
import com.xms.dao.entity.domain.UserInfo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * <p>
 * 用户信息表
 * </p>
 *
 *
 * @since 2023-07-25
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoReqBo extends UserInfo {

	private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "google验证码")
    private String authCode;


}
