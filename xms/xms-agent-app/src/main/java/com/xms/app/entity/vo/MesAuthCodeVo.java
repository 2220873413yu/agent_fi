package com.xms.app.entity.vo;

import com.xms.common.annotation.ValidDiyStatus;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MesAuthCodeVo {

	/**
	 * 邮箱
	 */
	@NotBlank(message = "邮箱不能为空")
	private String email;

	/**
	 * 业务类型 1:注册,2:绑定邮箱,3:提现,4:修改密码,5:忘记密码
	 */
	@NotNull(message = "业务类型不能为空")
	@ValidDiyStatus(values = {1, 2, 3, 4, 5})
	private Integer bizType;
}
