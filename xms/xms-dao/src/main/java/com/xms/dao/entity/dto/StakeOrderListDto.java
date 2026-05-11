package com.xms.dao.entity.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.xms.common.annotation.Excel;
import com.xms.common.core.domain.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 后台质押订单列表DTO。
 *
 * <p>后台列表和导出使用该对象承载展示字段，避免直接返回 t_stake_order 数据库实体；
 * 同时补充 AFI 加速倍率等页面展示字段。</p>
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class StakeOrderListDto extends BaseEntity {
	private static final long serialVersionUID = 1L;

	@Excel(name = "主键ID", sort = 1)
	@ApiModelProperty(value = "主键ID")
	private Long id;

	@Excel(name = "用户ID", sort = 2)
	@ApiModelProperty(value = "用户ID")
	private Long userId;

	@Excel(name = "质押订单号", sort = 3, width = 30)
	@ApiModelProperty(value = "质押订单号")
	private String orderNo;

	@Excel(name = "质押金额", sort = 4)
	@ApiModelProperty(value = "质押金额，单位USDT")
	private BigDecimal stakeUsdtAmount;

	@Excel(name = "AFI质押比例", sort = 5)
	@ApiModelProperty(value = "AFI质押比例，单位%")
	private BigDecimal afiPledgeRatio;

	@Excel(name = "AFI加速倍率", sort = 6)
	@ApiModelProperty(value = "AFI加速倍率，例如1.10；未加速为空")
	private BigDecimal afiAccelerateRate;

	@Excel(name = "订单状态", sort = 7, dictType = "t_stake_order_status")
	@ApiModelProperty(value = "状态 0:待链上支付确认,1:产出中,2:已出局(已完成),3:已过期未支付,4:已暂停产出,5:支付了但是过期了,6:已下架")
	private Integer status;

	@Excel(name = "商品数量", sort = 8)
	@ApiModelProperty(value = "商品数量")
	private Integer num;

	@ApiModelProperty(value = "业务状态 0:正常,1:暂停产出")
	private Integer bizStatus1;

	@Excel(name = "剩余可产出", sort = 8)
	@ApiModelProperty(value = "剩余可产出")
	private BigDecimal remainingOutAmount;

	@ApiModelProperty(value = "今日收益")
	private BigDecimal todayReward;

	@Excel(name = "出局目标产出总额", sort = 9)
	@ApiModelProperty(value = "出局目标产出总额")
	private BigDecimal allOutAmount;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@Excel(name = "支付时间", sort = 10, width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
	@ApiModelProperty(value = "支付时间")
	private Date payTime;

	@Excel(name = "支付hash", sort = 11, width = 40)
	@ApiModelProperty(value = "支付hash")
	private String payHash;

	@Excel(name = "是否首单发货", sort = 12, dictType = "t_user_info_is_valid")
	@ApiModelProperty(value = "本订单是否命中首单发货 0否1是")
	private Integer hashFirstShipOrder;

	@Excel(name = "发货状态", sort = 13, readConverterExp = "0=待发货,1=已发货")
	@ApiModelProperty(value = "发货状态 0待发货 1已发货")
	private Integer shipStatus;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@Excel(name = "发货时间", sort = 14, width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
	@ApiModelProperty(value = "发货时间")
	private Date shipTime;

	@Excel(name = "收货信息JSON快照", sort = 15)
	@ApiModelProperty(value = "收货信息JSON快照")
	private String receiverInfo;

	@Excel(name = "物流公司", sort = 16)
	@ApiModelProperty(value = "物流公司")
	private String shipCompany;

	@Excel(name = "物流单号", sort = 17)
	@ApiModelProperty(value = "物流单号")
	private String shipNo;

	@Excel(name = "商品快照信息", sort = 18)
	@ApiModelProperty(value = "商品快照信息")
	private String productSnapshot;
}
