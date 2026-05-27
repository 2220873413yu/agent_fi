package com.xms.dao.entity.req;

import lombok.Data;

/**
 * 示例质押购买请求。
 */
@Data
public class DemoPledgeBuyReq {
	/**
	 * 购买用户ID。
	 */
	private Long userId;

	/**
	 * 示例质押套餐ID。
	 */
	private Long packageId;
}
