package com.xms.dao.entity.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.xms.common.annotation.Excel;
import com.xms.common.core.domain.BaseXmsEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * 提现表
 * </p>
 *
 *
 * @since 2023-06-30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_withdrawal")
@ApiModel(value="Withdrawal对象", description="提现表")
public class Withdrawal extends BaseXmsEntity {

	/**
	 * id
	 */
    @ApiModelProperty(value = "主键id")
	@TableId(value = "id", type = IdType.AUTO)
    private Integer id;

	/**
	 * 用户id
	 */
	@Excel(name = "用户ID",sort = 1)
    private Long userId;

	/**
	 * 提现单号
	 */
    @ApiModelProperty(value = "提现单号")
	@Excel(name = "提现单号",sort = 2, width = 30)
    private String code;


	/** 提现币种 1:USDT */
	@Excel(name = "提现币种",dictType = "t_user_money_log_coin_type", sort = 3)
	private Integer coinType;

	/**
	 * 提现额度
	 */
	@Excel(name = "提现额度",sort = 4)
	@ApiModelProperty(value = "提现额度")
	private BigDecimal changeBalance;

	/**
	 * 手续费率
	 */
	@Excel(name = "手续费率%",sort = 6)
	@ApiModelProperty(value = "手续费率")
	private BigDecimal feeRatio;
	/**
	 * 手续费额度
	 */
	@Excel(name = "手续费",sort = 7)
	@ApiModelProperty(value = "手续费额")
	private BigDecimal feeBalance;

	/**
	 * 状态(0.待审核,1.审核成功,2.审核驳回,3:提现成功,4:打款失败)
	 */
	@Excel(name = "状态",sort = 7,dictType = "t_withdrawal_status")
	private Integer status;


	/**
	 * 备注(提现hash)
	 */
	@Excel(name = "交易hash", sort = 8, width = 40)
	private String remark;


	/**
	 * 业务状态是否处理
	 */
	//@Excel(name = "业务状态是否处理",sort = 4, dictType = "t_user_info_is_valid")
	private Integer bizStatus;





	/** 账号(银行卡号/USDT地址)暂时废弃 */
	//@Excel(name = "提现地址",sort = 7, width = 40)
	private String accountNo;


	/**
	 * 钱包地址
	 */
	@TableField(exist = false)
	@Excel(name = "钱包地址",sort = 1, width = 40)
	private String userAccount;


	/** 更新时间 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@TableField(fill = FieldFill.UPDATE)
	@Excel(name = "修改时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
	private Date updateTime;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@Excel(name = "到账日期", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
	private Date creditedTime;

	/**
	 * 团队负责人账号
	 */
	@TableField(exist = false)
	private String teamUserAccount;
	/**
	 * 团队用户id
	 */
	@TableField(exist = false)
	private List<Long> childUserIds;
}
