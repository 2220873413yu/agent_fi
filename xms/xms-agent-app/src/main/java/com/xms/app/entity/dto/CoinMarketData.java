package com.xms.app.entity.dto;

import lombok.Data;
import java.math.BigDecimal;

/**
 * CoinGlass 币种市场数据 DTO
 * 用于接收币种市场列表 API 数据
 */
@Data
public class CoinMarketData {

    private String symbol;  // 币种代号，如 BTC
    //图标
	private String icon;
	private BigDecimal currentPrice;  // 当前币价（美元）
    private BigDecimal marketCap;  // 当前市值（美元）

    // 价格变化（美元）- 只取15分钟、1小时、4小时、24小时
    private BigDecimal priceChange15m;  // 最近15分钟价格变化
    private BigDecimal priceChange1h;   // 最近1小时价格变化
    private BigDecimal priceChange4h;   // 最近4小时价格变化
    private BigDecimal priceChange24h;  // 最近24小时价格变化

    // 价格变化百分比（%）
    private BigDecimal priceChangePercent15m;  // 最近15分钟价格涨跌幅
    private BigDecimal priceChangePercent1h;   // 最近1小时价格涨跌幅
    private BigDecimal priceChangePercent4h;   // 最近4小时价格涨跌幅
    private BigDecimal priceChangePercent24h;  // 最近24小时价格涨跌幅

    // 成交量（美元）
    private BigDecimal volumeUsd15m;  // 最近15分钟成交总量
    private BigDecimal volumeUsd1h;   // 最近1小时成交总量
    private BigDecimal volumeUsd4h;   // 最近4小时成交总量
    private BigDecimal volumeUsd24h;  // 最近24小时成交总量

    // 成交量变化百分比（%）
    private BigDecimal volumeChangePercent15m;  // 成交量变化百分比：15分钟
    private BigDecimal volumeChangePercent1h;   // 成交量变化百分比：1小时
    private BigDecimal volumeChangePercent4h;   // 成交量变化百分比：4小时
    private BigDecimal volumeChangePercent24h;  // 成交量变化百分比：24小时

    // 主动买入成交量（美元）
    private BigDecimal buyVolumeUsd15m;  // 最近15分钟主动买入成交量
    private BigDecimal buyVolumeUsd1h;   // 最近1小时主动买入成交量
    private BigDecimal buyVolumeUsd4h;   // 最近4小时主动买入成交量
    private BigDecimal buyVolumeUsd24h;  // 最近24小时主动买入成交量

    // 主动卖出成交量（美元）
    private BigDecimal sellVolumeUsd15m;  // 最近15分钟主动卖出成交量
    private BigDecimal sellVolumeUsd1h;   // 最近1小时主动卖出成交量
    private BigDecimal sellVolumeUsd4h;   // 最近4小时主动卖出成交量
    private BigDecimal sellVolumeUsd24h;  // 最近24小时主动卖出成交量

    // 主动成交量净流入（= 买入 - 卖出）
    private BigDecimal volumeFlowUsd15m;  // 最近15分钟成交量净流入
    private BigDecimal volumeFlowUsd1h;   // 最近1小时成交量净流入
    private BigDecimal volumeFlowUsd4h;   // 最近4小时成交量净流入
    private BigDecimal volumeFlowUsd24h;  // 最近24小时成交量净流入

    // 临时setter方法，解决Lombok问题
    public void setSymbol(String symbol) { this.symbol = symbol; }
    public void setCurrentPrice(BigDecimal currentPrice) { this.currentPrice = currentPrice; }
    public void setMarketCap(BigDecimal marketCap) { this.marketCap = marketCap; }

    public void setPriceChange15m(BigDecimal priceChange15m) { this.priceChange15m = priceChange15m; }
    public void setPriceChange1h(BigDecimal priceChange1h) { this.priceChange1h = priceChange1h; }
    public void setPriceChange4h(BigDecimal priceChange4h) { this.priceChange4h = priceChange4h; }
    public void setPriceChange24h(BigDecimal priceChange24h) { this.priceChange24h = priceChange24h; }

    public void setPriceChangePercent15m(BigDecimal priceChangePercent15m) { this.priceChangePercent15m = priceChangePercent15m; }
    public void setPriceChangePercent1h(BigDecimal priceChangePercent1h) { this.priceChangePercent1h = priceChangePercent1h; }
    public void setPriceChangePercent4h(BigDecimal priceChangePercent4h) { this.priceChangePercent4h = priceChangePercent4h; }
    public void setPriceChangePercent24h(BigDecimal priceChangePercent24h) { this.priceChangePercent24h = priceChangePercent24h; }

    public void setVolumeUsd15m(BigDecimal volumeUsd15m) { this.volumeUsd15m = volumeUsd15m; }
    public void setVolumeUsd1h(BigDecimal volumeUsd1h) { this.volumeUsd1h = volumeUsd1h; }
    public void setVolumeUsd4h(BigDecimal volumeUsd4h) { this.volumeUsd4h = volumeUsd4h; }
    public void setVolumeUsd24h(BigDecimal volumeUsd24h) { this.volumeUsd24h = volumeUsd24h; }

    public void setVolumeChangePercent15m(BigDecimal volumeChangePercent15m) { this.volumeChangePercent15m = volumeChangePercent15m; }
    public void setVolumeChangePercent1h(BigDecimal volumeChangePercent1h) { this.volumeChangePercent1h = volumeChangePercent1h; }
    public void setVolumeChangePercent4h(BigDecimal volumeChangePercent4h) { this.volumeChangePercent4h = volumeChangePercent4h; }
    public void setVolumeChangePercent24h(BigDecimal volumeChangePercent24h) { this.volumeChangePercent24h = volumeChangePercent24h; }

    public void setBuyVolumeUsd15m(BigDecimal buyVolumeUsd15m) { this.buyVolumeUsd15m = buyVolumeUsd15m; }
    public void setBuyVolumeUsd1h(BigDecimal buyVolumeUsd1h) { this.buyVolumeUsd1h = buyVolumeUsd1h; }
    public void setBuyVolumeUsd4h(BigDecimal buyVolumeUsd4h) { this.buyVolumeUsd4h = buyVolumeUsd4h; }
    public void setBuyVolumeUsd24h(BigDecimal buyVolumeUsd24h) { this.buyVolumeUsd24h = buyVolumeUsd24h; }

    public void setSellVolumeUsd15m(BigDecimal sellVolumeUsd15m) { this.sellVolumeUsd15m = sellVolumeUsd15m; }
    public void setSellVolumeUsd1h(BigDecimal sellVolumeUsd1h) { this.sellVolumeUsd1h = sellVolumeUsd1h; }
    public void setSellVolumeUsd4h(BigDecimal sellVolumeUsd4h) { this.sellVolumeUsd4h = sellVolumeUsd4h; }
    public void setSellVolumeUsd24h(BigDecimal sellVolumeUsd24h) { this.sellVolumeUsd24h = sellVolumeUsd24h; }

    public void setVolumeFlowUsd15m(BigDecimal volumeFlowUsd15m) { this.volumeFlowUsd15m = volumeFlowUsd15m; }
    public void setVolumeFlowUsd1h(BigDecimal volumeFlowUsd1h) { this.volumeFlowUsd1h = volumeFlowUsd1h; }
    public void setVolumeFlowUsd4h(BigDecimal volumeFlowUsd4h) { this.volumeFlowUsd4h = volumeFlowUsd4h; }
    public void setVolumeFlowUsd24h(BigDecimal volumeFlowUsd24h) { this.volumeFlowUsd24h = volumeFlowUsd24h; }
}
