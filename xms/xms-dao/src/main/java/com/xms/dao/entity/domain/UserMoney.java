package com.xms.dao.entity.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.xms.common.annotation.Excel;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

/**
 * <p>
 * 用户钱包表
 * </p>
 *
 *
 * @since 2023-07-25
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_user_money")
@ApiModel(value="UserMoney对象", description="用户钱包表")
public class UserMoney {

    @ApiModelProperty(value = "用户id")
    @TableId(value = "id", type = IdType.AUTO)
	@Excel(name = "用户id", sort = 1)
    private Long id;

	/**
	 * USDT
	 */
	@Excel(name = "USDT", sort = 2)
    private BigDecimal validNum1;
	/**
	 * 可用余额数
	 */
	//@Excel(name = "可用余额数", sort = 3)
    private BigDecimal validNum2;
	/**
	 * 可用余额数
	 */
	//@Excel(name = "可用余额数", sort = 4)
    private BigDecimal validNum3;

	/**
	 * 可用余额数
	 */
	//@Excel(name = "可用余额数", sort = 5)
    private BigDecimal validNum4;

	/**
	 * 可用余额数
	 */
	//@Excel(name = "可用余额数", sort = 6)
    private BigDecimal validNum5;

    @ApiModelProperty(value = "可用余额数")
    private BigDecimal validNum6;

    @ApiModelProperty(value = "可用余额数")
    private BigDecimal validNum7;

    @ApiModelProperty(value = "可用余额数")
    private BigDecimal validNum8;

    @ApiModelProperty(value = "可用余额数")
    private BigDecimal validNum9;

	/**
	 * 删除标志0:删除,1:正常
	 */
	private Integer deleted;

	/**
	 * 每次更新的唯一序号，后续可用来修正数据
	 */
	private String gtId;
	/**
	 * 来源订单编号
	 */
	private String sourceCode;
	/**
	 * 来源类型(1.充值 2.提现 3.推荐奖 4.级差奖 5.平级奖 6.购买套餐 7.平台扣拨)
	 */
	private Integer sourceType;
	/**
	 * 来源用户ID
	 */
	private Long sourceId;

	/**
	 * 昵称
	 */
	@TableField(exist = false)
	@Excel(name = "钱包地址", sort = 1)
	private String account;

	/**
	 * 更新时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@Excel(name = "修改时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
	private Date updateTime;

	/**
	 * 谷歌验证码
	 */
	@TableField(exist = false)
	private String autoCode;

	/**
	 * 请求参数
	 */
	@TableField(exist = false)
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private Map<String, Object> params;
}
