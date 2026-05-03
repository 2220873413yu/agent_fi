package com.xms.web.strategy;



import com.xms.web.strategy.context.StrategyContext;

import java.util.List;

/**
 * @author MIER
 */
public interface StrategyService<T extends StrategyContext> {

    /**
     * 策略类型列表
     *
     * @return
     */
    List<String> listType();

    /**
     * 处理策略
     *
     * @param t
     * @return
     */
    boolean handle(T t) throws Exception;
}
