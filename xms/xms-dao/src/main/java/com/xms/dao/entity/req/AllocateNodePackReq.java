package com.xms.dao.entity.req;

import lombok.Data;

@Data
public class AllocateNodePackReq {

	/**
	 * 节点等级
	 */
	private Integer packageLevel;
	/**
	 * 钱包地址
	 */
	private String address;
}
