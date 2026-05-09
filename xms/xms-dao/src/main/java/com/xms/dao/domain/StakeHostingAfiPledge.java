package com.xms.dao.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.xms.common.annotation.Excel;
import com.xms.common.core.domain.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 托管订单AFI质押记录对象 t_stake_hosting_afi_pledge
 *
 * @author xms
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "t_stake_hosting_afi_pledge")
public class StakeHostingAfiPledge extends BaseEntity {
	private static final long serialVersionUID = 1L;

	@TableId(type = IdType.AUTO)
	@Excel(name = "主键ID", sort = 1)
	@ApiModelProperty(value = "主键ID")
	private Long id;

	@Excel(name = "质押单号", sort = 2, width = 30)
	@ApiModelProperty(value = "质押单号")
	private String pledgeNo;

	@ApiModelProperty(value = "托管订单ID")
	private Long stakeHostingOrderId;

	@Excel(name = "托管订单号", sort = 3, width = 30)
	@ApiModelProperty(value = "托管订单号")
	private String stakeHostingOrderNo;

	@Excel(name = "用户ID", sort = 4)
	@ApiModelProperty(value = "用户ID")
	private Long userId;

	@Excel(name = "钱包地址", sort = 5, width = 40)
	@ApiModelProperty(value = "钱包地址")
	private String account;

	@Excel(name = "托管USDT金额", sort = 6)
	@ApiModelProperty(value = "托管订单金额快照")
	private BigDecimal stakeUsdtAmount;

	@Excel(name = "AFI数量", sort = 7)
	@ApiModelProperty(value = "质押AFI数量")
	private BigDecimal afiAmount;

	@Excel(name = "AFI价格", sort = 8)
	@ApiModelProperty(value = "AFI价格快照")
	private BigDecimal afiPrice;

	@Excel(name = "AFI等值USDT", sort = 9)
	@ApiModelProperty(value = "AFI等值USDT")
	private BigDecimal afiUsdtAmount;

	@Excel(name = "命中比例", sort = 10)
	@ApiModelProperty(value = "命中质押比例，单位%")
	private BigDecimal pledgeRatio;

	@Excel(name = "加速倍率", sort = 11)
	@ApiModelProperty(value = "命中加速倍率")
	private BigDecimal accelerateRate;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@Excel(name = "质押时间", sort = 12, width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
	@ApiModelProperty(value = "质押时间")
	private Date pledgeTime;

	@Excel(name = "生效日期", sort = 13)
	@ApiModelProperty(value = "生效日期，格式yyyyMMdd")
	private Integer effectiveDay;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@Excel(name = "退还时间", sort = 14, width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
	@ApiModelProperty(value = "退还时间")
	private Date returnTime;

	@Excel(name = "状态", sort = 15, dictType = "t_stake_hosting_afi_pledge_status")
	@ApiModelProperty(value = "状态 0:未生效 1:生效中 2:已退还")
	private Integer status;
}
