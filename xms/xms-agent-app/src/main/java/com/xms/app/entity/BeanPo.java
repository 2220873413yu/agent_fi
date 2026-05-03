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
public class BeanPo {
	private String name;

	private Integer age;

	private String time;

	private List<Integer> list;

	private Set<String> set;
}
