package com.xms.app.entity.req;

import com.xms.common.annotation.Excel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;


/**
 * 收货地址对象 xms_user_addressRequestVo
 *
 * @author xms
 * @date 2023-06-12
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class UserAddressReqVo {
	private static final long serialVersionUID = 1L;

	/**
	 * id
	 */
	private Long id;
	/** 省/洲 */
	@NotBlank(message = "省不能为空")
	private String province;
	/** 市 */
	@NotBlank(message = "市不能为空")
	private String city;
	/** 区/县/街道地址 */
	@NotBlank(message = "街道地址不能为空")
	private String area;
	/** 详细地址/街道地址2 */
	@NotBlank(message = "详细地址不能为空")
	private String detailed;
	/** 手机号 */
	@NotBlank(message = "手机号不能为空")
	private String phone;
	/** 收货人姓名 */
	@NotBlank(message = "收货人姓名不能为空")
	private String userName;
	/** 用户ID */
	private Long userId;
	/** 是否默认0:否,1:是 */
	@NotBlank(message = "是否默认不能为空")
	private Integer addressState;


}
