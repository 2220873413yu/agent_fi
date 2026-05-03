package com.xms.dao.entity.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * <p>
 * 佣金钱包流水表
 * </p>
 *
 *
 * @since 2023-06-12
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserMoneyVo {

    @ApiModelProperty(value = "用户id/对应用户钱包主键ID")
    private Long userId;

    @ApiModelProperty(value = "版本号")
    private Integer version;

    @ApiModelProperty(value = "来源订单")
    private String sourceCode;

	@ApiModelProperty(value = "变动币种")
	private List<UserMoneyLogVo> userMoneyLogList;


	/**
	 * 来源用户
	 */
	private Long sourceId;
	/**
	 * 来源类型
	 */
	private Integer sourceType;
}
