package com.xms.app.entity.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 绑定邀请用户请求参数
 *
 * @author xms
 * @date 2023/04/05
 */
@Data
public class BindInviteUserReq {

	/**
	 * 用户id(需要绑定的用户id)
	 */
	@NotNull
	private Long userId;
	/**
	 * 邀请码
	 */
	@NotBlank
	private String inviteCode;
}
