package com.xms.app.entity.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

/**
 * 币种资金费率数据
 * @author xms
 */
@Data
public class FundingRateData {
    
    /**
     * 币种名称
     */
    private String symbol;
    
    /**
     * USDT/USD 保证金模式资金费率列表
     */
    private List<ExchangeFundingRate> stablecoinMarginList;
    
    /**
     * 币本位保证金模式资金费率列表
     */
    private List<ExchangeFundingRate> tokenMarginList;
    
    // 临时添加setter方法，解决Lombok问题
    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }
    
    public void setStablecoinMarginList(List<ExchangeFundingRate> stablecoinMarginList) {
        this.stablecoinMarginList = stablecoinMarginList;
    }
    
    public void setTokenMarginList(List<ExchangeFundingRate> tokenMarginList) {
        this.tokenMarginList = tokenMarginList;
    }
    
    /**
     * 交易所资金费率信息
     */
    @Data
    public static class ExchangeFundingRate {
        
        /**
         * 交易所名称
         */
        private String exchange;
        
        /**
         * 资金费率结算周期（小时）
         */
        private Integer fundingRateInterval;
        
        /**
         * 当前资金费率
         */
        private BigDecimal fundingRate;
        
        /**
         * 下次资金费率结算时间（毫秒时间戳）
         */
        private Long nextFundingTime;
        
        // 临时添加setter方法，解决Lombok问题
        public void setExchange(String exchange) {
            this.exchange = exchange;
        }
        
        public void setFundingRateInterval(Integer fundingRateInterval) {
            this.fundingRateInterval = fundingRateInterval;
        }
        
        public void setFundingRate(BigDecimal fundingRate) {
            this.fundingRate = fundingRate;
        }
        
        public void setNextFundingTime(Long nextFundingTime) {
            this.nextFundingTime = nextFundingTime;
        }
    }
}