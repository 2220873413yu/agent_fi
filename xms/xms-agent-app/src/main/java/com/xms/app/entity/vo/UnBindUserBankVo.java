package com.xms.app.entity.vo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UnBindUserBankVo {
	@NotNull
	private Long id;
}
