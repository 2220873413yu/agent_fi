package com.xms.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @createDate: 2023/7/26 14:12
 */
@Data
@Component
@ConfigurationProperties(prefix = "udun.config")
public class UdunConfig {
	private String domain;
	private String merchant;
	private String key;
	private String callUrl;

	/**
	 * bep-20 类型
	 */
	private String mainBepCoinType;
	/**
	 * 币安
	 */
	private String USDTBEP20;

	/**
	 * 币安提现的apiKey
	 */
	private String bscApiKey;

	/**
	 * 币安提现的apiKey
	 */
	private String bscSecretKey;
}
