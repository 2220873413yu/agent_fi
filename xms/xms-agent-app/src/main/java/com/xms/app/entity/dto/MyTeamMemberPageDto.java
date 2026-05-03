package com.xms.app.entity.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class MyTeamMemberPageDto implements Serializable {
	@Serial
	private static final long serialVersionUID = 1L;

	private Long total;

	private List<MyTeamMemberDto> records;
}

