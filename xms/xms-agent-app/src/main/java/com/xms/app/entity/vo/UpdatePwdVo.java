package com.xms.app.entity.vo;

import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <p>
 * 用户信息表
 * </p>
 *
 *
 * @since 2023-05-22
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePwdVo {

	/**
	 * 新密码(MD5加密)
	 */
	@ApiModelProperty(value = "新密码(MD5加密)",required=true)
    @NotBlank
    private String newPwd;

	@ApiModelProperty(value = "谷歌验证码不能为空",required=true)
	@NotBlank
	private String googleCode;

	/**
	 * 邮箱验证码
	 */
	@NotBlank
	private String code;

	/**
	 * 验证码的uuid
	 */
	@NotBlank
	private String uuid;
}
