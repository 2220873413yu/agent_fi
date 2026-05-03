package com.xms.app.server;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author: GT63S
 * @createDate: 2025/2/24
 */
@Data
@Configuration
@ConfigurationProperties(WebsocketProperties.PREFIX)
public class WebsocketProperties {
	public static final String PREFIX = "websocket.client";
	private String url = "192.0.0.1:6005";
	private Boolean enable;
	/**
	 *  dev prod test
	 */
	private String mode;
}
