package com.xms.app.service.impl;

import com.xms.app.service.MarketDataService;
import com.xms.common.domain.MarketData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 模拟行情数据服务实现类
 * @author xms
 */
@Slf4j
@Service
public class MockMarketDataService implements MarketDataService {
    
    // 模拟的合约数据
    private static final Map<String, String> SYMBOL_NAMES = Map.of(
        "CEDAX40", "DAX主连",
        "CEDAXH0", "DAX03",
        "CEDAXM0", "DAX06", 
        "CEESA0", "小标普连",
        "CEESH0", "小标普03",
        "CEESM0", "小标普06",
        "CEESZ0", "小标普12"
    );
    
    private static final Map<String, BigDecimal> BASE_PRICES = Map.of(
        "CEDAX40", new BigDecimal("24181"),
        "CEDAXH0", new BigDecimal("23813"),
        "CEDAXM0", new BigDecimal("24537"),
        "CEESA0", new BigDecimal("6419.25"),
        "CEESH0", new BigDecimal("6539.5"),
        "CEESM0", new BigDecimal("6551.5"),
        "CEESZ0", new BigDecimal("6473.75")
    );
    
    @Override
    public List<MarketData> getRealTimeData(List<String> symbols) {
        List<MarketData> dataList = new ArrayList<>();
        
        for (String symbol : symbols) {
            MarketData marketData = generateMockData(symbol);
            if (marketData != null) {
                dataList.add(marketData);
            }
        }
        
        log.info("生成模拟实时行情数据，数量: {}", dataList.size());
        return dataList;
    }
    
    @Override
    public MarketData getRealTimeData(String symbol) {
        return generateMockData(symbol);
    }
    
    @Override
    public int saveMarketData(List<MarketData> marketDataList) {
        // 模拟保存操作
        log.info("模拟保存行情数据，数量: {}", marketDataList.size());
        return marketDataList.size();
    }
    
    @Override
    public boolean saveMarketData(MarketData marketData) {
        // 模拟保存操作
        log.debug("模拟保存单条行情数据: {}", marketData.getSymbol());
        return true;
    }
    
    @Override
    public List<MarketData> getHistoryData(String symbol, String startTime, String endTime) {
        // 生成模拟历史数据
        List<MarketData> historyData = new ArrayList<>();
        
        // 生成最近10条历史记录
        for (int i = 0; i < 10; i++) {
            MarketData marketData = generateMockData(symbol);
            if (marketData != null) {
                // 模拟不同时间的数据
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.MINUTE, -i * 5);
                marketData.setDataTime(cal.getTime());
                historyData.add(marketData);
            }
        }
        
        log.info("生成模拟历史行情数据: {}, 数量: {}", symbol, historyData.size());
        return historyData;
    }
    
    @Override
    public List<String> getActiveSymbols() {
        return new ArrayList<>(SYMBOL_NAMES.keySet());
    }
    
    @Override
    public boolean subscribeMarketData(List<String> symbols) {
        log.info("模拟订阅行情数据: {}", String.join(",", symbols));
        return true;
    }
    
    @Override
    public boolean unsubscribeMarketData(List<String> symbols) {
        log.info("模拟取消订阅行情数据: {}", String.join(",", symbols));
        return true;
    }
    
    @Override
    public Map<String, Object> getMarketStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalSymbols", SYMBOL_NAMES.size());
        statistics.put("activeSymbols", SYMBOL_NAMES.size());
        statistics.put("totalVolume", ThreadLocalRandom.current().nextLong(1000000, 10000000));
        statistics.put("totalTurnover", new BigDecimal(ThreadLocalRandom.current().nextDouble(1000000, 50000000)));
        statistics.put("updateTime", new Date());
        
        return statistics;
    }
    
    /**
     * 生成模拟行情数据
     */
    private MarketData generateMockData(String symbol) {
        if (!SYMBOL_NAMES.containsKey(symbol)) {
            return null;
        }
        
        MarketData marketData = new MarketData();
        marketData.setSymbol(symbol);
        marketData.setName(SYMBOL_NAMES.get(symbol));
        marketData.setExchange("CFFEX"); // 中金所
        
        // 基础价格
        BigDecimal basePrice = BASE_PRICES.get(symbol);
        
        // 生成随机波动 (-2% 到 +2%)
        double changePercent = ThreadLocalRandom.current().nextDouble(-2.0, 2.0);
        BigDecimal change = basePrice.multiply(new BigDecimal(changePercent / 100));
        BigDecimal currentPrice = basePrice.add(change);
        
        // 设置价格数据
        marketData.setLastPrice(currentPrice);
        marketData.setPreClosePrice(basePrice);
        marketData.setChange(change);
        marketData.setChangePercent(new BigDecimal(changePercent));
        
        // 生成开高低价
        BigDecimal high = currentPrice.add(currentPrice.multiply(new BigDecimal(ThreadLocalRandom.current().nextDouble(0, 0.01))));
        BigDecimal low = currentPrice.subtract(currentPrice.multiply(new BigDecimal(ThreadLocalRandom.current().nextDouble(0, 0.01))));
        BigDecimal open = basePrice.add(basePrice.multiply(new BigDecimal(ThreadLocalRandom.current().nextDouble(-0.01, 0.01))));
        
        marketData.setHighPrice(high);
        marketData.setLowPrice(low);
        marketData.setOpenPrice(open);
        
        // 生成成交量和成交额
        long volume = ThreadLocalRandom.current().nextLong(1000, 50000);
        BigDecimal turnover = currentPrice.multiply(new BigDecimal(volume));
        marketData.setVolume(volume);
        marketData.setTurnover(turnover);
        
        // 生成买卖盘
        BigDecimal bidPrice = currentPrice.subtract(new BigDecimal("0.5"));
        BigDecimal askPrice = currentPrice.add(new BigDecimal("0.5"));
        marketData.setBidPrice1(bidPrice);
        marketData.setAskPrice1(askPrice);
        marketData.setBidVolume1((long) ThreadLocalRandom.current().nextInt(10, 100));
        marketData.setAskVolume1((long) ThreadLocalRandom.current().nextInt(10, 100));
        
        // 设置时间和状态
        marketData.setDataTime(new Date());
        marketData.setStatus(0); // 正常
        marketData.setCreateTime(new Date());
        
        return marketData;
    }
}