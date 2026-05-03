package com.xms.app.entity.req;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 删除收货地址的时候使用
 */
@Data
public class UserAddressRemoveReq {
	/**
	 * id
	 */
	@NotNull
	private Long id;
}
