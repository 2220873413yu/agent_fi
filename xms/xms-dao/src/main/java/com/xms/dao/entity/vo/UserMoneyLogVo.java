package com.xms.dao.entity.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * <p>
 * 钱包流水表
 * </p>
 *
 *
 * @since 2023-07-25
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserMoneyLogVo {

    @ApiModelProperty(value = "币种(对应钱包)")
    private Integer coinType;

    @ApiModelProperty(value = "变动额度")
    private BigDecimal changeBalance;

    @ApiModelProperty(value = "变动前余额")
    private BigDecimal beforeBalance;

    @ApiModelProperty(value = "来源类型(1.充值 2.提现 3.推荐奖 4.级差奖 5.平级奖 6.购买套餐)")
    private Integer sourceType;

    @ApiModelProperty(value = "备注")
    private String remark;

}
