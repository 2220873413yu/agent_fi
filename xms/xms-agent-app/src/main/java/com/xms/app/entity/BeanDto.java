package com.xms.app.entity;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Set;

/**
 * @author: renengadePISTA
 */
@Data
@Accessors(chain = true)
public class BeanDto {
	private String name;

	private String age;

	private String newTime;

	private List<String> list1;

	private Set<String> set1;
}
