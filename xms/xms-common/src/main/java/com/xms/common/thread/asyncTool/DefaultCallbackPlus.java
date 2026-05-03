package com.xms.common.thread.asyncTool;


import com.jd.platform.async.worker.WorkResult;

/**
 * 默认回调类，如果不设置的话，会默认给这个回调
 * @author wuweifeng wrote on 2019-11-19.
 */
public class DefaultCallbackPlus<T, V> implements ICallbackPlus<T, V> {

    @Override
    public void result(boolean success, T param, WorkResult<V> workResult) {

    }

}
