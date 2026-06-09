package com.xms.dao.entity.dto;

import com.xms.common.annotation.Excel;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 网体关系树导出DTO。
 *
 * <p>该对象只服务后台网体树页面导出，字段口径与页面节点展示保持一致，避免复用UserInfo影响用户列表导出。</p>
 */
@Data
public class UserNetBodyExportDto {

	@Excel(name = "备注", sort = 1, width = 30)
	private String remark;

	@Excel(name = "钱包地址", sort = 2, width = 40)
	private String account;

	@Excel(name = "用户ID", sort = 3)
	private Long userId;

	@Excel(name = "节点等级", sort = 4)
	private String nodeLevel;

	@Excel(name = "团队等级(真)", sort = 5)
	private String gameLevel;

	@Excel(name = "团队等级(后台)", sort = 6)
	private String adminGameLevel;

	@Excel(name = "直推人数", sort = 7)
	private Integer subNum;

	@Excel(name = "团队人数", sort = 8)
	private Integer umbrellaNum;

	@Excel(name = "团队节点数量", sort = 9)
	private BigDecimal nodeTeamPerformance;

	@Excel(name = "团队节点支付", sort = 10)
	private BigDecimal umbrellaNodePerformance;

	@Excel(name = "团队节点金额", sort = 11)
	private BigDecimal allUmbrellaNodePerformance;

	@Excel(name = "自身托管", sort = 12)
	private BigDecimal performance;

	@Excel(name = "团队托管", sort = 13)
	private BigDecimal umbrellaPerformance;

	@Excel(name = "小区托管", sort = 14)
	private BigDecimal communityPerformance;
}
