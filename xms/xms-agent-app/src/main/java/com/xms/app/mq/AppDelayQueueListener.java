//package com.xms.app.mq;
//
//import com.xms.common.config.redis.delayqueue.RedissonDelayHandler;
//import com.xms.common.config.redis.delayqueue.RedissonDelayOrder;
//import com.xms.common.constant.RedisConstant;
//import com.xms.common.thread.ExecutorRegionKit;
//import com.xms.common.utils.uuid.IDUtils;
//import lombok.AllArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.boot.ApplicationArguments;
//import org.springframework.boot.ApplicationRunner;
//import org.springframework.stereotype.Component;
//
///**
// * 应用模块延时队列监听器
// */
//@Component
//@AllArgsConstructor
//@Slf4j
//public class AppDelayQueueListener implements ApplicationRunner {
//
//    private final RedissonDelayHandler delayHandler;
//
//    @Override
//    public void run(ApplicationArguments args) {
//        log.info("应用模块延时队列监听器启动...");
//
//        delayHandler.start(order -> {
//            log.info("【应用模块】收到延时队列消息: {}", order);
//
//            // 使用虚拟线程处理业务逻辑
//            ExecutorRegionKit.getExecutorRegion().getBlockUserVirtualThreadExecutor(IDUtils.getSnowflakeId(), false).execute(() -> {
//                try {
//                    processDelayMessage(order);
//                } catch (Exception e) {
//                    log.error("处理延时消息异常", e);
//                }
//            });
//        }, RedisConstant.StreamMsgConstant.DELAY_ORDER_TIMEOUT_QUEUE);
//
//        log.info("应用模块延时队列监听器启动完成");
//    }
//
//    /**
//     * 处理延时消息
//     */
//    private void processDelayMessage(RedissonDelayOrder order) {
//        log.info("开始处理延时消息: {}", order);
//
//        // 根据业务类型进行处理
//        Integer bizType = order.getBizType();
//        if (bizType != null) {
//            switch (bizType) {
//                case 1:
//                    log.info("处理类型1的延时消息: {}", order.getOrderId());
//                    break;
//                case 2:
//                    log.info("处理类型2的延时消息: {}", order.getOrderId());
//                    break;
//                default:
//                    log.info("处理未知类型的延时消息: {} - 类型: {}", order.getOrderId(), bizType);
//            }
//        }
//
//        log.info("延时消息处理完成: {}", order.getOrderId());
//    }
//}
