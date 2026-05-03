package com.xms.app.plugin.disruptor;

import com.lmax.disruptor.EventFactory;

/**
 *
 * @Description: 事件生成工厂（用来初始化预分配事件对象）
 *
 */
public class ObjectEventFactory<T> implements EventFactory<ObjectEvent<T>> {

	@Override
    public ObjectEvent<T> newInstance() {
        return new ObjectEvent<>();
    }
}
