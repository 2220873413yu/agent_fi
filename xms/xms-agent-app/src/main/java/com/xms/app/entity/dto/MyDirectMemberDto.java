package com.xms.app.entity.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.xms.common.annotation.Excel;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 我的直推成员
 */
@Data
public class MyDirectMemberDto {

	/**
	 * 用户id
	 */
	private Long userId;

	/**
	 * 钱包地址
	 */
	private String account;

	/**
	 * 用户等级 0:暂无,1:F1,2:F2,3:F3,4:F4,5:F5,6:F6
	 */
	private Integer gameLevel;

	/**
	 * 节点等级 0:A0,1:A1,2:A2,3:A3
	 */
	private Integer nodeLevel;

//	/**
//	 * 团队业绩(节点数量)
//	 */
//	private BigDecimal nodeTeamPerformance;
//
//	/**
//	 * 直推业绩(节点数量)
//	 */
//	private BigDecimal subNodePerformance;

	/**
	 * 直推人数
	 */
	private Integer subNum;

	/**
	 * 团队人数
	 */
	private Integer umbrellaNum;

	/**
	 * 直推业绩(托管量)
	 */
	private BigDecimal subPerformance;

	/**
	 * 团队业绩(质押量)
	 */
	private BigDecimal umbrellaPerformance;

	/**
	 * 团队业绩(销售价值)
	 */
	private BigDecimal umbrellaNodePerformance;


	/**
	 * 直推节点业绩(销售额)
	 */
	private BigDecimal subUmbrellaNodePerformance;



//	/**
//	 * 团队业绩(质押量)
//	 */
//	private BigDecimal umbrellaPerformance;
//
//
//	/**
//	 * 我的业绩(质押量)
//	 */
//	private BigDecimal performance;

	/**
	 * 创建时间
	 */
	private Date createTime;

//
//	/**
//	 * 小区业绩
//	 */
//	private BigDecimal communityPerformance;

}
