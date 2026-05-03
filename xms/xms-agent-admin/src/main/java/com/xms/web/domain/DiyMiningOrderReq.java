package com.xms.web.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 闪卡/矿机记录对象 xms_mining_order
 *
 * @author xms
 * @date 2023-12-05
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiyMiningOrderReq implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 用户id
	 */
	private String userId;
	/**
	 * 矿机ID
	 */
	private String miningId;

}
