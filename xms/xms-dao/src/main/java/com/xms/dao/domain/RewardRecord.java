package com.xms.dao.domain;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.xms.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import java.math.BigDecimal;
import com.xms.common.annotation.Excel;
import com.baomidou.mybatisplus.annotation.TableField;

/**
 * 奖金记录对象 xms_reward_record
 *
 * @author xms
 * @date 2025-11-19
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "xms_reward_record")
public class RewardRecord extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** id */
    @TableId(type = IdType.AUTO)
    private Long id;


    /** 订单号 */
    @Excel(name = "订单号", sort = 1, width = 30)
    @ApiModelProperty(value = "订单号")
    private String orderCode;
    /** 用户id */
    @Excel(name = "用户ID", sort = 2)
    @ApiModelProperty(value = "用户ID")
    private Long userId;
    /** 数量 */
    @Excel(name = "数量", sort = 3)
    @ApiModelProperty(value = "数量")
    private BigDecimal amount;




	/**
	 * 币种类型 1:算力,2:usdt
	 */
	@Excel(name = "币种", sort = 4,dictType = "reward_record_coin_type")
	private Integer coinType;

	/**
	 * 暂时用不到
	 * 业务类型：例如 级差、平级等等(枚举类型有多少个还不确定)1:矿机静态释放
	 */
    //@Excel(name = "业务类型", sort = 5, dictType = "xms_reward_record_business_type")
    @ApiModelProperty(value = "业务类型：例如 级差、平级等等(枚举类型有多少个还不确定)1:矿机静态释放")
    private Integer businessType;
    /** 来源类型: 1:每日静态产出 */
    @Excel(name = "来源类型", sort = 6, dictType = "reward_record_source_type")
    @ApiModelProperty(value = "来源类型: 1:每日静态产出")
    private Integer sourceType;
    /** 来源订单号 */
    @Excel(name = "来源订单号", sort = 7, width = 30)
    @ApiModelProperty(value = "来源订单号")
    private String sourceOrderCode;
    /** 来源用户 */
    @Excel(name = "来源用户", sort = 8)
    @ApiModelProperty(value = "来源用户")
    private Long sourceUserId;
    /** gtId(废弃) */
    //@Excel(name = "gtId")
    @ApiModelProperty(value = "gtId")
    private String gtId;
    /** 废弃 */
	//@Excel(name = "废弃")
    @ApiModelProperty(value = "废弃")
    private Integer settlementStatus;

	/**
	 * 废弃
	 */
    //@Excel(name = "结算时价格", sort = 9)
    @ApiModelProperty(value = "结算时price")
    private BigDecimal realTimePrice;



	@TableField(exist = false)
	@Excel(name = "钱包地址", sort = 2, width = 40)
	private String userAccount;
	/**
	 * 矿机每日释放的管理费
	 */
	@TableField(exist = false)
	private BigDecimal manageReward;

	@TableField(exist = false)
	private Integer deleted;
	@TableField(exist = false)
	private String updateBy;
	@TableField(exist = false)
	private String createBy;
    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("orderCode", getOrderCode())
            .append("userId", getUserId())
            .append("amount", getAmount())
            .append("businessType", getBusinessType())
            .append("sourceType", getSourceType())
            .append("sourceOrderCode", getSourceOrderCode())
            .append("sourceUserId", getSourceUserId())
            .append("gtId", getGtId())
            .append("settlementStatus", getSettlementStatus())
            .append("realTimePrice", getRealTimePrice())
            .append("remark", getRemark())
            .append("createTime", getCreateTime())
            .append("updateTime", getUpdateTime())
        .toString();
    }
}
