package com.xms.app.entity.req;

import lombok.Data;

import java.io.Serializable;

/**
 * @author: GT63S
 * @createDate: 2025/7/25
 */
@Data
public class JuNotifyReq implements Serializable {
	/**
	 * 币种 .eg:usdt
	 */
	private String currency;

	/**
	 * 划转金额，精度控制在8位以内
	 */
	private String amount;

	/**
	 * 订单id,幂等字符串 应用内唯一
	 */
	private String orderId;

	/**
	 * openId
	 */
	private String openId;

	/**
	 * 订单创建时间,13位时间戳
	 */
	private Long createTime;

	/**
	 * 划转类型 user_to_application: 充值  application_to_user: wdw
	 */
	private String type;

	/**
	 * appId
	 */
	private String appId;

	/**
	 * 向项目方划转自定义参数
	 */
	private String customParam;
	/**
	 *  wdw 专属状态: 1 成功 2 失败
	 */
	private Integer status;

	/**
	 * 签名
	 */
	private String sign;
}
