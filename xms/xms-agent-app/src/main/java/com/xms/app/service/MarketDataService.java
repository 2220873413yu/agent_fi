package com.xms.app.service;

import com.xms.common.domain.MarketData;
import java.util.List;
import java.util.Map;

/**
 * 行情数据服务接口
 * @author xms
 */
public interface MarketDataService {
    
    /**
     * 获取实时行情数据
     * @param symbols 合约代码列表
     * @return 行情数据列表
     */
    List<MarketData> getRealTimeData(List<String> symbols);
    
    /**
     * 获取单个合约实时行情
     * @param symbol 合约代码
     * @return 行情数据
     */
    MarketData getRealTimeData(String symbol);
    
    /**
     * 批量保存行情数据
     * @param marketDataList 行情数据列表
     * @return 保存成功数量
     */
    int saveMarketData(List<MarketData> marketDataList);
    
    /**
     * 保存单条行情数据
     * @param marketData 行情数据
     * @return 保存结果
     */
    boolean saveMarketData(MarketData marketData);
    
    /**
     * 获取历史行情数据
     * @param symbol 合约代码
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 历史行情数据
     */
    List<MarketData> getHistoryData(String symbol, String startTime, String endTime);
    
    /**
     * 获取所有活跃合约
     * @return 合约代码列表
     */
    List<String> getActiveSymbols();
    
    /**
     * 订阅行情数据
     * @param symbols 合约代码列表
     * @return 订阅结果
     */
    boolean subscribeMarketData(List<String> symbols);
    
    /**
     * 取消订阅行情数据
     * @param symbols 合约代码列表
     * @return 取消订阅结果
     */
    boolean unsubscribeMarketData(List<String> symbols);
    
    /**
     * 获取行情统计数据
     * @return 统计数据
     */
    Map<String, Object> getMarketStatistics();
}