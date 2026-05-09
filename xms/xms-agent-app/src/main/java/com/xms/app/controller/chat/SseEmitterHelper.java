package com.xms.app.controller.chat;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;


@Slf4j
public class SseEmitterHelper {
    private static final ScheduledExecutorService SCHEDULER = Executors.newScheduledThreadPool(4);

    /**
     * 简化版：只需传递业务推送逻辑，内部自动处理线程、stopped等
     */
    public static SseEmitter createEmitter(long timeoutMillis, Consumer<SseEmitter> businessLogic) {
        return createEmitter(timeoutMillis, (emitter, stopped) -> {
            CompletableFuture.runAsync(() -> {
                try {
                    businessLogic.accept(emitter);
                } catch (Exception e) {
                    log.error("SSE business error", e);
                    emitter.completeWithError(e);
                }
            });
        });
    }

    public static SseEmitter createEmitter(long timeoutMillis, SseMessageHandler handler) {
        SseEmitter emitter = new SseEmitter(timeoutMillis);
        AtomicBoolean stopped = new AtomicBoolean(false);

        // 心跳调度器（每10秒）
        ScheduledFuture<?> heartbeat = SCHEDULER.scheduleAtFixedRate(() -> {
            if (stopped.get()) return;
            try {
                emitter.send(":\n\n");
            } catch (Exception e) {
                log.error("SSE heartbeat error", e);
                stopped.set(true);
                emitter.completeWithError(e);
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
        });
        emitter.onError(e -> {
            log.error("SSE error", e);
            cleanup.run();
        });

        // 业务处理
        handler.handle(emitter, stopped);
        return emitter;
    }

    @FunctionalInterface
    public interface SseMessageHandler {
        void handle(SseEmitter emitter, AtomicBoolean stopped);
    }
}



