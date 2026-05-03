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
 * @author MIER
 * @since 2023-06-12
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserWalletVo {

	@ApiModelProperty(value = "用户id")
	private Long userId;

	@ApiModelProperty(value = "每次更新的唯一序号，后续可用来修正数据")
	private String gtId;

	@ApiModelProperty(value = "来源订单")
	private String sourceCode;

	@ApiModelProperty(value = "变动币种")
	private List<UserWalletLogVo> userWalletLogList;
	/**
	 * 来源用户
	 */
	private Long sourceId;
	/**
	 * 来源类型
	 */
	private Integer sourceType;
}
