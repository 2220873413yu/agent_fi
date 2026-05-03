package com.xms.app.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * CoinGlass API配置
 * @author xms
 */
@Data
@Component
@ConfigurationProperties(prefix = "coinglass.api")
public class CoinGlassConfig {
    
    /**
     * CoinGlass API密钥
     */
    private String secret;
    
    /**
     * API基础URL
     */
    private String baseUrl = "https://open-api-v4.coinglass.com";
    
    // 临时添加getter方法，解决Lombok问题
    public String getSecret() {
        return secret;
    }
    
    public String getBaseUrl() {
        return baseUrl;
    }
}