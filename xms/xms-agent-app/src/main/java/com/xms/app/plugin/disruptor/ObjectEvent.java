package com.xms.app.plugin.disruptor;

import lombok.Data;

/**
 * @author MIER
 * @Description: 事件对象
 *
 */
@Data
public class ObjectEvent<T> {
	private T obj;

}
