package com.xms.app.entity.vo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 聊天机器人vo对象
 * @Description:
 * @Author: xms
 * @Date: 2023/7/18 17:09
 */
@Data
public class HandleWordVo {
	@NotBlank
	public String text;
	
	// 手动添加getter方法（防止Lombok不工作）
	public String getText() {
		return text;
	}
	
	public void setText(String text) {
		this.text = text;
	}
}
