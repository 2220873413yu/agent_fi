package com.xms.common.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 行情数据实体类
 * @author xms
 */
@Data
public class MarketData {
    
    /** 主键ID */
    private Long id;
    
    /** 合约代码 */
    private String symbol;
    
    /** 合约名称 */
    private String name;
    
    /** 交易所代码 */
    private String exchange;
    
    /** 最新价 */
    private BigDecimal lastPrice;
    
    /** 开盘价 */
    private BigDecimal openPrice;
    
    /** 最高价 */
    private BigDecimal highPrice;
    
    /** 最低价 */
    private BigDecimal lowPrice;
    
    /** 昨收价 */
    private BigDecimal preClosePrice;
    
    /** 成交量 */
    private Long volume;
    
    /** 成交额 */
    private BigDecimal turnover;
    
    /** 涨跌额 */
    private BigDecimal change;
    
    /** 涨跌幅(%) */
    private BigDecimal changePercent;
    
    /** 买一价 */
    private BigDecimal bidPrice1;
    
    /** 买一量 */
    private Long bidVolume1;
    
    /** 卖一价 */
    private BigDecimal askPrice1;
    
    /** 卖一量 */
    private Long askVolume1;
    
    /** 数据时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date dataTime;
    
    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    
    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;
    
    /** 数据状态 0-正常 1-停牌 2-异常 */
    private Integer status;
    
    /** 备注 */
    private String remark;
}