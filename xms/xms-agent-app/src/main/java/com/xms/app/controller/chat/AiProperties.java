package com.xms.app.controller.chat;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ai")
@Data
public class AiProperties {
    private String apiKey;
    private String apiUrl;
    private String model;
    private Integer timeout;
}
