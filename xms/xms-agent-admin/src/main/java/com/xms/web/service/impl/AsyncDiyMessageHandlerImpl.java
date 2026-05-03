package com.xms.web.service.impl;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import top.javatool.canal.client.handler.AbstractMessageHandler;
import top.javatool.canal.client.handler.EntryHandler;
import top.javatool.canal.client.handler.RowDataHandler;

import java.util.List;

/**
 * 消息处理器
 *
 * @author MIER
 */
@Slf4j
public class AsyncDiyMessageHandlerImpl extends AbstractMessageHandler {


	public AsyncDiyMessageHandlerImpl(List<? extends EntryHandler> entryHandlers, RowDataHandler<CanalEntry.RowData> rowDataHandler) {
		super(entryHandlers, rowDataHandler);

	}

	/**
	 * 开启多线程失败，一般不会确认，默认线程开启，一般会处理成功，适用数据非强一致性场景
	 *
	 * @param message
	 */
	@Override
	@Async("asyncExecutor")
	public void handleMessage(Message message) {
		super.handleMessage(message);

	}


}
