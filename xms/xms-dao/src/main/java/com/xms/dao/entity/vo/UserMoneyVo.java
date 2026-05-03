package com.xms.dao.entity.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
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
public class UserMoneyVo {

	@ApiModelProperty(value = "钱包id")
	private Long id;


	@ApiModelProperty(value = "变动额度")
	private BigDecimal changeBalance;

	@ApiModelProperty(value = "币种(对应钱包)")
	private Integer coinType;

	@ApiModelProperty(value = "谷歌验证码")
	private String autoCode;
}
