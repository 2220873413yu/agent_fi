package com.xms.app.entity.dto;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 资金费率套利数据
 * @author xms
 */
@Data
public class ArbitrageData {

    /**
     * 币种名称
     */
    private String symbol;

    /**
     * 做多的交易所信息（低资金费率）
     */
    private ExchangeInfo buy;

    /**
     * 做空的交易所信息（高资金费率）
     */
    private ExchangeInfo sell;

    /**
     * 年化收益率（APR，%）
     */
    private BigDecimal apr;

    /**
     * 资金费率收益差（long和short之间的资金费率差）
     */
    private BigDecimal funding;

	/**
     * 收益
     */
	private BigDecimal profit;

	/**
     * 交易手续费率
     */
	private BigDecimal transactionFee;

    /**
     * 交易手续费率（双边总和）
     */
    private BigDecimal fee;

    /**
     * 跨平台价格差（%）
     */
    private BigDecimal spread;

    /**
     * 下一次资金费率结算时间（时间戳，毫秒）
     */
    private Long nextFundingTime;

    // 临时添加setter方法，解决Lombok问题
    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public void setBuy(ExchangeInfo buy) {
        this.buy = buy;
    }

    public void setSell(ExchangeInfo sell) {
        this.sell = sell;
    }

    public void setApr(BigDecimal apr) {
        this.apr = apr;
    }

    public void setFunding(BigDecimal funding) {
        this.funding = funding;
    }

    public void setFee(BigDecimal fee) {
        this.fee = fee;
    }

    public void setSpread(BigDecimal spread) {
        this.spread = spread;
    }

    public void setNextFundingTime(Long nextFundingTime) {
        this.nextFundingTime = nextFundingTime;
    }

    /**
     * 交易所信息
     */
    @Data
    public static class ExchangeInfo {

        /**
         * 交易所名称
         */
        private String exchange;

        /**
         * 平台未平仓合约金额（美元）
         */
        private BigDecimal openInterestUsd;

        /**
         * 资金费率结算间隔（小时）
         */
        private Integer fundingRateInterval;

        /**
         * 当前资金费率（%）
         */
        private BigDecimal fundingRate;

        // 临时添加setter方法，解决Lombok问题
        public void setExchange(String exchange) {
            this.exchange = exchange;
        }

        public void setOpenInterestUsd(BigDecimal openInterestUsd) {
            this.openInterestUsd = openInterestUsd;
        }

        public void setFundingRateInterval(Integer fundingRateInterval) {
            this.fundingRateInterval = fundingRateInterval;
        }

        public void setFundingRate(BigDecimal fundingRate) {
            this.fundingRate = fundingRate;
        }
    }
}
