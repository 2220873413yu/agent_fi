package com.xms.dao.entity.bo;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 七牛云Bo对象
 *
 *
 * @since 2023-05-22
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QiNiuBo {

	/**
	 * token
	 */
    @ApiModelProperty(value = "token")
    private String token;

	/**
	 * 域名
	 */
	@ApiModelProperty(value = "域名")
    private String domain;

}
