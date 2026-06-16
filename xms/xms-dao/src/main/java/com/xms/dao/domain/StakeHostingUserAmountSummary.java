package com.xms.dao.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.xms.common.annotation.Excel;
import com.xms.common.core.domain.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 全平台托管累计金额汇总对象 t_stake_hosting_user_amount_summary。
 *
 * <p>本表只维护 id=1 一条全平台汇总记录，id=1 不是业务用户ID。订单生效时增加托管金额；
 * 订单到期、取消托管和后台取消时扣减托管金额，最低扣到0。</p>
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "t_stake_hosting_user_amount_summary")
public class StakeHostingUserAmountSummary extends BaseEntity {
	private static final long serialVersionUID = 1L;

	/** 主键ID */
	@TableId(value = "id", type = IdType.AUTO)
	@Excel(name = "主键ID", sort = 1)
	@ApiModelProperty(value = "主键ID")
	private Long id;

	/** 全平台托管累计金额 */
	@Excel(name = "全平台托管累计金额", sort = 2)
	@ApiModelProperty(value = "全平台托管累计金额")
	private BigDecimal totalAmount;
}
