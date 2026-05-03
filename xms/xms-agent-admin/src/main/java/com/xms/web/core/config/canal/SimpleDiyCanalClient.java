package com.xms.web.core.config.canal;

import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.client.CanalConnectors;
import org.apache.commons.lang.StringUtils;
import top.javatool.canal.client.handler.MessageHandler;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

/**
 * 重写，搞重试次数
 */
public class SimpleDiyCanalClient extends AbstractDiyCanalClient {


	public static Builder builder() {
		return Builder.builder();
	}


	public static class Builder {
		private String filter = StringUtils.EMPTY;
		private Integer batchSize = 1;
		private Long timeout = 1L;
		private TimeUnit unit = TimeUnit.SECONDS;
		private String hostname;
		private Integer port;
		private String destination;
		private String userName;
		private String password;
		private MessageHandler messageHandler;
		// 设置-1时可以subscribe阻塞等待时优雅停机
		private int retryTimes = 9;
		// 重试的时间间隔，默认5秒
		private int retryInterval = 2000;

		private Builder() {
		}

		public static Builder builder() {
			return new Builder();
		}

		public Builder hostname(String hostname) {
			this.hostname = hostname;
			return this;
		}

		public Builder port(Integer port) {
			this.port = port;
			return this;
		}

		public Builder retryTimes(Integer retryTimes) {
			this.retryTimes = retryTimes;
			return this;
		}

		public Builder retryInterval(Integer retryInterval) {
			this.retryInterval = retryInterval;
			return this;
		}

		public Builder destination(String destination) {
			this.destination = destination;
			return this;
		}

		public Builder userName(String userName) {
			this.userName = userName;
			return this;
		}

		public Builder password(String password) {
			this.password = password;
			return this;
		}


		public Builder filter(String filter) {
			this.filter = filter;
			return this;
		}

		public Builder batchSize(Integer batchSize) {
			this.batchSize = batchSize;
			return this;
		}

		public Builder timeout(Long timeout) {
			this.timeout = timeout;
			return this;
		}

		public Builder unit(TimeUnit unit) {
			this.unit = unit;
			return this;
		}

		public Builder messageHandler(MessageHandler messageHandler) {
			this.messageHandler = messageHandler;
			return this;
		}

		public SimpleDiyCanalClient build() {
			CanalConnector canalConnector = CanalConnectors.newSingleConnector(new InetSocketAddress(hostname, port), destination, userName, password);
			// CanalConnector canalConnector = newSingleConnector(new InetSocketAddress(hostname, port), destination, userName, password);
			SimpleDiyCanalClient simpleDiyCanalClient = new SimpleDiyCanalClient();
			simpleDiyCanalClient.setConnector(canalConnector);
			simpleDiyCanalClient.setMessageHandler(messageHandler);
			simpleDiyCanalClient.filter = this.filter;
			simpleDiyCanalClient.unit = this.unit;
			simpleDiyCanalClient.batchSize = this.batchSize;
			simpleDiyCanalClient.timeout = this.timeout;
			simpleDiyCanalClient.retryTimes = this.retryTimes;
			simpleDiyCanalClient.retryInterval = this.retryInterval;
			return simpleDiyCanalClient;
		}

	}
}
