package com.xms.web.core.config.canal;

import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.protocol.Message;
import com.alibaba.otter.canal.protocol.exception.CanalClientException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import top.javatool.canal.client.client.CanalClient;
import top.javatool.canal.client.handler.MessageHandler;

import java.util.concurrent.TimeUnit;

/**
 * @author yang peng
 * @date 2019/3/2619:11
 */
@Slf4j
public abstract class AbstractDiyCanalClient implements CanalClient {


	protected volatile boolean flag;
	protected String filter = StringUtils.EMPTY;
	protected Integer batchSize = 1;
	protected Long timeout = 1L;
	protected TimeUnit unit = TimeUnit.SECONDS;
	// 设置-1时可以subscribe阻塞等待时优雅停机
	protected int retryTimes = 9;
	// 重试的时间间隔，默认5秒
	protected int retryInterval = 2000;
	private Thread workThread;
	private CanalConnector connector;
	private MessageHandler messageHandler;


	@Override
	public void start() {
		log.info("start canal client");
		workThread = new Thread(this::process);
		workThread.setName("canal-client-thread");
		flag = true;
		workThread.start();
	}

	@Override
	public void stop() {
		log.info("stop canal client");
		flag = false;
		if (null != workThread) {
			workThread.interrupt();
		}

	}


	@Override
	public void process() {
		int times = 0;
		int initRetryInterval=retryInterval;
		while (flag) {
			try {
				connector.connect();
				connector.subscribe(filter);
				//连上以后，重试变量重置
				times = 0;
				retryInterval = initRetryInterval;
				while (flag) {
					Message message = connector.getWithoutAck(batchSize, timeout, unit);
					// log.debug("获取消息 {}", message);
					long batchId = message.getId();
					if (message.getId() != -1 && message.getEntries().size() != 0) {
						messageHandler.handleMessage(message);
					}
					connector.ack(batchId);
				}
			} catch (Exception e) {
				log.warn("单机版的 failed to connect after retry  times {}", times);
				times = times + 1;
				if (times >= retryTimes) {
					log.error("canal client 异常", e);
					return;
				} else {
					// fixed issue #55，增加sleep控制，避免重试connect时cpu使用过高
					try {
						retryInterval = retryInterval * times;
						Thread.sleep(retryInterval);
					} catch (InterruptedException e1) {
						throw new CanalClientException(e1);
					}
				}
			} finally {
				connector.disconnect();
			}
		}
	}

	public CanalConnector getConnector() {
		return connector;
	}

	public void setConnector(CanalConnector connector) {
		this.connector = connector;
	}

	public MessageHandler getMessageHandler() {
		return messageHandler;
	}

	public void setMessageHandler(MessageHandler messageHandler) {
		this.messageHandler = messageHandler;
	}
}
