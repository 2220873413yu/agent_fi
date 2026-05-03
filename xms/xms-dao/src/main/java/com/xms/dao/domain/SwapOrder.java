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
import java.util.Date;

import com.xms.common.annotation.Excel;
import com.baomidou.mybatisplus.annotation.TableField;

/**
 * swap订单对象 t_swap_order
 *
 * @author xms
 * @date 2026-01-04
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "t_swap_order")
public class SwapOrder extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 用户id 如果没有存在用户系统为0 */
    @Excel(name = "用户id", sort = 1)
    @ApiModelProperty(value = "用户id 如果没有存在用户系统为0")
    private Long userId;
    /** 购买的钱包地址 */
    @Excel(name = "钱包地址", sort = 2, width = 40)
    @ApiModelProperty(value = "购买的钱包地址")
    private String address;
    /** swap数量 */
    @Excel(name = "swap数量", sort = 3)
    @ApiModelProperty(value = "swap数量")
    private BigDecimal swapAmount;

	/** 可用swap数量 */
	@Excel(name = "可用swap数量", sort = 3)
	private BigDecimal availableAmount;
    /** 交易hash */
    @Excel(name = "交易hash", sort = 4, width = 60)
    @ApiModelProperty(value = "交易hash")
    private String txHash;
    /** 业务状态 1:待处理,2:已处理,3:未注册丢弃 */
    @Excel(name = "业务状态", sort = 5, dictType = "t_ido_order_biz_status")
    @ApiModelProperty(value = "业务状态 1:待处理,2:已处理,3:未注册丢弃")
    private Integer bizStatus;
    /** 是否处理提现额度 0:否,1:是 */
    @Excel(name = "是否处理提现额度", sort = 6, dictType = "t_user_info_is_valid")
    @ApiModelProperty(value = "是否处理提现额度 0:否,1:是")
    private Integer bizStatus1;

	/**
	 * 过期时间(创建后24小时，到期未完成则处理)
	 */
	@Excel(name = "过期时间", sort = 7, dateFormat = "yyyy-MM-dd HH:mm:ss")
	private Date expireTime;


	@TableField(exist = false)
	private String createBy;

	@TableField(exist = false)
	private String updateBy;

	@TableField(exist = false)
	private Integer deleted;

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("userId", getUserId())
            .append("address", getAddress())
            .append("swapAmount", getSwapAmount())
            .append("txHash", getTxHash())
            .append("createTime", getCreateTime())
            .append("bizStatus", getBizStatus())
            .append("updateTime", getUpdateTime())
            .append("bizStatus1", getBizStatus1())
        .toString();
    }
}
