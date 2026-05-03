package com.xms.app.entity.vo;

import jakarta.validation.constraints.NotBlank;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginVo {

	/**
	 * 签名
	 */
    @ApiModelProperty(value = "签名")
    private String signature;

	/**
	 * (钱包地址)
	 */
	@ApiModelProperty(value = "钱包地址")
    @NotBlank
    private String address;

	/**
	 * 邀请码
	 */
    @ApiModelProperty(value = "邀请码")
    private String inviteCode;

	/**
	 * 随机数不能为空
	 */
	@NotBlank(message = "随机数不能为空")
	private String randomNum;
}
