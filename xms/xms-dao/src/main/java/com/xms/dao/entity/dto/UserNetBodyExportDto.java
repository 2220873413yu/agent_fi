package com.xms.dao.entity.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.xms.common.annotation.Excel;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 网体关系树导出DTO。
 *
 * <p>该对象只服务后台网体树页面导出，字段口径与页面节点展示保持一致，避免复用UserInfo影响用户列表导出。</p>
 */
@Data
public class UserNetBodyExportDto {

	@Excel(name = "用户ID", sort = 1)
	private Long userId;

	@Excel(name = "钱包地址", sort = 2, width = 40)
	private String account;

	@Excel(name = "节点等级", sort = 3)
	private String nodeLevel;

	@Excel(name = "直推人数", sort = 4)
	private Integer subNum;

	@Excel(name = "团队人数", sort = 5)
	private Integer umbrellaNum;

	@Excel(name = "直推节点数量", sort = 6)
	private BigDecimal subNodePerformance;

	@Excel(name = "团队节点数量", sort = 7)
	private BigDecimal nodeTeamPerformance;

	@Excel(name = "团队节点支付", sort = 8)
	private BigDecimal umbrellaNodePerformance;

	@Excel(name = "后台拨付团队节点业绩", sort = 9)
	private BigDecimal adminUmbrellaNodePerformance;

	@Excel(name = "团队节点金额", sort = 10)
	private BigDecimal allUmbrellaNodePerformance;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@Excel(name = "创建时间", sort = 11, width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
	private Date createTime;
}
