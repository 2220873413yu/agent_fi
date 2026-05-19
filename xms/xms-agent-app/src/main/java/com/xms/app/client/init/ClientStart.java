package com.xms.app.client.init;

import com.xms.app.client.LongLiveClientStart;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * App启动后初始化Polymarket WebSocket客户端。
 *
 * <p>旧版本这里启动数海期货行情订阅；当前只启动Polymarket Market Channel，
 * 用于接收market_resolved事件并派发内部市场结算。</p>
 */
@Slf4j
@Component
@AllArgsConstructor
public class ClientStart implements ApplicationRunner {

	private final LongLiveClientStart wsClientStart;

	/**
	 * Spring Boot启动完成后建立Polymarket WebSocket连接。
	 *
	 * @param args 启动参数
	 */
	@Override
	public void run(ApplicationArguments args) throws Exception {
		log.info("开始初始化Polymarket WebSocket订阅客户端");
		//wsClientStart.handerWsMsg();
	}
}
