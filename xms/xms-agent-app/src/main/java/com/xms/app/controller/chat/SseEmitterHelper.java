package com.xms.app.controller.chat;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

@Slf4j
public class SseEmitterHelper {
	private static final ScheduledExecutorService SCHEDULER = Executors.newScheduledThreadPool(4);

	/**
	 * 创建 SSE 长连接并异步执行业务推送逻辑。
	 *
	 * <p>业务异常只记录日志并关闭当前 SSE 连接，不再交给全局异常处理器返回 JSON，
	 * 避免 text/event-stream 响应中再次写入 ResultPista 导致二次报错。</p>
	 *
	 * @param timeoutMillis SSE 连接超时时间，单位毫秒；0 表示不主动超时
	 * @param businessLogic 业务推送逻辑，内部通过 emitter.send(...) 输出 SSE 数据
	 * @return 已配置心跳、超时和清理逻辑的 SSE emitter
	 */
	public static SseEmitter createEmitter(long timeoutMillis, Consumer<SseEmitter> businessLogic) {
		return createEmitter(timeoutMillis, (emitter, stopped) -> {
			CompletableFuture.runAsync(() -> {
				try {
					businessLogic.accept(emitter);
				} catch (Exception e) {
					log.error("SSE business error", e);
					stopped.set(true);
					emitter.complete();
				}
			});
		});
	}

	/**
	 * 创建 SSE 长连接并注册心跳、超时、异常清理逻辑。
	 *
	 * <p>心跳用于防止中间代理认为连接空闲；客户端断开、发送失败或超时时只清理连接，
	 * 不把异常继续抛给 Spring MVC 的普通 JSON 异常处理链。</p>
	 *
	 * @param timeoutMillis SSE 连接超时时间，单位毫秒；0 表示不主动超时
	 * @param handler 接收 emitter 和 stopped 标记的业务处理器
	 * @return 已启动业务处理的 SSE emitter
	 */
	public static SseEmitter createEmitter(long timeoutMillis, SseMessageHandler handler) {
		SseEmitter emitter = new SseEmitter(timeoutMillis);
		AtomicBoolean stopped = new AtomicBoolean(false);

		// 每10秒发送一次SSE注释心跳，降低网关/浏览器因连接空闲而断开的概率。
		ScheduledFuture<?> heartbeat = SCHEDULER.scheduleAtFixedRate(() -> {
			if (stopped.get()) {
				return;
			}
			try {
				emitter.send(":\n\n");
			} catch (Exception e) {
				log.warn("SSE heartbeat stopped, client may have disconnected", e);
				stopped.set(true);
				emitter.complete();
			}
		}, 0, 10, TimeUnit.SECONDS);

		Runnable cleanup = () -> {
			stopped.set(true);
			heartbeat.cancel(true);
			log.info("SSE cleaned");
		};
		emitter.onCompletion(cleanup);
		emitter.onTimeout(() -> {
			log.warn("SSE timeout");
			cleanup.run();
			emitter.complete();
		});
		emitter.onError(e -> {
			log.error("SSE error", e);
			cleanup.run();
		});

		// 启动具体业务流式推送，业务内部应在完成后主动 complete。
		handler.handle(emitter, stopped);
		return emitter;
	}

	@FunctionalInterface
	public interface SseMessageHandler {
		void handle(SseEmitter emitter, AtomicBoolean stopped);
	}
}
