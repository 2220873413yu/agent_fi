package com.xms.dao.entity.req;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.math.BigDecimal;

/**
 *
 * @Description:
 * @Author: xms
 * @Date: 2023/7/27 16:05
 */
@Data
public class AddMiningOrderReq {

	/**
	 * 基金套餐id
	 */
	private Long miningPackageId;

	/**
	 * 领取1U所需积分
	 */
	private BigDecimal pointsPerUsdt;

	/**
	 * 用户账号
	 */
	private String userAccount;

	/**
	 * 投入金额
	 */
	private BigDecimal buyPrice;

	/**
	 * 用户id(通过查询userAccount获得userId)
	 */
	private Long userId;

	//谷歌验证码
	private String autoCode;
}
