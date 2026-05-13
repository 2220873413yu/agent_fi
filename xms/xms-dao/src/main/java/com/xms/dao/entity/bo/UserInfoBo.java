package com.xms.dao.entity.bo;

import com.xms.common.annotation.Excel;
import lombok.Data;

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
public class UserInfoBo{


	/**
	 * 用户id
	 */
	private Long userId;

	/**
	 * 用户钱包地址
	 */
	private String account;


	/**
	 * 用户等级 0:暂无,1:F1,2:F2,3:F3,4:F4,5:F5,6:F6
	 */
	private Integer gameLevel;

	/**
	 * OpenAI聊天扣费状态 0:未扣费 1:已扣费
	 */
	private Integer openAiPaidStatus;

//	/**
//	 * 邀请用户编码
//	 */
//	private String inviteUserCode;

	/**
	 * 邀请用户钱包地址
	 */
	private String inviteUserAccount;

	/**
	 * 节点等级 0:A0,1:A1,2:A2,3:A3
	 */
	private Integer nodeLevel;

	/**
	 * 团队业绩(节点数量)
	 */
	private BigDecimal nodeTeamPerformance;

	/**
	 * 直推业绩(节点数量)
	 */
	private BigDecimal subNodePerformance;

//	/**
//	 * 邀请用户id
//	 */
//	private Long inviteUserId;

//	/**
//	 * 是否有效用户 买过矿机(0.否 1.是)
//	 */
//	private Integer isValid;

	/**
	 * 直推用户数
	 */
	private Integer subNum;

	/**
	 * 团队用户数
	 */
	private Integer umbrellaNum;

//
//	/**
//	 * 团队业绩(质押量)
//	 */
//	private BigDecimal umbrellaPerformance;
//
//
//	/**
//	 * 小区业绩
//	 */
//	private BigDecimal communityPerformance;
//
//	/**
//	 * 个人业绩
//	 */
//	private BigDecimal performance;
}
